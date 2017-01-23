package uk.gov.justice.digital.noms.hub.ports.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

@Repository
public class MongoMetadataRepository implements MetadataRepository {
    private static final String COLLECTION_NAME = "contentItem";

    private final MongoDatabase database;

    public MongoMetadataRepository() {
        String mongoConnectionUri = System.getenv("MONGODB_CONNECTION_URI");
        if (mongoConnectionUri == null || mongoConnectionUri.isEmpty()) {
            mongoConnectionUri = "mongodb://localhost:27017";
        }

        MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoConnectionUri));
        database = mongoClient.getDatabase("hub_metadata");
    }

    @Override
    public String save(ContentItem contentItem) {
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        Document contentItemDocument = new Document("title", contentItem.getTitle())
                .append("uri", contentItem.getMediaUri())
                .append("filename", contentItem.getFilename());

        collection.insertOne(contentItemDocument);

        return contentItemDocument.getObjectId("_id").toString();
    }
}
