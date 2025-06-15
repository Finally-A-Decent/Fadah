package info.preva1l.fadah.data.dao.mongo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.gson.BukkitSerializableAdapter;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.bson.Document;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public class HistoryMongoDao implements Dao<History> {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitSerializableAdapter())
            .serializeNulls().disableHtmlEscaping().create();
    private static final Type HISTORY_LIST_TYPE = new TypeToken<ArrayList<HistoricItem>>() {}.getType();
    private final MongoDatabase database;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<History> get(UUID id) {
        try {
            MongoCollection<Document> collection = database.getCollection("history");
            final Document document = collection.find().filter(Filters.eq("playerUUID", id)).first();
            if (document == null) return Optional.empty();

            ArrayList<HistoricItem> items = GSON.fromJson(document.getString("items"), HISTORY_LIST_TYPE);

            if (items == null) items = new ArrayList<>();

            return Optional.of(new History(id, items));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<History> getAll() {
        throw new NotImplementedException("getAll");
    }

    /**
     * Save an object of type T to the database.
     *
     * @param expiredItems the object to save.
     */
    @Override
    public void save(History expiredItems) {
        try {
            Document document = new Document("playerUUID", expiredItems.owner())
                    .append("items", GSON.toJson(expiredItems.historicItems(), HISTORY_LIST_TYPE));
            database.getCollection("history").replaceOne(Filters.eq("playerUUID", expiredItems.owner()), document);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param expiredItems the object to update.
     * @param params       the parameters to update the object with.
     */
    @Override
    public void update(History expiredItems, String[] params) {
        throw new NotImplementedException("update");
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param expiredItems the object to delete.
     */
    @Override
    public void delete(History expiredItems) {
        throw new NotImplementedException("delete");
    }
}
