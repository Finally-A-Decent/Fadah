package info.preva1l.fadah.utils;

import com.github.puregero.multilib.MultiLib;
import com.github.puregero.multilib.regionized.RegionizedTask;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

@UtilityClass
public class Tasks {
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

    public RegionizedTask syncRepeating(Plugin plugin, Runnable runnable, long delay, long period) {
        return MultiLib.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> runnable.run(), delay, period);
    }

    public RegionizedTask syncRepeating(Plugin plugin, Entity entity, Runnable runnable, Runnable fail, long delay, long period) {
        return MultiLib.getEntityScheduler(entity).runAtFixedRate(plugin, t -> runnable.run(), fail, delay, period);
    }

    public CompletableFuture<Void> syncFuture(Plugin plugin, Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        sync(plugin, () -> completeFuture(future, runnable));
        return future;
    }

    public CompletableFuture<Void> syncFuture(Plugin plugin, Entity entity, Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        sync(
                plugin,
                entity,
                () -> completeFuture(future, runnable),
                () -> sync(plugin, () -> completeFuture(future, runnable))
        );
        return future;
    }

    private void completeFuture(CompletableFuture<Void> future, Runnable runnable) {
        try {
            runnable.run();
            future.complete(null);
        } catch (Throwable throwable) {
            future.completeExceptionally(throwable);
        }
    }
}
