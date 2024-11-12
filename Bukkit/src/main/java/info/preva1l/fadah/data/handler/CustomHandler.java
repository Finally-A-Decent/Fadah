package info.preva1l.fadah.data.handler;

import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.fixers.v2.EmptyFixerV2;
import info.preva1l.fadah.data.fixers.v2.V2Fixer;
import info.preva1l.fadah.storage.clients.Clients;
import info.preva1l.fadah.storage.clients.pooled.ClientPool;
import lombok.Getter;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class CustomHandler implements DatabaseHandler {
    private final Map<Class<?>, Dao<?>> daos = new HashMap<>();

    @Getter private boolean connected = false;
    @Getter private final V2Fixer v2Fixer = EmptyFixerV2.get();

    private ClientPool clients;

    @Override
    @Blocking
    public void connect() {
        Config.Database conf = Config.i().getDatabase();
        clients = Clients.createPooled(conf.getUri());
        registerDaos();
        connected = true;
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private String[] getSchemaStatements(@NotNull String schemaFileName) throws IOException {
        return new String(Objects.requireNonNull(Fadah.getINSTANCE().getResource(schemaFileName))
                .readAllBytes(), StandardCharsets.UTF_8).split(";");
    }

    private void backupFlatFile(@NotNull File file) {
        if (!file.exists()) {
            return;
        }

        final File backup = new File(file.getParent(), String.format("%s.bak", file.getName()));
        try {
            if (!backup.exists() || backup.delete()) {
                Files.copy(file.toPath(), backup.toPath());
            }
        } catch (IOException e) {
            Fadah.getConsole().log(Level.WARNING, "Failed to backup flat file database", e);
        }
    }

    @Override
    public void destroy() {
        if (clients != null) clients.close();
    }

    @Override
    public void registerDaos() {
//        daos.put(Listing.class, new ListingSQLiteDao(dataSource));
//        daos.put(CollectionBox.class, new CollectionBoxSQLiteDao(dataSource));
//        daos.put(ExpiredItems.class, new ExpiredItemsSQLiteDao(dataSource));
//        daos.put(History.class, new HistorySQLiteDao(dataSource));
    }

    @Override
    public void wipeDatabase() {
        // nothing yet
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

    @Override
    public <T> void deleteSpecific(Class<T> clazz, T t, Object o) {
        getDao(clazz).deleteSpecific(t, o);
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