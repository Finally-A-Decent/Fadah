package info.preva1l.fadah.commands.subcommands;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.api.ListingCreateEvent;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.ListHelper;
import info.preva1l.fadah.config.Tuple;
import info.preva1l.fadah.currency.Currency;
import info.preva1l.fadah.currency.CurrencyRegistry;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.data.PermissionsData;
import info.preva1l.fadah.filters.Restrictions;
import info.preva1l.fadah.guis.NewListingMenu;
import info.preva1l.fadah.hooks.impl.DiscordHook;
import info.preva1l.fadah.multiserver.Message;
import info.preva1l.fadah.multiserver.Payload;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.TimeUtil;
import info.preva1l.fadah.utils.commands.SubCommand;
import info.preva1l.fadah.utils.commands.SubCommandArgs;
import info.preva1l.fadah.utils.commands.SubCommandArguments;
import info.preva1l.fadah.utils.logging.TransactionLogger;
import info.preva1l.fadah.watcher.AuctionWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SellSubCommand extends SubCommand {
    public static List<UUID> running = new ArrayList<>();

    public SellSubCommand(Fadah plugin) {
        super(plugin, Lang.i().getCommands().getSell().getAliases(), Lang.i().getCommands().getSell().getDescription());
    }

    @SubCommandArgs(name = "sell", permission = "fadah.use")
    public void execute(@NotNull SubCommandArguments command) {
        if (!Config.i().isEnabled()) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getDisabled());
            return;
        }
        assert command.getPlayer() != null;
        if (running.contains(command.getPlayer().getUniqueId())) return;
        running.add(command.getPlayer().getUniqueId());
        if (command.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustHoldItem());
            running.remove(command.getPlayer().getUniqueId());
            return;
        }
        if (command.args().length == 0) {
            command.reply(Lang.i().getPrefix() + Lang.i().getErrors().getInvalidUsage()
                    .replace("%command%", Lang.i().getCommands().getSell().getUsage()));
            running.remove(command.getPlayer().getUniqueId());
            return;
        }
        String priceString = command.args()[0];

        if (priceString.toLowerCase().contains("nan")) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustBeNumber());
            running.remove(command.getPlayer().getUniqueId());
            return;
        }

        double price = StringUtils.getAmountFromString(priceString);

        try {
            if (price < Config.i().getListingPrice().getMin()) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getListingPrice().getMin()
                        .replace("%price%", Config.i().getListingPrice().getMin() + ""));
                running.remove(command.getPlayer().getUniqueId());
                return;
            }
            if (price > Config.i().getListingPrice().getMax()) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getListingPrice().getMax()
                        .replace("%price%", Config.i().getListingPrice().getMax() + ""));
                running.remove(command.getPlayer().getUniqueId());
                return;
            }
            int currentListings = PermissionsData.getCurrentListings(command.getPlayer());
            int maxListings = PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, command.getPlayer());
            if (currentListings >= maxListings) {
                command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMaxListings()
                        .replace("%current%", currentListings + "")
                        .replace("%max%", maxListings + ""));
                running.remove(command.getPlayer().getUniqueId());
                return;
            }
            handleSell(command, price);
        } catch (NumberFormatException e) {
            command.reply(Lang.i().getPrefix() + Lang.i().getCommands().getSell().getMustBeNumber());
            running.remove(command.getPlayer().getUniqueId());
        }
    }

    private void handleSell(SubCommandArguments command, double price) {
        if (!Config.i().isMinimalMode()) {
            MultiLib.getEntityScheduler(command.getPlayer()).run(Fadah.getINSTANCE(),
                    t -> new NewListingMenu(command.getPlayer(), price).open(command.getPlayer()),
                    () -> command.reply("&A critical error has occurred while trying to obtain the correct thread."));
            return;
        }

        startListing(command.getPlayer(), price);
    }

    private void startListing(Player player, double price) {
        ItemStack itemToSell = player.getInventory().getItemInMainHand();
        player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        Currency currency = CurrencyRegistry.getAll().getFirst();
        Restrictions.isRestrictedItem(itemToSell).thenAccept(restricted ->
                CategoryCache.getCategoryForItem(itemToSell).thenAccept(category ->
                        MultiLib.getEntityScheduler(player).run(Fadah.getINSTANCE(), t -> {
                            if (restricted) {
                                player.getInventory().setItemInMainHand(itemToSell);
                                Lang.sendMessage(player, Lang.i().getPrefix() + Lang.i().getErrors().getRestricted());
                                return;
                            }

                            double tax = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);

                            Listing listing = new BinListing(
                                    UUID.randomUUID(), player.getUniqueId(), player.getName(),
                                    itemToSell, category, currency.getId(), price, tax,
                                    Instant.now().toEpochMilli(),
                                    Instant.now().plus(Config.i().getDefaultListingLength().toMillis(), ChronoUnit.MILLIS).toEpochMilli(),
                                    false, Collections.emptyList()
                            );

                            ListingCreateEvent createEvent = new ListingCreateEvent(player, listing);
                            Bukkit.getServer().getPluginManager().callEvent(createEvent);

                            if (createEvent.isCancelled()) {
                                player.getInventory().setItemInMainHand(itemToSell);
                                Lang.sendMessage(player, Lang.i().getPrefix() + createEvent.getCancelReason());
                                player.closeInventory();
                                return;
                            }

                            ListingCache.addListing(listing);
                            DatabaseManager.getInstance().save(Listing.class, listing).thenRunAsync(() -> {
                                if (Config.i().getBroker().isEnabled()) {
                                    Message.builder()
                                            .type(Message.Type.LISTING_ADD)
                                            .payload(Payload.withUUID(listing.getId()))
                                            .build()
                                            .send(Fadah.getINSTANCE().getBroker());
                                }
                            }, DatabaseManager.getInstance().getThreadPool());

                            player.closeInventory();

                            double taxAmount = PermissionsData.getHighestDouble(PermissionsData.PermissionType.LISTING_TAX, player);
                            String itemName = StringUtils.extractItemName(listing.getItemStack());
                            String message = String.join("\n", ListHelper.replace(
                                    Lang.i().getNotifications().getNewListing(),
                                    Tuple.of("%item%", itemName),
                                    Tuple.of("%price%", new DecimalFormat(Config.i().getFormatting().getNumbers()).format(listing.getPrice())),
                                    Tuple.of("%time%", TimeUtil.formatTimeUntil(listing.getDeletionDate())),
                                    Tuple.of("%current_listings%", PermissionsData.getCurrentListings(player) + ""),
                                    Tuple.of("%max_listings%", PermissionsData.getHighestInt(PermissionsData.PermissionType.MAX_LISTINGS, player) + ""),
                                    Tuple.of("%tax%", taxAmount + ""),
                                    Tuple.of("%price_after_tax%", new DecimalFormat(Config.i().getFormatting().getNumbers())
                                            .format((taxAmount / 100) * price))
                            ));
                            Lang.sendMessage(player, message);

                            TransactionLogger.listingCreated(listing);

                            Config.Hooks.Discord discConf = Config.i().getHooks().getDiscord();
                            if ((discConf.isEnabled() && plugin.getHookManager().getHook(DiscordHook.class).isPresent()) &&
                                    !discConf.isOnlySendOnAdvert()) {
                                plugin.getHookManager().getHook(DiscordHook.class).get().send(listing);
                            }

                            TaskManager.Async.run(Fadah.getINSTANCE(), () -> AuctionWatcher.alertWatchers(listing));
                        }, () -> player.sendMessage(StringUtils.colorize("&cA critical error occurred while trying to obtain the correct thread.")))
                )
        );
    }
}
