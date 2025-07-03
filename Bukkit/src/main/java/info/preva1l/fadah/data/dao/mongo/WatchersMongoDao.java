package info.preva1l.fadah.data.dao.mongo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.watcher.Watching;
import org.apache.commons.lang3.NotImplementedException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class WatchersMongoDao implements Dao<Watching> {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls().disableHtmlEscaping().create();
    private final MongoCollection<Document> collection;

    public WatchersMongoDao(MongoDatabase database) {
        this.collection = database.getCollection("watchers");

        collection.createIndex(Indexes.ascending("playerUUID"), new IndexOptions().unique(true));
    }

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<Watching> get(UUID id) {
        try {
            Document document = collection.find(Filters.eq("playerUUID", id.toString())).first();
            if (document == null) {
                return Optional.empty();
            }
            Watching watching = GSON.fromJson(document.getString("watching"), Watching.class);
            return Optional.of(watching);
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
    public List<Watching> getAll() {
        List<Watching> result = new ArrayList<>();
        try {
            FindIterable<Document> documents = collection.find();
            for (Document document : documents) {
                Watching watching = GSON.fromJson(document.getString("watching"), Watching.class);
                result.add(watching);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        return result;
    }

    /**
     * Save an object of type T to the database.
     *
     * @param watching the object to save.
     */
    @Override
    public void save(Watching watching) {
        try {
            Document document = new Document("playerUUID", watching.getPlayer().toString())
                    .append("watching", GSON.toJson(watching));

            collection.replaceOne(
                    Filters.eq("playerUUID", watching.getPlayer().toString()),
                    document,
                    new ReplaceOptions().upsert(true)
            );
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param watching the object to update.
     * @param params   the parameters to update the object with.
     */
    @Override
    public void update(Watching watching, String[] params) {
        throw new NotImplementedException("update");
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param watching the object to delete.
     */
    @Override
    public void delete(Watching watching) {
        throw new NotImplementedException("delete");
    }
}
