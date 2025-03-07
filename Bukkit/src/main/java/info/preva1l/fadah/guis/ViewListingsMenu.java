package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;

public class ViewListingsMenu extends PaginatedFastInv {
    private final OfflinePlayer owner;
    private final List<Listing> listings;

    public ViewListingsMenu(Player viewer, OfflinePlayer owner) {
        super(LayoutManager.MenuType.VIEW_LISTINGS.getLayout().guiSize(),
                LayoutManager.MenuType.VIEW_LISTINGS.getLayout().formattedTitle(viewer.getUniqueId() == owner.getUniqueId()
                        ? StringUtils.capitalize(Lang.i().getWords().getYour())
                        : owner.getName()+"'s", owner.getName()+"'s"), viewer, LayoutManager.MenuType.VIEW_LISTINGS,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
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
            String buyMode = listing instanceof BidListing
                    ? getLang().getStringFormatted("listing.lore-buy.bidding")
                    : getLang().getStringFormatted("listing.lore-buy.buy-it-now");

            ItemBuilder itemStack = new ItemBuilder(listing.getItemStack().clone())
                    .addLore(getLang().getLore(player, "listing.lore-body",
                            listing.getOwnerName(),
                            StringUtils.removeColorCodes(CategoryRegistry.getCatName(listing.getCategoryID())), buyMode,
                            new DecimalFormat(Config.i().getFormatting().getNumbers())
                                    .format(listing.getPrice()), TimeUtil.formatTimeUntil(listing.getDeletionDate())));

            if (player.getUniqueId().equals(listing.getOwner())) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.own-listing"));
            } else if (listing.getCurrency().canAfford(player, listing.getPrice())) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.buy"));
            } else {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.too-expensive"));
            }
            if (listing.getItemStack().getType().name().toUpperCase().endsWith("SHULKER_BOX")) {
                itemStack.addLore(getLang().getStringFormatted("listing.lore-footer.is-shulker"));
            }

            addPaginationItem(new PaginatedItem(itemStack.build(), e -> {
                if (e.isShiftClick() && e.getWhoClicked().hasPermission("fadah.manage.active-listings")) {
                    if (listing.cancel(((Player) e.getWhoClicked()))) {
                        updatePagination();
                    }
                    return;
                }

                if (e.isRightClick() && listing.getItemStack().getType().name().toUpperCase().endsWith("SHULKER_BOX")) {
                    new ShulkerBoxPreviewMenu(listing, player, null, null,
                            null, null, true, owner).open(player);
                    return;
                }

                if (!listing.canBuy(player)) return;

                new ConfirmPurchaseMenu(listing, player, null, null,
                        null, null, true, owner).open(player);
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
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CLOSE, -1),
                GuiHelper.constructButton(GuiButtonType.CLOSE), e -> e.getWhoClicked().closeInventory());
    }
}
