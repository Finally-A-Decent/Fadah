package info.preva1l.fadah.data.dao.mongo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.gson.BukkitSerializableAdapter;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.utils.mongo.CollectionHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ExpiredItemsMongoDao implements Dao<ExpiredItems> {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitSerializableAdapter())
            .serializeNulls().disableHtmlEscaping().create();
    private static final Type EXPIRED_LIST_TYPE = new TypeToken<ArrayList<CollectableItem>>() {}.getType();
    private final CollectionHelper collectionHelper;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<ExpiredItems> get(UUID id) {
        try {
            MongoCollection<Document> collection = collectionHelper.getCollection("expired_items");
            final Document document = collection.find().filter(Filters.eq("playerUUID", id)).first();
            if (document == null) return Optional.empty();

            ArrayList<CollectableItem> items = GSON.fromJson(document.getString("items"), EXPIRED_LIST_TYPE);
            if (items == null) items = new ArrayList<>();
            return Optional.of(new ExpiredItems(id, items));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<ExpiredItems> getAll() {
        throw new NotImplementedException();
    }

    /**
     * Save an object of type T to the database.
     *
     * @param expiredItems the object to save.
     */
    @Override
    public void save(ExpiredItems expiredItems) {
        try {
            Document document = new Document("playerUUID", expiredItems.owner())
                    .append("items", GSON.toJson(expiredItems.expiredItems(), EXPIRED_LIST_TYPE));
            collectionHelper.insertDocument("expired_items", document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param expiredItems the object to update.
     * @param params       the parameters to update the object with.
     */
    @Override
    public void update(ExpiredItems expiredItems, String[] params) {
        throw new NotImplementedException();
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param expiredItems the object to delete.
     */
    @Override
    public void delete(ExpiredItems expiredItems) {
        throw new NotImplementedException();
    }
}
