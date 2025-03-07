package info.preva1l.fadah.cache.history;

import info.preva1l.fadah.cache.Cache;
import info.preva1l.fadah.records.history.History;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryHistoryCache implements Cache<History> {
    private final Map<UUID, History> historicItems = new ConcurrentHashMap<>();

    @Override
    public void add(@Nullable History obj) {
        if (obj == null) return;
        historicItems.put(obj.owner(), obj);
    }

    @Override
    public @Nullable History get(UUID uuid) {
        return historicItems.get(uuid);
    }

    @Override
    public void invalidate(@NotNull UUID uuid) {
        historicItems.remove(uuid);
    }

    @Override
    public void invalidate(@NotNull History obj) {
        historicItems.remove(obj.owner());
    }

    @Override
    public @NotNull List<History> getAll() {
        return new ArrayList<>(historicItems.values());
    }
}
