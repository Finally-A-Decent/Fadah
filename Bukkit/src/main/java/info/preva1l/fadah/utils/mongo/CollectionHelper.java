package info.preva1l.fadah.utils.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

public class CollectionHelper {
    private final MongoDatabase database;

    public CollectionHelper(MongoDatabase database) {
        this.database = database;
    }

    public void createCollection(String collectionName) {
        database.createCollection(collectionName);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    public void insertDocument(String collectionName, Document document) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.insertOne(document);
    }

    public void updateDocument(String collectionName, Document document, Bson updates) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.updateOne(document, updates);
    }

    public void deleteDocument(String collectionName, Document document) {
        MongoCollection<Document> collection = getCollection(collectionName);
        collection.deleteOne(document);
    }
}
