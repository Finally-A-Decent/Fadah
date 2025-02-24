package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.api.ListingEndEvent;
import info.preva1l.fadah.api.ListingEndReason;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import org.bukkit.Bukkit;

import java.time.Instant;

public interface ListingExpiryProvider {
    default Runnable listingExpiryTask() {
        return () -> {
            for (Listing listing : CacheAccess.getAll(Listing.class)) {
                if (Instant.now().toEpochMilli() >= listing.getDeletionDate()) {
                    CacheAccess.invalidate(Listing.class, listing);
                    DatabaseManager.getInstance().delete(Listing.class, listing);

                    CollectableItem collectableItem = new CollectableItem(listing.getItemStack(), Instant.now().toEpochMilli());
                    ExpiredItems items = ExpiredItems.of(listing.getOwner());
                    items.collectableItems().add(collectableItem);

                    ExpiredListingsCache.addItem(listing.getOwner(), collectableItem); // todo: update to new cache

                    DatabaseManager.getInstance().save(ExpiredItems.class, items);

                    TransactionLogger.listingExpired(listing);

                    Bukkit.getServer().getPluginManager().callEvent(new ListingEndEvent(listing, ListingEndReason.EXPIRED));
                }
            }
        };
    }
}
