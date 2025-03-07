package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
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
        this.historicItems = CacheAccess.getNotNull(History.class, owner.getUniqueId()).historicItems();

        if (dateSearch != null) {
            this.historicItems.removeIf(historicItem -> !TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()).contains(dateSearch));
        }

        fillers();
        setPaginationMappings(getLayout().paginationSlots());

        addNavigationButtons();
        fillPaginationItems();
        populatePage();
        addPaginationControls();
    }

    @Override
    protected synchronized void fillPaginationItems() {
        for (HistoricItem historicItem : historicItems) {
            ItemBuilder itemStack = new ItemBuilder(historicItem.itemStack().clone());
            if (historicItem.purchaserUUID() != null) {
                itemStack.addLore(historicItem.action() == HistoricItem.LoggedAction.LISTING_SOLD
                        ? getLang().getLore(player,"lore-with-buyer",
                        historicItem.action().getLocaleActionName(),
                        Bukkit.getOfflinePlayer(historicItem.purchaserUUID()).getName(),
                        new DecimalFormat(Config.i().getFormatting().getNumbers()).format(historicItem.price()),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()))

                        : getLang().getLore(player, "lore-with-seller",
                        historicItem.action().getLocaleActionName(),
                        Bukkit.getOfflinePlayer(historicItem.purchaserUUID()).getName(),
                        new DecimalFormat(Config.i().getFormatting().getNumbers()).format(historicItem.price()),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate()))
                );
            } else if (historicItem.price() != null && historicItem.price() != 0d) {
                itemStack.addLore(getLang().getLore(player, "lore-with-price",
                        historicItem.action().getLocaleActionName(),
                        new DecimalFormat(Config.i().getFormatting().getNumbers()).format(historicItem.price()),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate())
                ));
            } else {
                itemStack.addLore(getLang().getLore(player, "lore",
                        historicItem.action().getLocaleActionName(),
                        TimeUtil.formatTimeToVisualDate(historicItem.loggedDate())
                ));
            }
            addPaginationItem(new PaginatedItem(itemStack.build(), (e) -> {}));
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