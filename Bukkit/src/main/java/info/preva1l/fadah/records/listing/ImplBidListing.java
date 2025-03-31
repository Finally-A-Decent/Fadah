package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.api.ListingPurchaseEvent;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

@Getter
public final class ImplBidListing extends ActiveListing implements BidListing {
    private final double startingBid;
    private final ConcurrentSkipListSet<Bid> bids;

    public ImplBidListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                          @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency, double startingBid,
                          double tax, long creationDate, long deletionDate, ConcurrentSkipListSet<Bid> bids) {
        super(id, owner, ownerName, itemStack, categoryID, currency, tax, creationDate, deletionDate);
        this.startingBid = startingBid;
        this.bids = bids;
    }

    @Override
    public double getPrice() {
        return getCurrentBid().bidAmount();
    }

    @Override
    public boolean canBuy(@NotNull Player player) {
        if (!getCurrency().canAfford(player, getCurrentBid().bidAmount() + 1)) {
            Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getTooExpensive());
            return false;
        }

        if (getCurrentBid().bidder().equals(player.getUniqueId())) {
            Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getAlreadyHighestBidder());
            return false;
        }

        return super.canBuy(player);
    }

    @Override
    public StaleListing getAsStale() {
        return new StaleListing(id, owner, ownerName, itemStack, categoryID, currencyId, getCurrentBid().bidAmount(), tax, creationDate, deletionDate, bids);
    }

    @Override
    public Bid getCurrentBid() {
        if (bids.isEmpty()) {
            return new Bid(
                    new UUID(0L, 0L),
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
     * @return true if the bid was successful, false if the bid is not high enough
     */
    @Override
    public boolean newBid(@NotNull Player bidder, double bidAmount) {
        if (!canBuy(bidder)) return false;

        Bid mostRecentBid = bids.first();
        if (mostRecentBid.bidAmount() >= bidAmount) {
            Lang.sendMessage(bidder, Lang.i().getPrefix() + Lang.i().getErrors().getBidTooLow());
            return false;
        }
        if (mostRecentBid.bidder().equals(bidder.getUniqueId())) {
            Lang.sendMessage(bidder, Lang.i().getPrefix() + Lang.i().getErrors().getAlreadyHighestBidder());
            return false;
        }

        bids.add(new Bid(bidder.getUniqueId(), bidder.getName(), bidAmount, System.currentTimeMillis()));
        return true;
    }

    @Override
    public void completeBidding() {
        Bid winningBid = getCurrentBid();

        // Money Transfer
        double taxed = (this.getTax()/100) * winningBid.bidAmount();
        getCurrency().add(Bukkit.getOfflinePlayer(this.getOwner()), winningBid.bidAmount() - taxed);

        // refund
        for (Bid bid : bids) {
            if (bid.equals(winningBid)) continue;

            getCurrency().add(Bukkit.getOfflinePlayer(bid.bidder()), bid.bidAmount());
        }

        // Remove Listing
        CacheAccess.invalidate(Listing.class, this);
        DatabaseManager.getInstance().delete(Listing.class, this);

        // Add to collection box
        ItemStack itemStack = this.getItemStack().clone();
        CacheAccess.get(CollectionBox.class, winningBid.bidder())
                .ifPresentOrElse(
                        cache -> cache.add(new CollectableItem(itemStack, System.currentTimeMillis())),
                        () -> DatabaseManager.getInstance()
                                .get(CollectionBox.class, winningBid.bidder())
                                .thenCompose(items -> {
                                    var box = items.orElseGet(() -> CollectionBox.empty(winningBid.bidder()));
                                    return DatabaseManager.getInstance().save(CollectionBox.class, box);
                                })
                );

        // Notify Both Players
        Player buyer = Bukkit.getPlayer(winningBid.bidder());
        if (buyer != null) {
            buyer.sendMessage(Text.text(Lang.i().getNotifications().getNewItem()));
        } else {
            if (Broker.getInstance().isConnected()) {
                Message.builder()
                        .type(Message.Type.NOTIFICATION)
                        .payload(Payload.withNotification(winningBid.bidder(), Text.text(Lang.i().getNotifications().getNewItem())))
                        .build().send(Broker.getInstance());
            }
        }

        String itemName = Text.extractItemName(itemStack);
        String formattedPrice = Config.i().getFormatting().numbers().format(winningBid.bidAmount() - taxed);
        Component message = Text.text(Lang.i().getNotifications().getSale(),
                Tuple.of("%item%", itemName),
                Tuple.of("%price%", formattedPrice),
                Tuple.of("%buyer%", winningBid.bidderName()));

        Player seller = Bukkit.getPlayer(this.getOwner());
        if (seller != null) {
            seller.sendMessage(message);
        } else {
            if (Broker.getInstance().isConnected()) {
                Message.builder()
                        .type(Message.Type.NOTIFICATION)
                        .payload(Payload.withNotification(this.getOwner(), message))
                        .build().send(Broker.getInstance());
            }
        }

        TransactionLogger.listingSold(this, buyer);

        Bukkit.getServer().getPluginManager().callEvent(new ListingPurchaseEvent(this.getAsStale(), buyer));
    }
}
