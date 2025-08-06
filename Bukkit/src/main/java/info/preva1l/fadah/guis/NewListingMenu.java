package info.preva1l.fadah.guis;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.commands.subcommands.SellSubCommand;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.CurrencySettings;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.config.misc.Tuple;
import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyRegistry;
import info.preva1l.fadah.hooks.impl.permissions.Permission;
import info.preva1l.fadah.hooks.impl.permissions.PermissionsHook;
import info.preva1l.fadah.records.listing.ImplListingBuilder;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.records.post.PostResult;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.guis.FastInv;
import info.preva1l.fadah.utils.guis.ItemBuilder;
import info.preva1l.fadah.utils.guis.LayoutService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;

public class NewListingMenu extends FastInv {
    private final Player player;
    private final ItemStack itemToSell;
    private long timeOffsetMillis;
    private boolean advertise = Config.i().getListingAdverts().isEnabledByDefault();
    private boolean isBidding = false;
    private Currency currency;

    private boolean startButtonClicked = false;
    private volatile boolean giveItemBack = true;

    public NewListingMenu(Player player, double price) {
        super(LayoutService.MenuType.NEW_LISTING.getLayout().guiSize(),
                LayoutService.MenuType.NEW_LISTING.getLayout().formattedTitle(), LayoutService.MenuType.NEW_LISTING);
        this.player = player;
        var temp = player.getInventory().getItemInMainHand().clone();
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        this.itemToSell = temp;
        this.timeOffsetMillis = Config.i().getDefaultListingLength().toMillis();
        this.currency = CurrencyRegistry.get(CurrencySettings.i().getDefaultCurrency());
        if (currency == null) currency = CurrencyRegistry.getAll().getFirst();

        fillers();
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_START, -1),
                new ItemBuilder(getLang().getAsMaterial("create.icon", Material.EMERALD))
                        .name(getLang().getStringFormatted("create.name", "&aClick to create listing!"))
                        .modelData(getLang().getInt("create.model-data"))
                        .addLore(getLang().getLore(player, "create.lore", Tuple.of("%price%", Config.i().getFormatting().numbers().format(price))))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .build(), e -> {
                    if (startButtonClicked) return;
                    startButtonClicked = true;
                    giveItemBack = false;
                    publishListing(price);
                }
        );
        setClock();
        setModeButton();
        setAdvertButton();
        setCurrencyButton();
        addNavigationButtons();

        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_ITEM, -1), itemToSell);
    }

    private void publishListing(double price) {
        new ImplListingBuilder(player)
                .currency(currency)
                .price(price)
                .tax(PermissionsHook.getValue(Double.class, Permission.LISTING_TAX, player))
                .itemStack(itemToSell)
                .length(timeOffsetMillis)
                .biddable(isBidding)
                .toPost()
                .postAdvert(advertise)
                .buildAndSubmit()
                .failure(result -> {
                    giveItemBack = true;
                    if (result == PostResult.RESTRICTED_ITEM) {
                        Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getRestricted());
                    } else if (result == PostResult.MAX_LISTINGS) {
                        Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMaxListings()
                                .replace("%max%", PermissionsHook.getValue(String.class, Permission.MAX_LISTINGS, player))
                                .replace("%current%", String.valueOf(CacheAccess.amountByPlayer(Listing.class, player.getUniqueId())))
                        );
                    } else {
                        Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getOther().replace("%ex%", result.message()));
                    }

                    player.closeInventory();
                })
                .success(result -> player.closeInventory());
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
        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_TIME, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_TIME, -1),
                new ItemBuilder(getLang().getAsMaterial("time.icon", Material.CLOCK))
                        .name(getLang().getStringFormatted("time.name", "&aTime for listing to be active"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .modelData(getLang().getInt("time.model-data"))
                        .addLore(getLang().getLore("time.lore",
                                Tuple.of("%time%", TimeUtil.formatTimeUntil(Instant.now().plusSeconds(1).plusMillis(timeOffsetMillis).toEpochMilli())))
                        ).build(), e -> {
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
        Component postAdvert = advertise
                        ? getLang().getStringFormatted("advert.options.selected", "&8> &e%option%", Tuple.of("%option%", Lang.i().getAdvertActions().getPost()))
                        : getLang().getStringFormatted("advert.options.not-selected", "&f%option%", Tuple.of("%option%", Lang.i().getAdvertActions().getPost()));
        Component dontPost = !advertise
                        ? getLang().getStringFormatted("advert.options.selected", "&8> &e%option%", Tuple.of("%option%", Lang.i().getAdvertActions().getSilent()))
                        : getLang().getStringFormatted("advert.options.not-selected", "&f%option%", Tuple.of("%option%", Lang.i().getAdvertActions().getSilent()));

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_ADVERT, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_ADVERT, -1),
                new ItemBuilder(getLang().getAsMaterial("advert.icon", Material.OAK_SIGN))
                        .name(getLang().getStringFormatted("advert.name", "&eAdvertise Listing"))
                        .modelData(getLang().getInt("advert.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore(player, "advert.lore",
                                Tuple.of("%cost%", Config.i().getFormatting().numbers().format(
                                        PermissionsHook.getValue(Double.class, Permission.ADVERT_PRICE, player))
                                ),
                                Tuple.of("%first%", postAdvert),
                                Tuple.of("%second%", dontPost))).build(), e -> {
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
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.CURRENCY, -1),
                new ItemBuilder(getLang().getAsMaterial("currency.icon", Material.GOLD_INGOT))
                        .name(getLang().getStringFormatted("currency.name", "&aCurrency"))
                        .modelData(getLang().getInt("currency.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore("currency.lore",
                                Tuple.of("%previous%", previous),
                                Tuple.of("%current%", current),
                                Tuple.of("%next%", next))).build(), e -> {
                    if (e.getClick().isLeftClick() && previousCurrency != null) {
                        currency = previousCurrency;
                    }

                    if (e.getClick().isRightClick() && nextCurrency != null) {
                        currency = nextCurrency;
                    }

                    setCurrencyButton();
                });
    }

    private void setModeButton() {
        Component bidding = isBidding
                        ? getLang().getStringFormatted("mode.options.selected", "&8> &e%option%", Tuple.of("%option%", Lang.i().getWords().getModes().getBidding()))
                        : getLang().getStringFormatted("mode.options.not-selected", "&f%option%", Tuple.of("%option%", Lang.i().getWords().getModes().getBidding()));
        Component bin = !isBidding
                        ? getLang().getStringFormatted("mode.options.selected", "&8> &e%option%", Tuple.of("%option%", Lang.i().getWords().getModes().getBuyItNow()))
                        : getLang().getStringFormatted("mode.options.not-selected", "&f%option%", Tuple.of("%option%", Lang.i().getWords().getModes().getBuyItNow()));

        removeItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_MODE, -1));
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.LISTING_MODE, -1),
                new ItemBuilder(getLang().getAsMaterial("mode.icon", Material.HOPPER))
                        .name(getLang().getStringFormatted("mode.name", "&bAuction Mode"))
                        .modelData(getLang().getInt("mode.model-data"))
                        .setAttributes(null)
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .lore(getLang().getLore("mode.lore",
                                Tuple.of("%first%", bidding),
                                Tuple.of("%second%", bin))).build(), e -> {
                    this.isBidding = !isBidding;
                    setModeButton();
                }
        );
    }

    private void addNavigationButtons() {
        setItem(getLayout().buttonSlots().getOrDefault(LayoutService.ButtonType.CLOSE, 49),
                Menus.i().getCloseButton().itemStack(), e -> e.getWhoClicked().closeInventory());
    }
}
