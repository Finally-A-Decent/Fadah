package info.preva1l.fadah.data.dao.sql;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.CurrentListing;
import info.preva1l.fadah.records.Listing;
import info.preva1l.fadah.utils.ItemSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ListingSQLDao implements Dao<Listing> {
    private final HikariDataSource dataSource;

    /**
     * Get a listing from the database by its id.
     *
     * @param id the id of the listing to get.
     * @return an optional containing the listing if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<Listing> get(UUID id) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT  `ownerUUID`, `ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`, `biddable`, `bids`
                        FROM `listings`
                        WHERE `uuid`=?;""")) {
                statement.setString(1, id.toString());
                final ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    final UUID ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
                    final String ownerName = resultSet.getString("ownerName");
                    final String categoryID = resultSet.getString("category");
                    final long creationDate = resultSet.getLong("creationDate");
                    final long deletionDate = resultSet.getLong("deletionDate");
                    final double price = resultSet.getDouble("price");
                    final double tax = resultSet.getDouble("tax");
                    final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                    final boolean biddable = resultSet.getBoolean("biddable");
                    return Optional.of(new CurrentListing(id, ownerUUID, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate, biddable, List.of()));
                }
            }
        } catch (SQLException e) {
            Fadah.getConsole().log(Level.SEVERE, "Failed to get listing!", e);
        }
        return Optional.empty();
    }

    /**
     * Get all listings from the database.
     *
     * @return a list of all the listings in the database.
     */
    @Override
    public List<Listing> getAll() {
        final List<Listing> retrievedData = Lists.newArrayList();
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        SELECT  `uuid`, `ownerUUID`, `ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`, `biddable`, `bids`
                        FROM `listings`;""")) {
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final UUID id = UUID.fromString(resultSet.getString("uuid"));
                    final UUID ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
                    final String ownerName = resultSet.getString("ownerName");
                    final String categoryID = resultSet.getString("category");
                    final long creationDate = resultSet.getLong("creationDate");
                    final long deletionDate = resultSet.getLong("deletionDate");
                    final double price = resultSet.getDouble("price");
                    final double tax = resultSet.getDouble("tax");
                    final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
                    final boolean biddable = resultSet.getBoolean("biddable");
                    retrievedData.add(new CurrentListing(id, ownerUUID, ownerName, itemStack, categoryID, price, tax, creationDate, deletionDate, biddable, List.of()));
                }
                return retrievedData;
            }
        } catch (SQLException e) {
            Fadah.getConsole().log(Level.SEVERE, "Failed to get all listings!", e);
        }
        return List.of();
    }

    /**
     * Save a listing to the database.
     *
     * @param listing the object to save.
     */
    @Override
    public void save(Listing listing) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO `listings`
                        (`uuid`,`ownerUUID`,`ownerName`, `category`, `creationDate`, `deletionDate`, `price`, `tax`, `itemStack`, `biddable`, `bids`)
                        VALUES (?,?,?,?,?,?,?,?,?,?,?);""")) {
                statement.setString(1, listing.getId().toString());
                statement.setString(2, listing.getOwner().toString());
                statement.setString(3, listing.getOwnerName());
                statement.setString(4, listing.getCategoryID());
                statement.setLong(5, listing.getCreationDate());
                statement.setLong(6, listing.getDeletionDate());
                statement.setDouble(7, listing.getPrice());
                statement.setDouble(8, listing.getTax());
                statement.setString(9, ItemSerializer.serialize(listing.getItemStack()));
                statement.setBoolean(10, false);
                statement.setString(11, "");
                statement.execute();
            }
        } catch (SQLException e) {
            Fadah.getConsole().log(Level.SEVERE, "Failed to add item to listings!", e);
        }
    }

    /**
     * Update a listing in the database.
     *
     * @param listing the listing to update.
     * @param params  the parameters to update the object with.
     */
    @Override
    public void update(Listing listing, String[] params) {
        throw new NotImplementedException();
    }

    /**
     * Delete the listing from the database.
     *
     * @param listing the listing to delete.
     */
    @Override
    public void delete(Listing listing) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM `listings`
                        WHERE uuid = ?;""")) {
                statement.setString(1, listing.getId().toString());
                statement.execute();
            }
        } catch (SQLException e) {
            Fadah.getConsole().log(Level.SEVERE, "Failed to remove item from listings!", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
