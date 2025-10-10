package info.preva1l.fadah.commands;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.commands.parsers.PriceParser;
import info.preva1l.fadah.commands.subcommands.AboutSubCommand;
import info.preva1l.fadah.commands.subcommands.AdminSubCommands;
import info.preva1l.fadah.commands.subcommands.InspectSubCommand;
import info.preva1l.fadah.commands.subcommands.SellSubCommand;
import info.preva1l.fadah.config.Categories;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.filters.SortingDirection;
import info.preva1l.fadah.filters.SortingMethod;
import info.preva1l.fadah.guis.*;
import info.preva1l.fadah.records.Category;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.guis.LayoutService;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.DefaultValue;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.Nullable;

import static org.incendo.cloud.bukkit.parser.OfflinePlayerParser.offlinePlayerParser;
import static org.incendo.cloud.bukkit.parser.PlayerParser.playerParser;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.incendo.cloud.parser.standard.UUIDParser.uuidParser;

public class AuctionHouseCommand implements InspectSubCommand, AboutSubCommand, AdminSubCommands {
    private static final CloudKey<Boolean> aliasMeta = CloudKey.of("alias", Boolean.class);

    private final Fadah plugin;
    private final LegacyPaperCommandManager<CommandSender> manager;

    private SellSubCommand sellCommand;
    private Lang.Commands conf;
    private Command.Builder<CommandSender> builder;
    private MinecraftHelp<CommandSender> help;

    AuctionHouseCommand(Fadah plugin, LegacyPaperCommandManager<CommandSender> manager) {
        this.plugin = plugin;
        this.manager = manager;
        configure();
    }

    private void configure() {
        this.sellCommand = new SellSubCommand(plugin);
        this.conf = Lang.i().getCommands();
        this.builder = manager.commandBuilder(
                "fadah",
                Description.of(conf.getMain().getDescription()),
                conf.getMain().getAliases().toArray(String[]::new)
        );

        setHelp();

        manager.command(
                builder.senderType(Player.class)
                        .permission("fadah.use")
                        .handler(cmd -> new MainMenu(
                                null,
                                cmd.sender(),
                                null,
                                null,
                                null
                        ).open(cmd.sender()))
        );

        registerSubCommands();
    }

    void reload() {
        setHelp();
    }

    private void setHelp() {
        this.help = MinecraftHelp.<CommandSender>builder()
                .commandManager(manager)
                .audienceProvider(AudienceProvider.nativeAudience())
                .commandPrefix("/fadah help")
                .maxResultsPerPage(5)
                .colors(MinecraftHelp.helpColors(
                        TextColor.fromHexString(conf.getHelp().getPrimaryColor()),
                        TextColor.fromHexString(conf.getHelp().getHighlightColor()),
                        TextColor.fromHexString(conf.getHelp().getAlternativeHighlightColor()),
                        TextColor.fromHexString(conf.getHelp().getTextColor()),
                        TextColor.fromHexString(conf.getHelp().getAccentColor())
                ))
                .commandFilter(predicate -> !predicate.commandMeta().getOrDefault(aliasMeta, false))
                .build();
    }

    private void registerSubCommands() {
        sellCommand(null);
        conf.getSell().getAliases().forEach(this::sellCommand);

        searchCommand(null);
        conf.getSearch().getAliases().forEach(this::searchCommand);

        profileCommand(null);
        conf.getProfile().getAliases().forEach(this::profileCommand);

        activeListingsCommand(null);
        conf.getActiveListings().getAliases().forEach(this::activeListingsCommand);

        collectionBoxCommand(null);
        conf.getCollectionBox().getAliases().forEach(this::collectionBoxCommand);

        expiredItemsCommand(null);
        conf.getExpiredItems().getAliases().forEach(this::expiredItemsCommand);

        historyCommand(null);
        conf.getHistory().getAliases().forEach(this::historyCommand);

        viewCommand(null);
        conf.getViewListing().getAliases().forEach(this::viewCommand);

        watchCommand(null);
        conf.getWatch().getAliases().forEach(this::watchCommand);

        aboutCommand(null);
        conf.getAbout().getAliases().forEach(this::aboutCommand);

        helpCommand(null);
        conf.getHelp().getAliases().forEach(this::helpCommand);

        toggleCommand(null);
        conf.getToggle().getAliases().forEach(this::toggleCommand);

        openCommand(null);
        conf.getOpen().getAliases().forEach(this::openCommand);

        reloadCommand(null);
        conf.getReload().getAliases().forEach(this::reloadCommand);

        viewListingCommand(null);
        conf.getViewListing().getAliases().forEach(this::viewListingCommand);
    }

