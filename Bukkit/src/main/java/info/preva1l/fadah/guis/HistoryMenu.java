package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

public class HistoryMenu extends PaginatedFastInv {
    private final Player viewer;
    private final OfflinePlayer owner;
    private final List<HistoricItem> historicItems;

    public HistoryMenu(Player viewer, OfflinePlayer owner, @Nullable String dateSearch) {
        super(LayoutManager.MenuType.HISTORY.getLayout().guiSize(), LayoutManager.MenuType.HISTORY.getLayout().formattedTitle(
                        viewer.getUniqueId() == owner.getUniqueId()
                                ? StringUtils.capitalize(Lang.i().getWords().getYour())
                                : owner.getName() + "'s", owner.getName() + "'s"),
                viewer, LayoutManager.MenuType.HISTORY,
                List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34));
        this.viewer = viewer;
        this.owner = owner;
        this.historicItems = HistoricItemsCache.getHistory(owner.getUniqueId());

        if (dateSearch != null) {
            this.historicItems.removeIf(historicItem -> !TimeUtil.formatTimeToVisualDate(historicItem.getLoggedDate()).contains(dateSearch));
        }

        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }
        setPaginationMappings(getLayout().paginationSlots());

        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected synchronized void fillPaginationItems() {
        for (HistoricItem historicItem : historicItems) {
            ItemBuilder itemStack = new ItemBuilder(historicItem.getItemStack().clone());
            if (historicItem.getPurchaserUUID() != null) {
                itemStack.addLore(historicItem.getAction() == HistoricItem.LoggedAction.LISTING_SOLD
                        ? getLang().getLore(player,"lore-with-buyer",
                        historicItem.getAction().getLocaleActionName(),
                        Bukkit.getOfflinePlayer(historicItem.getPurchaserUUID()).getName(),
                        new DecimalFormat(Config.i().getFormatting().getNumbers()).format(historicItem.getPrice()),
                        TimeUtil.formatTimeToVisualDate(historicItem.getLoggedDate()))

                        : getLang().getLore(player, "lore-with-seller",
                        historicItem.getAction().getLocaleActionName(),
                        Bukkit.getOfflinePlayer(historicItem.getPurchaserUUID()).getName(),
                        new DecimalFormat(Config.i().getFormatting().getNumbers()).format(historicItem.getPrice()),
                        TimeUtil.formatTimeToVisualDate(historicItem.getLoggedDate()))
                );
            } else if (historicItem.getPrice() != null && historicItem.getPrice() != 0d) {
                itemStack.addLore(getLang().getLore(player, "lore-with-price",
                        historicItem.getAction().getLocaleActionName(),
                        new DecimalFormat(Config.i().getFormatting().getNumbers()).format(historicItem.getPrice()),
                        TimeUtil.formatTimeToVisualDate(historicItem.getLoggedDate())
                ));
            } else {
                itemStack.addLore(getLang().getLore(player, "lore",
                        historicItem.getAction().getLocaleActionName(),
                        TimeUtil.formatTimeToVisualDate(historicItem.getLoggedDate())
                ));
            }
            addPaginationItem(new PaginatedItem(itemStack.build(), (e) -> {}));
        }
    }

    @Override
    protected void addPaginationControls() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, -1),
                GuiHelper.constructButton(GuiButtonType.BORDER));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,-1),
                GuiHelper.constructButton(GuiButtonType.BORDER));
        if (page > 0) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_ONE, -1),
                    GuiHelper.constructButton(GuiButtonType.PREVIOUS_PAGE), e -> previousPage());
        }

        if (historicItems != null && historicItems.size() >= index + 1) {
            setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.PAGINATION_CONTROL_TWO,-1),
                    GuiHelper.constructButton(GuiButtonType.NEXT_PAGE), e -> nextPage());
        }
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.BACK, -1),
                GuiHelper.constructButton(GuiButtonType.BACK), e -> new ProfileMenu(viewer, owner).open(viewer));

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.SEARCH, -1),
                new ItemBuilder(getLang().getAsMaterial("search.icon", Material.OAK_SIGN))
                        .name(getLang().getStringFormatted("search.name", "&eSearch Date"))
                        .modelData(getLang().getInt("search.model-data"))
                        .lore(getLang().getLore("search.lore")).build(), e ->
                        new SearchMenu(viewer, getLang().getString("search.placeholder", "Ex: 21/04/2024 22:26"),
                                search -> new HistoryMenu(viewer, owner, search).open(viewer)));
    }
}