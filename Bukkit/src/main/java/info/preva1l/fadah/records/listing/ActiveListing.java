package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingEndEvent;
import info.preva1l.fadah.api.ListingEndReason;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public abstract class ActiveListing extends Listing {
    protected ActiveListing(@NotNull UUID id, @NotNull UUID owner, @NotNull String ownerName,
                            @NotNull ItemStack itemStack, @NotNull String categoryID, @NotNull String currency,
                            double price, double tax, long creationDate, long deletionDate, boolean biddable, List<Bid> bids) {
        super(id, owner, ownerName, itemStack, categoryID, currency, price, tax, creationDate, deletionDate, biddable, bids);
    }

    @Override
    public boolean cancel(@NotNull Player canceller) {
        if (CacheAccess.get(Listing.class, this.getId()) == null) { // todo: re-add strict checks
            Lang.sendMessage(canceller, Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist());
            return false;
        }
        Lang.sendMessage(canceller, Lang.i().getPrefix() + Lang.i().getNotifications().getCancelled());
        CacheAccess.invalidate(Listing.class, this);
        DatabaseManager.getInstance().delete(Listing.class, this);


        CollectableItem collectableItem = new CollectableItem(this.getItemStack(), Instant.now().toEpochMilli());
        ExpiredItems items = ExpiredItems.of(getOwner());
        items.collectableItems().add(collectableItem);
        ExpiredListingsCache.addItem(getOwner(), collectableItem);
        DatabaseManager.getInstance().save(ExpiredItems.class, items);

        boolean isAdmin = !this.isOwner(canceller);
        TransactionLogger.listingRemoval(this, isAdmin);
        TaskManager.Async.run(Fadah.getINSTANCE(), () ->
                Bukkit.getServer().getPluginManager().callEvent(
                        new ListingEndEvent(this, isAdmin
                                ? ListingEndReason.CANCELLED_ADMIN
                                : ListingEndReason.CANCELLED)
                )
        );
        return true;
    }

    public StaleListing getAsStale() {
        return new StaleListing(id, owner, ownerName, itemStack, categoryID, currencyId, price, tax, creationDate, deletionDate, biddable, bids);
    }
}
