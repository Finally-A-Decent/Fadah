package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingEndEvent;
import info.preva1l.fadah.api.ListingEndReason;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import info.preva1l.fadah.watcher.AuctionWatcher;
import info.preva1l.fadah.watcher.Watching;
import info.preva1l.trashcan.plugin.annotations.PluginEnable;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface DataProvider {
    Fadah getPlugin();

    @PluginEnable
    static void loadDataAndPopulateCaches() {
        DatabaseManager.getInstance(); // Make the connection happen during startup
        CategoryRegistry.loadCategories();

        DatabaseManager.getInstance().getAll(Listing.class)
                .thenAccept(listings ->
                        listings.forEach(listing ->
                                CacheAccess.add(Listing.class, listing))).join();

        TaskManager.Async.runTask(Fadah.getInstance(), listingExpiryTask(), 10L);
    }



    default CompletableFuture<Void> loadPlayerData(UUID uuid) {
        DatabaseManager db = DatabaseManager.getInstance();

        return db.fixPlayerData(uuid)
                .thenCompose(ignored -> CompletableFuture.allOf(
                        loadAndCache(CollectionBox.class, uuid, () -> CollectionBox.empty(uuid)),
                        loadAndCache(ExpiredItems.class, uuid, () -> ExpiredItems.empty(uuid)),
                        loadAndCache(History.class, uuid, () -> History.empty(uuid)),
                        db.get(Watching.class, uuid)
                                .thenAccept(opt -> opt.ifPresent(AuctionWatcher::watch))
                ));
    }

    default CompletableFuture<Void> invalidateAndSavePlayerData(UUID uuid) {
        DatabaseManager db = DatabaseManager.getInstance();

        return CompletableFuture.allOf(
                saveAndInvalidate(CollectionBox.class, uuid),
                saveAndInvalidate(ExpiredItems.class, uuid),
                saveAndInvalidate(History.class, uuid),
                AuctionWatcher.get(uuid)
                        .map(w -> db.save(Watching.class, w))
                        .orElseGet(() -> CompletableFuture.completedFuture(null))
        );
    }

    private <T> CompletableFuture<Void> loadAndCache(Class<T> type, UUID uuid, Supplier<T> supplier) {
        return DatabaseManager.getInstance()
                .get(type, uuid)
                .thenAccept(opt -> CacheAccess.add(type, opt.orElse(supplier.get())));
    }

    private <T> CompletableFuture<Void> saveAndInvalidate(Class<T> type, UUID uuid) {
        return CacheAccess.get(type, uuid)
                .map(value -> DatabaseManager.getInstance().save(type, value)
                        .thenRun(() -> CacheAccess.invalidate(type, value)))
                .orElseGet(() -> CompletableFuture.completedFuture(null));
    }

    private static Runnable listingExpiryTask() {
        return () -> {
            for (Listing listing : CacheAccess.getAll(Listing.class)) {
                if (System.currentTimeMillis() <= listing.getDeletionDate()) continue;

                CacheAccess.invalidate(Listing.class, listing);
                DatabaseManager.getInstance().delete(Listing.class, listing);

                CollectableItem collectableItem = new CollectableItem(listing.getItemStack(), System.currentTimeMillis());

                CacheAccess.get(ExpiredItems.class, listing.getOwner())
                        .ifPresentOrElse(
                                cache -> cache.add(collectableItem),
                                () -> DatabaseManager.getInstance()
                                        .get(ExpiredItems.class, listing.getOwner())
                                        .thenCompose(items -> {
                                            var expiredItems = items.orElseGet(() -> ExpiredItems.empty(listing.getOwner()));
                                            return DatabaseManager.getInstance().save(ExpiredItems.class, expiredItems);
                                        })
                        );

                TransactionLogger.listingExpired(listing);

                TaskManager.Sync.run(Fadah.getInstance(), () ->
                        Bukkit.getServer().getPluginManager().callEvent(
                                new ListingEndEvent(listing, ListingEndReason.EXPIRED)
                        )
                );
            }
        };
    }
}
