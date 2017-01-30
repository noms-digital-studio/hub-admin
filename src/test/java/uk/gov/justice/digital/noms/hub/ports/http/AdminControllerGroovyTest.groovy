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

class AdminControllerGroovyTest extends Specification {
    private static final String TITLE = "aTitle"
    private static final String FILENAME = "aFilename"
    private static final String CATEGORY = "aCategory"

    private AdminController adminController

    def file = Mock(MultipartFile)
    def mediaRepository = Mock(MediaRepository)
    def metadataRepository = Mock(MetadataRepository)

    def setup() {
        adminController = new AdminController(metadataRepository, mediaRepository)
    }

    def 'content item is saved with its metadata and a location header is returned'() {
        given:
        def uri = aMediaRepositoryThatReturnsAUri()
        def id = aMetadataRepositoryThatReturnsAnId(uri)

        when:
        ResponseEntity responseEntity =
                adminController.saveFileAndMetadata(file, TITLE, CATEGORY, UriComponentsBuilder.newInstance())

        then:
        responseEntity.getStatusCodeValue() == HttpStatus.SC_CREATED
        responseEntity.getHeaders().Location.first() == "/content-items/" + id
    }

    def 'all content item metadata is returned'() {
        given:
        def expectedContentItems = someContentItems()
        metadataRepository.findAll() >> expectedContentItems

        when:
        ContentItemsResponse response = adminController.findAll()

        then:
        response.getContentItems() == expectedContentItems
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
        metadataRepository.save(new ContentItem(TITLE, uri, FILENAME, CATEGORY)) >> id
        id
    }

    def someContentItems() {
        [aContentItem(), aContentItem()]
    }

    def aContentItem() {
        new ContentItem(UUID.randomUUID().toString(), "aTitle", "aUri", "aFilename", "aCategory")
    }

}
