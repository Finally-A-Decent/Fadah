package info.preva1l.fadah.records.collection;

import java.util.List;
import java.util.UUID;

public record CollectionBox(
        UUID owner,
        List<CollectableItem> collectableItems
) {
    public void add(CollectableItem collectableItem) {
        collectableItems.add(collectableItem);
    }

    public void remove(CollectableItem collectableItem) {
        collectableItems.remove(collectableItem);
    }
}
