package info.preva1l.fadah.hooks;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.impl.DiscordHook;
import info.preva1l.fadah.hooks.impl.EcoItemsHook;
import info.preva1l.fadah.hooks.impl.InfluxDBHook;
import info.preva1l.fadah.hooks.impl.PapiHook;
import org.bukkit.Bukkit;

public interface HookProvider {
    HookManager getHookManager();

    default void loadHooks() {
        Fadah.getConsole().info("Configuring Hooks...");

        if (Config.i().getHooks().isEcoItems()) {
            getHookManager().registerHook(new EcoItemsHook());
        }

        if (Config.i().getHooks().getDiscord().isEnabled()) {
            getHookManager().registerHook(new DiscordHook());
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getHookManager().registerHook(new PapiHook());
        }

        if (Config.i().getHooks().getInfluxdb().isEnabled()) {
            getHookManager().registerHook(new InfluxDBHook());
        }

        Fadah.getConsole().info("Hooked into %s plugins!".formatted(getHookManager().hookCount()));
    }
}
