package info.preva1l.fadah.data.dao.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import info.preva1l.fadah.data.dao.Dao;
import info.preva1l.fadah.records.collection.CollectableItem;
import info.preva1l.fadah.records.collection.CollectionBox;
import info.preva1l.fadah.utils.ItemSerializer;
import info.preva1l.fadah.utils.mongo.CollectionHelper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.NotImplementedException;
import org.bson.Document;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class CollectionBoxMongoDao implements Dao<CollectionBox> {
    private final CollectionHelper collectionHelper;

    /**
     * Get an object from the database by its id.
     *
     * @param id the id of the object to get.
     * @return an optional containing the object if it exists, or an empty optional if it does not.
     */
    @Override
    public Optional<CollectionBox> get(UUID id) {
        try {
            List<CollectableItem> list = new ArrayList<>();
            MongoCollection<Document> collection = collectionHelper.getCollection("collection_box");
            final FindIterable<Document> documents = collection.find().filter(Filters.eq("playerUUID", id));
            for (Document document : documents) {
                long dateAdded = document.getLong("dateAdded");
                ItemStack itemStack = ItemSerializer.deserialize(document.getString("itemStack"))[0];
                list.add(new CollectableItem(itemStack, dateAdded));
            }
            return Optional.of(new CollectionBox(id, list));
        } catch (Exception e) {
            e.printStackTrace();
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
     * @param collectionBox the object to save.
     */
    @Override
    public void save(CollectionBox collectionBox) {
        for (CollectableItem item : collectionBox.collectableItems()) {
            try {
                Optional<CollectionBox> current = get(collectionBox.owner());
                if (current.isPresent() && current.get().collectableItems().contains(item)) continue;
                Document document = new Document("playerUUID", collectionBox.owner())
                        .append("itemStack", ItemSerializer.serialize(item.itemStack()))
                        .append("dateAdded", item.dateAdded());
                collectionHelper.insertDocument("collection_box", document);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Update an object of type T in the database.
     *
     * @param collectionBox the object to update.
     * @param params        the parameters to update the object with.
     */
    @Override
    public void update(CollectionBox collectionBox, String[] params) {
        throw new NotImplementedException();
    }

    /**
     * Delete an object of type T from the database.
     *
     * @param collectionBox the object to delete.
     */
    @Override
    public void delete(CollectionBox collectionBox) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteSpecific(CollectionBox collectionBox, Object o) {
        try {
            if (!(o instanceof CollectableItem item))
                throw new IllegalStateException("Specific object must be a collectable item");
            MongoCollection<Document> collection = collectionHelper.getCollection("collection_box");
            final Document listingDocument = collection.find().filter(Filters.eq("playerUUID", collectionBox.owner()))
                    .filter(Filters.eq("itemStack", ItemSerializer.serialize(item.itemStack()))).first();
            if (listingDocument == null) return;
            collectionHelper.deleteDocument("collection_box", listingDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
