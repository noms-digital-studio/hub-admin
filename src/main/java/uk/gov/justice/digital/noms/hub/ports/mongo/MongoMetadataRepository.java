package uk.gov.justice.digital.noms.hub.ports.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

@Repository
public class MongoMetadataRepository implements MetadataRepository {
    private static final String COLLECTION_NAME = "contentItem";

    private final MongoDatabase database;

    public MongoMetadataRepository() {
        String mongoConnectionUri = System.getenv("MONGODB_CONNECTION_URI");
        if (mongoConnectionUri == null || mongoConnectionUri.isEmpty()) {
            mongoConnectionUri = "mongodb://localhost:27017";
        }

        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoConnectionUri));
        database = mongoClient.getDatabase("hub_metadata");
    }

    @Override
    public String save(ContentItem contentItem) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        Document updatedDocument = collection
                .findOneAndUpdate(eq("filename", contentItem.getFilename()),
                                  anUpdateFor(contentItem),
                                  upsertOptions());

        if (updatedDocument != null) {
            return updatedDocument.getObjectId("_id").toString();
        } else {
            throw new RuntimeException("No metadata record found for: " + contentItem.getFilename());
        }
    }

    private BasicDBObject anUpdateFor(ContentItem contentItem) {
        BasicDBObject contentItemDocument = new BasicDBObject("title", contentItem.getTitle())
                .append("uri", contentItem.getMediaUri())
                .append("filename", contentItem.getFilename());

        return new BasicDBObject("$set", contentItemDocument);
    }

    private FindOneAndUpdateOptions upsertOptions() {
        FindOneAndUpdateOptions findOneAndUpdateOptions = new FindOneAndUpdateOptions();
        findOneAndUpdateOptions.upsert(true);
        findOneAndUpdateOptions.returnDocument(AFTER);
        return findOneAndUpdateOptions;
    }
}
