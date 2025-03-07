package info.preva1l.fadah.migrator;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.listing.Listing;

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
                CacheAccess.add(Listing.class, listing);
                DatabaseManager.getInstance().save(Listing.class, listing);
                migratedListings++;
            }
            Fadah.getConsole().info("Migrated %s listings!".formatted(migratedListings));

            int migratedCollectionBoxes = 0;
            Map<UUID, List<CollectableItem>> collectionBoxes = migrateCollectionBoxes();

            for (UUID owner : collectionBoxes.keySet()) {
                CollectionBox box = DatabaseManager.getInstance().get(CollectionBox.class, owner).join()
                        .orElse(new CollectionBox(owner, collectionBoxes.get(owner)));
                DatabaseManager.getInstance().save(CollectionBox.class, box);
                migratedCollectionBoxes++;
            }
            Fadah.getConsole().info("Migrated %s collection boxes!".formatted(migratedCollectionBoxes));

            int migratedExpiredItems = 0;
            Map<UUID, List<CollectableItem>> expiredItems = migrateExpiredListings();

            for (UUID owner : expiredItems.keySet()) {
                for (CollectableItem item : expiredItems.get(owner)) {
                    CacheAccess.getNotNull(ExpiredItems.class, owner).add(item);
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
