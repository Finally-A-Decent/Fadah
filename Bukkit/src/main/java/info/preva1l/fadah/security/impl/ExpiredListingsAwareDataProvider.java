package info.preva1l.fadah.security.impl;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.security.AwareCollectableDataProvider;
import info.preva1l.fadah.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Created on 16/06/2025
 *
 * @author Preva1l
 */
public final class ExpiredListingsAwareDataProvider implements AwareCollectableDataProvider<ExpiredItems> {
    @Override
    public void execute(ExpiredItems box, CollectableItem item, Runnable action) {
        CacheAccess.get(ExpiredItems.class, box.owner())
                .ifPresent(b -> {
                    if (!b.contains(item)) return;
                    checkDatabase(b, item, action);
                });
    }

    private void checkDatabase(ExpiredItems box, CollectableItem item, Runnable action) {
        DataService.instance.get(ExpiredItems.class, box.owner())
                .thenCompose(it -> {
                    if (it.isEmpty()) return CompletableFuture.completedFuture(null);
                    ExpiredItems b = it.get();
                    if (!b.contains(item)) {
                        box.remove(item);
                        return CompletableFuture.completedFuture(null);
                    }
                    return runOwnerAware(box.owner(), action);
                });
    }

    private CompletableFuture<Void> runOwnerAware(UUID owner, Runnable action) {
        Player player = Bukkit.getPlayer(owner);
        if (player != null && player.isOnline()) {
            return Tasks.syncFuture(Fadah.getInstance(), player, action);
        }
        return Tasks.syncFuture(Fadah.getInstance(), action);
    }
}
