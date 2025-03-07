package info.preva1l.fadah.data.dao.mongo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.utils.mongo.CollectionHelper;
import info.preva1l.fadah.watcher.Watching;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class WatchersMongoDao implements Dao<Watching> {
    private static final Gson GSON = new GsonBuilder()
            .serializeNulls().disableHtmlEscaping().create();
    private final CollectionHelper collectionHelper;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<Watching> get(UUID id) {
        try {
            MongoCollection<Document> collection = collectionHelper.getCollection("watchers");
            Document document = collection.find(Filters.eq("playerUUID", id.toString())).first();
            if (document == null) {
                return Optional.empty();
            }
            Watching watching = GSON.fromJson(document.getString("watching"), Watching.class);
            return Optional.of(watching);
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
    public List<Watching> getAll() {
        List<Watching> result = new ArrayList<>();
        try {
            MongoCollection<Document> collection = collectionHelper.getCollection("watchers");
            FindIterable<Document> documents = collection.find();
            for (Document document : documents) {
                Watching watching = GSON.fromJson(document.getString("watching"), Watching.class);
                result.add(watching);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            MongoCollection<Document> collection = collectionHelper.getCollection("watchers");
            Document document = new Document("playerUUID", watching.getPlayer().toString())
                    .append("watching", GSON.toJson(watching));
            collection.replaceOne(Filters.eq("playerUUID", watching.getPlayer().toString()), document, new ReplaceOptions().upsert(true));
        } catch (Exception e) {
            e.printStackTrace();
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
        throw new NotImplementedException();
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param watching the object to delete.
     */
    @Override
    public void delete(Watching watching) {
        throw new NotImplementedException();
    }
}
