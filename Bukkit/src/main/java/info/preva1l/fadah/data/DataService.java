package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.handler.DatabaseHandler;
import info.preva1l.fadah.data.handler.MongoHandler;
import info.preva1l.fadah.data.handler.MySQLHandler;
import info.preva1l.fadah.data.handler.SQLiteHandler;
import info.preva1l.fadah.multiserver.Broker;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.collection.ImplCollectionBox;
import info.preva1l.fadah.records.collection.ImplExpiredItems;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.history.ImplHistory;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.Tasks;
import info.preva1l.fadah.watcher.AuctionWatcher;
import info.preva1l.fadah.watcher.Watching;
import info.preva1l.trashcan.flavor.annotations.Close;
import info.preva1l.trashcan.flavor.annotations.Configure;
import info.preva1l.trashcan.flavor.annotations.Service;
import info.preva1l.trashcan.flavor.annotations.inject.Inject;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * This is the manager for all database interactions.
 * There should be no case where this is modified.
 * Access this class via {@link DataService#instance)}
 */
@Service(priority = 3)
public final class DataService {
    @Getter public static final DataService instance = new DataService();

    @Inject private Fadah plugin;
    @Inject public Logger logger;

    @Getter private final ExecutorService threadPool;
    private final Map<DatabaseType, Class<? extends DatabaseHandler>> databaseHandlers = new HashMap<>();
    private DatabaseHandler handler;

    private DataService() {
        threadPool = Executors.newVirtualThreadPerTaskExecutor();
        databaseHandlers.put(DatabaseType.SQLITE, SQLiteHandler.class);
        databaseHandlers.put(DatabaseType.MARIADB, MySQLHandler.class);
        databaseHandlers.put(DatabaseType.MYSQL, MySQLHandler.class);
        databaseHandlers.put(DatabaseType.MONGO, MongoHandler.class);
    }

    @Configure
    public void configure() {
        handler = initHandler();
        handler.connect();

        getAll(Listing.class).thenAccept(listings ->
                listings.forEach(listing -> CacheAccess.add(Listing.class, listing))).join();

        Broker.getInstance().load();

        Tasks.getLoopDeLoop().scheduleAtFixedRate(
                listingExpiryTask(),
                0L,
                1L,
                TimeUnit.SECONDS
        );
    }

    public <T> CompletableFuture<List<T>> getAll(Class<T> clazz) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(() -> handler.getAll(clazz), threadPool);
    }

    public <T> CompletableFuture<Optional<T>> get(Class<T> clazz, UUID id) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> handler.get(clazz, id), threadPool);
    }

    public <T> CompletableFuture<Void> save(Class<T> clazz, T t) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.save(clazz, t);
            return null;
        }, threadPool);
    }

    public <T> CompletableFuture<Void> delete(Class<T> clazz, T t) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.delete(clazz, t);
            return null;
        }, threadPool);
    }

    public <T> CompletableFuture<Void> update(Class<T> clazz, T t, Map<String, ?> params) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.update(clazz, t, params);
            return null;
        }, threadPool);
    }

    public CompletableFuture<Void> fixPlayerData(UUID player) {
        if (!isConnected()) {
            logger.severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.fixData(player);
            return null;
        }, threadPool);
    }

    public CompletableFuture<Void> loadPlayerData(UUID uuid) {
        if (CacheAccess.get(History.class, uuid).isPresent()) return CompletableFuture.completedFuture(null);
        return fixPlayerData(uuid)
                .thenCompose(ignored -> CompletableFuture.allOf(
                        loadAndCache(CollectionBox.class, uuid, () -> ImplCollectionBox.empty(uuid)),
                        loadAndCache(ExpiredItems.class, uuid, () -> ImplExpiredItems.empty(uuid)),
                        loadAndCache(History.class, uuid, () -> ImplHistory.empty(uuid)),
                        get(Watching.class, uuid)
                                .thenAccept(opt -> opt.ifPresent(AuctionWatcher::watch))
                ));
    }

    public CompletableFuture<Void> invalidateAndSavePlayerData(UUID uuid) {
        return CompletableFuture.allOf(
                saveAndInvalidate(CollectionBox.class, uuid),
                saveAndInvalidate(ExpiredItems.class, uuid),
                saveAndInvalidate(History.class, uuid),
                AuctionWatcher.get(uuid)
                        .map(w -> save(Watching.class, w))
                        .orElseGet(() -> CompletableFuture.completedFuture(null))
        );
    }

    private <T> CompletableFuture<Void> loadAndCache(Class<T> type, UUID uuid, Supplier<T> supplier) {
        return get(type, uuid)
                .thenAccept(opt -> CacheAccess.add(type, opt.orElse(supplier.get())));
    }

    private <T> CompletableFuture<Void> saveAndInvalidate(Class<T> type, UUID uuid) {
        return CacheAccess.get(type, uuid)
                .map(value -> save(type, value)
                        .thenRun(() -> CacheAccess.invalidate(type, value)))
                .orElseGet(() -> CompletableFuture.completedFuture(null));
    }

    public boolean isConnected() {
        return handler.isConnected();
    }

    @Close
    public void shutdown() {
        try {
            threadPool.shutdown();
            boolean success = threadPool.awaitTermination(10, TimeUnit.SECONDS);
            if (!success) throw new RuntimeException("Failed to shutdown thread pool");
            handler.destroy();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (Config.i().getBroker().isEnabled()) Broker.getInstance().destroy();
    }

    private DatabaseHandler initHandler() {
        DatabaseType type = Config.i().getDatabase().getType();
        logger.info("DB Type: %s".formatted(type.getFriendlyName()));
        try {
            Class<? extends DatabaseHandler> handlerClass = databaseHandlers.get(type);
            if (handlerClass == null) {
                throw new IllegalStateException("No handler for database type %s registered!".formatted(type.getFriendlyName()));
            }
            return handlerClass.getDeclaredConstructor(Fadah.class).newInstance(plugin);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Runnable listingExpiryTask() {
        return () -> {
            for (Listing listing : CacheAccess.getAll(Listing.class)) {
                if (System.currentTimeMillis() <= listing.getDeletionDate()) continue;

                if (listing instanceof BidListing bidListing) {
                    bidListing.completeBidding();
                } else {
                    listing.expire();
                }
            }
        };
    }
}
