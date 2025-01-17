package info.preva1l.fadah.cache;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.collectionbox.DistributedCollectionCache;
import info.preva1l.fadah.cache.collectionbox.MemoryCollectionCache;
import info.preva1l.fadah.cache.listing.DistributedListingCache;
import info.preva1l.fadah.cache.listing.MemoryListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.listing.Listing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CacheAccess {
    private static final Map<Class<?>, Cache<?>> cacheMap = new ConcurrentHashMap<>();

    static {
        registerCache(Listing.class, MemoryListingCache::new, DistributedListingCache::new);
        registerCache(CollectionBox.class, MemoryCollectionCache::new, DistributedCollectionCache::new);
    }

    private static <T> void registerCache(Class<T> clazz, CacheFactory<T> memoryCache, CacheFactory<T> distributedCache) {
        Cache<T> cache;
        if (Config.i().getBroker().isEnabled() && Fadah.getINSTANCE().getBroker() != null) {
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

    public static <T> @Nullable T get(Class<T> clazz, UUID uuid) {
        return getCacheForClass(clazz).get(uuid);
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

    @FunctionalInterface
    private interface CacheFactory<T> {
        Cache<T> create();
    }
}
