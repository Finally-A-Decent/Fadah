package info.preva1l.fadah.listeners;

import com.github.puregero.multilib.regionized.RegionizedTask;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.guis.NewListingMenu;
import info.preva1l.fadah.utils.StringUtils;
import info.preva1l.fadah.utils.TaskManager;
import info.preva1l.fadah.utils.guis.InventoryEventHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final Map<UUID, RegionizedTask> invalidateIfNoJoin = new HashMap<>();

    @EventHandler
    public void joinListener(AsyncPlayerPreLoginEvent e) {
        if (!DatabaseManager.getInstance().isConnected()) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.setKickMessage(StringUtils.colorize(Lang.i().getPrefix() + Lang.i().getErrors().getDatabaseLoading()));
            return;
        }

        invalidateIfNoJoin.put(e.getUniqueId(), TaskManager.Sync.runLater(Fadah.getINSTANCE(),
                () -> Fadah.getINSTANCE()
                        .invalidateAndSavePlayerData(e.getUniqueId())
                        .thenRun(() -> invalidateIfNoJoin.remove(e.getUniqueId())), 1200L));
        Fadah.getINSTANCE().loadPlayerData(e.getUniqueId()).join();
    }

    @EventHandler
    public void finalJoin(PlayerJoinEvent e) {
        RegionizedTask task = invalidateIfNoJoin.remove(e.getPlayer().getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler
    public void leaveListener(PlayerQuitEvent e) {
        Fadah.getINSTANCE().invalidateAndSavePlayerData(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void pickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof NewListingMenu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        RegionizedTask task = InventoryEventHandler.tasksToQuit.get(event.getInventory());
        if (task != null) {
            task.cancel();
        }
    }
}
