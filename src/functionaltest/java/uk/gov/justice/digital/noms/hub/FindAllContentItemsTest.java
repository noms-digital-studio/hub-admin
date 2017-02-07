package uk.gov.justice.digital.noms.hub;

import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.JsonPath;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.minidev.json.JSONArray;
import org.apache.http.HttpStatus;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.reverse;
import static java.util.stream.Collectors.toList;
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
        List<String> expectedIds = multipleItemsExistInTheMetadataStore();

        // when
        HttpResponse<JsonNode> response = Unirest.get(applicationUrl + "/content-items")
                .header("accept", "application/json")
                .asJson();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
        assertThat(contentIdsFrom(response, expectedIds)).containsExactlyElementsOf(reverse(expectedIds));

        verifyMetadata(expectedIds, response);
    }

    private void verifyMetadata(List<String> expectedIds, HttpResponse<JsonNode> response) {
        String id1 = expectedIds.get(0);
        assertThat(aValueFrom(response, "title", id1)).isEqualTo("aTitle1");
        assertThat(aValueFrom(response, "mediaUri", id1)).isEqualTo("aUri1");
        assertThat(aValueFrom(response, "category", id1)).isEqualTo("aCategory1");
        assertThat(aValueFrom(response, "filename", id1)).isEqualTo("hub-admin-1-pixel.png");

        String id2 = expectedIds.get(1);
        assertThat(aValueFrom(response, "title", id2)).isEqualTo("aTitle2");
        assertThat(aValueFrom(response, "mediaUri", id2)).isEqualTo("aUri2");
        assertThat(aValueFrom(response, "category", id2)).isEqualTo("aCategory2");
        assertThat(aValueFrom(response, "filename", id2)).isEqualTo("hub-admin-2-pixel.png");
    }

    private String aValueFrom(HttpResponse<JsonNode> response, String field, String id) {
        JSONArray titles = JsonPath.read(response.getBody().toString(),
                "$.contentItems[?(@.id == '" + id + "')]." + field);
        return (String) titles.get(0);
    }

    private List<String> contentIdsFrom(HttpResponse<JsonNode> response, List<String> expectedIds) {
        List<String> allIds = JsonPath.read(response.getBody().toString(), "$.contentItems[*].id");
        return allIds.stream()
                     .filter(expectedIds::contains)
                     .collect(toList());
    }

    private List<String> multipleItemsExistInTheMetadataStore() throws InterruptedException {
        MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);
        Document contentItemDocument1 = new Document("title", "aTitle1")
                .append("uri", "aUri1")
                .append("category", "aCategory1")
                .append("filename", "hub-admin-1-pixel.png")
                .append("timestamp", new Date());
        collection.insertOne(contentItemDocument1);
        String key1 = contentItemDocument1.get("_id").toString();

        Document contentItemDocument2 = new Document("title", "aTitle2")
                .append("uri", "aUri2")
                .append("category", "aCategory2")
                .append("filename", "hub-admin-2-pixel.png")
                .append("timestamp", new Date());
        collection.insertOne(contentItemDocument2);
        String key2 = contentItemDocument2.get("_id").toString();

        return ImmutableList.of(key1, key2);
    }
}