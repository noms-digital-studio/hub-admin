package uk.gov.justice.digital.noms.hub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
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

import static com.mongodb.client.model.Filters.eq;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static uk.gov.justice.digital.noms.hub.Metadata.*;

public class UploadFileTest extends BaseTest {
    private static final String FILE_NAME = "hub-admin-1-pixel.png";
    private static final String AZURE_CONTAINER_NAME = "content-items";
    private static final String MONGO_COLLECTION_NAME = "contentItem";

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
    public void uploadsANewImageAndTitleSuccessfully() throws Exception {
        // given
        theImageDoesNotExistInAzure();
        theImageMetadataDoesNotExistInMongoDb();

        performAndValidateFileUpload();
    }

    @Test
    public void uploadsAnExistingImageAndTitleSuccessfully() throws Exception {
        // given
        theImageDoesExistInAzure();
        theImageMetadataDoesExistInMongoDb();

        performAndValidateFileUpload();
    }

    private void performAndValidateFileUpload() throws UnirestException, URISyntaxException, IOException, NoSuchAlgorithmException {
        // when
        HttpResponse<String> response =
                Unirest.post(applicationUrl + "/content-items")
                        .basicAuth(userName, password)
                        .field("file", getOriginalFile(FILE_NAME))
                        .field("metadata", someJsonMetadata("1"))
                        .asString();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());

        String theContentItemResource = response.getHeaders().get("Location").get(0);
        assertThat(theContentItemResource).contains("/content-items/");

        Document document = theDocumentInTheMongoDbFor(theContentItemResource);
        assertThat(document).contains(entry("filename", FILE_NAME));
        assertThat(document).contains(entry("uri", format("%s/%s/%s", azurePublicUrlBase, AZURE_CONTAINER_NAME, FILE_NAME)));
        assertThat(document.get("timestamp")).isNotNull();
        assertThat(convertToMap(document.get("metadata"))).containsAllEntriesOf(someMetadata("1"));

        HttpResponse<String> imageResponse = Unirest.get(document.getString("uri")).asString();
        assertThat(imageResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(mD5For(imageResponse.getRawBody())).isEqualTo(mD5For(originalFileInputStream()));
    }

    private FileInputStream originalFileInputStream() throws FileNotFoundException, URISyntaxException {
        return new FileInputStream(getOriginalFile(FILE_NAME));
    }

    private File getOriginalFile(String filename) throws URISyntaxException {
        return new File(this.getClass().getResource("/" + filename).toURI());
    }

    private byte[] mD5For(InputStream is) throws URISyntaxException, NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (DigestInputStream dis = new DigestInputStream(is, md)) {
            while (dis.read() != -1) {
            }
        }
        return md.digest();
    }

    private void theImageDoesExistInAzure() throws URISyntaxException, StorageException, IOException {
        CloudBlockBlob blob = container.getBlockBlobReference(FILE_NAME);
        blob.upload(originalFileInputStream(), getOriginalFile(FILE_NAME).length());
    }

    private void theImageDoesNotExistInAzure() throws URISyntaxException, StorageException {
        CloudBlockBlob blob = container.getBlockBlobReference(FILE_NAME);
        blob.deleteIfExists();
    }

    private void theImageMetadataDoesNotExistInMongoDb() {
        MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);
        DeleteResult result = collection.deleteOne(eq("filename", FILE_NAME));
        assertThat(result.wasAcknowledged()).isTrue();
    }

    private void theImageMetadataDoesExistInMongoDb() throws JsonProcessingException {
        MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);
        Document contentItemDocument = new Document()
                .append("uri", "aUri")
                .append("metadata", someMetadata("1"))
                .append("filename", FILE_NAME);

        collection.insertOne(contentItemDocument);
        assertThat(contentItemDocument.get("_id")).isNotNull();
    }

    private Document theDocumentInTheMongoDbFor(String location) {
        MongoCollection<Document> collection = database.getCollection(MONGO_COLLECTION_NAME);
        return collection.find(new BasicDBObject("_id", new ObjectId(idFrom(location)))).first();
    }

    private String idFrom(String location) {
        return location.substring(location.lastIndexOf("/") + 1, location.length());
    }


}