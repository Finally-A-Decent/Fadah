package info.preva1l.fadah;

import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import info.preva1l.fadah.api.AuctionHouseAPI;
import info.preva1l.fadah.api.BukkitAuctionHouseAPI;
import info.preva1l.fadah.commands.CommandProvider;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DataProvider;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.listeners.PlayerListener;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.config.BasicConfig;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.trashcan.plugin.BasePlugin;
import info.preva1l.trashcan.plugin.annotations.PluginDisable;
import info.preva1l.trashcan.plugin.annotations.PluginEnable;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.logging.Logger;

public final class Fadah extends BasePlugin implements CommandProvider, DataProvider {
    @Getter public static Fadah instance;
    @Getter private static Logger console;

    @Getter private final Logger transactionLogger = Logger.getLogger("AuctionHouse-Transactions");
    @Getter private BasicConfig categoriesFile;

    public Fadah() {
        instance = this;
        console = instance.getLogger();
    }

    @PluginEnable
    public void enable() {
        getConsole().info("Enabling the API...");
        AuctionHouseAPI.setInstance(new BukkitAuctionHouseAPI());
        getConsole().info("API Enabled!");

        loadFiles();
        loadCommands();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        FastInvManager.register(this);

        Broker.getInstance().load();

        Text.list(List.of(
                        "<green>&l-------------------------------",
                        "&a Finally a Decent Auction House",
                        "&a   has successfully started!",
                        "&2&l-------------------------------")
        ).forEach(Bukkit.getConsoleSender()::sendMessage);
    }

    @PluginDisable
    public void disable() {
        DatabaseManager.getInstance().shutdown();
        if (Config.i().getBroker().isEnabled()) Broker.getInstance().destroy();
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

    @Override
    public Fadah getPlugin() {
        return this;
    }
}
