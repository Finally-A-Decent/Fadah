package info.preva1l.fadah.migrator;

import com.google.common.base.Suppliers;
import fr.maxlego08.zauctionhouse.api.AuctionItem;
import fr.maxlego08.zauctionhouse.api.AuctionManager;
import fr.maxlego08.zauctionhouse.api.AuctionPlugin;
import fr.maxlego08.zauctionhouse.api.enums.StorageType;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryCache;
import info.preva1l.fadah.currency.CurrencyRegistry;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Getter
public final class zAuctionHouseMigrator implements Migrator {

    private static final Supplier<AuctionPlugin> PLUGIN = Suppliers.memoize(() -> (AuctionPlugin) Bukkit.getPluginManager().getPlugin("zAuctionHouse"));

    private final String migratorName = "zAuctionHouse";
    private final AuctionManager auctionManager;

    public zAuctionHouseMigrator() {
        auctionManager = getProvider(AuctionManager.class);
    }

    @Override
    public CompletableFuture<Void> startMigration(Fadah plugin) {
        // auctionManager.getStorage().save(PLUGIN.get());
        return Migrator.super.startMigration(plugin);
    }

    @Override
    public List<Listing> migrateListings() {
        List<Listing> listings = new ArrayList<>();
        for (AuctionItem item : auctionManager.getStorage().getItems(PLUGIN.get(), StorageType.STORAGE)) {
            UUID id = item.getUniqueId();
            UUID owner = item.getSellerUniqueId();
            String ownerName = item.getSellerName();
            ItemStack itemStack = item.getItemStack();
            double price = item.getPrice();
            long expiry = item.getExpireAt();
            String categoryId = CategoryCache.getCategoryForItem(itemStack).join();
            String currency = item.getEconomy().getCurrency();
            if (CurrencyRegistry.get(currency) == null) currency = "vault";
            listings.add(new BinListing(id, owner, ownerName, itemStack, categoryId, currency, price, 0,
                    Instant.now().toEpochMilli(), expiry, false, List.of()));
        }
        return listings;
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateCollectionBoxes() {
        Fadah.getConsole().warning("Not migrating collection boxes! (zAuctionHouse API does not permit)");
        return Collections.emptyMap();
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateExpiredListings() {
        Map<UUID, List<CollectableItem>> allItems = new ConcurrentHashMap<>();
        for (AuctionItem item : auctionManager.getStorage().getItems(PLUGIN.get(), StorageType.EXPIRE)) {
            UUID owner = item.getSellerUniqueId();
            ItemStack itemStack = item.getItemStack();
            CollectableItem collectableItem = new CollectableItem(itemStack, item.getExpireAt());
            allItems.compute(owner, (uuid, items) -> {
                if (items == null) {
                    items = new ArrayList<>();
                }
                items.add(collectableItem);
                return items;
            });
        }
        return allItems;
    }

    private <T> T getProvider(Class<T> clazz) {
        RegisteredServiceProvider<T> provider = Bukkit.getServicesManager().getRegistration(clazz);
        if (provider == null) return null;
        return provider.getProvider() != null ? (T) provider.getProvider() : null;
    }
}
