package info.preva1l.fadah.records.history;

import java.util.List;
import java.util.UUID;

public record History(
        UUID owner,
        List<HistoricItem> historicItems
) {
    public void add(HistoricItem collectableItem) {
        historicItems.add(collectableItem);
    }

    public void remove(HistoricItem collectableItem) {
        historicItems.remove(collectableItem);
    }
}
