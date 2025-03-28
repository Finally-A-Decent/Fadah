package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.hooks.impl.InfluxDBHook;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.hooker.Hooker;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@UtilityClass
public class TransactionLogger {
    public void listingCreated(Listing listing) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(
                System.currentTimeMillis(),
                HistoricItem.LoggedAction.LISTING_START,
                listing.getItemStack(),
                listing.getPrice(),
                null
        );

        CacheAccess.getNotNull(History.class, listing.getOwner()).add(historicItem);

        // Log file logs
        String logMessage = "[NEW LISTING] Seller: %s (%s), Price: %.2f, ItemStack: %s".formatted(
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(),
                Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(),
                listing.getItemStack().toString());

        Fadah.getInstance().getTransactionLogger().info(logMessage);

        Hooker.getHook(InfluxDBHook.class).ifPresent(hook -> hook.log(logMessage));
    }

    public void listingSold(Listing listing, Player buyer) {
        // In Game logs
        HistoricItem historicItemSeller = new HistoricItem(
                System.currentTimeMillis(),
                HistoricItem.LoggedAction.LISTING_SOLD,
                listing.getItemStack(),
                listing.getPrice(),
                buyer.getUniqueId()
        );

        CacheAccess.get(History.class, listing.getOwner())
                .ifPresentOrElse(
                        cache -> cache.add(historicItemSeller),
                        () -> fetchAndSaveHistory(listing.getOwner(), historicItemSeller)
                );

        HistoricItem historicItemBuyer = new HistoricItem(
                System.currentTimeMillis(),
                HistoricItem.LoggedAction.LISTING_PURCHASED,
                listing.getItemStack(),
                listing.getPrice(),
                listing.getOwner()
        );

        CacheAccess.getNotNull(History.class, buyer.getUniqueId()).add(historicItemBuyer);

        // Log file logs
        String logMessage = "[LISTING SOLD] Seller: %s (%s), Buyer: %s (%s), Price: %.2f, ItemStack: %s".formatted(
                listing.getOwnerName(),
                listing.getOwner(),
                buyer.getName(),
                buyer.getUniqueId(),
                listing.getPrice(),
                listing.getItemStack()
        );

        Fadah.getInstance().getTransactionLogger().info(logMessage);

        Hooker.getHook(InfluxDBHook.class).ifPresent(hook -> hook.log(logMessage));
    }

    public void listingRemoval(Listing listing, boolean isAdmin) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(
                System.currentTimeMillis(),
                isAdmin ? HistoricItem.LoggedAction.LISTING_ADMIN_CANCEL : HistoricItem.LoggedAction.LISTING_CANCEL,
                listing.getItemStack(),
                null,
                null
        );

        CacheAccess.get(History.class, listing.getOwner())
                .ifPresentOrElse(
                        cache -> cache.add(historicItem),
                        () -> fetchAndSaveHistory(listing.getOwner(), historicItem)
                );

        // Log file logs
        String logMessage = "[LISTING REMOVED] Seller: %s (%s), Price: %.2f, ItemStack: %s".formatted(
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(),
                Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(),
                listing.getItemStack().toString()
        );

        Fadah.getInstance().getTransactionLogger().info(logMessage);

        Hooker.getHook(InfluxDBHook.class).ifPresent(hook -> hook.log(logMessage));
    }

    public void listingExpired(Listing listing) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(
                System.currentTimeMillis(),
                HistoricItem.LoggedAction.LISTING_EXPIRE,
                listing.getItemStack(),
                null,
                null
        );
        CacheAccess.get(History.class, listing.getOwner())
                .ifPresentOrElse(
                        cache -> cache.add(historicItem),
                        () -> fetchAndSaveHistory(listing.getOwner(), historicItem)
                );

        // Log file logs
        String logMessage = "[LISTING EXPIRED] Seller: %s (%s), Price: %.2f, ItemStack: %s".formatted(
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(),
                Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(),
                listing.getItemStack().toString()
        );

        Fadah.getInstance().getTransactionLogger().info(logMessage);

        Hooker.getHook(InfluxDBHook.class).ifPresent(hook -> hook.log(logMessage));
    }

    private void fetchAndSaveHistory(UUID owner, HistoricItem historicItem) {
        DatabaseManager.getInstance()
                .get(History.class, owner)
                .thenAccept(historyOpt -> {
                    var history = historyOpt.orElseGet(() -> History.empty(owner));
                    history.add(historicItem);
                    DatabaseManager.getInstance().save(History.class, history);
                });
    }
}
