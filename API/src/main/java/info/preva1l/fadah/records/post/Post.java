package info.preva1l.fadah.records.post;

import info.preva1l.fadah.records.listing.ListingBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * A post is the final step before publishing a listing,
 * it allows you to configure everything about the initial listing.
 * </br>
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
@SuppressWarnings("unused")
public abstract class Post {
    protected final ListingBuilder listingBuilder;
    protected boolean postAdvert = false;
    protected boolean bypassAdvertCost = false;
    protected boolean bypassMaxListings = false;
    protected boolean bypassRestrictedItems = false;
    protected boolean bypassTax = false;
    protected boolean callEvent = true;
    protected boolean notifyPlayer = true;
    protected boolean submitLog = true;
    protected boolean alertWatchers = true;

    protected Post(ListingBuilder listing) {
        this.listingBuilder = listing;
    }

    public Post postAdvert(boolean postAdvert) {
        this.postAdvert = postAdvert;
        return this;
    }

    public Post bypassAdvertCost(boolean bypassAdvertCost) {
        this.bypassAdvertCost = bypassAdvertCost;
        return this;
    }

    public Post bypassMaxListings(boolean bypassMaxListings) {
        this.bypassMaxListings = bypassMaxListings;
        return this;
    }

    public Post bypassRestrictedItems(boolean bypassRestrictedItems) {
        this.bypassRestrictedItems = bypassRestrictedItems;
        return this;
    }

    public Post bypassTax(boolean bypassTax) {
        this.bypassTax = bypassTax;
        return this;
    }

    public Post callEvent(boolean callEvent) {
        this.callEvent = callEvent;
        return this;
    }

    public Post notifyPlayer(boolean notifyPlayer) {
        this.notifyPlayer = notifyPlayer;
        return this;
    }

    public Post submitLog(boolean submitLog) {
        this.submitLog = submitLog;
        return this;
    }

    public Post alertWatchers(boolean alertWatchers) {
        this.alertWatchers = alertWatchers;
        return this;
    }

    /**
     * Build the post and the listing and submit it to the auction house.
     *
     * @return a completable future of {@link PostResult}
     */
    public abstract CompletableFuture<PostResult> buildAndSubmit();
}
