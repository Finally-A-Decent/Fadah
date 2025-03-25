package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingEndEvent;
import info.preva1l.fadah.api.ListingEndReason;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.utils.guis.LayoutManager;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import info.preva1l.fadah.watcher.AuctionWatcher;
import info.preva1l.fadah.watcher.Watching;
import info.preva1l.hooker.Hooker;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface DataProvider {
    Fadah getPlugin();

    default void reload() {
        FastInvManager.closeAll(getPlugin());
        Config.reload();
        Lang.reload();
        getPlugin().getMenusFile().load();
        Stream.of(
                LayoutManager.MenuType.MAIN,
                LayoutManager.MenuType.NEW_LISTING,
                LayoutManager.MenuType.PROFILE,
                LayoutManager.MenuType.EXPIRED_LISTINGS,
                LayoutManager.MenuType.ACTIVE_LISTINGS,
                LayoutManager.MenuType.COLLECTION_BOX,
                LayoutManager.MenuType.CONFIRM_PURCHASE,
                LayoutManager.MenuType.HISTORY,
                LayoutManager.MenuType.WATCH
        ).forEach(getPlugin().getLayoutManager()::reloadLayout);
        CategoryRegistry.loadCategories();
        Hooker.reload();
    }

    default void loadDataAndPopulateCaches() {
        DatabaseManager.getInstance(); // Make the connection happen during startup
        CacheAccess.init();
        CategoryRegistry.loadCategories();

        DatabaseManager.getInstance().getAll(Listing.class)
                .thenAccept(listings ->
                        listings.forEach(listing ->
                                CacheAccess.add(Listing.class, listing))).join();

        TaskManager.Async.runTask(getPlugin(), listingExpiryTask(), 10L);
    }



    default CompletableFuture<Void> loadPlayerData(UUID uuid) {
        DatabaseManager db = DatabaseManager.getInstance();

        return db.fixPlayerData(uuid)
                .thenCompose(ignored -> CompletableFuture.allOf(
                        loadAndCache(CollectionBox.class, uuid, () -> new CollectionBox(uuid, new ArrayList<>())),
                        loadAndCache(ExpiredItems.class, uuid, () -> new ExpiredItems(uuid, new ArrayList<>())),
                        loadAndCache(History.class, uuid, () -> new History(uuid, new ArrayList<>())),
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

    private Runnable listingExpiryTask() {
        return () -> {
            for (Listing listing : CacheAccess.getAll(Listing.class)) {
                if (System.currentTimeMillis() < listing.getDeletionDate()) continue;

                CacheAccess.invalidate(Listing.class, listing);

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
