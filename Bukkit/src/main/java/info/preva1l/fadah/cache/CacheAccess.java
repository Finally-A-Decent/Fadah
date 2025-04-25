package info.preva1l.fadah.cache;

import info.preva1l.fadah.cache.collectionbox.DistributedCollectionCache;
import info.preva1l.fadah.cache.collectionbox.MemoryCollectionCache;
import info.preva1l.fadah.cache.expired.DistributedExpiredCache;
import info.preva1l.fadah.cache.expired.MemoryExpiredCache;
import info.preva1l.fadah.cache.history.DistributedHistoryCache;
import info.preva1l.fadah.cache.history.MemoryHistoryCache;
import info.preva1l.fadah.cache.listing.DistributedListingCache;
import info.preva1l.fadah.cache.listing.MemoryListingCache;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.listing.Listing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheAccess {
    private static final Map<Class<?>, Cache<?>> cacheMap = new ConcurrentHashMap<>();

    static {
        registerCache(Listing.class, MemoryListingCache::new, DistributedListingCache::new);
        registerCache(CollectionBox.class, MemoryCollectionCache::new, DistributedCollectionCache::new);
        registerCache(ExpiredItems.class, MemoryExpiredCache::new, DistributedExpiredCache::new);
        registerCache(History.class, MemoryHistoryCache::new, DistributedHistoryCache::new);
    }

    private static <T> void registerCache(Class<T> clazz, CacheFactory<T> memoryCache, CacheFactory<T> distributedCache) {
        Cache<T> cache;
        if (Broker.getInstance().isConnected()) {
            cache = distributedCache.create();
        } else {
            cache = memoryCache.create();
        }
        cacheMap.put(clazz, cache);
    }

    @SuppressWarnings("unchecked")
    private static <T> Cache<T> getCacheForClass(Class<T> clazz) {
        Cache<?> cache = cacheMap.get(clazz);
        if (cache == null) {
            throw new RuntimeException("No cache found for class '%s'".formatted(clazz.getName()));
        }
        return (Cache<T>) cache;
    }

    public static <T> void add(Class<T> clazz, @Nullable T obj) {
        getCacheForClass(clazz).add(obj);
    }

    public static <T> Optional<T> get(Class<T> clazz, UUID uuid) {
        return Optional.ofNullable(getCacheForClass(clazz).get(uuid));
    }

    public static <T> @Nullable T getNullable(Class<T> clazz, UUID uuid) {
        return getCacheForClass(clazz).get(uuid);
    }

    public static <T> @NotNull T getNotNull(Class<T> clazz, UUID uuid) {
        var cached = getCacheForClass(clazz).get(uuid);
        if (cached != null) {
            return cached;
        }
        throw new NullPointerException("No cached item found for class %s with identifier %s".formatted(clazz.getName(), uuid));
    }

    public static <T> void invalidate(Class<T> clazz, @NotNull UUID uuid) {
        getCacheForClass(clazz).invalidate(uuid);
    }

    public static <T> void invalidate(Class<T> clazz, @NotNull T obj) {
        getCacheForClass(clazz).invalidate(obj);
    }

    public static <T> @NotNull List<T> getAll(Class<T> clazz) {
        return getCacheForClass(clazz).getAll();
    }

    public static <T> int size(Class<T> clazz) {
        return getCacheForClass(clazz).size();
    }

    public static <T> int amountByPlayer(Class<T> clazz, @NotNull UUID player) {
        return getCacheForClass(clazz).amountByPlayer(player);
    }

    @FunctionalInterface
    private interface CacheFactory<T> {
        Cache<T> create();
    }
}
