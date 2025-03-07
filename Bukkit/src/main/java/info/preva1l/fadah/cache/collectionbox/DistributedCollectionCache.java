package info.preva1l.fadah.cache.collectionbox;

import info.preva1l.fadah.cache.Cache;
import info.preva1l.fadah.multiserver.RedisBroker;
import info.preva1l.fadah.records.collection.CollectionBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.options.LocalCachedMapOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DistributedCollectionCache implements Cache<CollectionBox> {
    private final RLocalCachedMap<UUID, CollectionBox> collectionBoxes;

    public DistributedCollectionCache() {
        final LocalCachedMapOptions<UUID, CollectionBox> options = LocalCachedMapOptions.<UUID, CollectionBox>name("collection-boxes")
                .cacheSize(100000)
                .maxIdle(Duration.ofSeconds(60))
                .timeToLive(Duration.ofSeconds(60))
                .evictionPolicy(LocalCachedMapOptions.EvictionPolicy.WEAK)
                .syncStrategy(LocalCachedMapOptions.SyncStrategy.INVALIDATE)
                .expirationEventPolicy(LocalCachedMapOptions.ExpirationEventPolicy.SUBSCRIBE_WITH_KEYSPACE_CHANNEL);

        collectionBoxes = RedisBroker.getRedisson().getLocalCachedMap(options);
    }

    @Override
    public void add(@Nullable CollectionBox obj) {
        if (obj == null) return;
        collectionBoxes.fastPutAsync(obj.owner(), obj);
    }

    @Override
    public @Nullable CollectionBox get(UUID uuid) {
        return collectionBoxes.get(uuid);
    }

    @Override
    public void invalidate(@NotNull UUID uuid) {
        collectionBoxes.fastRemoveAsync(uuid);
    }

    @Override
    public void invalidate(@NotNull CollectionBox obj) {
        collectionBoxes.fastRemoveAsync(obj.owner());
    }

    @Override
    public @NotNull List<CollectionBox> getAll() {
        return new ArrayList<>(collectionBoxes.values());
    }
}
