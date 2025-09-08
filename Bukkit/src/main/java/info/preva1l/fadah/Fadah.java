package info.preva1l.fadah;

import info.preva1l.fadah.api.AuctionHouseAPI;
import info.preva1l.fadah.api.BukkitAuctionHouseAPI;
import info.preva1l.fadah.listeners.BombyListener;
import info.preva1l.fadah.listeners.PlayerListener;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.guis.FastInvManager;
import info.preva1l.fadah.warnings.LeafWarning;
import info.preva1l.trashcan.extension.BasePlugin;
import info.preva1l.trashcan.extension.annotations.PluginEnable;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.List;

public final class Fadah extends BasePlugin {
    @Getter public static Fadah instance;

    public Fadah() {
        instance = this;
    }

    @PluginEnable
    public void enable() {
        if (Bukkit.getName().equalsIgnoreCase("leaf")) new LeafWarning().warn();

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
}
