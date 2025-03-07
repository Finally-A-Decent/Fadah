package info.preva1l.fadah.api;

import info.preva1l.fadah.api.managers.CategoryManager;
import info.preva1l.fadah.api.managers.ImplCategoryManager;
import info.preva1l.fadah.api.managers.ImplListingManager;
import info.preva1l.fadah.api.managers.ListingManager;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BukkitAuctionHouseAPI extends AuctionHouseAPI {
    private final CategoryManager categoryManager = new ImplCategoryManager();
    private final ListingManager listingManager = new ImplListingManager();

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

        return DatabaseManager.getInstance()
                .get(CollectionBox.class, playerUniqueId)
                .thenApply(it -> it.orElse(new CollectionBox(playerUniqueId, new ArrayList<>())));
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

        return DatabaseManager.getInstance()
                .get(ExpiredItems.class, playerUniqueId)
                .thenApply(it -> it.orElse(new ExpiredItems(playerUniqueId, new ArrayList<>())));
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

        return DatabaseManager.getInstance()
                .get(History.class, playerUniqueId)
                .thenApply(it -> it.orElse(new History(playerUniqueId, new ArrayList<>())));
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
        };
    }
}
