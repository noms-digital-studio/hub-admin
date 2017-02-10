package uk.gov.justice.digital.noms.hub;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.reverse;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.digital.noms.hub.Metadata.MEDIA_TYPE;
import static uk.gov.justice.digital.noms.hub.Metadata.someMetadata;

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
    public void findAllReturnsDataInCorrectOrder() throws Exception {
        // given
        List<String> expectedIds = multipleItemsExistInTheMetadataStore();

        // when
        HttpResponse<JsonNode> response = Unirest.get(applicationUrl + "/content-items")
                .header("accept", "application/json")
                .basicAuth(userName, password)
                .asJson();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);

        List<String> actualIds = contentIdsFrom(response, expectedIds);
        assertThat(actualIds).containsExactlyElementsOf(reverse(expectedIds));

        verifyMetadata(expectedIds, response);
    }

    @Test
    public void findAllWithFilterReturnsOnlySelectedData() throws Exception {
        // given
        List<String> ids = multipleItemsExistInTheMetadataStore();
        List<String> originalIds = anotherItemWithDifferentMediaTypeisAdded(ids);

        // when
        HttpResponse<JsonNode> response = Unirest.get(applicationUrl + "/content-items")
                .header("accept", "application/json")
                .basicAuth(userName, password)
                .queryString("filter", "{ 'metadata.mediaType': 'image/jpg'}")
                .asJson();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);

        List<String> actualIds = contentIdsFrom(response, originalIds);
        List<String> expectedIds = originalIds.subList(2, 3);
        assertThat(actualIds).containsExactlyElementsOf(reverse(expectedIds));
    }

    private List<String> anotherItemWithDifferentMediaTypeisAdded(List<String> ids) {
        MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);
        Document contentItemDocument3 = new Document()
                .append("uri", "aUri3")
                .append("filename", "hub-admin-3-pixel.png")
                .append("metadata", someMetadata("3", "image/jpg"))
                .append("timestamp", ISO_INSTANT.format(Instant.now()));
        collection.insertOne(contentItemDocument3);
        List<String> originalIds = new ArrayList<>(ids);
        originalIds.add(contentItemDocument3.get("_id").toString());
        return originalIds;
    }


    private void verifyMetadata(List<String> expectedIds, HttpResponse<JsonNode> response) {
        String jsonObject = response.getBody().toString();
        String id1 = expectedIds.get(0);
        assertThat(aValueFrom(jsonObject, "filename", id1)).isEqualTo("hub-admin-1-pixel.png");
        assertThat(aValueFrom(jsonObject, "mediaUri", id1)).isEqualTo("aUri1");
        assertThat(aMapFrom(jsonObject, "metadata", id1)).containsAllEntriesOf(someMetadata("1", MEDIA_TYPE));

        String id2 = expectedIds.get(1);
        assertThat(aValueFrom(jsonObject, "filename", id2)).isEqualTo("hub-admin-2-pixel.png");
        assertThat(aValueFrom(jsonObject, "mediaUri", id2)).isEqualTo("aUri2");
        assertThat(aMapFrom(jsonObject, "metadata", id2)).containsAllEntriesOf(someMetadata("2", MEDIA_TYPE));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> aMapFrom(String jsonObject, String field, String id) {
        JSONArray items = JsonPath.read(jsonObject, "$.contentItems[?(@.id == '" + id + "')]." + field);
        return (Map<String, Object>) items.get(0);
    }


    private String aValueFrom(String jsonObject, String field, String id) {

        JSONArray items = JsonPath.read(jsonObject, "$.contentItems[?(@.id == '" + id + "')]." + field);
        Object value = items.get(0);
        return (String) value;
    }

    private List<String> contentIdsFrom(HttpResponse<JsonNode> response, List<String> expectedIds) {
        List<String> allIds = JsonPath.read(response.getBody().toString(), "$.contentItems[*].id");
        return allIds.stream()
                .filter(expectedIds::contains)
                .collect(toList());
    }

    private List<String> multipleItemsExistInTheMetadataStore() throws InterruptedException, JsonProcessingException {
        MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);
        Document contentItemDocument1 = new Document()
                .append("uri", "aUri1")
                .append("filename", "hub-admin-1-pixel.png")
                .append("metadata", someMetadata("1", MEDIA_TYPE))
                .append("timestamp", ISO_INSTANT.format(Instant.now()));
        collection.insertOne(contentItemDocument1);
        String key1 = contentItemDocument1.get("_id").toString();

        Document contentItemDocument2 = new Document()
                .append("uri", "aUri2")
                .append("filename", "hub-admin-2-pixel.png")
                .append("metadata", someMetadata("2", MEDIA_TYPE))
                .append("timestamp", ISO_INSTANT.format(Instant.now()));
        collection.insertOne(contentItemDocument2);
        String key2 = contentItemDocument2.get("_id").toString();

        return ImmutableList.of(key1, key2);
    }
}