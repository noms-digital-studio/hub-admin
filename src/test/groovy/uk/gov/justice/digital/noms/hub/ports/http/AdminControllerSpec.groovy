package uk.gov.justice.digital.noms.hub.ports.http

import org.apache.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification
import uk.gov.justice.digital.noms.hub.domain.ContentItem
import uk.gov.justice.digital.noms.hub.domain.MediaRepository
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository

class AdminControllerSpec extends Specification {

    private static final String TITLE = "aTitle"
    private static final String FILENAME = "aFilename"
    private static final String TIMESTAMP = "2017-02-13T11:46:14.154Z"
    private static final String CATEGORY = "aCategory"
    private static final String MEDIA_TYPE = "aMediaType"

    private AdminController adminController
    private MediaStore mediaStore

    def file = Mock(MultipartFile)
    def file2 = Mock(MultipartFile)
    def mediaRepository = Mock(MediaRepository)
    def metadataRepository = Mock(MetadataRepository)

    def setup() {
        mediaStore = new MediaStore(mediaRepository)
        adminController = new AdminController(metadataRepository, mediaStore)
    }

    def 'saveFileAndMetadata saves the file and its data then returns a location header'() {
        given:
        def files = [file] as MultipartFile[]
        def fileLabels = """["main"]"""

        def id = UUID.randomUUID().toString()
        metadataRepository.save(_) >> id

        when:
        ResponseEntity responseEntity =
                adminController.saveFileAndMetadata(files, someJsonMetadata(fileLabels), UriComponentsBuilder.newInstance())

        then:
        responseEntity.getStatusCodeValue() == HttpStatus.SC_CREATED
        responseEntity.getHeaders().Location.first() == "/content-items/" + id
    }

    def 'uses name of first file as primary identifier'() {
        given:
        file.name >> 'file1'
        file2.name >> 'file2'
        def files = [file, file2] as MultipartFile[]
        def fileLabels = """["main", "thumbnail"]"""

        when:
        adminController.saveFileAndMetadata(files, someJsonMetadata(fileLabels), UriComponentsBuilder.newInstance())

        then:
        metadataRepository.save(_ as ContentItem) >> { contentItem ->
            contentItem.filename == 'file1'
        }
    }

    def 'saveFileAndMetadata throws RuntimeException when the JSON metadata is malformed'() {
        when:
        adminController.saveFileAndMetadata([file] as MultipartFile[], 'not a json string', UriComponentsBuilder.newInstance())

        then:
        thrown RuntimeException
    }

    def 'all content items are returned by findAll'() {
        given:
        def expectedContentItems = someContentItems()
        metadataRepository.findAll("{ 'metadata.mediaType': 'application/pdf' }") >> expectedContentItems

        when:
        ContentItemsResponse response = adminController.findAll(null)

        then:
        response.contentItems == expectedContentItems
    }

    def someContentItems() {
        [aContentItem(), aContentItem()]
    }

    def aContentItem() {
        new ContentItem(UUID.randomUUID().toString(), ['main': "aUri"], FILENAME, TIMESTAMP, someMetadata())
    }

    def someJsonMetadata(fileLabels) {
        """
        { 
            "title": "${TITLE}", 
            "category": "${CATEGORY}", 
            "mediaType": "${MEDIA_TYPE}",
            "fileLabels": ${fileLabels}
        }
        """
    }

    def someMetadata() {
        [title: TITLE, category: CATEGORY, mediaType: MEDIA_TYPE]
    }

}
