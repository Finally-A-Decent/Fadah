package info.preva1l.fadah.api;

import info.preva1l.fadah.api.managers.*;
import info.preva1l.fadah.api.menus.MenuManager;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.collection.ImplCollectionBox;
import info.preva1l.fadah.records.collection.ImplExpiredItems;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.history.ImplHistory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BukkitAuctionHouseAPI extends AuctionHouseAPI {
    private final CategoryManager categoryManager = new ImplCategoryManager();
    private final ListingManager listingManager = new ImplListingManager();
    private final MenuManager menuManager = new ImplMenuManager();

    @Override
    public MenuManager menuManager() {
        return menuManager;
    }

    @Override
    public ListingManager listingManager() {
        return listingManager;
    }

    @Override
    public CategoryManager categoryManager() {
        return categoryManager;
    }

    @Override
    public CollectionBox collectionBox(UUID playerUniqueId) throws IllegalStateException {
        return CacheAccess.get(CollectionBox.class, playerUniqueId)
                .orElseThrow(() -> new IllegalStateException("Collection box is not cached! (Player is offline)"));
    }

    @Override
    public CompletableFuture<CollectionBox> loadCollectionBox(UUID playerUniqueId) {
        var cached = CacheAccess.getNullable(CollectionBox.class, playerUniqueId);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return DataService.getInstance()
                .get(CollectionBox.class, playerUniqueId)
                .thenApply(it -> it.orElse(ImplCollectionBox.empty(playerUniqueId)));
    }

    @Override
    public ExpiredItems expiredItems(UUID playerUniqueId) throws IllegalStateException {
        return CacheAccess.get(ExpiredItems.class, playerUniqueId)
                .orElseThrow(() -> new IllegalStateException("Expired items are not cached! (Player is offline)"));
    }

    @Override
    public CompletableFuture<ExpiredItems> loadExpiredItems(UUID playerUniqueId) {
        var cached = CacheAccess.getNullable(ExpiredItems.class, playerUniqueId);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return DataService.getInstance()
                .get(ExpiredItems.class, playerUniqueId)
                .thenApply(it -> it.orElse(ImplExpiredItems.empty(playerUniqueId)));
    }

    @Override
    public History history(UUID playerUniqueId) throws IllegalStateException {
        return CacheAccess.get(History.class, playerUniqueId)
                .orElseThrow(() -> new IllegalStateException("History is not cached! (Player is offline)"));
    }

    @Override
    public CompletableFuture<History> loadHistory(UUID playerUniqueId) {
        var cached = CacheAccess.getNullable(History.class, playerUniqueId);
        if (cached != null) return CompletableFuture.completedFuture(cached);

        return DataService.getInstance()
                .get(History.class, playerUniqueId)
                .thenApply(it -> it.orElse(ImplHistory.empty(playerUniqueId)));
    }

    @Override
    public String getLoggedActionLocale(HistoricItem.LoggedAction action) {
        Lang.LogActions actions = Lang.i().getLogActions();
        return switch (action) {
            case LISTING_SOLD -> actions.getListingSold();
            case LISTING_CANCEL -> actions.getListingCancelled();
            case LISTING_START -> actions.getListingStarted();
            case LISTING_EXPIRE -> actions.getListingExpired();
            case LISTING_PURCHASED -> actions.getListingPurchased();
            case EXPIRED_ITEM_CLAIM -> actions.getExpiredItemClaimed();
            case COLLECTION_BOX_CLAIM -> actions.getCollectionBoxClaimed();
            case LISTING_ADMIN_CANCEL -> actions.getListingCancelledAdmin();
            case EXPIRED_ITEM_ADMIN_CLAIM -> actions.getExpiredItemClaimedAdmin();
            case COLLECTION_BOX_ADMIN_CLAIM -> actions.getCollectionBoxClaimedAdmin();
            case LISTING_BID_RECEIVED -> actions.getListingBidReceived();
            case LISTING_BID_PLACED -> actions.getListingBidPlaced();
        };
    }
}
