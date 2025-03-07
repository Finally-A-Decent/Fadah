package info.preva1l.fadah.migrator;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.modules.ListingType;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CategoryRegistry;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
public final class AuctionHouseMigrator implements Migrator {
    private final String migratorName = "AuctionHouse";

    @Override
    public List<Listing> migrateListings() {
        Map<Long, com.spawnchunk.auctionhouse.modules.Listing> listingMap = AuctionHouse.listings.getListings();
        listingMap = listingMap.entrySet().parallelStream().filter(entry -> entry.getKey() > Instant.now().toEpochMilli())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        List<Listing> listings = new ArrayList<>();
        for (long expiry : listingMap.keySet()) {
            com.spawnchunk.auctionhouse.modules.Listing listing = listingMap.get(expiry);
            if (listing.getType() == ListingType.PLAYER_AUCTION) {
                Fadah.getConsole().warning("Not migrating listing %s! (Is a biddable listing, Not supported yet!)".formatted(listing.toString()));
                continue;
            }

            UUID id = UUID.randomUUID();
            UUID owner = UUID.fromString(listing.getSeller_UUID());
            String ownerName = listing.getSellerName();
            ItemStack itemStack = listing.getItem();
            double price = listing.getPrice();
            String categoryId = CategoryRegistry.getCategoryForItem(itemStack).join();
            listings.add(new BinListing(id, owner, ownerName, itemStack, categoryId, "vault", price, 0,
                    Instant.now().toEpochMilli(), expiry, new TreeSet<>()));
        }
        return listings;
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateCollectionBoxes() {
        Fadah.getConsole().warning("Not migrating collection boxes! (AuctionHouse does not permit)");
        return Collections.emptyMap();
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateExpiredListings() {
        Map<Long, com.spawnchunk.auctionhouse.modules.Listing> listingMap = AuctionHouse.listings.getExpiredListings().getListings();
        Map<UUID, List<CollectableItem>> allItems = new ConcurrentHashMap<>();
        for (long added : listingMap.keySet()) {
            com.spawnchunk.auctionhouse.modules.Listing listing = listingMap.get(added);
            UUID owner = UUID.fromString(listing.getSeller_UUID());
            ItemStack itemStack = listing.getItem();
            CollectableItem item = new CollectableItem(itemStack, Instant.now().toEpochMilli());
            allItems.compute(owner, (uuid, items) -> {
                if (items == null) {
                    items = new ArrayList<>();
                }
                items.add(item);
                return items;
            });
        }
        return allItems;
    }
}
