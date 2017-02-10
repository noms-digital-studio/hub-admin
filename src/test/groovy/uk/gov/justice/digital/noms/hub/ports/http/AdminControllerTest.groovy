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

class AdminControllerTest extends Specification {
    private static final String TITLE = "aTitle"
    private static final String FILENAME = "aFilename"
    private static final String CATEGORY = "aCategory"
    private static final String MEDIA_TYPE = "aMediaType"

    private AdminController adminController

    def file = Mock(MultipartFile)
    def mediaRepository = Mock(MediaRepository)
    def metadataRepository = Mock(MetadataRepository)

    def setup() {
        adminController = new AdminController(metadataRepository, mediaRepository)
    }

    def 'saveFileAndMetadata saves the file and its data then returns a location header'() {
        given:
        def uri = aMediaRepositoryThatReturnsAUri()
        def id = aMetadataRepositoryThatReturnsAnId(uri)

        when:
        ResponseEntity responseEntity =
                adminController.saveFileAndMetadata(file, someJsonMetadata(), UriComponentsBuilder.newInstance())

        then:
        responseEntity.getStatusCodeValue() == HttpStatus.SC_CREATED
        responseEntity.getHeaders().Location.first() == "/content-items/" + id
    }

    def 'saveFileAndMetadata throws RuntimeException when the JSON metadata is malformed'() {
        when:
        adminController.saveFileAndMetadata(file, 'not a json string', UriComponentsBuilder.newInstance())

        then:
        thrown(RuntimeException)
    }

    def 'all content items are returned by findAll'() {
        given:
        def expectedContentItems = someContentItems()
        metadataRepository.findAll("{ 'metadata.mediaType': 'application/pdf'}") >> expectedContentItems

        when:
        ContentItemsResponse response = adminController.findAll(null)

        then:
        response.contentItems == expectedContentItems
    }

    def aMediaRepositoryThatReturnsAUri() throws IOException {
        InputStream io = StreamUtils.emptyInput()
        file.getInputStream() >> io
        file.getSize() >> 0L
        String uri = "aUri"
        mediaRepository.save(io, FILENAME, 0L) >> uri
        uri
    }

    def aMetadataRepositoryThatReturnsAnId(String uri) {
        file.getOriginalFilename() >> FILENAME
        String id = UUID.randomUUID().toString()
        metadataRepository.save(new ContentItem(uri, FILENAME, someMetadata())) >> id
        id
    }

    def someContentItems() {
        [aContentItem(), aContentItem()]
    }

    def aContentItem() {
        new ContentItem(UUID.randomUUID().toString(), "aUri", someMetadata())
    }

    def someJsonMetadata() {
        """
        { 
            "title": "${TITLE}", 
            "category": "${CATEGORY}", 
            "mediaType": "${MEDIA_TYPE}" 
        }
        """
    }

    def someMetadata() {
        [title: TITLE, category: CATEGORY, mediaType: MEDIA_TYPE]
    }

}
