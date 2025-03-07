package info.preva1l.fadah.cache.expired;

import info.preva1l.fadah.cache.Cache;
import info.preva1l.fadah.multiserver.RedisBroker;
import info.preva1l.fadah.records.collection.ExpiredItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.options.LocalCachedMapOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DistributedExpiredCache implements Cache<ExpiredItems> {
    private final RLocalCachedMap<UUID, ExpiredItems> expiredItems;

    public DistributedExpiredCache() {
        final LocalCachedMapOptions<UUID, ExpiredItems> options = LocalCachedMapOptions.<UUID, ExpiredItems>name("expired-listings")
                .cacheSize(100000)
                .maxIdle(Duration.ofSeconds(60))
                .timeToLive(Duration.ofSeconds(60))
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.WEAK)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE)
                .expirationEventPolicy(LocalCachedMapOptions.ExpirationEventPolicy.SUBSCRIBE_WITH_KEYSPACE_CHANNEL);

        expiredItems = RedisBroker.getRedisson().getLocalCachedMap(options);
    }

    @Override
    public void add(@Nullable ExpiredItems obj) {
        if (obj == null) return;
        expiredItems.fastPutAsync(obj.owner(), obj);
    }

    @Override
    public @Nullable ExpiredItems get(UUID uuid) {
        return expiredItems.get(uuid);
    }

    @Override
    public void invalidate(@NotNull UUID uuid) {
        expiredItems.fastRemoveAsync(uuid);
    }

    @Override
    public void invalidate(@NotNull ExpiredItems obj) {
        expiredItems.fastRemoveAsync(obj.owner());
    }

    @Override
    public @NotNull List<ExpiredItems> getAll() {
        return new ArrayList<>(expiredItems.values());
    }
}
