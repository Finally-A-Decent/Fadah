package info.preva1l.fadah.data.handler;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.dao.mongo.*;
import info.preva1l.fadah.data.fixers.v2.MongoFixerV2;
import info.preva1l.fadah.data.fixers.v2.V2Fixer;
import info.preva1l.fadah.data.fixers.v3.V3Fixer;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.mongo.CollectionHelper;
import info.preva1l.fadah.utils.mongo.MongoConnectionHandler;
import info.preva1l.fadah.watcher.Watching;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MongoHandler implements DatabaseHandler {
    private final Map<Class<?>, Dao<?>> daos = new HashMap<>();

    @Getter private boolean connected = false;

    private MongoConnectionHandler connectionHandler;
    private CollectionHelper collectionHelper;
    @Getter private V2Fixer v2Fixer;
    @Getter private V3Fixer v3Fixer;

    @Override
    public void connect() {
        Config.Database conf = Config.i().getDatabase();
        try {
            @NotNull String connectionURI = conf.getUri();
            @NotNull String database = conf.getDatabase();
            Fadah.getConsole().info("Connecting to: " + connectionURI);
            connectionHandler = new MongoConnectionHandler(connectionURI, database);
            collectionHelper = new CollectionHelper(connectionHandler.getDatabase());
            connected = true;
        } catch (Exception e) {
            destroy();
            throw new IllegalStateException("Failed to establish a connection to the MongoDB database. " +
                    "Please check the supplied database credentials in the config file", e);
        }

        registerDaos();
        v2Fixer = new MongoFixerV2();
        v3Fixer = V3Fixer.empty();
    }

    @Override
    public void destroy() {
        if (connectionHandler != null) connectionHandler.closeConnection();
    }

    @Override
    public void registerDaos() {
        daos.put(Listing.class, new ListingMongoDao(collectionHelper));
        daos.put(CollectionBox.class, new CollectionBoxMongoDao(collectionHelper));
        daos.put(ExpiredItems.class, new ExpiredItemsMongoDao(collectionHelper));
        daos.put(History.class, new HistoryMongoDao(collectionHelper));
        daos.put(Watching.class, new WatchersMongoDao(collectionHelper));
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz) {
        return (List<T>) getDao(clazz).getAll();
    }

    @Override
    public <T> Optional<T> get(Class<T> clazz, UUID id) {
        return (Optional<T>) getDao(clazz).get(id);
    }

    @Override
    public <T> void save(Class<T> clazz, T t) {
        getDao(clazz).save(t);
    }

    @Override
    public <T> void update(Class<T> clazz, T t, String[] params) {
        getDao(clazz).update(t, params);
    }

    @Override
    public <T> void delete(Class<T> clazz, T t) {
        getDao(clazz).delete(t);
    }

    /**
     * Gets the DAO for a specific class.
     *
     * @param clazz The class to get the DAO for.
     * @param <T>   The type of the class.
     * @return The DAO for the specified class.
     */
    private <T> Dao<T> getDao(Class<?> clazz) {
        if (!daos.containsKey(clazz))
            throw new IllegalArgumentException("No DAO registered for class " + clazz.getName());
        return (Dao<T>) daos.get(clazz);
    }
}