    private void sellCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "sell";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getSell().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.use")
                        .required("price", PriceParser.create())
                        .handler(cmd -> sellCommand.execute(cmd.sender(), cmd.<Double>get("price")))
        );
    }

    private void searchCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "search";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getSearch().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.search")
                        .required("search", stringParser())
                        .flag(manager.flagBuilder("category").withAliases("c").withComponent(stringParser()).build())
                        .flag(manager.flagBuilder("sort-method").withAliases("s").withComponent(enumParser(SortingMethod.class)).build())
                        .flag(manager.flagBuilder("sort-direction").withAliases("d").withComponent(enumParser(SortingDirection.class)).build())
                        .flag(manager.flagBuilder("player").withAliases("p").withComponent(offlinePlayerParser()).build())
                        .handler(cmd -> {
                            Category category = cmd.flags().<String>getValue("category")
                                    .flatMap(Categories::getCategory)
                                    .orElse(null);
                            SortingMethod sort = cmd.flags().<SortingMethod>getValue("sort-method").orElse(null);
                            SortingDirection direction = cmd.flags().<SortingDirection>getValue("sort-direction").orElse(null);
                            OfflinePlayer owner = cmd.flags().<OfflinePlayer>getValue("p").orElse(null);

                            if (owner != null) {
                                inspect(cmd.sender(), owner, cmd.get("search"), sort, direction);
                                return;
                            }

                            new MainMenu(category, cmd.sender(), cmd.get("search"), sort, direction).open(cmd.sender());
                        })
        );
    }

    private void profileCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "profile";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getProfile().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.profile")
                        .optional("owner", offlinePlayerParser())
                        .handler(cmd -> {
                            OfflinePlayer owner = cmd.getOrDefault("owner", null);
                            if (owner == null || !cmd.sender().hasPermission("fadah.manage.profile")) {
                                owner = cmd.sender();
                            }

                            DataService.instance.loadPlayerData(owner.getUniqueId()).join();

                            new ProfileMenu(cmd.sender(), owner).open(cmd.sender());
                        })
        );
    }

    private void activeListingsCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "active-listings";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getActiveListings().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.active-listings")
                        .optional("owner", offlinePlayerParser())
                        .handler(cmd -> {
                            OfflinePlayer owner = cmd.getOrDefault("owner", null);
                            if (owner == null || !cmd.sender().hasPermission("fadah.manage.active-listings")) {
                                owner = cmd.sender();
                            }

                            inspect(cmd.sender(), owner, null, null, null);
                        })
        );
    }

    private void collectionBoxCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "collection-box";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getCollectionBox().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.collection-box")
                        .optional("owner", offlinePlayerParser())
                        .handler(cmd -> {
                            OfflinePlayer owner = cmd.getOrDefault("owner", null);
                            if (owner == null || !cmd.sender().hasPermission("fadah.manage.collection-box")) {
                                owner = cmd.sender();
                            }

                            OfflinePlayer finalOwner = owner;
                            DataService.instance.loadPlayerData(owner.getUniqueId())
                                    .thenRun(() ->
                                            new CollectionMenu(cmd.sender(), finalOwner, LayoutService.MenuType.COLLECTION_BOX)
                                                    .open(cmd.sender()));
                        })
        );
    }

    private void expiredItemsCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "expired-items";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getExpiredItems().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.expired-items")
                        .optional("owner", offlinePlayerParser())
                        .handler(cmd -> {
                            OfflinePlayer owner = cmd.getOrDefault("owner", null);
                            if (owner == null || !cmd.sender().hasPermission("fadah.manage.expired-items")) {
                                owner = cmd.sender();
                            }

                            OfflinePlayer finalOwner = owner;
                            DataService.instance.loadPlayerData(owner.getUniqueId())
                                    .thenRun(() ->
                                            new CollectionMenu(cmd.sender(), finalOwner, LayoutService.MenuType.EXPIRED_LISTINGS)
                                                    .open(cmd.sender()));
                        })
        );
    }

    private void historyCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "history";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getHistory().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.use")
                        .optional("owner", offlinePlayerParser())
                        .handler(cmd -> {
                            OfflinePlayer owner = cmd.getOrDefault("owner", null);
                            if (owner == null || !cmd.sender().hasPermission("fadah.manage.history")) {
                                owner = cmd.sender();
                            }

                            DataService.instance.loadPlayerData(owner.getUniqueId()).join();

                            new HistoryMenu(cmd.sender(), owner, null).open(cmd.sender());
                        })
        );
    }

    private void viewCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "view";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getView().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.view")
                        .required("owner", offlinePlayerParser())
                        .handler(cmd -> inspect(cmd.sender(), cmd.get("owner"), null, null, null))
        );
    }

    private void watchCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "watch";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getWatch().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.watch")
                        .handler(cmd -> new WatchMenu(cmd.sender()).open(cmd.sender()))
        );
    }

    private void aboutCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "about";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getAbout().getDescription()))
                        .meta(aliasMeta, alias)
                        .handler(this::about)
        );
    }

    private void helpCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "help";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getHelp().getDescription()))
                        .meta(aliasMeta, alias)
                        .optional("query", greedyStringParser(), DefaultValue.constant(""))
                        .handler(context ->
                                help.queryCommands(context.get("query"), context.sender()))
        );
    }

    private void toggleCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "toggle";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getToggle().getDescription()))
                        .meta(aliasMeta, alias)
                        .permission("fadah.toggle-status")
                        .handler(this::toggle)
        );
    }

    private void openCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "open";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getOpen().getDescription()))
                        .meta(aliasMeta, alias)
                        .permission("fadah.open-other")
                        .required("player", playerParser())
                        .handler(this::open)
        );
    }

    private void reloadCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "reload";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getReload().getDescription()))
                        .meta(aliasMeta, alias)
                        .permission("fadah.reload")
                        .handler(this::reload)
        );
    }


    private void viewListingCommand(@Nullable String name) {
        boolean alias = true;
        if (name == null) {
            name = "view-listing";
            alias = false;
        }
        manager.command(
                builder.literal(name, Description.of(conf.getViewListing().getDescription()))
                        .meta(aliasMeta, alias)
                        .senderType(Player.class)
                        .permission("fadah.use")
                        .required("listingId", uuidParser())
                        .handler(cmd -> CacheAccess.get(Listing.class, cmd.get("listingId")).ifPresentOrElse(listing -> {
                                    Player player = cmd.sender();
                                    if (listing.isOwner(player)) {
                                        player.sendMessage(Text.text(Lang.i().getPrefix() + Lang.i().getErrors().getOwnListings()));
                                        return;
                                    }

                                    if (listing instanceof BinListing bin) {
                                        new ConfirmPurchaseMenu(bin, player, player::closeInventory).open(player);
                                    } else if (listing instanceof BidListing bid) {
                                        new PlaceBidMenu(bid, player, player::closeInventory).open(player);
                                    }
                                }, () -> cmd.sender().sendMessage(Text.text(Lang.i().getPrefix() + Lang.i().getErrors().getDoesNotExist())))
                        )
        );
    }
}
