package info.preva1l.fadah.guis;

import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutManager;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class ConfirmPurchaseMenu extends FastInv {
    public ConfirmPurchaseMenu(Listing listing,
                               Player player,
                               @Nullable Category category,
                               @Nullable String search,
                               @Nullable SortingMethod sortingMethod,
                               @Nullable SortingDirection sortingDirection,
                               boolean isViewListings,
                               @Nullable OfflinePlayer listingsPlayer) {
        super(LayoutManager.MenuType.CONFIRM_PURCHASE.getLayout().guiSize(),
                LayoutManager.MenuType.CONFIRM_PURCHASE.getLayout().guiTitle(),
                LayoutManager.MenuType.CONFIRM_PURCHASE);

        fillers();
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CONFIRM, -1),
                new ItemBuilder(getLang().getAsMaterial("confirm.icon", Material.LIME_CONCRETE))
                        .name(getLang().getStringFormatted("confirm.name", "&a&lCONFIRM"))
                        .modelData(getLang().getInt("confirm.model-data"))
                        .lore(getLang().getLore("confirm.lore")).build(), e -> {
            player.closeInventory();
            listing.purchase(((Player) e.getWhoClicked()));
        });

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CANCEL, -1),
                new ItemBuilder(getLang().getAsMaterial("cancel.icon", Material.RED_CONCRETE))
                        .name(getLang().getStringFormatted("cancel.name", "&c&lCANCEL"))
                        .modelData(getLang().getInt("cancel.model-data"))
                        .lore(getLang().getLore("cancel.lore")).build(), e -> {
            if (isViewListings) {
                assert listingsPlayer != null;
                new ViewListingsMenu(player, listingsPlayer).open(player);
                return;
            }
            new MainMenu(category, player, search, sortingMethod, sortingDirection).open(player);
        });

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.ITEM_TO_PURCHASE, -1),
                listing.getItemStack().clone());
    }
}
