package info.preva1l.fadah.api.menus;

import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.listing.Listing;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Access to open menus via the api.
 *
 * @author Preva1l
 * @since 3.2.2
 */
@SuppressWarnings("unused")
public interface MenuManager {
    boolean mainMenu(@NotNull Player player);

    boolean mainMenu(@NotNull Player player,
                  @Nullable Category category,
                  @Nullable String search,
                  @Nullable SortingMethod sortingMethod,
                  @Nullable SortingDirection sortingDirection
    );

    boolean viewListing(Player player, UUID listingId);

    boolean viewListing(Player player, Listing listing);
}
