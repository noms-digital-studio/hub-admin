package uk.gov.justice.digital.noms.hub

import com.gmongo.GMongo
import com.gmongo.GMongoClient
import com.mashape.unirest.http.Unirest
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.*
import com.mongodb.DB
import com.mongodb.MongoClientURI
import groovy.json.JsonSlurper
import org.bson.types.ObjectId
import spock.lang.Shared
import spock.lang.Specification

import java.security.DigestInputStream
import java.security.MessageDigest

class UploadFileSpec extends Specification {

    private static final String FILE_NAME = "hub-admin-1-pixel.png"
    private static final String TITLE = "aTitle"
    private static final String CATEGORY = "aCategory"
    private static final String CONTENT_TYPE = "aMediaType"

    @Shared
    private Hub theHub

    @Shared
    private String adminAppRoot

    @Shared
    private DB db

    @Shared
    private CloudBlobContainer container

    private Date startDate

    def setup() {
        theHub = new Hub()
        adminAppRoot = theHub.adminUri

        MongoClientURI mongoUri = new MongoClientURI(theHub.mongoConnectionUri)
        db = new GMongoClient(mongoUri).getDB("hub_metadata")

        startDate = new Date()

        CloudStorageAccount storageAccount = CloudStorageAccount.parse(theHub.azureConnectionUri)
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient()
        container = blobClient.getContainerReference('content-items')
        container.createIfNotExists()

        BlobContainerPermissions containerPermissions = new BlobContainerPermissions()
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER)
        container.uploadPermissions(containerPermissions)
    }


    def 'uploads a new image with title'() {

        given:
        deleteImageFromFileStore(FILE_NAME)
        db.contentItem.remove(filename: FILE_NAME)

        when:
        def postResponse =
                Unirest.post(adminAppRoot + '/content-items')
                        .basicAuth(theHub.username, theHub.password)
                        .field("file", getFile(FILE_NAME))
                        .field("metadata", someJsonMetadata())
                        .asJson()

        then: 'the item is created'
        postResponse.statusCode == 201
        def locationHeader = postResponse.headers.getFirst('Location')
        locationHeader.contains('/content-items/')

        and: 'the data record is created'
        def id = locationHeader.tokenize('/').last()
        def itemData = db.contentItem.findOne(_id: new ObjectId(id))

        with(itemData) {
            filename == FILE_NAME
            metadata.title == TITLE
            metadata.category == CATEGORY
            metadata.contentType == CONTENT_TYPE
        }

        and: 'the data record has a timestamp'
        def timestamp = Date.parse("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", itemData.timestamp)
        timestamp.compareTo(startDate) >= 0

        and: 'the data record contains the content uri'
        itemData.files.main == "${theHub.azurePublicUrlBase}/content-items/${FILE_NAME}"

        and: 'the content uri can be used to retrive the same file content'
        def imageResponse = Unirest.get(itemData.files.main).asString()
        imageResponse.status == 200
        mD5For(imageResponse.getRawBody()) == mD5For(getFileStream(FILE_NAME))

        cleanup:
        deleteImageFromFileStore(FILE_NAME)
        db.contentItem.remove(_id: new ObjectId(id))

    }

    def 'overwrites existing data record for same filename'() {

        given:
        putImageInFileStore(FILE_NAME)

        db.contentItem.insert(
                filename: "${FILE_NAME}",
                metadata: [
                        title      : "oldTitle",
                        category   : "oldCategory",
                        contentType: "oldContentType"
                ]
        )

        assert db.contentItem.find(filename: FILE_NAME).count() == 1

        when:
        Unirest.post(adminAppRoot + '/content-items')
                .basicAuth(theHub.username, theHub.password)
                .field("file", getFile(FILE_NAME))
                .field("metadata", someJsonMetadata())
                .asJson()

        then: 'there should only be one record with that filename'
        db.contentItem.find(filename: FILE_NAME).count() == 1

        and: 'the record should have the new metadata'
        def itemData = db.contentItem.findOne(filename: FILE_NAME)

        with(itemData) {
            filename == FILE_NAME
            metadata.title == TITLE
            metadata.category == CATEGORY
            metadata.contentType == CONTENT_TYPE
        }

        cleanup:
        deleteImageFromFileStore(FILE_NAME)
        db.contentItem.remove(filename: FILE_NAME)
    }

    def deleteImageFromFileStore(identifier) {
        CloudBlockBlob blob = container.getBlockBlobReference(identifier)
        blob.deleteIfExists()
    }

    def putImageInFileStore(identifier) {
        CloudBlockBlob blob = container.getBlockBlobReference(identifier)
        blob.upload(getFileStream(identifier), getFile(identifier).length())
    }

    def someJsonMetadata() {
        return """
        { 
            "title": "${TITLE}", 
            "category": "${CATEGORY}", 
            "contentType": "${CONTENT_TYPE}",
            "fileLabels": ["main"]
        }
        """
    }

    def getFileStream(filename) throws FileNotFoundException, URISyntaxException {
        return new FileInputStream(getFile(filename))
    }

    def getFile(String filename) throws URISyntaxException {
        return new File(this.getClass().getResource("/" + filename).toURI())
    }

    def mD5For(InputStream is) {
        MessageDigest md = MessageDigest.getInstance("MD5")
        DigestInputStream dis = new DigestInputStream(is, md)
        while (dis.read() != -1) {
        }
        dis.close()
        return md.digest()
    }

}
