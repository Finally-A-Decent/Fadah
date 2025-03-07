package info.preva1l.fadah;

import info.preva1l.fadah.api.AuctionHouseAPI;
import info.preva1l.fadah.api.BukkitAuctionHouseAPI;
import info.preva1l.fadah.commands.CommandProvider;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.currency.CurrencyProvider;
import info.preva1l.fadah.data.DataProvider;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.hooks.HookProvider;
import info.preva1l.fadah.listeners.PlayerListener;
import info.preva1l.fadah.metrics.Metrics;
import info.preva1l.fadah.metrics.MetricsProvider;
import info.preva1l.fadah.migrator.MigrationProvider;
import info.preva1l.fadah.migrator.MigratorManager;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.processor.DefaultProcessorArgsProvider;
import info.preva1l.fadah.records.listing.ListingExpiryProvider;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.config.BasicConfig;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.utils.guis.LayoutManager;
import info.preva1l.fadah.utils.logging.LoggingProvider;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.william278.desertwell.util.UpdateChecker;
import net.william278.desertwell.util.Version;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;
import java.util.stream.Stream;

public final class Fadah extends JavaPlugin implements MigrationProvider, CurrencyProvider, ListingExpiryProvider,
        CommandProvider, MetricsProvider, LoggingProvider, HookProvider, DataProvider, DefaultProcessorArgsProvider {
    private static final int SPIGOT_ID = 116157;
    @Getter private static Fadah INSTANCE;
    @Getter private static Logger console;
    @Getter private final Logger transactionLogger = Logger.getLogger("AuctionHouse-Transactions");
    private Version pluginVersion;
    @Getter private BasicConfig categoriesFile;
    @Getter private BasicConfig menusFile;

    @Getter private LayoutManager layoutManager;

    @Getter private BukkitAudiences adventureAudience;
    @Getter @Setter private MigratorManager migrationManager;

    @Getter @Setter private Metrics metrics;

    @Getter private UpdateChecker.Completed checked;

    @Override
    public void onLoad() {
        INSTANCE = this;
        pluginVersion = Version.fromString(getDescription().getVersion());
        console = getLogger();
    }

    @Override
    public void onEnable() {
        adventureAudience = BukkitAudiences.create(this);
        getConsole().info("Enabling the API...");
        AuctionHouseAPI.setInstance(new BukkitAuctionHouseAPI());
        getConsole().info("API Enabled!");

        registerDefaultProcessorArgs();
        loadCurrencies();
        loadMenus();
        loadFiles();
        loadDataAndPopulateCaches();
        loadCommands(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        TaskManager.Async.runTask(this, listingExpiryTask(), 10L);
        FastInvManager.register(this);

        Broker.getInstance().load();

        loadHooks();
        loadMigrators();

        initLogger(this);
        setupMetrics(this);

        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&2&l------------------------------"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&a Finally a Decent Auction House"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&a   has successfully started!"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&2&l------------------------------"));

        TaskManager.Sync.runLater(this, this::checkForUpdates, 60L);
    }

    @Override
    public void onDisable() {
        FastInvManager.closeAll(this);
        disableHooks();
        DatabaseManager.getInstance().shutdown();
        if (Config.i().getBroker().isEnabled()) Broker.getInstance().destroy();
        shutdownMetrics();
    }

    private void loadFiles() {
        getConsole().info("Loading Configuration Files...");
        categoriesFile = new BasicConfig(this, "categories.yml");

        Config.i();
        Lang.i();

        categoriesFile.save();
        categoriesFile.load();
        getConsole().info("Configuration Files Loaded!");
    }

    private void loadMenus() {
        layoutManager = new LayoutManager();

        menusFile = new BasicConfig(this, "menus/misc.yml");
        Menus.loadDefault();

        Stream.of(
                new BasicConfig(this, "menus/main.yml"),
                new BasicConfig(this, "menus/new-listing.yml"),
                new BasicConfig(this, "menus/expired-listings.yml"),
                new BasicConfig(this, "menus/active-listings.yml"),
                new BasicConfig(this, "menus/historic-items.yml"),
                new BasicConfig(this, "menus/confirm.yml"),
                new BasicConfig(this, "menus/collection-box.yml"),
                new BasicConfig(this, "menus/profile.yml"),
                new BasicConfig(this, "menus/view-listings.yml"),
                new BasicConfig(this, "menus/watch.yml")
        ).forEach(layoutManager::loadLayout);
    }

    private void checkForUpdates() {
        final UpdateChecker checker = UpdateChecker.builder()
                .currentVersion(pluginVersion)
                .endpoint(UpdateChecker.Endpoint.SPIGOT)
                .resource(Integer.toString(SPIGOT_ID))
                .build();
        checker.check().thenAccept(checked -> {
            if (checked.isUpToDate()) {
                return;
            }
            this.checked = checked;
            Bukkit.getConsoleSender().sendMessage(StringUtils.colorize("&f[Fadah] Fadah is &#D63C3COUTDATED&f! " +
                    "&7Current: &#D63C3C%s &7Latest: &#18D53A%s".formatted(checked.getCurrentVersion(), checked.getLatestVersion())));
        });
    }

    public void reload() {
        reload(this);
    }
}
