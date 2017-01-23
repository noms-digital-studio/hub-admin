package uk.gov.justice.digital.noms.hub.ports.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MediaRepository;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.io.IOException;

@RestController
public class AdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final MetadataRepository metadataRepository;
    private final MediaRepository mediaRepository;

    public AdminController(MetadataRepository metadataRepository, MediaRepository mediaRepository) {
        this.metadataRepository = metadataRepository;
        this.mediaRepository = mediaRepository;
    }

    @PostMapping("/content-items")
    public ResponseEntity saveFileAndMetadata(@RequestParam("file") MultipartFile file,
                                              @RequestParam("title") String title,
                                              UriComponentsBuilder uriComponentsBuilder) throws IOException {

        log.info("title: " + title);
        log.info("filename: " + file.getOriginalFilename());
        log.info("file size: " + file.getSize());

        String mediaUri = mediaRepository.save(file.getInputStream(), file.getOriginalFilename(), file.getSize());
        String id = metadataRepository.save(new ContentItem(title, mediaUri, file.getOriginalFilename()));

        return new ResponseEntity<Void>(createLocationHeader(uriComponentsBuilder, id), HttpStatus.CREATED);
    }

    private HttpHeaders createLocationHeader(UriComponentsBuilder uriComponentsBuilder, String id) {
        HttpHeaders headers = new HttpHeaders();
        UriComponents uriComponents = uriComponentsBuilder.path("/content-items/{id}").buildAndExpand(id);
        headers.setLocation(uriComponents.toUri());
        return headers;
    }
}
