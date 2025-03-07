package info.preva1l.fadah.records.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ExpiredItems(
        UUID owner,
        List<CollectableItem> expiredItems
) {
    public static ExpiredItems empty(UUID owner) {
        return new ExpiredItems(owner, new ArrayList<>());
    }

    public void add(CollectableItem collectableItem) {
        expiredItems.add(collectableItem);
    }

    public void remove(CollectableItem collectableItem) {
        expiredItems.remove(collectableItem);
    }
}
