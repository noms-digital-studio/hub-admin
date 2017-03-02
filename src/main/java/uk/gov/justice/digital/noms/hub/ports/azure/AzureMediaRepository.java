package uk.gov.justice.digital.noms.hub.ports.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.springframework.boot.context.embedded.MimeMappings;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.noms.hub.domain.FileSpec;
import uk.gov.justice.digital.noms.hub.domain.MediaRepository;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Repository
public class AzureMediaRepository implements MediaRepository {
    private static final String CONTAINER_NAME = "content-items";

    private CloudBlobContainer container;
    private String azurePublicUrlBase;

    public AzureMediaRepository() throws URISyntaxException, InvalidKeyException, StorageException {
        String azureConnectionUri = System.getenv("AZURE_BLOB_STORE_CONNECTION_URI");
        if (azureConnectionUri == null || azureConnectionUri.isEmpty()) {
            azureConnectionUri = "AccountName=abc;AccountKey=YWJjCg==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;";
        }

        azurePublicUrlBase = System.getenv("AZURE_BLOB_STORE_PUBLIC_URL_BASE");
        if (azurePublicUrlBase == null || azurePublicUrlBase.isEmpty()) {
            azurePublicUrlBase = "http://127.0.0.1:10000";
        }

        setupContainer(azureConnectionUri);
    }

    private void setupContainer(String azureConnectionUri) throws URISyntaxException, InvalidKeyException, StorageException {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureConnectionUri);
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        container = blobClient.getContainerReference(CONTAINER_NAME);
        container.createIfNotExists();
        BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        container.uploadPermissions(containerPermissions);
    }

    @Override
    public String save(FileSpec file) {

        try {
            CloudBlockBlob blob = container.getBlockBlobReference(file.getFilename());
            blob.getProperties().setContentType(getMimeType(file.getFilename()));
            blob.upload(file.getInputStream(), file.getSize());

            return String.format("%s/%s/%s", azurePublicUrlBase, CONTAINER_NAME, file.getFilename());
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String getMimeType(String fileName) {
        String mimeType = MimeMappings.DEFAULT.get(fileName.substring(fileName.lastIndexOf(".") + 1));

        return mimeType == null ? "application/octet-stream" : mimeType;
    }
}
