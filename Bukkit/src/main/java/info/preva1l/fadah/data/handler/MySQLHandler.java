package info.preva1l.fadah.data.handler;

import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.config.Config;
import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.dao.sql.*;
import info.preva1l.fadah.data.fixers.v2.MySQLFixerV2;
import info.preva1l.fadah.data.fixers.v2.V2Fixer;
import info.preva1l.fadah.data.fixers.v3.MySQLFixerV3;
import info.preva1l.fadah.data.fixers.v3.V3Fixer;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ExpiredItems;
import info.preva1l.fadah.records.history.History;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.watcher.Watching;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class MySQLHandler implements DatabaseHandler {
    private final Map<Class<?>, Dao<?>> daos = new HashMap<>();

    @Getter private boolean connected = false;

    private final String driverClass;
    private HikariDataSource dataSource;
    @Getter private V2Fixer v2Fixer;
    @Getter private V3Fixer v3Fixer;

    private final Config.Database conf = Config.i().getDatabase();

    public MySQLHandler() {
        this.driverClass = conf.getType() == DatabaseType.MARIADB ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
    }

    @SuppressWarnings("SameParameterValue")
    @NotNull
    private String[] getSchemaStatements(@NotNull String schemaFileName) throws IOException {
        return new String(Objects.requireNonNull(Fadah.getINSTANCE().getResource(schemaFileName))
                .readAllBytes(), StandardCharsets.UTF_8).split(";");
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void connect() {
        dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClass);
        dataSource.setJdbcUrl(conf.getUri());
        if (!conf.getUri().contains("@")) {
            dataSource.setUsername(conf.getUsername());
            dataSource.setPassword(conf.getPassword());
        }

        dataSource.setMaximumPoolSize(conf.getAdvanced().getPoolSize());
        dataSource.setMinimumIdle(conf.getAdvanced().getMinIdle());
        dataSource.setMaxLifetime(conf.getAdvanced().getMaxLifetime());
        dataSource.setKeepaliveTime(conf.getAdvanced().getKeepaliveTime());
        dataSource.setConnectionTimeout(conf.getAdvanced().getConnectionTimeout());
        dataSource.setPoolName("FadahHikariPool");

        final Properties properties = new Properties();
        properties.putAll(
                Map.of("cachePrepStmts", "true",
                        "prepStmtCacheSize", "250",
                        "prepStmtCacheSqlLimit", "2048",
                        "useServerPrepStmts", "true",
                        "useLocalSessionState", "true",
                        "useLocalTransactionState", "true"
                ));
        properties.putAll(
                Map.of(
                        "rewriteBatchedStatements", "true",
                        "cacheResultSetMetadata", "true",
                        "cacheServerConfiguration", "true",
                        "elideSetAutoCommits", "true",
                        "maintainTimeStats", "false")
        );
        dataSource.setDataSourceProperties(properties);

        try (Connection connection = getConnection()) {
            final String[] databaseSchema = getSchemaStatements(String.format("database/%s_schema.sql", conf.getType().getId()));
            try (Statement statement = connection.createStatement()) {
                for (String tableCreationStatement : databaseSchema) {
                    statement.execute(tableCreationStatement);
                }
                connected = true;
            } catch (SQLException e) {
                destroy();
                throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ " +
                        "and that your connecting user account has privileges to create tables.", e);
            }
        } catch (SQLException | IOException e) {
            destroy();
            throw new IllegalStateException("Failed to establish a connection to the MySQL database. " +
                    "Please check the supplied database credentials in the config file", e);
        }
        registerDaos();
        v2Fixer = new MySQLFixerV2(dataSource);
        v3Fixer = new MySQLFixerV3(dataSource);
    }

    @Override
    public void destroy() {
        if (dataSource != null) dataSource.close();
    }

    @Override
    public void registerDaos() {
        daos.put(Listing.class, new ListingSQLDao(dataSource));
        daos.put(CollectionBox.class, new CollectionBoxSQLDao(dataSource));
        daos.put(ExpiredItems.class, new ExpiredItemsSQLDao(dataSource));
        daos.put(History.class, new HistorySQLDao(dataSource));
        daos.put(Watching.class, new WatchersSQLDao(dataSource));
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
