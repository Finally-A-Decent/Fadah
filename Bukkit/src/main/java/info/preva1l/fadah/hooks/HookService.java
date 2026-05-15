package info.preva1l.fadah.hooks;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.hooks.impl.DiscordHook;
import info.preva1l.fadah.hooks.impl.EcoItemsHook;
import info.preva1l.fadah.hooks.impl.InfluxDBHook;
import info.preva1l.fadah.hooks.impl.McMMOHook;
import info.preva1l.fadah.hooks.impl.PapiHook;
import info.preva1l.fadah.hooks.impl.permissions.LuckPermsHook;
import info.preva1l.fadah.utils.Tasks;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class HookService {
    public static final HookService instance = new HookService();

    private final Map<Class<?>, Object> hooks = new ConcurrentHashMap<>();

    public void loadHooks() {
        hooks.clear();
        enableHook(DiscordHook.class, DiscordHook::new, () -> Config.i().getHooks().getDiscord().isEnabled(), DiscordHook::onStart);
        enableHook(InfluxDBHook.class, InfluxDBHook::new, () -> Config.i().getHooks().getInfluxdb().isEnabled(), InfluxDBHook::onStart);
        enableHook(EcoItemsHook.class, EcoItemsHook::new, () -> hasPlugin("EcoItems") && Config.i().getHooks().isEcoItems(), hook -> {
            hook.onStart();
            return true;
        });
        enableHook(McMMOHook.class, McMMOHook::new, () -> hasPlugin("mcMMO"), hook -> {
            hook.start();
            return true;
        });
        enableHook(PapiHook.class, PapiHook::new, () -> hasPlugin("PlaceholderAPI"), PapiHook::onEnable);
        enableHook(LuckPermsHook.class, LuckPermsHook::new, () -> hasPlugin("LuckPerms"), LuckPermsHook::onStart);
    }

    public void reloadHooks() {
        shutdown();
        loadHooks();
    }

    public void shutdown() {
        getHook(InfluxDBHook.class).ifPresent(InfluxDBHook::onStop);
        hooks.clear();
    }

    public static <T> Optional<T> getHook(Class<T> hookClass) {
        return Optional.ofNullable(hookClass.cast(instance.hooks.get(hookClass)));
    }

    public static void runAsync(Runnable runnable) {
        MultiLib.getAsyncScheduler().runNow(Fadah.instance, task -> runnable.run());
    }

    public static void runSync(Runnable runnable) {
        Tasks.sync(Fadah.instance, runnable);
    }

    public static void runDelayed(Runnable runnable) {
        Tasks.syncDelayed(Fadah.instance, runnable, 60L);
    }

    private <T> void enableHook(Class<T> hookClass, Supplier<T> supplier, BooleanSupplier requirement, HookStarter<T> starter) {
        if (!requirement.getAsBoolean()) return;

        T hook = supplier.get();
        if (starter.start(hook)) {
            hooks.put(hookClass, hook);
            Fadah.instance.getLogger().info("[Hooks] Enabled " + hookClass.getSimpleName());
        }
    }

    private boolean hasPlugin(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    @FunctionalInterface
    private interface HookStarter<T> {
        boolean start(T hook);
    }
}
