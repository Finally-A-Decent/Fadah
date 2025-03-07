package info.preva1l.fadah.guis;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.commands.subcommands.SellSubCommand;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyRegistry;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.records.listing.ImplListingBuilder;
import info.preva1l.fadah.records.post.PostResult;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.logging.Level;

public class NewListingMenu extends FastInv {
    private final Fadah plugin = Fadah.getINSTANCE();
    private final Player player;
    private final ItemStack itemToSell;
    private long timeOffsetMillis;
    private boolean advertise = Config.i().getListingAdverts().isEnabledByDefault();
    private boolean isBidding = false;
    private Currency currency;

    private boolean startButtonClicked = false;
    private boolean giveItemBack = true;

    public NewListingMenu(Player player, double price) {
        super(LayoutManager.MenuType.NEW_LISTING.getLayout().guiSize(),
                LayoutManager.MenuType.NEW_LISTING.getLayout().guiTitle(), LayoutManager.MenuType.NEW_LISTING);
        this.player = player;
        var temp = player.getInventory().getItemInMainHand().clone();
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        this.itemToSell = temp;
        this.timeOffsetMillis = Config.i().getDefaultListingLength().toMillis();
        this.currency = CurrencyRegistry.get(Config.i().getCurrency().getDefaultCurrency());
        if (currency == null) currency = CurrencyRegistry.getAll().getFirst();

        fillers();
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_START, -1),
                new ItemBuilder(getLang().getAsMaterial("create.icon", Material.EMERALD))
                        .name(getLang().getStringFormatted("create.name", "&aClick to create listing!"))
                        .modelData(getLang().getInt("create.model-data"))
                        .addLore(getLang().getLore(player, "create.lore",
                                new DecimalFormat(Config.i().getFormatting().getNumbers())
                                        .format(price)))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .build(), e -> {
                    if (startButtonClicked) return;
                    startButtonClicked = true;
                    giveItemBack = false;
                    new ImplListingBuilder(player)
                            .currency(currency)
                            .price(price)
                            .tax(PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player))
                            .itemStack(itemToSell)
                            .length(timeOffsetMillis)
                            .biddable(isBidding)
                            .toPost()
                            .postAdvert(advertise)
                            .buildAndSubmit().thenAcceptAsync(result -> TaskManager.Sync.run(plugin, player, () -> {
                                if (result == PostResult.RESTRICTED_ITEM) {
                                    giveItemBack = true;
                                    Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getRestricted());
                                    SellSubCommand.running.remove(player.getUniqueId());
                                    return;
                                }

                                if (result == PostResult.MAX_LISTINGS) {
                                    giveItemBack = true;
                                    Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMaxListings()
                                            .replace("%max%", String.valueOf(PermissionsData.getHighestInt(
                                                    PermissionsData.PermissionType.MAX_LISTINGS,
                                                    player))
                                            )
                                            .replace("%current%", String.valueOf(PermissionsData.getCurrentListings(player)))
                                    );
                                    SellSubCommand.running.remove(player.getUniqueId());
                                    return;
                                }

                                if (!result.successful()) {
                                    giveItemBack = true;
                                    Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getOther().replace("%ex%", result.message()));
                                }

