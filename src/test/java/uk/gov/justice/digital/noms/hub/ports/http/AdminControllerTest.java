package uk.gov.justice.digital.noms.hub.ports.http;

import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {

    @Mock private MetadataRepository metadataRepository;
    @Mock private UriInfo uriInfo;
    @Mock private UriBuilder uriBuilder;

    @Test
    public void savesTheArticleWithGivenTitle() throws Exception {
        // given
        AdminController adminController = new AdminController(metadataRepository);
        UUID uuid = UUID.randomUUID();
        CreateArticleRequest createArticleRequest = new CreateArticleRequest("aTitle");
        when(metadataRepository.saveArticle(createArticleRequest.buildArticle())).thenReturn(uuid);
        buildUriContext();

        // when
        Response responseEntity = adminController.saveArticle(createArticleRequest, uriInfo);

        // then
        assertThat(responseEntity.getStatus()).isEqualTo(201);
        assertThat(responseEntity.getLocation().toString()).isEqualTo(uuid.toString());
    }

    private void buildUriContext() {
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(new JerseyUriBuilder());
    }

}