package info.preva1l.fadah.data.fixers.v2;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.cache.CacheAccess;
import info.preva1l.fadah.cache.ExpiredListingsCache;
import info.preva1l.fadah.data.DatabaseManager;
import info.preva1l.fadah.records.CollectableItem;
import info.preva1l.fadah.records.CollectionBox;
import info.preva1l.fadah.records.ExpiredItems;
import info.preva1l.fadah.utils.ItemSerializer;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class MySQLFixerV2 implements V2Fixer {
    private final HikariDataSource dataSource;

    @Override
    public void fixExpiredItems(UUID player) {
        final List<CollectableItem> retrievedData = Lists.newArrayList();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT `itemStack`, `dateAdded`
                FROM `expired_items`
                WHERE `playerUUID`=?;""")) {
                statement.setString(1, player.toString());
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                    final long dateAdded = resultSet.getLong("dateAdded");
                    CollectableItem collectableItem = new CollectableItem(itemStack, dateAdded);
                    retrievedData.add(collectableItem);
                    ExpiredListingsCache.addItem(player, collectableItem);
                }
                DatabaseManager.getInstance().save(ExpiredItems.class, ExpiredItems.of(player));
            }

            try (PreparedStatement deleteStatement = connection.prepareStatement("""
                DELETE FROM `expired_items`
                WHERE `playerUUID`=?;""")) {
                deleteStatement.setString(1, player.toString());
                deleteStatement.executeUpdate();
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get or remove items from expired items!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fixCollectionBox(UUID player) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT `itemStack`, `dateAdded`
                FROM `collection_box`
                WHERE `playerUUID`=?;""")) {
                statement.setString(1, player.toString());
                final ResultSet resultSet = statement.executeQuery();
                CollectionBox box = CacheAccess.get(CollectionBox.class, player);
                while (resultSet.next()) {
                    final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                    final long dateAdded = resultSet.getLong("dateAdded");
                    box.add(new CollectableItem(itemStack, dateAdded));
                }

                CacheAccess.add(CollectionBox.class, box);
                DatabaseManager.getInstance().save(CollectionBox.class, box);
            }
            try (PreparedStatement deleteStatement = connection.prepareStatement("""
                DELETE FROM `collection_box`
                WHERE `playerUUID`=?;""")) {
                deleteStatement.setString(1, player.toString());
                deleteStatement.executeUpdate();
            }
        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to get or remove items from collection box!");
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean needsFixing(UUID player) {
        boolean collection = false;
        boolean expired = false;

        try (Connection connection = getConnection()) {
            if (tableExists(connection, "collection_box")) {
                try (PreparedStatement collectionStatement = connection.prepareStatement("""
                SELECT * FROM `collection_box` WHERE `playerUUID`=?;""")) {

                    collectionStatement.setString(1, player.toString());
                    try (ResultSet collectionResult = collectionStatement.executeQuery()) {
                        collection = collectionResult.next();
                    }
                } catch (SQLException e) {
                    if (e.getErrorCode() != 1146) {
                        e.printStackTrace();
                    }
                }
            }
            if (tableExists(connection, "expired_items")) {
                try (PreparedStatement expiredStatement = connection.prepareStatement("""
                SELECT * FROM `expired_items` WHERE `playerUUID`=?;""")) {

                    expiredStatement.setString(1, player.toString());
                    try (ResultSet expiredResult = expiredStatement.executeQuery()) {
                        expired = expiredResult.next();
                    }
                } catch (SQLException e) {
                    if (e.getErrorCode() != 1146) {
                        e.printStackTrace();
                    }
                }
            }

        } catch (SQLException e) {
            Fadah.getConsole().severe("Failed to check if player needs fixing!");
            throw new RuntimeException(e);
        }

        return collection || expired;
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