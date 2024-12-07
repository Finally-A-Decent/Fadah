package info.preva1l.fadah.guis;

import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.guis.*;
import info.preva1l.fadah.watcher.AuctionWatcher;
import info.preva1l.fadah.watcher.Watching;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;

import java.text.DecimalFormat;
import java.util.List;

public class WatchMenu extends FastInv {
    private final Watching watching;
    private final Player player;

    public WatchMenu(Player player) {
        super(LayoutManager.MenuType.WATCH);
        this.player = player;
        this.watching = AuctionWatcher.getWatchingListings().getOrDefault(player.getUniqueId(), Watching.base(player));

        List<Integer> fillerSlots = getLayout().fillerSlots();
        if (!fillerSlots.isEmpty()) {
            setItems(fillerSlots.stream().mapToInt(Integer::intValue).toArray(),
                    GuiHelper.constructButton(GuiButtonType.BORDER));
        }

        buttons();
    }

    private void buttons() {
        setSearch();
        setMinPrice();
        setMaxPrice();
        setComplete();
    }

    private void setSearch() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.SEARCH, -1),
                new ItemBuilder(getLang().getAsMaterial("search.icon"))
                        .name(getLang().getStringFormatted("search.name"))
                        .modelData(getLang().getInt("search.model-data"))
                        .lore(getLang().getLore("search.lore", watching.getSearch())).build(), e ->
                        new SearchMenu(player, getLang().getString("search.placeholder", "Search Query..."), search -> {
                            watching.setSearch(search);
                            buttons();
                            open(player);
                        }));
    }

    private void setMinPrice() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.MIN_PRICE, -1),
                new ItemBuilder(getLang().getAsMaterial("min-price.icon"))
                        .name(getLang().getStringFormatted("min-price.name"))
                        .modelData(getLang().getInt("min-price.model-data"))
                        .lore(getLang().getLore("min-price.lore",
                                new DecimalFormat(Config.i().getFormatting().getNumbers()).format(watching.getMinPrice()))).build(), e ->
                        new SearchMenu(player, getLang().getString("min-price.placeholder", "100"), search -> {
                            try {
                                watching.setMinPrice(StringUtils.getAmountFromString(search));
                            } catch (NumberFormatException ex) {
                                player.sendMessage(StringUtils.message(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustBeNumber()));
                            }
                            buttons();
                            open(player);
                        }));
    }

    private void setMaxPrice() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.MAX_PRICE, -1),
                new ItemBuilder(getLang().getAsMaterial("max-price.icon"))
                        .name(getLang().getStringFormatted("max-price.name"))
                        .modelData(getLang().getInt("max-price.model-data"))
                        .lore(getLang().getLore("max-price.lore",
                                new DecimalFormat(Config.i().getFormatting().getNumbers()).format(watching.getMaxPrice()))).build(), e ->
                        new SearchMenu(player, getLang().getString("max-price.placeholder", "10k"), search -> {
                            try {
                                watching.setMaxPrice(StringUtils.getAmountFromString(search));
                            } catch (NumberFormatException ex) {
                                player.sendMessage(StringUtils.message(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustBeNumber()));
                            }
                            buttons();
                            open(player);
                        }));
    }

    private void setComplete() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_START, -1),
                new ItemBuilder(getLang().getAsMaterial("start.icon", Material.EMERALD))
                        .name(getLang().getStringFormatted("start.name"))
                        .modelData(getLang().getInt("start.model-data"))
                        .addLore(getLang().getLore("start.lore"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .build(), e -> {
                    AuctionWatcher.watch(watching);
                    e.getWhoClicked().closeInventory();
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.5f, 1.5f);
                });
    }
}