                                player.closeInventory();
                                SellSubCommand.running.remove(player.getUniqueId());
                            }), DatabaseManager.getInstance().getThreadPool())
                            .exceptionally(t -> {
                                Fadah.getConsole().log(Level.SEVERE, t.getMessage(), t);
                                return null;
                            });
                }
        );
        setClock();
        setAdvertButton();
        setCurrencyButton();
        //setModeButton();
        addNavigationButtons();

        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_ITEM, -1), itemToSell);
    }

    @Override
    protected void onClose(InventoryCloseEvent event) {
        super.onClose(event);
        if (giveItemBack) {
            player.getInventory().setItemInMainHand(itemToSell);
            giveItemBack = false;
        }
        SellSubCommand.running.remove(player.getUniqueId());
    }

    private void setClock() {
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_TIME, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_TIME, -1),
                new ItemBuilder(getLang().getAsMaterial("time.icon", Material.CLOCK))
                        .name(getLang().getStringFormatted("time.name", "&aTime for listing to be active"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .modelData(getLang().getInt("time.model-data"))
                        .addLore(getLang().getLore("time.lore", TimeUtil.formatTimeUntil(Instant.now().plusSeconds(1).plusMillis(timeOffsetMillis).toEpochMilli()))).build(), e -> {
                    if (e.isRightClick()) {
                        if (e.isShiftClick()) {
                            if (timeOffsetMillis - 30 * 60 * 1000 < 0)
                                return;
                            timeOffsetMillis -= 30 * 60 * 1000;
                            setClock();
                            return;
                        }
                        if (timeOffsetMillis - 60 * 60 * 1000 < 0)
                            return;
                        timeOffsetMillis -= 60 * 60 * 1000;
                        setClock();
                    }

                    if (e.isLeftClick()) {
                        if (e.isShiftClick()) {
                            if (timeOffsetMillis + 30 * 60 * 1000 > Config.i().getMaxListingLength().toMillis())
                                return;
                            timeOffsetMillis += 30 * 60 * 1000;
                            setClock();
                            return;
                        }
                        if (timeOffsetMillis + 60 * 60 * 1000 > Config.i().getMaxListingLength().toMillis())
                            return;
                        timeOffsetMillis += 60 * 60 * 1000;
                        setClock();
                    }
                });
    }

    private void setAdvertButton() {
        String postAdvert = StringUtils.formatPlaceholders(advertise
                        ? getLang().getStringFormatted("advert.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("advert.options.not-selected", "&f{0}"),
                Lang.i().getAdvertActions().getPost());
        String dontPost = StringUtils.formatPlaceholders(!advertise
                        ? getLang().getStringFormatted("advert.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("advert.options.not-selected", "&f{0}"),
                Lang.i().getAdvertActions().getSilent());

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_ADVERT, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_ADVERT, -1),
                new ItemBuilder(getLang().getAsMaterial("advert.icon", Material.OAK_SIGN))
                        .name(getLang().getStringFormatted("advert.name", "&eAdvertise Listing"))
                        .modelData(getLang().getInt("advert.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore(player, "advert.lore",
                                new DecimalFormat(Config.i().getFormatting().getNumbers())
                                        .format(PermissionsData.getHighestDouble(PermissionsData.PermissionType.ADVERT_PRICE, player)),
                                postAdvert, dontPost)).build(), e -> {
                    this.advertise = !advertise;
                    setAdvertButton();
                }
        );
    }

    private void setCurrencyButton() {
        Currency previousCurrency = CurrencyRegistry.getPrevious(currency);
        String previous = previousCurrency == null
                ? Lang.i().getWords().getNone()
                : previousCurrency.getName();
        String current = currency.getName();
        Currency nextCurrency = CurrencyRegistry.getNext(currency);
        String next = nextCurrency == null
                ? Lang.i().getWords().getNone()
                : nextCurrency.getName();
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CURRENCY, -1),
                new ItemBuilder(getLang().getAsMaterial("currency.icon", Material.GOLD_INGOT))
                        .name(getLang().getStringFormatted("currency.name", "&aCurrency"))
                        .modelData(getLang().getInt("currency.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore("currency.lore",
                                StringUtils.colorize(previous),
                                StringUtils.colorize(current),
                                StringUtils.colorize(next))).build(), e -> {
                    if (e.getClick().isLeftClick() && previousCurrency != null) {
                        currency = previousCurrency;
                    }

                    if (e.getClick().isRightClick() && nextCurrency != null) {
                        currency = nextCurrency;
                    }

                    setCurrencyButton();
                });
    }

    // Not Used (For future bidding update)
    private void setModeButton() {
        String bidding = StringUtils.formatPlaceholders(isBidding
                        ? getLang().getStringFormatted("mode.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("mode.options.not-selected", "&f{0}"),
                Lang.i().getWords().getModes().getBidding());
        String bin = StringUtils.formatPlaceholders(!isBidding
                        ? getLang().getStringFormatted("mode.options.selected", "&8> &e{0}")
                        : getLang().getStringFormatted("mode.options.not-selected", "&f{0}"),
                Lang.i().getWords().getModes().getBuyItNow());

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_MODE, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.LISTING_MODE, -1),
                new ItemBuilder(getLang().getAsMaterial("mode.icon", Material.HOPPER))
                        .name(getLang().getStringFormatted("mode.name", "&bAuction Mode"))
                        .modelData(getLang().getInt("mode.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore("mode.lore", bidding, bin)).build(), e -> {
                    this.isBidding = !isBidding;
                    setModeButton();
                }
        );
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutManager.ButtonType.CLOSE, 49),
                GuiHelper.constructButton(GuiButtonType.CLOSE), e -> e.getWhoClicked().closeInventory());
    }
}
