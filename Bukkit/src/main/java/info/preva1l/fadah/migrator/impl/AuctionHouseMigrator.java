package info.preva1l.fadah.migrator.impl;

import com.spawnchunk.auctionhouse.AuctionHouse;
import com.spawnchunk.auctionhouse.modules.ListingType;
import info.preva1l.fadah.migrator.MigrationService;
import info.preva1l.fadah.migrator.Migrator;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.listing.ImplBinListing;
import info.preva1l.fadah.records.listing.Listing;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
                MigrationService.instance.logger.warning("Not migrating listing %s! (Is a biddable listing, Not supported yet!)".formatted(listing.toString()));
                continue;
            }

            UUID id = UUID.randomUUID();
            UUID owner = UUID.fromString(listing.getSeller_UUID());
            String ownerName = listing.getSellerName();
            ItemStack itemStack = listing.getItem();
            double price = listing.getPrice();
            listings.add(new ImplBinListing(id, owner, ownerName, itemStack, "vault", price, 0,
                    Instant.now().toEpochMilli(), expiry));
        }
        return listings;
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateCollectionBoxes() {
        Map<Long, com.spawnchunk.auctionhouse.modules.Listing> listingMap = AuctionHouse.listings.getUnclaimedListings().getListings();
        return convert(listingMap);
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateExpiredListings() {
        Map<Long, com.spawnchunk.auctionhouse.modules.Listing> listingMap = AuctionHouse.listings.getExpiredListings().getListings();
        return convert(listingMap);
    }

    private @NotNull Map<UUID, List<CollectableItem>> convert(Map<Long, com.spawnchunk.auctionhouse.modules.Listing> listingMap) {
        Map<UUID, List<CollectableItem>> allItems = new ConcurrentHashMap<>();
        for (com.spawnchunk.auctionhouse.modules.Listing listing : listingMap.values()) {
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
