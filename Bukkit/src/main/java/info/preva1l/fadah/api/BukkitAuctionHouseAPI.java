package info.preva1l.fadah.api;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.listing.Listing;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.UUID;

public final class BukkitAuctionHouseAPI extends AuctionHouseAPI {
    @Override
    public NamespacedKey getCustomItemNameSpacedKey() {
        return Fadah.getCustomItemKey();
    }

    @Override
    public void setCustomItemNameSpacedKey(NamespacedKey key) {
        Fadah.setCustomItemKey(key);
    }

    @Override
    public Listing getListing(UUID uuid) {
        return CacheAccess.get(Listing.class, uuid);
    }

    @Override
    public Category getCategory(String id) {
        return CategoryCache.getCategory(id);
    }

    @Override
    public List<CollectableItem> getCollectionBox(OfflinePlayer offlinePlayer) {
        return CacheAccess.get(CollectionBox.class, offlinePlayer.getUniqueId()).collectableItems();
    }

    @Override
    public List<CollectableItem> getCollectionBox(UUID uuid) {
        return CacheAccess.get(CollectionBox.class, uuid).collectableItems();
    }

    @Override
    public List<CollectableItem> getExpiredItems(OfflinePlayer offlinePlayer) {
        return ExpiredListingsCache.getExpiredListings(offlinePlayer.getUniqueId());
    }

    @Override
    public List<CollectableItem> getExpiredItems(UUID uuid) {
        return ExpiredListingsCache.getExpiredListings(uuid);
    }

    @Override
    public List<HistoricItem> getHistory(OfflinePlayer offlinePlayer) {
        return HistoricItemsCache.getHistory(offlinePlayer.getUniqueId());
    }

    @Override
    public List<HistoricItem> getHistory(UUID uuid) {
        return HistoricItemsCache.getHistory(uuid);
    }

    @Override
    public void verboseWarning(String message) {
        if (Config.i().isVerbose()) {
            Fadah.getConsole().warning(message);
        }
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
