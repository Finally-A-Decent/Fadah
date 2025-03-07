package info.preva1l.fadah.hooks;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.hooks.impl.DiscordHook;
import info.preva1l.fadah.hooks.impl.EcoItemsHook;
import info.preva1l.fadah.hooks.impl.InfluxDBHook;
import info.preva1l.fadah.hooks.impl.PapiHook;

import java.util.List;

public interface HookProvider {
    List<Class<? extends Hook>> allHooks = List.of(
            EcoItemsHook.class,
            DiscordHook.class,
            PapiHook.class,
            InfluxDBHook.class
    );

    default void loadHooks() {
        Fadah.getConsole().info("Configuring Hooks...");
        allHooks.forEach(HookManager.i()::registerHook);
        Fadah.getConsole().info("Hooked into %s plugins/services!".formatted(HookManager.i().hookCount()));
    }

    default void disableHooks() {
        Fadah.getConsole().info("Disabling Hooks...");
        allHooks.forEach(HookManager.i()::disableHook);
        Fadah.getConsole().info("Hooks disabled!");
    }
}
