package info.preva1l.fadah.listeners;

import com.github.puregero.multilib.regionized.RegionizedTask;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Lang;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.guis.NewListingMenu;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.utils.Reflections;
import info.preva1l.fadah.utils.Tasks;
import info.preva1l.fadah.utils.Text;
import info.preva1l.fadah.utils.UpdateService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerListener implements Listener {
    private final Map<UUID, RegionizedTask> invalidateIfNoJoin = new ConcurrentHashMap<>();

    @EventHandler
    public void joinListener(AsyncPlayerPreLoginEvent e) {
        if (!DataService.instance.isConnected()) {
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.kickMessage(Text.text(Lang.i().getPrefix() + Lang.i().getErrors().getDatabaseLoading()));
            return;
        }

        invalidateIfNoJoin.put(e.getUniqueId(), Tasks.syncDelayed(Fadah.getInstance(),
                () -> DataService.instance
                        .invalidateAndSavePlayerData(e.getUniqueId())
                        .thenRun(() -> invalidateIfNoJoin.remove(e.getUniqueId())), 1200L));
        DataService.instance.loadPlayerData(e.getUniqueId()).join();
    }

    @EventHandler
    public void finalJoin(PlayerJoinEvent e) {
        RegionizedTask task = invalidateIfNoJoin.remove(e.getPlayer().getUniqueId());
        if (task != null) {
            task.cancel();
        }

        CacheAccess.get(ExpiredItems.class, e.getPlayer().getUniqueId())
                .ifPresent(items -> {
                    if (items.items().isEmpty()) return;
                    Lang.sendMessage(e.getPlayer(), String.join("\n", Lang.i().getNotifications().getExpiredItem()));
                });

        CacheAccess.get(CollectionBox.class, e.getPlayer().getUniqueId())
                .ifPresent(items -> {
                    if (items.items().isEmpty()) return;
                    Lang.sendMessage(e.getPlayer(), String.join("\n", Lang.i().getNotifications().getNewItem()));
                });

        UpdateService.instance.notifyUpdate(e.getPlayer());
    }

    @EventHandler
    public void leaveListener(PlayerQuitEvent e) {
        DataService.instance.invalidateAndSavePlayerData(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void pickupItem(EntityPickupItemEvent event) {
        try {
            if (!(event.getEntity() instanceof Player player)) return;

            if (Reflections.getHolder(player) instanceof NewListingMenu) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
