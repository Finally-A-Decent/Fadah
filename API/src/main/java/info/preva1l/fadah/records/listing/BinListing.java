package info.preva1l.fadah.records.listing;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created on 31/03/2025
 *
 * @author Preva1l
 */
public interface BinListing extends Listing {
    /**
     * Purchase the listing if it is active.
     *
     * @param buyer the player buying the listing.
     * @throws IllegalStateException if the listing is not a {@code Buy It Now} listing.
     */
    void purchase(@NotNull Player buyer);
}
