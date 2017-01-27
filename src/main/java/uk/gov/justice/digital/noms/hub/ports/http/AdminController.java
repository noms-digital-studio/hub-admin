package uk.gov.justice.digital.noms.hub.ports.http;

import lombok.extern.slf4j.Slf4j;
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
import uk.gov.justice.digital.noms.hub.domain.MediaRepository;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.io.IOException;

@Slf4j
@RestController
public class AdminController {

    private final MetadataRepository metadataRepository;
    private final MediaRepository mediaRepository;

    public AdminController(MetadataRepository metadataRepository, MediaRepository mediaRepository) {
        this.metadataRepository = metadataRepository;
        this.mediaRepository = mediaRepository;
    }

    @PostMapping("/content-items")
    public ResponseEntity saveFileAndMetadata(@RequestParam("file") MultipartFile file,
                                              @RequestParam("title") String title,
                                              @RequestParam("category") String category,
                                              UriComponentsBuilder uriComponentsBuilder) throws IOException {

        logParameters(file, title, category);

        String mediaUri = mediaRepository.save(file.getInputStream(), file.getOriginalFilename(), file.getSize());
        String id = metadataRepository.save(new ContentItem(title, mediaUri, file.getOriginalFilename(), category));

        return new ResponseEntity<Void>(createLocationHeader(uriComponentsBuilder, id), HttpStatus.CREATED);
    }

    private void logParameters(@RequestParam("file") MultipartFile file, @RequestParam("title") String title, @RequestParam("category") String category) {
        log.info("title: " + title);
        log.info("category: " + category);
        log.info("filename: " + file.getOriginalFilename());
        log.info("file size: " + file.getSize());
    }

    private HttpHeaders createLocationHeader(UriComponentsBuilder uriComponentsBuilder, String id) {
        HttpHeaders headers = new HttpHeaders();
        UriComponents uriComponents = uriComponentsBuilder.path("/content-items/{id}").buildAndExpand(id);
        headers.setLocation(uriComponents.toUri());
        return headers;
    }
}
