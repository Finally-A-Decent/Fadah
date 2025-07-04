package info.preva1l.fadah.security.impl;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.security.AwareDataProvider;
import lombok.AllArgsConstructor;

import java.util.concurrent.ExecutorService;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
@AllArgsConstructor
public final class ListingAwareDataProvider implements AwareDataProvider<Listing> {
    private final ExecutorService executor;

    @Override
    public void execute(Listing listing, Runnable action) {
        if (CacheAccess.get(Listing.class, listing.getId()).isEmpty()) return;
        checkDatabase(listing, action);
    }

    private void checkDatabase(Listing listing, Runnable action) {
        DataService.instance.get(Listing.class, listing.getId()).thenAcceptAsync(it -> {
            if (it.isEmpty()) {
                CacheAccess.invalidate(Listing.class, listing);
                return;
            }
            action.run();
        }, executor);
    }
}
