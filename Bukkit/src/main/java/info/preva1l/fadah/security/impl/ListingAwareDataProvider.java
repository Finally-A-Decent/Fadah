package info.preva1l.fadah.security.impl;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.security.AwareDataProvider;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
public final class ListingAwareDataProvider implements AwareDataProvider<Listing> {
    @Override
    public CompletableFuture<Void> execute(Listing listing, Supplier<CompletableFuture<Void>> action) {
        if (CacheAccess.get(Listing.class, listing.getId()).isEmpty()) return CompletableFuture.completedFuture(null);
        return checkDatabase(listing, action);
    }

    private CompletableFuture<Void> checkDatabase(Listing listing, Supplier<CompletableFuture<Void>> action) {
        return DataService.instance.get(Listing.class, listing.getId()).thenCompose(it -> {
            if (it.isEmpty()) {
                CacheAccess.invalidate(Listing.class, listing);
                return CompletableFuture.completedFuture(null);
            }
            return action.get();
        });
    }
}
