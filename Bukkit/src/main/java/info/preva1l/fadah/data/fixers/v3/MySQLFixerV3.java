package info.preva1l.fadah.data.fixers.v3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.data.gson.BukkitSerializableAdapter;
import info.preva1l.fadah.records.history.HistoricItem;
import info.preva1l.fadah.records.history.History;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MySQLFixerV3 implements V3Fixer {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitSerializableAdapter())
            .serializeNulls().disableHtmlEscaping().create();
    private static final Type HISTORY_LIST_TYPE = new TypeToken<ArrayList<HistoricItem>>() {
    }.getType();
    private final HikariDataSource dataSource;

    @Override
    public void fixHistory(UUID player) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT `items`
                    FROM `history`
                    WHERE `playerUUID`=?;""")) {
                statement.setString(1, player.toString());
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    List<HistoricItem> items = GSON.fromJson(resultSet.getString("items"), HISTORY_LIST_TYPE);
                    DatabaseManager.getInstance().save(History.class, new History(player, items));
                }
            }
            try (PreparedStatement deleteStatement = connection.prepareStatement("""
                    DELETE FROM `history`
                    WHERE `playerUUID`=?;""")) {
                deleteStatement.setString(1, player.toString());
                deleteStatement.executeUpdate();
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get or remove items from history!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean needsFixing(UUID player) {
        try (Connection connection = getConnection()) {
            if (!tableExists(connection, "history")) return false;
            try (PreparedStatement collectionStatement = connection.prepareStatement("""
                    SELECT * FROM `history` WHERE `playerUUID`=?;""")) {
                collectionStatement.setString(1, player.toString());
                try (ResultSet collectionResult = collectionStatement.executeQuery()) {
                    if (collectionResult.next()) return true;
                }
            } catch (SQLException e) {
                if (e.getErrorCode() != 1146) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get or remove items from history!");
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean tableExists(Connection connection, String tableName) {
        try (ResultSet rs = connection.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
