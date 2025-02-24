package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingPurchaseEvent;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.ListHelper;
import info.preva1l.fadah.config.Tuple;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class BinListing extends ActiveListing {
    public BinListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                      @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency, double price,
                      double tax, long creationDate, long deletionDate, boolean biddable, List<Bid> bids) {
        super(id, owner, ownerName, itemStack, categoryID, currency, price, tax, creationDate, deletionDate, biddable, bids);
    }

    @Override
    public void purchase(@NotNull Player buyer) {
        if (!getCurrency().canAfford(buyer, this.getPrice())) {
            buyer.sendMessage(Lang.i().getPrefix() + Lang.i().getErrors().getTooExpensive());
            return;
        }
        if (CacheAccess.get(Listing.class, this.getId()) == null) { // todo: readd strict checks
            buyer.sendMessage(Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist());
            return;
        }
        // Money Transfer
        getCurrency().withdraw(buyer, this.getPrice());
        double taxed = (this.getTax()/100) * this.getPrice();
        getCurrency().add(Bukkit.getOfflinePlayer(this.getOwner()), this.getPrice() - taxed);

        // Remove Listing
        CacheAccess.invalidate(Listing.class, this);
        DatabaseManager.getInstance().delete(Listing.class, this);

        // Add to collection box
        ItemStack itemStack = this.getItemStack().clone();
        CollectableItem collectableItem = new CollectableItem(itemStack, Instant.now().toEpochMilli());
        CollectionBox box = CacheAccess.get(CollectionBox.class, buyer.getUniqueId());
        box.add(collectableItem);
        CacheAccess.add(CollectionBox.class, box);
        DatabaseManager.getInstance().save(CollectionBox.class, box);

        // Notify Both Players
        Lang.sendMessage(buyer, String.join("\n", Lang.i().getNotifications().getNewItem()));

        String itemName = StringUtils.extractItemName(itemStack);
        String formattedPrice = new DecimalFormat(Config.i().getFormatting().getNumbers()).format(this.getPrice() - taxed);
        String message = String.join("\n", ListHelper.replace(Lang.i().getNotifications().getSale(),
                Tuple.of("%item%", itemName),
                Tuple.of("%price%", formattedPrice),
                Tuple.of("%buyer%", buyer.getName())));

        Player seller = Bukkit.getPlayer(this.getOwner());
        if (seller != null) {
            Lang.sendMessage(seller, message);
        } else {
            if (Config.i().getBroker().isEnabled()) {
                Message.builder()
                        .type(Message.Type.NOTIFICATION)
                        .payload(Payload.withNotification(this.getOwner(), message))
                        .build().send(Fadah.getINSTANCE().getBroker());
            }
        }

        TransactionLogger.listingSold(this, buyer);

        Bukkit.getServer().getPluginManager().callEvent(new ListingPurchaseEvent(this.getAsStale(), buyer));
    }

    @Override
    public boolean newBid(@NotNull Player bidder, double bidAmount) {
        throw new IllegalStateException("Tried to bid on a Buy listing.");
    }
}
