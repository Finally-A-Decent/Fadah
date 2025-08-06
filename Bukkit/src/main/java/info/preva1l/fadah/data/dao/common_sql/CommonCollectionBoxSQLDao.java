package info.preva1l.fadah.data.dao.common_sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.data.gson.BukkitSerializableAdapter;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.records.collection.ImplCollectionBox;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created on 20/03/2025
 *
 * @author Preva1l
 */
public abstract class CommonCollectionBoxSQLDao implements Dao<CollectionBox> {
    protected static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitSerializableAdapter())
            .serializeNulls().disableHtmlEscaping().create();
    protected static final Type COLLECTION_LIST_TYPE = new TypeToken<CopyOnWriteArrayList<CollectableItem>>() {}.getType();

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
                    CopyOnWriteArrayList<CollectableItem> items = GSON.fromJson(resultSet.getString("items"), COLLECTION_LIST_TYPE);
                    return Optional.of(new ImplCollectionBox(id, items));
                }
            }
        } catch (SQLException e) {
            getLogger().severe("Failed to get items from collection box!");
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /**
     * Update an object of type T in the database.
     *
     * @param collectableItem the object to update.
     * @param params          the parameters to update the object with.
     */
    @Override
    public void update(CollectionBox collectableItem, Map<String, ?> params) {
        throw new NotImplementedException("cannot update on collection box dao");
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param collectableItem the object to delete.
     */
    @Override
    public void delete(CollectionBox collectableItem) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM `collection_boxV2`
                WHERE `playerUUID` = ?;""")) {
                statement.setString(1, collectableItem.owner().toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            getLogger().severe("Failed to delete collection box!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<CollectionBox> getAll() {
        throw new NotImplementedException("cannot get all on collection box dao");
    }

    protected abstract Connection getConnection() throws SQLException;
}
