package info.preva1l.fadah.data.dao.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.HistoricItem;
import info.preva1l.fadah.records.History;
import info.preva1l.fadah.utils.ItemSerializer;
import info.preva1l.fadah.utils.mongo.CollectionHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class HistoryMongoDao implements Dao<History> {
    private final CollectionHelper collectionHelper;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<History> get(UUID id) {
        List<HistoricItem> list = new ArrayList<>();
        MongoCollection<Document> collection = collectionHelper.getCollection("history");
        FindIterable<Document> documents = collection.find(Filters.eq("playerUUID", id));
        for (Document document : documents) {
            try {
                final long loggedDate = document.getLong("loggedDate");
                final Double price = document.getDouble("price");
                final ItemStack itemStack = ItemSerializer.deserialize(document.getString("itemStack"))[0];
                final HistoricItem.LoggedAction loggedAction = HistoricItem.LoggedAction.values()[document.getInteger("loggedAction")];
                final UUID purchaserUUID = document.get("purchaserUUID", UUID.class);
                list.add(new HistoricItem(id, loggedDate, loggedAction, itemStack, price, purchaserUUID));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Optional.of(new History(id, list));
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<History> getAll() {
        throw new NotImplementedException();
    }

    /**
     * Save an object of type T to the database.
     *
     * @param history the object to save.
     */
    @Override
    public void save(History history) {
        for (HistoricItem item : history.collectableItems()) {
            try {
                Optional<History> current = get(item.getOwnerUUID());
                if (current.isPresent() && current.get().collectableItems().contains(item)) continue;
                Document document = new Document("playerUUID", history.owner())
                        .append("itemStack", ItemSerializer.serialize(item.getItemStack()))
                        .append("loggedDate", item.getLoggedDate())
                        .append("loggedAction", item.getAction().ordinal())
                        .append("price", item.getPrice())
                        .append("purchaserUUID", item.getPurchaserUUID());
                collectionHelper.insertDocument("history", document);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param history the object to update.
     * @param params  the parameters to update the object with.
     */
    @Override
    public void update(History history, String[] params) {
        throw new NotImplementedException();
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param history the object to delete.
     */
    @Override
    public void delete(History history) {
        throw new NotImplementedException();
    }
}
