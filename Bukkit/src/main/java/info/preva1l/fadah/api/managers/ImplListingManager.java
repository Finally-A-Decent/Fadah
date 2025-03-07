package info.preva1l.fadah.api.managers;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.records.listing.ImplListingBuilder;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.records.listing.ListingBuilder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
public final class ImplListingManager implements ListingManager {
    @Override
    public Optional<Listing> get(UUID uuid) {
        return CacheAccess.get(Listing.class, uuid);
    }

    @Override
    public List<Listing> all() {
        return CacheAccess.getAll(Listing.class);
    }

    @Override
    public ListingBuilder listingBuilder(Player player) {
        return new ImplListingBuilder(player);
    }

    @Override
    public ListingBuilder listingBuilder(UUID uniqueId, String name) {
        return new ImplListingBuilder(uniqueId, name);
    }
}
