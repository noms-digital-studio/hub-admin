package uk.gov.justice.digital.noms;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class UploadImageTest {

    private String hostname;
    private String port;
    private MongoDatabase database;

    @Before
    public void readHostnameAndPort() {
        hostname = System.getenv("APPLICATION_HOSTNAME");
        if (hostname == null || hostname.isEmpty()) {
            hostname = "localhost";
        }

        port = System.getenv("APPLICATION_PORT");
        if (port == null || port.isEmpty()) {
            port = "8080";
        }
    }

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
    public void uploadsImageAndTitleSuccessfully() throws Exception {

        // when
        HttpResponse<String> response = Unirest.post("http://" + hostname + ":" + port +"/content-items")
                .header("accept", "application/json")
                .field("title", "A one pixel image")
                .field("file", new File(this.getClass().getResource("/1-pixel.png").toURI()))
                .asString();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        String theContentItemResource = response.getHeaders().get("Location").get(0);
        assertThat(theContentItemResource).contains("/content-items/");

        Document document = theDocumentInTheMongoDbFor(theContentItemResource);
        assertThat(document).contains(entry("title", "A one pixel image"));
        assertThat(document).contains(entry("uri", "http://digitalhub2.blob.core.windows.net/content-items/1-pixel.png"));

        HttpResponse<String> imageResponse = Unirest.get(document.getString("uri")).asString();
        assertThat(imageResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    private Document theDocumentInTheMongoDbFor(String location) {
        MongoCollection<Document> collection = database.getCollection("content_items");
        return collection.find(new BasicDBObject("_id", new ObjectId(idFrom(location)))).first();
    }

    private String idFrom(String location) {
        return location.substring(location.lastIndexOf("/") + 1, location.length());
    }

}