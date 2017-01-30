package uk.gov.justice.digital.noms.hub.ports.http;

import com.google.common.collect.ImmutableList;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MediaRepository;
import uk.gov.justice.digital.noms.hub.ports.mongo.MongoMetadataRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {
    private static final String TITLE = "aTitle";
    private static final String FILENAME = "aFilename";
    private static final String CATEGORY = "aCategory";
    private AdminController adminController;

    @Mock private MediaRepository mediaRepository;
    @Mock private MongoMetadataRepository mongoMetadataRepository;
    @Mock private MultipartFile file;

    @Before
    public void setupClassUnderTest() {
        adminController = new AdminController(mongoMetadataRepository, mediaRepository);
    }

    @Test
    public void createsContentItemAndReturnsTheLocation() throws IOException {
        // given
        String uri = aMediaRepositoryThatReturnsAUri();
        String id = aMetadataRepositoryThatReturnsAnId(uri);

        // when
        ResponseEntity responseEntity =
                adminController.saveFileAndMetadata(file, TITLE, CATEGORY, UriComponentsBuilder.newInstance());

        // then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.SC_CREATED);
        assertThat(responseEntity.getHeaders().get("Location").get(0)).isEqualTo("/content-items/" + id);
    }

    @Test
    public void getsTheListOfContentMetadata() {
        // given
        ContentItemsResponse expectedContentItems = someContentItems();
        aMetadataRepositoryThatReturnsAListOfItems(expectedContentItems);

        // when
        ContentItemsResponse contentItems = adminController.findAll();

        // then
        assertThat(contentItems).isEqualTo(expectedContentItems);
    }

    private void aMetadataRepositoryThatReturnsAListOfItems(ContentItemsResponse contentItemsResponse) {
        when(mongoMetadataRepository.findAll()).thenReturn(contentItemsResponse.getContentItems());
    }

    private ContentItemsResponse someContentItems() {
        return new ContentItemsResponse(ImmutableList.of(aContentItem(), aContentItem()));
    }

    private ContentItem aContentItem() {
        return new ContentItem(UUID.randomUUID().toString(), "aTitle", "aUri", "aFilename", "aCategory");
    }

    private String aMediaRepositoryThatReturnsAUri() throws IOException {
        InputStream io = StreamUtils.emptyInput();
        when(file.getInputStream()).thenReturn(io);
        when(file.getSize()).thenReturn(0L);
        String uri = "aUri";
        when(mediaRepository.save(io, FILENAME, 0L)).thenReturn(uri);
        return uri;
    }

    private String aMetadataRepositoryThatReturnsAnId(String uri) {
        when(file.getOriginalFilename()).thenReturn(FILENAME);
        String id = UUID.randomUUID().toString();
        when(mongoMetadataRepository.save(new ContentItem(TITLE, uri, FILENAME, CATEGORY))).thenReturn(id);
        return id;
    }
}
