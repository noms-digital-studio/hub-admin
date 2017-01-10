package uk.gov.justice.digital.noms.hub.ports.http;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.digital.noms.hub.ports.mongo.MongoMetadataRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdminControllerTest {

    @Mock MongoMetadataRepository mongoMetadataRepository;
    @Mock MultipartFile file;

    @Test
    public void createsContentItemAndReturnsTheLocation() {
        // given
        when(file.getOriginalFilename()).thenReturn("aFilename");
        UUID id = UUID.randomUUID();
        when(mongoMetadataRepository.save(any())).thenReturn(id);
        AdminController adminController = new AdminController(mongoMetadataRepository);

        // when
        ResponseEntity responseEntity = adminController.saveFileAndMetadata(file, "title", UriComponentsBuilder.newInstance());

        // then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(HttpStatus.SC_CREATED);
        assertThat(responseEntity.getHeaders().get("Location").get(0)).isEqualTo("/content-items/" + id.toString());
    }
}
