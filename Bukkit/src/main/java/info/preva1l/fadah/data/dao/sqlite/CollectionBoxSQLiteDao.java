package info.preva1l.fadah.data.dao.sqlite;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.gson.BukkitSerializableAdapter;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class CollectionBoxSQLiteDao implements Dao<CollectionBox> {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitSerializableAdapter())
            .serializeNulls().disableHtmlEscaping().create();
    private static final Type COLLECTION_LIST_TYPE = new TypeToken<ArrayList<CollectableItem>>() {
    }.getType();
    private final HikariDataSource dataSource;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<CollectionBox> get(UUID id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT `items`
                    FROM `collection_boxV2`
                    WHERE `playerUUID`=?;""")) {
                statement.setString(1, id.toString());
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    List<CollectableItem> items = GSON.fromJson(resultSet.getString("items"), COLLECTION_LIST_TYPE);
                    return Optional.of(new CollectionBox(id, items));
                }
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get items from collection box!");
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<CollectionBox> getAll() {
        throw new NotImplementedException();
    }

    /**
     * Save an object of type T to the database.
     *
     * @param collectableList the object to save.
     */
    @Override
    public void save(CollectionBox collectableList) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO collection_boxV2 (playerUUID, items)
                     VALUES (?, ?)
                     ON CONFLICT(playerUUID) DO UPDATE SET
                         items = excluded.items;""")) {
                statement.setString(1, collectableList.owner().toString());
                statement.setString(2, GSON.toJson(collectableList.collectableItems(), COLLECTION_LIST_TYPE));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to add item to collection box!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param collectableItem the object to update.
     * @param params          the parameters to update the object with.
     */
    @Override
    public void update(CollectionBox collectableItem, String[] params) {
        throw new NotImplementedException();
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param collectableItem the object to delete.
     */
    @Override
    public void delete(CollectionBox collectableItem) {
        throw new NotImplementedException();
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
