package info.preva1l.fadah.cache.expired;

import info.preva1l.fadah.cache.Cache;
import info.preva1l.fadah.records.collection.ExpiredItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryExpiredCache implements Cache<ExpiredItems> {
    private final Map<UUID, ExpiredItems> expiredItems = new ConcurrentHashMap<>();

    @Override
    public void add(@Nullable ExpiredItems obj) {
        if (obj == null) return;
        expiredItems.put(obj.owner(), obj);
    }

    @Override
    public @Nullable ExpiredItems get(UUID uuid) {
        return expiredItems.get(uuid);
    }

    @Override
    public void invalidate(@NotNull UUID uuid) {
        expiredItems.remove(uuid);
    }

    @Override
    public void invalidate(@NotNull ExpiredItems obj) {
        expiredItems.remove(obj.owner());
    }

    @Override
    public @NotNull List<ExpiredItems> getAll() {
        return new ArrayList<>(expiredItems.values());
    }
}
