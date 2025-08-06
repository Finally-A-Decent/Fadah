package info.preva1l.fadah.data.dao.common_sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.watcher.Watching;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created on 20/03/2025
 *
 * @author Preva1l
 */
public abstract class CommonWatchersSQLDao implements Dao<Watching> {
    protected static final Gson GSON = new GsonBuilder()
            .serializeNulls().disableHtmlEscaping().create();

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<Watching> get(UUID id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT `watching`
                    FROM `watchers`
                    WHERE `playerUUID` = ?;""")) {
                statement.setString(1, id.toString());
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    Watching item = GSON.fromJson(resultSet.getString("watching"), Watching.class);
                    return Optional.of(item);
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to fetch all items from watchers!", e);
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
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT `watching`
                    FROM `watchers`;""")) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    Watching item = GSON.fromJson(resultSet.getString("watching"), Watching.class);
                    result.add(item);
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to fetch all items from watchers!", e);
        }
        return result;
    }

    /**
     * Update an object of type T in the database.
     *
     * @param watching the object to update.
     * @param params   the parameters to update the object with.
     */
    @Override
    public void update(Watching watching, Map<String, ?> params) {
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

    protected abstract Connection getConnection() throws SQLException;
}
