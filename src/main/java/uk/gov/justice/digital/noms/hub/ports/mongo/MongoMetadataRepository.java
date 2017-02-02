package uk.gov.justice.digital.noms.hub.ports.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;

@Slf4j
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
            String message = "No metadata record found for: " + contentItem.getFilename();
            log.error(message);
            throw new RuntimeException(message);
        }
    }

    @Override
    public List<ContentItem> findAll() {
        List<ContentItem> result = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME);
        FindIterable<Document> documents = collection.find();
        for (Document document : documents) {
            result.add(aContentItemFrom(document));
        }

        return result;
    }

    private ContentItem aContentItemFrom(Document document) {
        return ContentItem.builder()
                .id(getValueFor(document, "_id"))
                .title(getValueFor(document, "title"))
                .category(getValueFor(document, "category"))
                .filename(getValueFor(document, "filename"))
                .mediaUri(getValueFor(document, "uri"))
                .build();
    }

    private String getValueFor(Document document, String key) {
        Object o = document.get(key);
        if (o != null) {
            return o.toString();
        } else {
            return "";
        }
    }

    private BasicDBObject anUpdateFor(ContentItem contentItem) {
        BasicDBObject contentItemDocument =
                new BasicDBObject("title", contentItem.getTitle())
                        .append("uri", contentItem.getMediaUri())
                        .append("filename", contentItem.getFilename())
                        .append("category", contentItem.getCategory());

        return new BasicDBObject("$set", contentItemDocument);
    }

    private FindOneAndUpdateOptions upsertOptions() {
        FindOneAndUpdateOptions findOneAndUpdateOptions = new FindOneAndUpdateOptions();
        findOneAndUpdateOptions.upsert(true);
        findOneAndUpdateOptions.returnDocument(AFTER);
        return findOneAndUpdateOptions;
    }
}
