package info.preva1l.fadah.data.dao.mongo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.listing.Bid;
import info.preva1l.fadah.records.listing.BidListing;
import info.preva1l.fadah.records.listing.Listing;
import info.preva1l.fadah.records.listing.ListingFactory;
import info.preva1l.fadah.utils.serialization.ItemSerializer;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;

public class ListingMongoDao implements Dao<Listing> {
    protected static final Gson GSON = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    protected static final Type BIDS_TYPE = new TypeToken<ConcurrentSkipListSet<Bid>>(){}.getType();

    private final MongoCollection<Document> collection;

    public ListingMongoDao(final MongoDatabase database) {
        this.collection = database.getCollection("listings");

        collection.createIndex(Indexes.ascending("uuid"), new IndexOptions().unique(true));
    }

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<Listing> get(UUID id) {
        try {
            final Document doc = collection.find().filter(Filters.eq("uuid", id)).first();
            if (doc == null) return Optional.empty();

            return Optional.of(createListing(id, doc));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
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
            for (Document doc : collection.find()) {
                final UUID id = doc.get("uuid", UUID.class);

                list.add(createListing(id, doc));
            }
            return list;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
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
                    .append("category", listing.getCurrencyId())
                    .append("creationDate", listing.getCreationDate())
                    .append("deletionDate", listing.getDeletionDate())
                    .append("price", listing.getPrice())
                    .append("tax", listing.getTax())
                    .append("itemStack", ItemSerializer.serialize(listing.getItemStack()))
                    .append("biddable", listing instanceof BidListing)
                    .append("bids", listing instanceof BidListing bid ? GSON.toJson(bid.getBids(), BIDS_TYPE) : "");
            collection.insertOne(document);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param listing the object to update.
     * @param params  the parameters to update the object with.
     */
    @Override
    public void update(Listing listing, Map<String, ?> params) {
        throw new NotImplementedException("update");
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param listing the object to delete.
     */
    @Override
    public void delete(Listing listing) {
        try {
            collection.findOneAndDelete(Filters.eq("uuid", listing.getId()));
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private Listing createListing(UUID id, Document doc) {
        final UUID owner = doc.get("ownerUUID", UUID.class);
        final String ownerName = doc.getString("ownerName");
        String temp = doc.getString("category");
        String currency;
        if (temp.contains("~")) { // support backwards compat
            String[] t2 = temp.split("~");
            currency = t2[1];
        } else {
            currency = temp;
        }
        final long creationDate = doc.getLong("creationDate");
        final long deletionDate = doc.getLong("deletionDate");
        final double price = doc.getDouble("price");
        final double tax = doc.getDouble("tax");
        final ItemStack itemStack = ItemSerializer.deserialize(doc.getString("itemStack"))[0];
        final boolean biddable = doc.getBoolean("biddable");
        final String bidString = doc.getString("bids");
        final ConcurrentSkipListSet<Bid> bids;
        if (bidString.isEmpty()) {
            bids = new ConcurrentSkipListSet<>();
        } else {
            bids = GSON.fromJson(bidString, BIDS_TYPE);
        }

        return ListingFactory.create(
                biddable,
                id,
                owner,
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
}
