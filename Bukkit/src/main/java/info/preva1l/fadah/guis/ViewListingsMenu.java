package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.guis.LayoutManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ViewListingsMenu extends BrowseMenu {
    public ViewListingsMenu(
            @NotNull Player player,
            OfflinePlayer owner,
            @Nullable String search,
            @Nullable SortingMethod sortingMethod,
            @Nullable SortingDirection sortingDirection) {
        super(
                player,
                owner,
                LayoutManager.MenuType.VIEW_LISTINGS,
                () -> {
                    List<Listing> listings = CacheAccess.getAll(Listing.class);
                    listings.removeIf(listing -> !listing.isOwner(owner.getUniqueId()));
                    return listings;
                },
                search,
                sortingMethod,
                sortingDirection,
                null
        );
    }

    protected void addNavigationButtons() {
        super.addNavigationButtons();

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.BACK, -1),
                Menus.i().getBackButton().itemStack(), e -> new ProfileMenu(player, owner).open(player));
    }

    @Override
    protected void fillScrollbarItems() {
        // do nothing
    }
}
