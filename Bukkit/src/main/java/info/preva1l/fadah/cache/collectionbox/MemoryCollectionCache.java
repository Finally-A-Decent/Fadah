package info.preva1l.fadah.cache.collectionbox;

import info.preva1l.fadah.cache.Cache;
import info.preva1l.fadah.records.collection.CollectionBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryCollectionCache implements Cache<CollectionBox> {
    private final Map<UUID, CollectionBox> collectionBoxes = new ConcurrentHashMap<>();

    @Override
    public void add(@Nullable CollectionBox obj) {
        if (obj == null) return;
        collectionBoxes.put(obj.owner(), obj);
    }

    @Override
    public @Nullable CollectionBox get(UUID uuid) {
        return collectionBoxes.get(uuid);
    }

    @Override
    public void invalidate(@NotNull UUID uuid) {
        collectionBoxes.remove(uuid);
    }

    @Override
    public void invalidate(@NotNull CollectionBox obj) {
        collectionBoxes.remove(obj.owner());
    }

    @Override
    public @NotNull List<CollectionBox> getAll() {
        return new ArrayList<>(collectionBoxes.values());
    }
}
