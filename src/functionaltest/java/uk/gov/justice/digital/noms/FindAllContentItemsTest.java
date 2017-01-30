package uk.gov.justice.digital.noms;

import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.JsonPath;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FindAllContentItemsTest extends BaseTest {
    private static final String MONGO_COLLECTION_NAME = "contentItem";
    private MongoDatabase database;

    @Before
    public void connectToMongoDb() {
        String mongoConnectionUri = System.getenv("MONGODB_CONNECTION_URI");
        if (mongoConnectionUri == null || mongoConnectionUri.isEmpty()) {
            mongoConnectionUri = "mongodb://localhost:27017";
        }

        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoConnectionUri));
        database = mongoClient.getDatabase("hub_metadata");
    }

    @Test
    public void findsAllContentItemsInMetadataStore() throws Exception {
        // given
        List<String> expectedIds = metadataExistsInMongoDb();

        // when
        HttpResponse<JsonNode> response = Unirest.get(applicationUrl + "/content-items")
                .header("accept", "application/json")
                .asJson();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(contentIdsFrom(response)).containsAll(expectedIds);
    }

    private List<String> contentIdsFrom(HttpResponse<JsonNode> response) {
        return JsonPath.read(response.getBody().toString(), "$.contentItems[*].id");
    }

    private List<String> metadataExistsInMongoDb() {
        MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);
        Document contentItemDocument1 = new Document("title", "aTitle1")
                .append("uri", "aUri1")
                .append("category", "aCategory1")
                .append("filename", "hub-admin-1-pixel.png");

        Document contentItemDocument2 = new Document("title", "aTitle2")
                .append("uri", "aUri2")
                .append("category", "aCategory2")
                .append("filename", "hub-admin-2-pixel.png");

        List<Document> contentItemDocuments = new ArrayList<>();
        contentItemDocuments.add(contentItemDocument1);
        contentItemDocuments.add(contentItemDocument2);

        collection.insertMany(contentItemDocuments);
        String key1 = contentItemDocuments.get(0).get("_id").toString();
        String key2 = contentItemDocuments.get(1).get("_id").toString();
        return ImmutableList.of(key1, key2);
    }
}