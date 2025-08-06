package info.preva1l.fadah.data.dao.common_sql;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zaxxer.hikari.HikariDataSource;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.listing.Bid;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.records.listing.ListingFactory;
import info.preva1l.fadah.utils.serialization.ItemSerializer;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;

/**
 * Created on 19/03/2025
 *
 * @author Preva1l
 */
@AllArgsConstructor
public abstract class CommonSQLListingDao implements Dao<Listing> {
    private final HikariDataSource dataSource;
    protected static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    protected static final Type BIDS_TYPE = new TypeToken<ConcurrentSkipListSet<Bid>>(){}.getType();

    /**
     * Converts a set of bids to its JSON string representation.
     *
     * @param bids a thread-safe sorted set of bid records to be serialized into JSON.
     * @return a JSON string representing the input set of bids.
     */
    public static String bidToJsonString(ConcurrentSkipListSet<Bid> bids) {
        return GSON.toJson(bids, BIDS_TYPE);
    }

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
                        SELECT  `ownerUUID`, `ownerName`, `category`,
                                `creationDate`, `deletionDate`, `price`, 
                                `tax`, `itemStack`, `biddable`, `bids`
                        FROM `listings`
                        WHERE `uuid`=?;""")) {
                statement.setString(1, id.toString());
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return Optional.of(createListing(id, resultSet));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to get listing!", e);
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
                        SELECT  `uuid`, `ownerUUID`, `ownerName`, `category`,
                                `creationDate`, `deletionDate`, `price`, `tax`,
                                `itemStack`, `biddable`, `bids`
                        FROM `listings`;""")) {
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    final UUID id = UUID.fromString(resultSet.getString("uuid"));
                    retrievedData.add(createListing(id, resultSet));
                }
                return retrievedData;
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to get all listings!", e);
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
                        (`uuid`,`ownerUUID`,`ownerName`, `category`,
                         `creationDate`, `deletionDate`, `price`, `tax`,
                         `itemStack`, `biddable`, `bids`)
                        VALUES (?,?,?,?,?,?,?,?,?,?,?);""")) {
                statement.setString(1, listing.getId().toString());
                statement.setString(2, listing.getOwner().toString());
                statement.setString(3, listing.getOwnerName());
                statement.setString(4, listing.getCurrencyId());
                statement.setLong(5, listing.getCreationDate());
                statement.setLong(6, listing.getDeletionDate());
                statement.setDouble(7, listing.getPrice());
                statement.setDouble(8, listing.getTax());
                statement.setString(9, ItemSerializer.serialize(listing.getItemStack()));
                statement.setBoolean(10, listing instanceof BidListing);
                statement.setString(11, listing instanceof BidListing bid ? GSON.toJson(bid.getBids(), BIDS_TYPE) : "");
                statement.execute();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to add item to listings!", e);
        }
    }

    /**
     * Update a listing in the database.
     *
     * @param listing the listing to update.
     * @param params  the parameters to update the object with.
     */
    @Override
    public void update(Listing listing, Map<String, ?> params) {
        if (params.isEmpty()) {
            getLogger().warning("Tried to update item listing with no parameters!");
            return;
        }
        try (Connection connection = getConnection()) {
            String sql = """
                    UPDATE `listings` SET %s WHERE uuid = ?;
                    """.formatted(params.keySet().stream()
                    .map("`%s` = ?"::formatted)
                    .collect(Collectors.joining(", ")));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(params.keySet().toArray()[i]));
                }
                statement.setString(params.size() + 1, listing.getId().toString());
                statement.execute();
            }
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to update item from listing!", e);
        }
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
            getLogger().log(Level.SEVERE, "Failed to remove item from listings!", e);
        }
    }

    private Listing createListing(UUID id, ResultSet resultSet) throws SQLException {
        final UUID ownerUUID = UUID.fromString(resultSet.getString("ownerUUID"));
        final String ownerName = resultSet.getString("ownerName");
        String temp = resultSet.getString("category");
        String currency;
        if (temp.contains("~")) { // support backwards compat
            String[] t2 = temp.split("~");
            currency = t2[1];
        } else {
            currency = temp;
        }
        final long creationDate = resultSet.getLong("creationDate");
        final long deletionDate = resultSet.getLong("deletionDate");
        final double price = resultSet.getDouble("price");
        final double tax = resultSet.getDouble("tax");
        final ItemStack itemStack = ItemSerializer.deserialize(resultSet.getString("itemStack"))[0];
        final boolean biddable = resultSet.getBoolean("biddable");
        final String bidString = resultSet.getString("bids");
        final ConcurrentSkipListSet<Bid> bids;
        if (bidString.isEmpty()) {
            bids = new ConcurrentSkipListSet<>();
        } else {
            bids = GSON.fromJson(bidString, BIDS_TYPE);
        }

        return ListingFactory.create(
                biddable,
                id,
                ownerUUID,
                ownerName,
                itemStack,
                currency,
                price,
                tax,
                creationDate,
                deletionDate,
                bids
        );
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
