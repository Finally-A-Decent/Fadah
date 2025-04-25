package info.preva1l.fadah;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import info.preva1l.fadah.api.AuctionHouseAPI;
import info.preva1l.fadah.api.BukkitAuctionHouseAPI;
import info.preva1l.fadah.commands.CommandProvider;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.currency.CurrencyProvider;
import info.preva1l.fadah.data.DataProvider;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.hooks.HookProvider;
import info.preva1l.fadah.listeners.PlayerListener;
import info.preva1l.fadah.metrics.MetricsProvider;
import info.preva1l.fadah.migrator.MigrationProvider;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.processor.DefaultProcessorArgsProvider;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.UpdatesProvider;
import info.preva1l.fadah.utils.config.BasicConfig;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.utils.guis.LayoutManager;
import info.preva1l.fadah.utils.logging.LoggingProvider;
import info.preva1l.hooker.Hooker;
import info.preva1l.trashcan.plugin.BasePlugin;
import info.preva1l.trashcan.plugin.annotations.PluginDisable;
import info.preva1l.trashcan.plugin.annotations.PluginEnable;
import info.preva1l.trashcan.plugin.annotations.PluginLoad;
import info.preva1l.trashcan.plugin.annotations.PluginReload;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class Fadah extends BasePlugin implements MigrationProvider, CurrencyProvider, CommandProvider,
        MetricsProvider, LoggingProvider, HookProvider, DataProvider, DefaultProcessorArgsProvider, UpdatesProvider {
    @Getter public static Fadah instance;
    @Getter private static Logger console;

    @Getter private final Logger transactionLogger = Logger.getLogger("AuctionHouse-Transactions");
    @Getter private BasicConfig categoriesFile;

    public Fadah() {
        instance = this;
    }

    @PluginLoad
    public void load() {
        console = getLogger();
        loadHooks();
        initLogger();
    }

    @PluginEnable
    public void enable() {
        getConsole().info("Enabling the API...");
        AuctionHouseAPI.setInstance(new BukkitAuctionHouseAPI());
        getConsole().info("API Enabled!");

        registerDefaultProcessorArgs();
        loadCurrencies();
        loadMenus();
        loadFiles();
        loadDataAndPopulateCaches();
        loadCommands();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        FastInvManager.register(this);

        Broker.getInstance().load();

        Hooker.enable();
        loadMigrators();

        setupMetrics();

        Text.list(List.of(
                        "<green>&l-------------------------------",
                        "&a Finally a Decent Auction House",
                        "&a   has successfully started!",
                        "&2&l-------------------------------")
        ).forEach(Bukkit.getConsoleSender()::sendMessage);

        checkForUpdates();
    }

    @PluginDisable
    public void disable() {
        DatabaseManager.getInstance().shutdown();
        if (Config.i().getBroker().isEnabled()) Broker.getInstance().destroy();
        shutdownMetrics();
    }

    @Override
    public BukkitCommandManager<?> getCommandManager() {
        return CommandManagerHolder.commandManager;
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
        Stream.of(
                new BasicConfig(this, "menus/main.yml"),
                new BasicConfig(this, "menus/new-listing.yml"),
                new BasicConfig(this, "menus/expired-listings.yml"),
                new BasicConfig(this, "menus/historic-items.yml"),
                new BasicConfig(this, "menus/confirm.yml"),
                new BasicConfig(this, "menus/collection-box.yml"),
                new BasicConfig(this, "menus/profile.yml"),
                new BasicConfig(this, "menus/view-listings.yml"),
                new BasicConfig(this, "menus/watch.yml")
        ).forEach(LayoutManager.instance::loadLayout);
    }

    @PluginReload
    public void extraReloads() {
        Hooker.reload();
    }

    @Override
    public Fadah getPlugin() {
        return this;
    }
}
