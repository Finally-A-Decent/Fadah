package info.preva1l.fadah.migrator.impl;

import info.preva1l.fadah.migrator.Migrator;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.listing.ImplBinListing;
import info.preva1l.fadah.records.listing.Listing;
import lombok.Getter;
import net.akarian.auctionhouse.AuctionHouse;
import net.akarian.auctionhouse.listings.ListingManager;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class AkarianAuctionHouseMigrator implements Migrator {
    private final String migratorName = "AuctionHouse";
    private final ListingManager listingManager = AuctionHouse.getInstance().getListingManager();

    @Override
    public List<Listing> migrateListings() {
        ArrayList<net.akarian.auctionhouse.listings.Listing> oldListings = this.listingManager.getActive();
        List<Listing> listings = new ArrayList<>();

        for (net.akarian.auctionhouse.listings.Listing oldListing : oldListings) {
            UUID id = oldListing.getId();
            UUID owner = oldListing.getCreator();
            String ownerName = Bukkit.getOfflinePlayer(owner).getName();
            ItemStack item = oldListing.getItemStack();
            double price = oldListing.getPrice();

            long expiry = oldListing.getEnd();
            listings.add(new ImplBinListing(id, owner, ownerName == null ? "Unknown Seller" : ownerName, item, "vault", price, 0,
                    Instant.now().toEpochMilli(), expiry));
        }

        return listings;
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateCollectionBoxes() {
        ArrayList<net.akarian.auctionhouse.listings.Listing> completed = this.listingManager.getCompleted();
        return convert(completed);
    }

    @Override
    public Map<UUID, List<CollectableItem>> migrateExpiredListings() {
        ArrayList<net.akarian.auctionhouse.listings.Listing> expired = this.listingManager.getExpired();
        return convert(expired);
    }

    private @NotNull Map<UUID, List<CollectableItem>> convert(ArrayList<net.akarian.auctionhouse.listings.Listing> completed) {
        Map<UUID, List<CollectableItem>> allItems = new ConcurrentHashMap<>();

        for (net.akarian.auctionhouse.listings.Listing listing : completed) {
            UUID owner = listing.getCreator();
            ItemStack itemStack = listing.getItemStack();
            CollectableItem item = new CollectableItem(itemStack, listing.getEnd());

            allItems.compute(owner, (uuid, items) -> {
                if (items == null) items = new ArrayList<>();

                items.add(item);
                return items;
            });
        }

        return allItems;
    }
}
