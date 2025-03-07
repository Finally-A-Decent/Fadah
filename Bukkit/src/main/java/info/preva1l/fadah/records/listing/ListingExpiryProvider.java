package info.preva1l.fadah.records.listing;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingEndEvent;
import info.preva1l.fadah.api.ListingEndReason;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import org.bukkit.Bukkit;

public interface ListingExpiryProvider {
    default Runnable listingExpiryTask() {
        return () -> {
            for (Listing listing : CacheAccess.getAll(Listing.class)) {
                if (System.currentTimeMillis() < listing.getDeletionDate()) continue;

                CacheAccess.invalidate(Listing.class, listing);

                CollectableItem collectableItem = new CollectableItem(listing.getItemStack(), System.currentTimeMillis());

                CacheAccess.get(ExpiredItems.class, listing.owner)
                        .ifPresentOrElse(
                                cache -> cache.add(collectableItem),
                                () -> DatabaseManager.getInstance()
                                        .get(ExpiredItems.class, listing.owner)
                                        .thenCompose(items -> {
                                            var expiredItems = items.orElseGet(() -> ExpiredItems.empty(listing.owner));
                                            return DatabaseManager.getInstance().save(ExpiredItems.class, expiredItems);
                                        })
                        );

                TransactionLogger.listingExpired(listing);

                TaskManager.Sync.run(Fadah.getINSTANCE(), () ->
                        Bukkit.getServer().getPluginManager().callEvent(
                                new ListingEndEvent(listing, ListingEndReason.EXPIRED)
                        )
                );
            }
        };
    }
}
