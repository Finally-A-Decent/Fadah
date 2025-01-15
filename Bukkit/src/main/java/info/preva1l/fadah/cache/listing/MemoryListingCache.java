package info.preva1l.fadah.cache.listing;

import info.preva1l.fadah.cache.Cache;
import info.preva1l.fadah.records.listing.Listing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryListingCache implements Cache<Listing> {
    private final Map<UUID, Listing> listings = new ConcurrentHashMap<>();

    @Override
    public void add(@Nullable Listing obj) {
        if (obj == null) return;
        listings.put(obj.getId(), obj);
    }

    @Override
    public @Nullable Listing get(@NotNull UUID uuid) {
        return listings.get(uuid);
    }

    @Override
    public void invalidate(@NotNull UUID uuid) {
        listings.remove(uuid);
    }

    @Override
    public void invalidate(@NotNull Listing obj) {
        listings.remove(obj.getId());
    }

    @Override
    public @NotNull List<Listing> getAll() {
        return new ArrayList<>(listings.values());
    }
}
