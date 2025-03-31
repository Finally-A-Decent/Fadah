package info.preva1l.fadah.commands;

import dev.triumphteam.cmd.bukkit.annotation.Permission;
import dev.triumphteam.cmd.core.BaseCommand;
import dev.triumphteam.cmd.core.annotation.*;
import dev.triumphteam.cmd.core.flag.Flags;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.commands.subcommands.*;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.guis.*;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.guis.LayoutManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@Command("fadah")
@Permission("fadah.use")
public class AuctionHouseCommand extends BaseCommand
        implements InspectSubCommand, AboutSubCommand, AdminSubCommands {
    private final Fadah plugin;
    private final SellSubCommand sellCommand;

    public AuctionHouseCommand(Fadah plugin) {
        super(Lang.i().getCommands().getMain().getAliases());
        this.plugin = plugin;
        this.sellCommand = new SellSubCommand(plugin);
    }

    @Default
    @Requirement("enabled")
    public void execute(Player player) {
        new MainMenu(
                null,
                player,
                null,
                null,
                null
        ).open(player);
    }

    @SubCommand("sell")
    @Permission("fadah.use")
    @Requirement("enabled")
    public void sell(Player player, double price) {
        sellCommand.execute(player, price);
    }

    @SubCommand("search")
    @Permission("fadah.search")
    @Requirement("enabled")
    @CommandFlags({
            @Flag(flag = "cat", longFlag = "category", argument = String.class),
            @Flag(flag = "sort", longFlag = "sorting-method", argument = SortingMethod.class),
            @Flag(flag = "direction", longFlag = "sorting-direction", argument = SortingDirection.class),
            @Flag(flag = "p", longFlag = "player", argument = OfflinePlayer.class),
    })
    public void search(Player player, String search, Flags flags) {
        Category category = flags.getValue("c")
                .flatMap(CategoryRegistry::getCategory)
                .orElse(null);
        SortingMethod sort = flags.getValue("s", SortingMethod.class).orElse(null);
        SortingDirection direction = flags.getValue("d", SortingDirection.class).orElse(null);
        OfflinePlayer owner = flags.getValue("p", OfflinePlayer.class)
                .orElse(null);

        if (owner != null) {
            inspect(player, owner, search, sort, direction);
            return;
        }

        new MainMenu(category, player, search, sort, direction).open(player);
    }

    @SubCommand("active-listings")
    @Permission("fadah.active-listings")
    @Requirement("enabled")
    public void execute(Player player, @Optional OfflinePlayer owner) {
        if (owner == null || !player.hasPermission("fadah.manage.active-listings")) {
            owner = player;
        }

        inspect(player, owner, null, null, null);
    }

    @SubCommand("view")
    @Permission("fadah.view")
    @Requirement("enabled")
    public void view(Player player, OfflinePlayer owner) {
        inspect(player, owner, null, null, null);
    }

    @SubCommand("view-listing")
    @Permission("fadah.use")
    @Requirement("enabled")
    public void viewListing(Player player, UUID listingId) {
        CacheAccess.get(Listing.class, listingId).ifPresentOrElse(listing -> {
            if (listing.isOwner(player)) {
                player.sendMessage(Text.text(Lang.i().getPrefix() + Lang.i().getErrors().getOwnListings()));
                return;
            }

            if (listing instanceof BinListing bin) {
                new ConfirmPurchaseMenu(bin, player, player::closeInventory).open(player);
            }
        }, () -> player.sendMessage(Text.text(Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist())));
    }

    @SubCommand("watch")
    @Permission("fadah.watch")
    @Requirement("enabled")
    public void watch(Player player) {
        new WatchMenu(player).open(player);
    }

    @SubCommand("profile")
    @Permission("fadah.profile")
    @Requirement("enabled")
    public void profile(Player player, @Optional OfflinePlayer owner) {
        if (owner == null || !player.hasPermission("fadah.manage.profile")) owner = player;

        new ProfileMenu(player, owner).open(player);
    }

    @SubCommand("expired-items")
    @Permission("fadah.expired-items")
    @Requirement("enabled")
    public void expired(Player player, @Optional OfflinePlayer owner) {
        if (owner == null || !player.hasPermission("fadah.manage.expired-items")) owner = player;

        new CollectionMenu(
                player,
                owner,
                LayoutManager.MenuType.EXPIRED_LISTINGS
        ).open(player);
    }

    @SubCommand("collection-box")
    @Permission("fadah.collection-box")
    @Requirement("enabled")
    public void collection(Player player, @Optional OfflinePlayer owner) {
        if (owner == null || !player.hasPermission("fadah.manage.collection-box")) owner = player;

        new CollectionMenu(
                player,
                owner,
                LayoutManager.MenuType.COLLECTION_BOX
        ).open(player);
    }

    @SubCommand("history")
    @Permission("fadah.history")
    @Requirement("enabled")
    public void history(Player player, @Optional OfflinePlayer owner) {
        if (owner == null || !player.hasPermission("fadah.manage.history")) owner = player;

        new HistoryMenu(player, owner, null).open(player);
    }

    @SubCommand("about")
    public void about(CommandSender sender) {
        about(sender, plugin);
    }

    @SubCommand("help")
    @Permission("fadah.help")
    public void help(CommandSender sender) {
        StringBuilder message = new StringBuilder(Lang.i().getCommands().getHelp().getHeader());
        for (SubCommandInfo subCommand : plugin.getSubCommands("fadah")) {
            if (!subCommand.permission().hasPermission(sender)) continue;
            message.append("\n").append(Lang.i().getCommands().getHelp().getFormat()
                    .replace("%command%", subCommand.name())
                    .replace("%description%", subCommand.description()));
        }
        sender.sendMessage(Text.text(message.toString()));
    }

    @SubCommand("toggle")
    @Permission("fadah.toggle-status")
    public void toggle(CommandSender sender) {
        toggle(sender, plugin);
    }

    @SubCommand("reload")
    @Permission("fadah.reload")
    public void reload(CommandSender sender) {
        reload(sender, plugin);
    }
}
