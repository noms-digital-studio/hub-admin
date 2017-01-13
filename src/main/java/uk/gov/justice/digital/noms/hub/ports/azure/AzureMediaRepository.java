package uk.gov.justice.digital.noms.hub.ports.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.noms.hub.domain.MediaRepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Repository
public class AzureMediaRepository implements MediaRepository {

    private CloudBlobContainer container;
    private String azurePublicUrlBase;

    public AzureMediaRepository() throws URISyntaxException, InvalidKeyException, StorageException {
        String azureConnectionUri = System.getenv("AZURE_BLOB_STORE_CONNECTION_URI");
        if (azureConnectionUri == null || azureConnectionUri.isEmpty()) {
            throw new RuntimeException("AZURE_BLOB_STORE_CONNECTION_URI environment variable was not set");
        }

        azurePublicUrlBase = System.getenv("AZURE_BLOB_STORE_PUBLIC_URL_BASE");
        if (azurePublicUrlBase == null || azurePublicUrlBase.isEmpty()) {
            azurePublicUrlBase = "http://digitalhub2.blob.core.windows.net/content-items/";
        }

        setupContainer(azureConnectionUri);
    }

    private void setupContainer(String azureConnectionUri) throws URISyntaxException, InvalidKeyException, StorageException {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(azureConnectionUri);
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        container = blobClient.getContainerReference("content-items");
        container.createIfNotExists();
        BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        container.uploadPermissions(containerPermissions);
    }

    @Override
    public String save(InputStream mediaStream, String filename, long size) {
        try {
            CloudBlockBlob blob = container.getBlockBlobReference(filename);
            blob.upload(mediaStream, size);
            return azurePublicUrlBase + filename;
        } catch (URISyntaxException | StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
