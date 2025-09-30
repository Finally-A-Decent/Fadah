package info.preva1l.fadah.api.managers;

import info.preva1l.fadah.api.menus.MenuManager;
import info.preva1l.fadah.api.menus.SortingDirection;
import info.preva1l.fadah.api.menus.SortingMethod;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.guis.ConfirmPurchaseMenu;
import info.preva1l.fadah.guis.MainMenu;
import info.preva1l.fadah.guis.PlaceBidMenu;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This is a class.
 *
 * @author Preva1l
 * @since 30/09/2025
 */
public final class ImplMenuManager implements MenuManager {

    @Override
    public boolean mainMenu(@NotNull Player player) {
        new MainMenu(null, player, null, null, null).open(player);
        return true;
    }

    @Override
    public boolean mainMenu(@NotNull Player player,
                         @Nullable Category category,
                         @Nullable String search,
                         @Nullable SortingMethod sortingMethod,
                         @Nullable SortingDirection sortingDirection
    ) {
        info.preva1l.fadah.filters.SortingMethod sortMethod;
        if (sortingMethod != null) {
            sortMethod = info.preva1l.fadah.filters.SortingMethod.valueOf(sortingMethod.name());
        } else sortMethod = null;

        info.preva1l.fadah.filters.SortingDirection sortDirection;
        if (sortingDirection != null) {
            sortDirection = info.preva1l.fadah.filters.SortingDirection.valueOf(sortingDirection.name());
        } else sortDirection = null;

        new MainMenu(
                category,
                player,
                search,
                sortMethod,
                sortDirection
        ).open(player);

        return true;
    }

    @Override
    public boolean viewListing(Player player, UUID listingId) {
        Listing listing = CacheAccess.get(Listing.class, listingId).orElse(null);
        if (listing == null) return false;
        if (listing.isOwner(player)) return false;

        if (listing instanceof BinListing bin) {
            new ConfirmPurchaseMenu(bin, player, player::closeInventory).open(player);
        } else if (listing instanceof BidListing bid) {
            new PlaceBidMenu(bid, player, player::closeInventory).open(player);
        }

        return true;
    }

    @Override
    public boolean viewListing(Player player, Listing listing) {
        if (listing.isOwner(player)) return false;

        if (listing instanceof BinListing bin) {
            new ConfirmPurchaseMenu(bin, player, player::closeInventory).open(player);
        } else if (listing instanceof BidListing bid) {
            new PlaceBidMenu(bid, player, player::closeInventory).open(player);
        }

        return true;
    }
}
