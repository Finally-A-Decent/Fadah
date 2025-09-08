package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.data.dao.common_sql.CommonSQLListingDao;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.security.AwareDataService;
import info.preva1l.fadah.utils.Text;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public final class ImplBidListing extends ActiveListing implements BidListing {
    private static final Logger LOGGER = Logger.getLogger(ImplBidListing.class.getName());
    private static final UUID ZERO_UUID = new UUID(0L, 0L);

    private final double startingBid;
    private final ConcurrentSkipListSet<Bid> bids;

    public ImplBidListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                          @NotNull ItemStack itemStack, @NotNull String currency, double startingBid,
                          double tax, long creationDate, long deletionDate, ConcurrentSkipListSet<Bid> bids) {
        super(id, owner, ownerName, itemStack, currency, tax, creationDate, deletionDate);

        if (startingBid <= 0) {
            throw new IllegalArgumentException("Starting bid must be positive");
        }

        this.startingBid = startingBid;
        this.bids = bids != null ? bids : new ConcurrentSkipListSet<>();
    }

    @Override
    public double getPrice() {
        return getCurrentBid().bidAmount();
    }

    @Override
    public boolean canBuy(@NotNull Player player) {
        try {
            Bid currentBid = getCurrentBid();
            double requiredAmount = currentBid.bidAmount() + 1;

            if (!getCurrency().canAfford(player, requiredAmount)) {
                Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getTooExpensive());
                return false;
            }

            if (currentBid.bidder().equals(player.getUniqueId())) {
                Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getAlreadyHighestBidder());
                return false;
            }

            return super.canBuy(player);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking if player can buy", e);
            return false;
        }
    }

    @Override
    public StaleListing getAsStale() {
        return new StaleListing(id, owner, ownerName, itemStack, currencyId,
                getCurrentBid().bidAmount(), tax, creationDate, deletionDate, bids, categoryID);
    }

    @Override
    public Bid getCurrentBid() {
        if (bids == null || bids.isEmpty()) {
            return new Bid(
                    ZERO_UUID,
                    Lang.i().getWords().getStartingBid(),
                    startingBid,
                    creationDate
            );
        }
        return bids.first();
    }

    /**
     * Add a new bid.
     *
     * @param bidder    the person bidding
     * @param bidAmount the amount of the bid
     */
    @Override
    public CompletableFuture<Void> newBid(@NotNull Player bidder, double bidAmount) {
        if (bidAmount <= 0) {
            Lang.sendMessage(bidder, Lang.i().getPrefix() + Lang.i().getErrors().getBidTooLow());
            return CompletableFuture.completedFuture(null);
        }

        return AwareDataService.instance.execute(Listing.class, this, () -> newBid0(bidder, bidAmount));
    }

    private void newBid0(@NotNull Player bidder, double bidAmount) {
        try {
            if (!canBuy(bidder)) return;

            Bid mostRecentBid = getCurrentBid();
            if (mostRecentBid.bidAmount() >= bidAmount) {
                Lang.sendMessage(bidder, Lang.i().getPrefix() + Lang.i().getErrors().getBidTooLow());
                return;
            }

            if (!getCurrency().withdraw(bidder, bidAmount)) {
                Lang.sendMessage(bidder, Lang.i().getPrefix() + Lang.i().getErrors().getTooExpensive());
                return;
            }

            Bid newBid = new Bid(bidder.getUniqueId(), bidder.getName(), bidAmount, System.currentTimeMillis());
            bids.add(newBid);

            sendBidConfirmation(bidder, bidAmount);
            handlePreviousBidder(mostRecentBid, bidAmount);
            updateListing();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing new bid", e);
            try {
                getCurrency().add(bidder, bidAmount);
            } catch (Exception refundError) {
                LOGGER.log(Level.SEVERE, "Failed to refund bidder after error", refundError);
            }
        }
    }

    private void sendBidConfirmation(@NotNull Player bidder, double bidAmount) {
        try {
            Component itemName = Text.extractItemName(itemStack);
            String formattedPrice = Config.i().getFormatting().numbers().format(bidAmount);
            Component message = Text.text(Lang.i().getNotifications().getBidPlaced(),
                    Tuple.of("%item%", itemName),
                    Tuple.of("%price%", formattedPrice)
            );
            bidder.sendMessage(message);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send bid confirmation", e);
        }
    }

    private void handlePreviousBidder(@NotNull Bid previousBid, double newBidAmount) {
        if (ZERO_UUID.equals(previousBid.bidder())) return;

        try {
            OfflinePlayer previousBidder = Bukkit.getOfflinePlayer(previousBid.bidder());
            getCurrency().add(previousBidder, previousBid.bidAmount());

            sendOutbidNotification(previousBid, newBidAmount);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to handle previous bidder refund/notification", e);
        }
    }

    private void sendOutbidNotification(@NotNull Bid outbidBid, double newBidAmount) {
        try {
            Component itemName = Text.extractItemName(itemStack);
            String formattedPrice = Config.i().getFormatting().numbers().format(newBidAmount);
            Component outbidMessage = Text.text(Lang.i().getNotifications().getOutBid(),
                    Tuple.of("%item%", itemName),
                    Tuple.of("%price%", formattedPrice)
            );

            Player onlinePlayer = Bukkit.getPlayer(outbidBid.bidder());
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                onlinePlayer.sendMessage(outbidMessage);
            } else if (Broker.getInstance() != null && Broker.getInstance().isConnected()) {
                Message.builder()
                        .type(Message.Type.NOTIFICATION)
                        .payload(Payload.withNotification(outbidBid.bidder(), outbidMessage))
                        .build().send(Broker.getInstance());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send outbid notification", e);
        }
    }

    @Override
    public CompletableFuture<Void> completeBidding() {
        return AwareDataService.instance.execute(Listing.class, this, this::completeBidding0);
    }

    private void completeBidding0() {
        try {
            Bid winningBid = getCurrentBid();

            if (ZERO_UUID.equals(winningBid.bidder())) {
                expire();
                return;
            }

            processSellerPayment(winningBid);
            removeListing();
            addItemToCollection(winningBid);
            sendCompletionNotifications(winningBid);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error completing bidding", e);
        }
    }

    @Override
    protected void cancel0(@NotNull Player canceller) {
        Bid lastBid = getCurrentBid();
        if (!ZERO_UUID.equals(lastBid.bidder())) {
            getCurrency().add(Bukkit.getOfflinePlayer(lastBid.bidder()), lastBid.bidAmount());
        }
        super.cancel0(canceller);
    }

    private void processSellerPayment(@NotNull Bid winningBid) {
        try {
            double taxAmount = (this.getTax() / 100.0) * winningBid.bidAmount();
            double sellerAmount = winningBid.bidAmount() - taxAmount;

            OfflinePlayer seller = Bukkit.getOfflinePlayer(this.getOwner());
            getCurrency().add(seller, sellerAmount);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process seller payment", e);
            throw e;
        }
    }

    private void removeListing() {
        try {
            CacheAccess.invalidate(Listing.class, this);
            DataService.getInstance().delete(Listing.class, this);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to remove listing", e);
            throw e;
        }
    }

    private void updateListing() {
        try {
            DataService.getInstance().update(Listing.class, this, Map.of("bids", CommonSQLListingDao.bidToJsonString(this.getBids())));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update listing", e);
            throw e;
        }
    }

    private void addItemToCollection(@NotNull Bid winningBid) {
        try {
            ItemStack clonedItem = this.getItemStack().clone();
            CollectableItem collectableItem = new CollectableItem(clonedItem, System.currentTimeMillis());

            CacheAccess.get(CollectionBox.class, winningBid.bidder())
                    .ifPresentOrElse(
                            cache -> {
                                try {
                                    cache.add(collectableItem);
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, "Failed to add to cached expired items", e);
                                }
                            }, () -> handleCollectionBoxFromDatabase(winningBid.bidder(), collectableItem)
                    );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to add item to collection", e);
        }
    }

    private void sendCompletionNotifications(@NotNull Bid winningBid) {
        try {
            sendBuyerNotification(winningBid);
            sendSellerNotification(winningBid);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send completion notifications", e);
        }
    }

    private void sendBuyerNotification(@NotNull Bid winningBid) {
        try {
            Component newItemMessage = Text.text(Lang.i().getNotifications().getNewItem());

            Player onlineBuyer = Bukkit.getPlayer(winningBid.bidder());
            if (onlineBuyer != null && onlineBuyer.isOnline()) {
                onlineBuyer.sendMessage(newItemMessage);
            } else if (Broker.getInstance() != null && Broker.getInstance().isConnected()) {
                Message.builder()
                        .type(Message.Type.NOTIFICATION)
                        .payload(Payload.withNotification(winningBid.bidder(), newItemMessage))
                        .build().send(Broker.getInstance());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send buyer notification", e);
        }
    }

    private void sendSellerNotification(@NotNull Bid winningBid) {
        try {
            double taxAmount = (this.getTax() / 100.0) * winningBid.bidAmount();
            double sellerAmount = winningBid.bidAmount() - taxAmount;

            Component itemName = Text.extractItemName(itemStack);
            String formattedPrice = Config.i().getFormatting().numbers().format(sellerAmount);
            Component message = Text.text(Lang.i().getNotifications().getSale(),
                    Tuple.of("%item%", itemName),
                    Tuple.of("%price%", formattedPrice),
                    Tuple.of("%buyer%", winningBid.bidderName()));

            OfflinePlayer seller = Bukkit.getOfflinePlayer(this.getOwner());
            complete(message, seller);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send seller notification", e);
        }
    }
}