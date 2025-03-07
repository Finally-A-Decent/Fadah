package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;

public class ActiveListingsMenu extends PaginatedFastInv {
    private final Player viewer;
    private final OfflinePlayer owner;
    private final List<Listing> listings;

    public ActiveListingsMenu(Player viewer, OfflinePlayer owner) {
        super(LayoutManager.MenuType.ACTIVE_LISTINGS.getLayout().guiSize(),
                LayoutManager.MenuType.ACTIVE_LISTINGS.getLayout().formattedTitle(viewer.getUniqueId() == owner.getUniqueId()
                ? StringUtils.capitalize(Lang.i().getWords().getYour())
                : owner.getName()+"'s", owner.getName()+"'s"), viewer, LayoutManager.MenuType.ACTIVE_LISTINGS,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;
        this.listings = CacheAccess.getAll(Listing.class);
        listings.removeIf(listing -> !listing.isOwner(owner.getUniqueId()));

        fillers();
        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected synchronized void fillPaginationItems() {
        for (Listing listing : listings) {
            ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                    .addLore(getLang().getLore(player, "lore", StringUtils.removeColorCodes(CategoryRegistry.getCatName(listing.getCategoryID())),
                            new DecimalFormat(Config.i().getFormatting().getNumbers()).format(listing.getPrice()),
                            TimeUtil.formatTimeUntil(listing.getDeletionDate())));

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                if (listing.cancel(viewer)) {
                    updatePagination();
                }
            }));
        }
    }

    @Override
    protected void updatePagination() {
        this.listings.clear();
        this.listings.addAll(CacheAccess.getAll(Listing.class));
        listings.removeIf(listing -> !listing.isOwner(owner.getUniqueId()));
        super.updatePagination();
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.BACK, -1),
                GuiHelper.constructButton(GuiButtonType.BACK), e ->
                        new ProfileMenu(viewer, owner).open(viewer));
    }
}