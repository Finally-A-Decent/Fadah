package info.preva1l.fadah.cache.collectionbox;

import info.preva1l.fadah.cache.Cache;
import info.preva1l.fadah.records.CollectionBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class DistributedCollectionCache implements Cache<CollectionBox> {
    @Override
    public void add(@Nullable CollectionBox obj) {

    }

    @Override
    public @Nullable CollectionBox get(UUID uuid) {
        return null;
    }

    @Override
    public void invalidate(@NotNull UUID uuid) {

    }

    @Override
    public void invalidate(@NotNull CollectionBox obj) {

    }

    @Override
    public @NotNull List<CollectionBox> getAll() {
        return List.of();
    }
}
