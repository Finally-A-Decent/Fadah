package info.preva1l.fadah.records.listing;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created on 31/03/2025
 *
 * @author Preva1l
 */
public interface BidListing extends Listing {
    /**
     * The starting bid amount.
     * @return the amount of the starting bid.
     */
    double getStartingBid();

    /**
     * All the bids.
     * @return returns a sorted set of all bids.
     */
    ConcurrentSkipListSet<Bid> getBids();

    /**
     * Gets the current/the highest bid on the listing.
     *
     * <p>The current will always be the highest.</p>
     *
     * @return the current bid object.
     */
    Bid getCurrentBid();

    /**
     * Add a new bid to the listing if it is active.
     *
     * @param bidder the player placing the bid.
     * @param bidAmount the amount of the bid.
     */
    CompletableFuture<Void> newBid(@NotNull Player bidder, double bidAmount);

    /**
     * Finish the bidding on the listing and award the item to the highest bidder.
     */
    CompletableFuture<Void> completeBidding();
}
