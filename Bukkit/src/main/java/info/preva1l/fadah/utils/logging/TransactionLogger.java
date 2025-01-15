package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.HistoricItemsCache;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.hooks.impl.InfluxDBHook;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.StringUtils;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Optional;

@UtilityClass
public class TransactionLogger {

    public void listingCreated(Listing listing) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(listing.getOwner(), Instant.now().toEpochMilli(), HistoricItem.LoggedAction.LISTING_START, listing.getItemStack(), listing.getPrice(), null);
        History history = History.of(listing.getOwner());
        history.collectableItems().add(historicItem);
        HistoricItemsCache.addLog(listing.getOwner(), historicItem);
        DatabaseManager.getInstance().save(History.class, history);

        // Log file logs
        String logMessage = StringUtils.formatPlaceholders("[NEW LISTING] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(), Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(), listing.getItemStack().toString());

        Fadah.getINSTANCE().getTransactionLogger().info(logMessage);
        Optional<InfluxDBHook> hook = Fadah.getINSTANCE().getHookManager().getHook(InfluxDBHook.class);
        if (Config.i().getHooks().getInfluxdb().isEnabled() && hook.isPresent() && hook.get().isEnabled()) {
            hook.get().log(logMessage);
        }
    }

    public void listingSold(Listing listing, Player buyer) {
        // In Game logs
        HistoricItem historicItemSeller = new HistoricItem(listing.getOwner(), Instant.now().toEpochMilli(),
                HistoricItem.LoggedAction.LISTING_SOLD, listing.getItemStack(), listing.getPrice(), buyer.getUniqueId());

        History historySeller = History.of(listing.getOwner());
        historySeller.collectableItems().add(historicItemSeller);
        HistoricItemsCache.addLog(listing.getOwner(), historicItemSeller);
        DatabaseManager.getInstance().save(History.class, History.of(listing.getOwner()));

        HistoricItem historicItemBuyer = new HistoricItem(buyer.getUniqueId(), Instant.now().toEpochMilli(),
                HistoricItem.LoggedAction.LISTING_PURCHASED, listing.getItemStack(), listing.getPrice(), listing.getOwner());
        History historyBuyer = History.of(buyer.getUniqueId());
        historyBuyer.collectableItems().add(historicItemBuyer);
        HistoricItemsCache.addLog(buyer.getUniqueId(), historicItemBuyer);
        DatabaseManager.getInstance().save(History.class, historyBuyer);

        // Log file logs
        String logMessage = StringUtils.formatPlaceholders("[LISTING SOLD] Seller: {0} ({1}), Buyer: {2} ({3}), Price: {4}, ItemStack: {5}",
                listing.getOwnerName(), listing.getOwner(), buyer.getName(), buyer.getUniqueId(), listing.getPrice(), listing.getItemStack());
        Fadah.getINSTANCE().getTransactionLogger().info(logMessage);
        Optional<InfluxDBHook> hook = Fadah.getINSTANCE().getHookManager().getHook(InfluxDBHook.class);
        if (Config.i().getHooks().getInfluxdb().isEnabled() && hook.isPresent() && hook.get().isEnabled()) {
            hook.get().log(logMessage);
        }
    }

    public void listingRemoval(Listing listing, boolean isAdmin) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(listing.getOwner(), Instant.now().toEpochMilli(),
                isAdmin ? HistoricItem.LoggedAction.LISTING_ADMIN_CANCEL : HistoricItem.LoggedAction.LISTING_CANCEL,
                listing.getItemStack(), null, null);
        History history = History.of(listing.getOwner());
        history.collectableItems().add(historicItem);
        HistoricItemsCache.addLog(listing.getOwner(), historicItem);
        DatabaseManager.getInstance().save(History.class, history);

        // Log file logs
        String logMessage = StringUtils.formatPlaceholders("[LISTING REMOVED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(), Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(), listing.getItemStack().toString());
        Fadah.getINSTANCE().getTransactionLogger().info(logMessage);
        Optional<InfluxDBHook> hook = Fadah.getINSTANCE().getHookManager().getHook(InfluxDBHook.class);
        if (Config.i().getHooks().getInfluxdb().isEnabled() && hook.isPresent() && hook.get().isEnabled()) {
            hook.get().log(logMessage);
        }
    }

    public void listingExpired(Listing listing) {
        // In game logs
        HistoricItem historicItem = new HistoricItem(listing.getOwner(), Instant.now().toEpochMilli(), HistoricItem.LoggedAction.LISTING_EXPIRE,
                listing.getItemStack(), null, null);
        History history = History.of(listing.getOwner());
        history.collectableItems().add(historicItem);
        HistoricItemsCache.addLog(listing.getOwner(), historicItem);
        DatabaseManager.getInstance().save(History.class, history);

        // Log file logs
        String logMessage = StringUtils.formatPlaceholders("[LISTING EXPIRED] Seller: {0} ({1}), Price: {2}, ItemStack: {3}",
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(), Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getPrice(), listing.getItemStack().toString());
        Fadah.getINSTANCE().getTransactionLogger().info(logMessage);
        Optional<InfluxDBHook> hook = Fadah.getINSTANCE().getHookManager().getHook(InfluxDBHook.class);
        if (Config.i().getHooks().getInfluxdb().isEnabled() && hook.isPresent() && hook.get().isEnabled()) {
            hook.get().log(logMessage);
        }
    }
}
