package info.preva1l.fadah.records;

import info.preva1l.fadah.cache.CacheAccess;

import java.util.List;
import java.util.UUID;

public record CollectionBox(
        UUID owner,
        List<CollectableItem> collectableItems
) {
    public void add(CollectableItem collectableItem) {
        collectableItems.add(collectableItem);
        CacheAccess.add(CollectionBox.class, this);
    }

    public void remove(CollectableItem collectableItem) {
        collectableItems.remove(collectableItem);
        CacheAccess.add(CollectionBox.class, this);
    }
}
