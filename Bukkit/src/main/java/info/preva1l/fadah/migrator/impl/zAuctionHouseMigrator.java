package info.preva1l.fadah.migrator.impl;

import com.google.common.base.Suppliers;
import fr.maxlego08.zauctionhouse.api.AuctionItem;
import fr.maxlego08.zauctionhouse.api.AuctionManager;
import fr.maxlego08.zauctionhouse.api.AuctionPlugin;
import fr.maxlego08.zauctionhouse.api.enums.StorageType;
import info.preva1l.fadah.currency.CurrencyRegistry;
import info.preva1l.fadah.migrator.MigrationService;
import info.preva1l.fadah.migrator.Migrator;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.listing.ImplBinListing;
import info.preva1l.fadah.records.listing.Listing;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Getter
public final class zAuctionHouseMigrator implements Migrator {
    private static final Supplier<AuctionPlugin> PLUGIN = Suppliers.memoize(() -> (AuctionPlugin) Bukkit.getPluginManager().getPlugin("zAuctionHouseV3"));

    private final String migratorName = "zAuctionHouseV3";
    private final AuctionManager auctionManager;

    public zAuctionHouseMigrator() {
        RegisteredServiceProvider<AuctionManager> provider = Bukkit.getServicesManager().getRegistration(AuctionManager.class);
        auctionManager = provider == null ? null : provider.getProvider();
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
            String currency = item.getEconomy().getCurrency();
            if (CurrencyRegistry.get(currency) == null) currency = "vault";
            listings.add(new ImplBinListing(id, owner, ownerName, itemStack, currency, price, 0,
                    Instant.now().toEpochMilli(), expiry));
        }
        return listings;
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateCollectionBoxes() {
        MigrationService.instance.logger.warning("Not migrating collection boxes! (zAuctionHouse API does not permit)");
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
}
