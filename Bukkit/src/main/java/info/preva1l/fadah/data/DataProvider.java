package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.utils.guis.LayoutManager;
import info.preva1l.fadah.watcher.AuctionWatcher;
import info.preva1l.fadah.watcher.Watching;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataProvider {
    default void reload(Fadah plugin) {
        FastInvManager.closeAll(plugin);
        Config.reload();
        Lang.reload();
        Fadah.getINSTANCE().getMenusFile().load();
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.MAIN);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.NEW_LISTING);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.PROFILE);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.EXPIRED_LISTINGS);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.ACTIVE_LISTINGS);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.COLLECTION_BOX);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.CONFIRM_PURCHASE);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.HISTORY);
        Fadah.getINSTANCE().getLayoutManager().reloadLayout(LayoutManager.MenuType.WATCH);
        Fadah.getINSTANCE().getCategoriesFile().load();
        CategoryCache.update();
    }

    default void loadDataAndPopulateCaches() {
        DatabaseManager.getInstance(); // Make the connection happen during startup
        CategoryCache.update();
        DatabaseManager.getInstance().getAll(Watching.class).join().forEach(AuctionWatcher::watch);
    }

    default CompletableFuture<Void> loadPlayerData(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            boolean needsFixing = DatabaseManager.getInstance().needsFixing(uuid).join();
            if (needsFixing) {
                DatabaseManager.getInstance().fixPlayerData(uuid).join();
            }

            Optional<CollectionBox> collectionBox = DatabaseManager.getInstance().get(CollectionBox.class, uuid).join();
            collectionBox.ifPresent(list -> CacheAccess.add(CollectionBox.class, list));

            Optional<ExpiredItems> expiredItems = DatabaseManager.getInstance().get(ExpiredItems.class, uuid).join();
            expiredItems.ifPresent(list -> ExpiredListingsCache.update(uuid, list.collectableItems()));

            Optional<History> history = DatabaseManager.getInstance().get(History.class, uuid).join();
            history.ifPresent(list -> HistoricItemsCache.update(uuid, list.collectableItems()));
            return null;
        }, DatabaseManager.getInstance().getThreadPool());
    }
}
