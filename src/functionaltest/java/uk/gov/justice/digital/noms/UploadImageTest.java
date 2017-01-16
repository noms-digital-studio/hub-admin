package uk.gov.justice.digital.noms;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
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

import java.io.*;
import java.net.URISyntaxException;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class UploadImageTest extends BaseTest {
    private static final String IMAGE_FILE_NAME = "hub-admin-1-pixel.png";
    private static final String AZURE_CONTAINER_NAME = "content-items";
    private static final String MONGO_COLLECTION_NAME = "contentItems";

    private MongoDatabase database;
    private CloudBlobContainer container;
    private String azurePublicUrlBase;

    @Before
    public void connectToMongoDb() {
        String mongoConnectionUri = System.getenv("MONGODB_CONNECTION_URI");
        if (mongoConnectionUri == null || mongoConnectionUri.isEmpty()) {
            mongoConnectionUri = "mongodb://localhost:27017";
        }

        MongoClient mongoClient = new MongoClient(new MongoClientURI(mongoConnectionUri));
        database = mongoClient.getDatabase("hub_metadata");
    }

    @Before
    public void connectToAzureBlobStore() throws URISyntaxException, InvalidKeyException, StorageException {
        String azureConnectionUri = System.getenv("AZURE_BLOB_STORE_CONNECTION_URI");
        if (azureConnectionUri == null || azureConnectionUri.isEmpty()) {
            throw new RuntimeException("AZURE_BLOB_STORE_CONNECTION_URI environment variable was not set");
        }

        azurePublicUrlBase = System.getenv("AZURE_BLOB_STORE_PUBLIC_URL_BASE");
        if (azurePublicUrlBase == null || azurePublicUrlBase.isEmpty()) {
            azurePublicUrlBase = "http://digitalhub2.blob.core.windows.net";
        }

        CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureConnectionUri);
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        container = blobClient.getContainerReference(AZURE_CONTAINER_NAME);
        container.createIfNotExists();

        BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        container.uploadPermissions(containerPermissions);
    }

    @Test
    public void uploadsImageAndTitleSuccessfully() throws Exception {
        // Given
        theImageDoesNotExistInAzure();
        byte[] originalMd5 = mD5For(originalFileInputStream());

        // when
        HttpResponse<String> response = Unirest.post("http://" + hostname + ":" + port + "/content-items")
                .header("accept", "application/json")
                .field("title", "A one pixel image")
                .field("file", new File(this.getClass().getResource("/" + IMAGE_FILE_NAME).toURI()))
                .asString();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        String theContentItemResource = response.getHeaders().get("Location").get(0);
        assertThat(theContentItemResource).contains("/content-items/");

        Document document = theDocumentInTheMongoDbFor(theContentItemResource);
        assertThat(document).contains(entry("title", "A one pixel image"));
        assertThat(document).contains(entry("uri", format("%s/%s/%s", azurePublicUrlBase, AZURE_CONTAINER_NAME, IMAGE_FILE_NAME)));

        HttpResponse<String> imageResponse = Unirest.get(document.getString("uri")).asString();
        assertThat(imageResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(mD5For(imageResponse.getRawBody())).isEqualTo(originalMd5);
    }

    private FileInputStream originalFileInputStream() throws FileNotFoundException, URISyntaxException {
        return new FileInputStream(new File(getClass().getResource("/" + IMAGE_FILE_NAME).toURI()));
    }

    private byte[] mD5For(InputStream is) throws URISyntaxException, NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(is, md))
        {
            while (dis.read() != -1) {}
        }
        return md.digest();
    }

    private void theImageDoesNotExistInAzure() throws URISyntaxException, StorageException {
        CloudBlockBlob blob = container.getBlockBlobReference(IMAGE_FILE_NAME);
        blob.deleteIfExists();
    }

    private Document theDocumentInTheMongoDbFor(String location) {
        MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);
        return collection.find(new BasicDBObject("_id", new ObjectId(idFrom(location)))).first();
    }

    private String idFrom(String location) {
        return location.substring(location.lastIndexOf("/") + 1, location.length());
    }

}