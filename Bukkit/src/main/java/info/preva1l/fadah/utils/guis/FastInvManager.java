package info.preva1l.fadah.utils.guis;

import com.github.puregero.multilib.MultiLib;
import info.preva1l.fadah.utils.Reflections;
import info.preva1l.fadah.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manager for FastInv listeners.
 *
 * @author MrMicky
 */
public final class FastInvManager {
    private static final AtomicReference<Plugin> PLUGIN = new AtomicReference<>();

    private FastInvManager() {
        throw new UnsupportedOperationException();
    }

    /**
     * Register listeners for FastInv.
     *
     * @param plugin plugin to register
     * @throws NullPointerException  if plugin is null
     * @throws IllegalStateException if FastInv is already registered
     */
    public static void register(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        if (PLUGIN.getAndSet(plugin) != null) {
            throw new IllegalStateException("FastInv is already registered");
        }

        Bukkit.getPluginManager().registerEvents(new InventoryListener(plugin), plugin);
    }

    /**
     * Close all open FastInv inventories.
     */
    public static void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            MultiLib.getEntityScheduler(player).execute(PLUGIN.get(), () -> {
                if (Reflections.getHolder(player) instanceof FastInv) {
                    player.closeInventory();
                }
            }, null, 0L);
        }
    }

    public static void onPluginDisable() {
        closeAll();
        PLUGIN.set(null);
    }

    public static final class InventoryListener implements Listener {
        private final Plugin plugin;

        public InventoryListener(Plugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getInventory().getHolder(false) instanceof FastInv inv && e.getClickedInventory() != null) {

                boolean wasCancelled = e.isCancelled();
                e.setCancelled(true);

                inv.handleClick(e);

                // This prevents un-canceling the event if another plugin canceled it before
                if (!wasCancelled && !e.isCancelled()) {
                    e.setCancelled(false);
                }
            }
        }

        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent e) {
            if (e.getInventory().getHolder(false) instanceof FastInv inv) {
                inv.handleOpen(e);
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().getHolder(false) instanceof FastInv inv) {
                if (inv.handleClose(e)) {
                    Player player = (Player) e.getPlayer();
                    Tasks.sync(this.plugin, player, () -> inv.open(player), () -> {});
                }
            }
        }
    }
}
