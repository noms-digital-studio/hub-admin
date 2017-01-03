package uk.gov.justice.digital.noms.hub.ports.http;

import org.glassfish.jersey.uri.internal.JerseyUriBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {
    @Mock private MetadataRepository metadataRepository;
    @Mock private UriInfo uriInfo;

    @Test
    public void savesTheContentItemWithGivenTitle() throws Exception {
        // given
        AdminController adminController = new AdminController(metadataRepository);
        UUID uuid = UUID.randomUUID();
        CreateContentItemRequest createContentItemRequest = new CreateContentItemRequest();
        when(metadataRepository.save(createContentItemRequest.buildContentItem())).thenReturn(uuid);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(new JerseyUriBuilder());

        // when
        Response responseEntity = adminController.save(createContentItemRequest, uriInfo);

        // then
        assertThat(responseEntity.getStatus()).isEqualTo(201);
        assertThat(responseEntity.getLocation().toString()).isEqualTo(uuid.toString());
    }

}