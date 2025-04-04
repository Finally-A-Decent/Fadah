package info.preva1l.fadah.data.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import info.preva1l.fadah.Fadah;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.listing.Bid;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.BinListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.utils.ItemSerializer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

@RequiredArgsConstructor
public class ListingMongoDao implements Dao<Listing> {
    private final MongoDatabase database;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<Listing> get(UUID id) {
        try {
            MongoCollection<Document> collection = database.getCollection("listings");
            final Document doc = collection.find().filter(Filters.eq("uuid", id)).first();
            if (doc == null) return Optional.empty();

            return Optional.of(createListing(id, doc));
        } catch (Exception e) {
            Fadah.getConsole().log(Level.SEVERE, e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * Get all objects of type T from the database.
     *
     * @return a list of all objects of type T in the database.
     */
    @Override
    public List<Listing> getAll() {
        try {
            List<Listing> list = new ArrayList<>();
            MongoCollection<Document> collection = database.getCollection("listings");
            for (Document doc : collection.find()) {
                final UUID id = doc.get("uuid", UUID.class);

                list.add(createListing(id, doc));
            }
            return list;
        } catch (Exception e) {
            Fadah.getConsole().log(Level.SEVERE, e.getMessage(), e);
        }
        return List.of();
    }

    /**
     * Save an object of type T to the database.
     *
     * @param listing the object to save.
     */
    @Override
    public void save(Listing listing) {
        try {
            Document document = new Document("uuid", listing.getId())
                    .append("ownerUUID", listing.getOwner())
                    .append("ownerName", listing.getOwnerName())
                    .append("category", listing.getCategoryID() + "~" + listing.getCurrencyId())
                    .append("creationDate", listing.getCreationDate())
                    .append("deletionDate", listing.getDeletionDate())
                    .append("price", listing.getPrice())
                    .append("tax", listing.getTax())
                    .append("itemStack", ItemSerializer.serialize(listing.getItemStack()))
                    .append("biddable", false)
                    .append("bids", "");
            database.getCollection("listings").insertOne(document);
        } catch (Exception e) {
            Fadah.getConsole().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param listing the object to update.
     * @param params  the parameters to update the object with.
     */
    @Override
    public void update(Listing listing, String[] params) {
        throw new NotImplementedException();
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param listing the object to delete.
     */
    @Override
    public void delete(Listing listing) {
        try {
            database.getCollection("listings").findOneAndDelete(Filters.eq("uuid", listing.getId()));
        } catch (Exception e) {
            Fadah.getConsole().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private Listing createListing(UUID id, Document doc) {
        final UUID owner = doc.get("ownerUUID", UUID.class);
        final String ownerName = doc.getString("ownerName");
        String temp = doc.getString("category");
        String currency;
        String category;
        if (temp.contains("~")) {
            String[] t2 = temp.split("~");
            currency = t2[1];
            category = t2[0];
        } else {
            currency = "vault";
            category = temp;
        }
        final long creationDate = doc.getLong("creationDate");
        final long deletionDate = doc.getLong("deletionDate");
        final double price = doc.getDouble("price");
        final double tax = doc.getDouble("tax");
        final ItemStack itemStack = ItemSerializer.deserialize(doc.getString("itemStack"))[0];
        final boolean biddable = doc.getBoolean("biddable");
        final SortedSet<Bid> bids = new TreeSet<>();

        final Listing listing;
        if (biddable) {
            listing = new BidListing(
                    id,
                    owner, ownerName,
                    itemStack,
                    category,
                    currency, price, tax,
                    creationDate, deletionDate,
                    new TreeSet<>()
            );
        } else {
            listing = new BinListing(
                    id,
                    owner, ownerName,
                    itemStack,
                    category,
                    currency, price, tax,
                    creationDate, deletionDate,
                    new TreeSet<>()
            );
        }
        return listing;
    }
}
