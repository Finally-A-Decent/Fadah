package info.preva1l.fadah.cache;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.listing.DistributedListingCache;
import info.preva1l.fadah.cache.listing.MemoryListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.records.listing.Listing;

public final class CacheAccess {
    private static Cache<Listing> listingCache;

    public static Cache<Listing> getListingCache() {
        if (listingCache != null) return listingCache;

        if (Config.i().getBroker().isEnabled() && Fadah.getINSTANCE().getBroker() != null) {
            listingCache = new DistributedListingCache();
        } else {
            listingCache = new MemoryListingCache();
        }

        return listingCache;
    }
}
