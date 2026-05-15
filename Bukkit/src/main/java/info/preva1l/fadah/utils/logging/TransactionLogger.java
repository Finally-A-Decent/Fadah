package info.preva1l.fadah.utils.logging;

import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.data.DataService;
import info.preva1l.fadah.hooks.HookService;
import info.preva1l.fadah.hooks.impl.InfluxDBHook;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.history.ImplHistory;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

@UtilityClass
public class TransactionLogger {
    public void listingCreated(Listing listing) {
        boolean bidding = false;
        double price;
        if (listing instanceof BinListing bin) {
            price = bin.getPrice();
        } else if (listing instanceof BidListing bid) {
            bidding = true;
            price = bid.getCurrentBid().bidAmount();
        } else throw new IllegalArgumentException("Invalid listing class: " + listing);

        // In game logs
        HistoricItem historicItem = new HistoricItem(
                System.currentTimeMillis(),
                HistoricItem.LoggedAction.LISTING_START,
                listing.getItemStack(),
                price,
                null,
                bidding,
                null
        );

        CacheAccess.get(History.class, listing.getOwner())
                .ifPresentOrElse(
                        cache -> cache.add(historicItem),
                        () -> fetchAndSaveHistory(listing.getOwner(), historicItem)
                );

        // Log file logs
        String logMessage = "[NEW LISTING] Seller: %s (%s), Price: %.2f, ItemStack: %s".formatted(
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(),
                Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                price,
                listing.getItemStack().toString());

        LoggingService.instance.transactionLogger.info(logMessage);

        HookService.getHook(InfluxDBHook.class).ifPresent(hook -> hook.log(logMessage));
    }

    public void listingSold(Listing listing, OfflinePlayer buyer) {
        boolean bidding = false;
        double price;
        Double startingBid = null;
        if (listing instanceof BinListing bin) {
            price = bin.getPrice();
        } else if (listing instanceof BidListing bid) {
            bidding = true;
            price = bid.getCurrentBid().bidAmount();
            startingBid = bid.getStartingBid();
        } else throw new IllegalArgumentException("Invalid listing class: " + listing);

        // In Game logs
        HistoricItem historicItemSeller = new HistoricItem(
                System.currentTimeMillis(),
                HistoricItem.LoggedAction.LISTING_SOLD,
                listing.getItemStack(),
                price,
                buyer.getUniqueId(),
                bidding,
                startingBid
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
                price,
                listing.getOwner(),
                bidding,
                startingBid
        );

        CacheAccess.get(History.class, buyer.getUniqueId())
                .ifPresentOrElse(
                        cache -> cache.add(historicItemBuyer),
                        () -> fetchAndSaveHistory(buyer.getUniqueId(), historicItemBuyer)
                );

        // Log file logs
        String logMessage = "[LISTING SOLD] Seller: %s (%s), Buyer: %s (%s), Price: %.2f, ItemStack: %s".formatted(
                listing.getOwnerName(),
                listing.getOwner(),
                buyer.getName(),
                buyer.getUniqueId(),
                price,
                listing.getItemStack()
        );

        LoggingService.instance.transactionLogger.info(logMessage);

        HookService.getHook(InfluxDBHook.class).ifPresent(hook -> hook.log(logMessage));
    }

    public void listingRemoval(Listing listing, boolean isAdmin) {
        // In game logs
        listingInGame(listing, isAdmin ? HistoricItem.LoggedAction.LISTING_ADMIN_CANCEL : HistoricItem.LoggedAction.LISTING_CANCEL);

        // Log file logs
        String logMessage = "[LISTING REMOVED] Seller: %s (%s), ItemStack: %s".formatted(
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(),
                Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getItemStack().toString()
        );

        LoggingService.instance.transactionLogger.info(logMessage);

        HookService.getHook(InfluxDBHook.class).ifPresent(hook -> hook.log(logMessage));
    }

    public void listingExpired(Listing listing) {
        // In game logs
        listingInGame(listing, HistoricItem.LoggedAction.LISTING_EXPIRE);

        // Log file logs
        String logMessage = "[LISTING EXPIRED] Seller: %s (%s), ItemStack: %s".formatted(
                Bukkit.getOfflinePlayer(listing.getOwner()).getName(),
                Bukkit.getOfflinePlayer(listing.getOwner()).getUniqueId().toString(),
                listing.getItemStack().toString()
        );

        LoggingService.instance.transactionLogger.info(logMessage);

        HookService.getHook(InfluxDBHook.class).ifPresent(hook -> hook.log(logMessage));
    }

    private void listingInGame(Listing listing, HistoricItem.LoggedAction loggedAction) {
        boolean bidding = listing instanceof BidListing;

        HistoricItem historicItem = new HistoricItem(
                System.currentTimeMillis(),
                loggedAction,
                listing.getItemStack(),
                null,
                null,
                bidding,
                null
        );
        CacheAccess.get(History.class, listing.getOwner())
                .ifPresentOrElse(
                        cache -> cache.add(historicItem),
                        () -> fetchAndSaveHistory(listing.getOwner(), historicItem)
                );
    }

    private void fetchAndSaveHistory(UUID owner, HistoricItem historicItem) {
        DataService.getInstance()
                .get(History.class, owner)
                .thenAccept(historyOpt -> {
                    var history = historyOpt.orElseGet(() -> ImplHistory.empty(owner));
                    history.add(historicItem);
                    DataService.getInstance().save(History.class, history);
                });
    }
}
