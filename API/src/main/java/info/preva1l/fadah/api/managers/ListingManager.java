package info.preva1l.fadah.api.managers;

import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.records.listing.ListingBuilder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manage all active listings via the API.
 * </br>
 * Created on 7/03/2025
 *
 * @author Preva1l
 */
@SuppressWarnings("unused")
public interface ListingManager {
    /**
     * Gets a listing by its id.
     *
     * @param uuid the listing's id.
     * @return an optional containing the listing, or empty if the listing does not exist.
     */
    Optional<Listing> get(UUID uuid);

    /**
     * Gets all the current active listings.
     *
     * @return an immutable list of all the active listings.
     */
    List<Listing> all();

    /**
     * Create a new listing builder for an online player.
     *
     * @param player the player who will own the listing.
     * @return a new listing builder to build a listing object.
     */
    ListingBuilder listingBuilder(Player player);

    /**
     * Create a new listing builder.
     * </br>
     * You can make the uniqueId and name a placeholder if you want the listing to be "listed by the server".
     *
     * @param uniqueId the player's uuid who will own the listing.
     * @param name the player's name who will own the listing.
     * @return a new listing builder to build a listing object.
     */
    ListingBuilder listingBuilder(UUID uniqueId, String name);
}
