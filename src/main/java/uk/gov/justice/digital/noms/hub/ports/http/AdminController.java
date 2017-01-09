package uk.gov.justice.digital.noms.hub.ports.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.util.UUID;

@RestController
public class AdminController {
    private MetadataRepository metadataRepository;

    public AdminController(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @PostMapping("/content-items")
    public ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file,
                                           @RequestParam("title") String title,
                                           UriComponentsBuilder uriComponentsBuilder) {

        UUID id = metadataRepository.save(new ContentItem(title));
        UriComponents uriComponents = uriComponentsBuilder.path("/content-items/{id}").buildAndExpand(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }
}
