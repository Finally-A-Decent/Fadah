package info.preva1l.fadah.data;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.handler.DatabaseHandler;
import info.preva1l.fadah.data.handler.MongoHandler;
import info.preva1l.fadah.data.handler.MySQLHandler;
import info.preva1l.fadah.data.handler.SQLiteHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Blocking;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This is the manager for all database interactions.
 * There should be no case where this is modified.
 * Access this class via {@link DatabaseManager#getInstance()}
 */
public final class DatabaseManager {
    private static DatabaseManager instance;

    @Getter private final ExecutorService threadPool;
    private final Map<DatabaseType, Class<? extends DatabaseHandler>> databaseHandlers = new HashMap<>();
    private final DatabaseHandler handler;

    private DatabaseManager() {
        Fadah.getConsole().info("Connecting to Database and populating caches...");
        threadPool = Executors.newCachedThreadPool();
        databaseHandlers.put(DatabaseType.SQLITE, SQLiteHandler.class);
        databaseHandlers.put(DatabaseType.MARIADB, MySQLHandler.class);
        databaseHandlers.put(DatabaseType.MYSQL, MySQLHandler.class);
        databaseHandlers.put(DatabaseType.MONGO, MongoHandler.class);

        this.handler = initHandler();
        Fadah.getConsole().info("Connected to Database and populated caches!");
    }

    public <T> CompletableFuture<List<T>> getAll(Class<T> clazz) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(() -> handler.getAll(clazz), threadPool);
    }

    public <T> CompletableFuture<Optional<T>> get(Class<T> clazz, UUID id) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> handler.get(clazz, id), threadPool);
    }

    public <T> CompletableFuture<Void> save(Class<T> clazz, T t) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.save(clazz, t);
            return null;
        }, threadPool);
    }

    public <T> CompletableFuture<Void> delete(Class<T> clazz, T t) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.delete(clazz, t);
            return null;
        }, threadPool);
    }

    public <T> CompletableFuture<Void> update(Class<T> clazz, T t, String[] params) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.update(clazz, t, params);
            return null;
        }, threadPool);
    }

    public CompletableFuture<Void> fixPlayerData(UUID player) {
        if (!isConnected()) {
            Fadah.getConsole().severe("Tried to perform database action when the database is not connected!");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            handler.fixData(player);
            return null;
        }, threadPool);
    }

    public boolean isConnected() {
        return handler.isConnected();
    }

    @SneakyThrows
    @Blocking
    public void shutdown() {
        threadPool.shutdown();
        threadPool.awaitTermination(10, TimeUnit.SECONDS);
        handler.destroy();
    }

    private DatabaseHandler initHandler() {
        DatabaseType type = Config.i().getDatabase().getType();
        Fadah.getConsole().info("DB Type: %s".formatted(type.getFriendlyName()));
        try {
            Class<? extends DatabaseHandler> handlerClass = databaseHandlers.get(type);
            if (handlerClass == null) {
                throw new IllegalStateException("No handler for database type %s registered!".formatted(type.getFriendlyName()));
            }
            return handlerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
            instance.handler.connect();
        }
        return instance;
    }
}
