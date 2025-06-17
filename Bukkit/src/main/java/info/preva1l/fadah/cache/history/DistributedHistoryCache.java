package info.preva1l.fadah.cache.history;

import info.preva1l.fadah.cache.Cache;
import info.preva1l.fadah.multiserver.RedisBroker;
import info.preva1l.fadah.records.history.History;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.options.LocalCachedMapOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DistributedHistoryCache implements Cache<History> {
    private final RLocalCachedMap<UUID, History> historicItems;

    public DistributedHistoryCache() {
        final LocalCachedMapOptions<UUID, History> options = LocalCachedMapOptions.<UUID, History>name("history")
                .cacheSize(0)
                .timeToLive(Duration.ZERO)
                .maxIdle(Duration.ZERO)
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.NONE)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                .storeMode(LocalCachedMapOptions.StoreMode.LOCALCACHE_REDIS)
                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.LOAD)
                .expirationEventPolicy(LocalCachedMapOptions.ExpirationEventPolicy.SUBSCRIBE_WITH_KEYSPACE_CHANNEL)
                .cacheProvider(LocalCachedMapOptions.CacheProvider.REDISSON)
                .storeCacheMiss(false);

        historicItems = RedisBroker.getRedisson().getLocalCachedMap(options);
    }

    @Override
    public void add(@Nullable History obj) {
        if (obj == null) return;
        historicItems.fastPutAsync(obj.owner(), obj);
    }

    @Override
    public @Nullable History get(UUID uuid) {
        return historicItems.get(uuid);
    }

    @Override
    public void invalidate(@NotNull UUID uuid) {
        historicItems.fastRemoveAsync(uuid);
    }

    @Override
    public void invalidate(@NotNull History obj) {
        historicItems.fastRemoveAsync(obj.owner());
    }

    @Override
    public @NotNull List<History> getAll() {
        return new ArrayList<>(historicItems.readAllValues());
    }

    @Override
    public int size() {
        return historicItems.size();
    }

    @Override
    public int amountByPlayer(@NotNull UUID player) {
        return historicItems.get(player).historicItems().size();
    }
}
