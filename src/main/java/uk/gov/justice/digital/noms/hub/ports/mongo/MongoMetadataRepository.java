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
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Collections.emptyMap;

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

        Document updatedDocument = collection()
                .findOneAndUpdate(eq("filename", contentItem.getFilename()),
                        updateFor(contentItem),
                        asUpsert());

        if (updatedDocument == null) {
            String message = "No metadata record found for: " + contentItem.getFilename();
            log.error(message);
            throw new RuntimeException(message);
        }

        return updatedDocument.getObjectId("_id").toString();
    }

    @Override
    public List<ContentItem> findAll(String filter) {

        FindIterable<Document> documents =
                collection().find(BasicDBObject.parse(filter))
                        .sort(orderBy(descending("timestamp")));

        List<ContentItem> result = new ArrayList<>();

        for (Document document : documents) {
            result.add(contentItemFrom(document));
        }

        return result;
    }


    @Override
    public Optional<ContentItem> findById(String id) {

        if (!isValid(id)) {
            return Optional.empty();
        }

        FindIterable<Document> documents =
                collection().find(eq("_id", new ObjectId(id)));

        if (documents.iterator().hasNext()) {
            return Optional.of(contentItemFrom(documents.first()));
        }

        return Optional.empty();
    }

    private boolean isValid(String id) {
        try {
            new ObjectId(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private MongoCollection<Document> collection() {
        return database.getCollection(COLLECTION_NAME);
    }

    private ContentItem contentItemFrom(Document document) {
        MongoDocument doc = new MongoDocument(document);
        return ContentItem.builder()
                .id(doc.getValue("_id"))
                .filename(doc.getValue("filename"))
                .timestamp(doc.getValue("timestamp"))
                .files(doc.getMap("files"))
                .metadata(doc.getMap("metadata"))
                .build();
    }

    private BasicDBObject updateFor(ContentItem contentItem) {
        BasicDBObject contentItemDocument =
                new BasicDBObject("filename", contentItem.getFilename())
                        .append("files", contentItem.getFiles())
                        .append("metadata", contentItem.getMetadata())
                        .append("timestamp", ISO_INSTANT.format(Instant.now()));

        return new BasicDBObject("$set", contentItemDocument);
    }

    private FindOneAndUpdateOptions asUpsert() {
        FindOneAndUpdateOptions findOneAndUpdateOptions = new FindOneAndUpdateOptions();
        findOneAndUpdateOptions.upsert(true);
        findOneAndUpdateOptions.returnDocument(AFTER);

        return findOneAndUpdateOptions;
    }

    private class MongoDocument {

        private final Document document;

        MongoDocument(Document document) {
            this.document = document;
        }

        String getValue(String key) {
            Object o = document.get(key);
            return o == null ? "" : o.toString();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> getMap(String key) {
            Object o = document.get(key);
            if (o != null && o instanceof Map) {
                return (Map<String, Object>) o;
            }

            return emptyMap();
        }
    }
}
