package info.preva1l.fadah;

import info.preva1l.fadah.api.AuctionHouseAPI;
import info.preva1l.fadah.api.BukkitAuctionHouseAPI;
import info.preva1l.fadah.commands.CommandService;
import info.preva1l.fadah.config.Categories;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.CurrencySettings;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.config.Menus;
import info.preva1l.fadah.config.upgraders.ConfigUpgraderService;
import info.preva1l.fadah.currency.CurrencyService;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.filters.MatcherService;
import info.preva1l.fadah.hooks.HookService;
import info.preva1l.fadah.listeners.BombyListener;
import info.preva1l.fadah.listeners.PlayerListener;
import info.preva1l.fadah.metrics.MetricsService;
import info.preva1l.fadah.migrator.MigrationService;
import info.preva1l.fadah.security.AwareDataService;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.UpdateService;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.utils.guis.LayoutService;
import info.preva1l.fadah.utils.logging.LoggingService;
import info.preva1l.fadah.warnings.LeafWarning;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Fadah extends JavaPlugin {
    @Getter public static Fadah instance;

    public Fadah() {
        instance = this;
    }

    @Override
    public void onEnable() {
        instance = this;
        if (Bukkit.getName().equalsIgnoreCase("leaf")) new LeafWarning().warn();

        bootstrapServices();

        AuctionHouseAPI.setInstance(new BukkitAuctionHouseAPI());

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new BombyListener(), this);
        FastInvManager.register(this);

        Text.list(List.of(
                        "&2&l-------------------------------",
                        "&a Finally a Decent Auction House",
                        "&a   has successfully started!",
                        "&2&l-------------------------------")
        ).forEach(Bukkit.getConsoleSender()::sendMessage);
    }

    @Override
    public void onDisable() {
        FastInvManager.onPluginDisable();
        MetricsService.instance.close();
        HookService.instance.shutdown();
        DataService.instance.shutdown();
    }

    public void reload() {
        Config.reload();
        Lang.reload();
        CurrencySettings.reload();
        Menus.reload();
        Categories.reload();
        LayoutService.instance.reload();
        CommandService.instance.reload();
        HookService.instance.reloadHooks();
        FastInvManager.closeAll();
    }

    public String getCurrentVersion() {
        return getDescription().getVersion();
    }

    private void bootstrapServices() {
        ConfigUpgraderService.instance.init(this).configure();

        Config.i();
        Lang.i();
        CurrencySettings.i();
        Menus.i();

        MatcherService.instance.init(this).configure();
        HookService.instance.loadHooks();
        Categories.i();
        CurrencyService.instance.init(this).loadCurrencies();
        AwareDataService.instance.configure();
        LoggingService.instance.init(this).initLogger();
        LayoutService.instance.init(this).load();
        DataService.instance.init(this).configure();
        MigrationService.instance.init(this).loadMigrators();
        CommandService.instance.init(this).configure();
        UpdateService.instance.init(this).checkForUpdates();
        MetricsService.instance.init(this).configure();
    }
}
