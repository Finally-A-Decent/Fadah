package info.preva1l.fadah.migrator;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CollectionBoxCache;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.cache.ListingCache;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.records.Listing;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Migrator {
    String getMigratorName();

    default CompletableFuture<Void> startMigration(Fadah plugin) {
        return CompletableFuture.supplyAsync(() -> {
            int migratedListings = 0;
            List<Listing> listings = migrateListings();

            for (Listing listing : listings) {
                ListingCache.addListing(listing);
                DatabaseManager.getInstance().save(Listing.class, listing);
                migratedListings++;
            }
            Fadah.getConsole().info("Migrated %s listings!".formatted(migratedListings));

            int migratedCollectionBoxes = 0;
            Map<UUID, List<CollectableItem>> collectionBoxes = migrateCollectionBoxes();

            for (UUID owner : collectionBoxes.keySet()) {
                for (CollectableItem item : collectionBoxes.get(owner)) {
                    CollectionBoxCache.addItem(owner, item);
                    DatabaseManager.getInstance().save(CollectionBox.class, new CollectionBox(owner, collectionBoxes.get(owner)));
                }
                migratedCollectionBoxes++;
            }
            Fadah.getConsole().info("Migrated %s collection boxes!".formatted(migratedCollectionBoxes));

            int migratedExpiredItems = 0;
            Map<UUID, List<CollectableItem>> expiredItems = migrateExpiredListings();

            for (UUID owner : expiredItems.keySet()) {
                for (CollectableItem item : expiredItems.get(owner)) {
                    ExpiredListingsCache.addItem(owner, item);
                    DatabaseManager.getInstance().save(ExpiredItems.class, ExpiredItems.of(owner));
                }
                migratedExpiredItems++;
            }
            Fadah.getConsole().info("Migrated %s players expired items!".formatted(migratedExpiredItems));
            return null;
        });
    }

    List<Listing> migrateListings();
    Map<UUID, List<CollectableItem>> migrateCollectionBoxes();
    Map<UUID, List<CollectableItem>> migrateExpiredListings();
}
