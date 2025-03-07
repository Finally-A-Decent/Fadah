package info.preva1l.fadah.data.handler;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.dao.sqlite.*;
import info.preva1l.fadah.data.fixers.v2.SQLiteFixerV2;
import info.preva1l.fadah.data.fixers.v2.V2Fixer;
import info.preva1l.fadah.data.fixers.v3.V3Fixer;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.watcher.Watching;
import lombok.Getter;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;

public class SQLiteHandler implements DatabaseHandler {
    private final Map<Class<?>, Dao<?>> daos = new HashMap<>();

    @Getter private boolean connected = false;

    private static final String DATABASE_FILE_NAME = "FadahData.db";
    private HikariDataSource dataSource;
    @Getter private V2Fixer v2Fixer;
    @Getter private V3Fixer v3Fixer;

    @Override
    @Blocking
    public void connect() {
        try {
            File databaseFile = new File(Fadah.getINSTANCE().getDataFolder(), DATABASE_FILE_NAME);
            if (databaseFile.createNewFile()) {
                Fadah.getConsole().info("Created the SQLite database file");
            }

            Class.forName("org.sqlite.JDBC");

            HikariConfig config = new HikariConfig();
            config.setPoolName("FadahHikariPool");
            config.setAutoCommit(true);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setJdbcUrl("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            config.setConnectionTestQuery("SELECT 1");
            config.setMaxLifetime(60000);
            config.setMaximumPoolSize(50);
            dataSource = new HikariDataSource(config);
            this.backupFlatFile(databaseFile);

            final String[] databaseSchema = getSchemaStatements(String.format("database/%s_schema.sql", Config.i().getDatabase().getType().getId()));
            try (Statement statement = dataSource.getConnection().createStatement()) {
                for (String tableCreationStatement : databaseSchema) {
                    statement.execute(tableCreationStatement);
                }
            } catch (SQLException e) {
                destroy();
                throw new IllegalStateException("Failed to create database tables.", e);
            }
        } catch (IOException e) {
            Fadah.getConsole().log(Level.SEVERE, "An exception occurred creating the database file", e);
            destroy();
        } catch (ClassNotFoundException e) {
            Fadah.getConsole().log(Level.SEVERE, "Failed to load the necessary SQLite driver", e);
            destroy();
        }
        registerDaos();
        v2Fixer = new SQLiteFixerV2(dataSource);
        v3Fixer = V3Fixer.empty();
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
        if (dataSource != null) dataSource.close();
    }

    @Override
    public void registerDaos() {
        daos.put(Listing.class, new ListingSQLiteDao(dataSource));
        daos.put(CollectionBox.class, new CollectionBoxSQLiteDao(dataSource));
        daos.put(ExpiredItems.class, new ExpiredItemsSQLiteDao(dataSource));
        daos.put(History.class, new HistorySQLiteDao(dataSource));
        daos.put(Watching.class, new WatchersSQLiteDao(dataSource));
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
