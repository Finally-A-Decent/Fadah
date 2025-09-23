package info.preva1l.fadah.utils;

import com.github.puregero.multilib.MultiLib;
import com.github.puregero.multilib.regionized.RegionizedTask;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@UtilityClass
public class Tasks {
    @Getter
    private final ScheduledExecutorService loopDeLoop = Executors.newSingleThreadScheduledExecutor();

    /**
     * Run a synchronous task once. Helpful when needing to sync some sync code in an async loop
     *
     * @param plugin   The current plugin
     * @param runnable The runnable
     */
    public RegionizedTask sync(Plugin plugin, Runnable runnable) {
        return MultiLib.getGlobalRegionScheduler().run(plugin, t -> runnable.run());
    }

    /**
     * Run a synchronous task attached to an entities thread.
     * If it fails to get the entities thread it uses the global thread.
     *
     * @param plugin   The current plugin
     * @param runnable The runnable
     */
    public RegionizedTask sync(Plugin plugin, Entity entity, Runnable runnable, Runnable fail) {
        return MultiLib.getEntityScheduler(entity).run(plugin, t -> runnable.run(), fail);
    }

    /**
     * Run a synchronous task once with a delay. Helpful when needing to sync some sync code in an async loop
     *
     * @param plugin   The current plugin
     * @param runnable The runnable
     * @param delay    Time before running.
     */
    public RegionizedTask syncDelayed(Plugin plugin, Runnable runnable, long delay) {
        return MultiLib.getGlobalRegionScheduler().runDelayed(plugin, t -> runnable.run(), delay);
    }
}