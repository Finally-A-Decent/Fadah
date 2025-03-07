package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.utils.guis.LayoutManager;
import info.preva1l.fadah.watcher.AuctionWatcher;
import info.preva1l.fadah.watcher.Watching;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface DataProvider {
    default void reload(Fadah plugin) {
        FastInvManager.closeAll(plugin);
        Config.reload();
        Lang.reload();
        Fadah.getINSTANCE().getMenusFile().load();
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
        ).forEach(Fadah.getINSTANCE().getLayoutManager()::reloadLayout);
        CategoryRegistry.loadCategories();
    }

    default void loadDataAndPopulateCaches() {
        DatabaseManager.getInstance(); // Make the connection happen during startup
        CacheAccess.init();
        CategoryRegistry.loadCategories();
    }

    default CompletableFuture<Void> loadPlayerData(UUID uuid) {
        DatabaseManager db = DatabaseManager.getInstance();

        return db.fixPlayerData(uuid)
                .thenCompose(ignored -> CompletableFuture.allOf(
                        loadAndCache(CollectionBox.class, uuid),
                        loadAndCache(ExpiredItems.class, uuid),
                        loadAndCache(History.class, uuid),
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

    private <T> CompletableFuture<Void> loadAndCache(Class<T> type, UUID uuid) {
        return DatabaseManager.getInstance()
                .get(type, uuid)
                .thenAccept(opt -> opt.ifPresent(item -> CacheAccess.add(type, item)));
    }

    private <T> CompletableFuture<Void> saveAndInvalidate(Class<T> type, UUID uuid) {
        return CacheAccess.get(type, uuid)
                .map(value -> DatabaseManager.getInstance().save(type, value)
                        .thenRun(() -> CacheAccess.invalidate(type, value)))
                .orElseGet(() -> CompletableFuture.completedFuture(null));
    }
}
