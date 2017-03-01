package uk.gov.justice.digital.noms.hub.ports.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.FileSpec;
import uk.gov.justice.digital.noms.hub.domain.MediaStore;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
public class AdminController {

    private final MetadataRepository metadataRepository;
    private final MediaStore mediaStore;

    public AdminController(MetadataRepository metadataRepository, MediaStore mediaStore) {
        this.metadataRepository = metadataRepository;
        this.mediaStore = mediaStore;
    }

    @GetMapping("/content-items")
    public
    @ResponseBody
    ContentItemsResponse findAll(@RequestParam(value = "filter", required = false) String filter) {
        if (filter == null || filter.isEmpty()) {
            filter = "{ 'metadata.mediaType': 'application/pdf' }";
        }

        return new ContentItemsResponse(metadataRepository.findAll(filter));
    }

    @GetMapping("/content-items/{id}")
    public ResponseEntity findById(@PathVariable String id) {

        Optional<ContentItem> item = metadataRepository.findById(id);

        if (item.isPresent()) {
            return new ResponseEntity<>(item.get(), HttpStatus.CREATED);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/content-items")
    public ResponseEntity saveFileAndMetadata(@RequestParam("file") MultipartFile[] files,
                                              @RequestParam("metadata") String metadata,
                                              UriComponentsBuilder uriComponentsBuilder) throws IOException {

        Map<String, Object> verifiedMetadata = parseMetadata(metadata);
        Map<String, Object> fileList = mediaStore.storeFiles(fileSpecsFor(files), verifiedMetadata);

        ContentItem contentItem = new ContentItem(fileList, files[0].getOriginalFilename(), verifiedMetadata);
        return saveOrUpdate(uriComponentsBuilder, contentItem);
    }


    @PutMapping("/content-items/{id}")
    public ResponseEntity updateById(@PathVariable String id,
                                     @RequestBody ContentItemRequest contentItemRequest,
                                     UriComponentsBuilder uriComponentsBuilder) {

        if (id.equals(contentItemRequest.getId())) {
            return saveOrUpdate(uriComponentsBuilder, contentItemRequest.toContentItem());
        }

        return ResponseEntity.badRequest().build();
    }

    private ResponseEntity saveOrUpdate(UriComponentsBuilder uriComponentsBuilder, ContentItem item) {
        String id = metadataRepository.save(item);
        log.info("Saved content item with id: {}", id);
        return creationResponse(uriComponentsBuilder, id);
    }

    private ResponseEntity creationResponse(UriComponentsBuilder uriComponentsBuilder, String id) {
        return new ResponseEntity<Void>(createLocationHeader(uriComponentsBuilder, id), HttpStatus.CREATED);
    }

    private HttpHeaders createLocationHeader(UriComponentsBuilder uriComponentsBuilder, String id) {
        HttpHeaders headers = new HttpHeaders();
        UriComponents uriComponents = uriComponentsBuilder.path("/content-items/{id}").buildAndExpand(id);
        headers.setLocation(uriComponents.toUri());
        return headers;
    }

    private Map<String, Object> parseMetadata(String metadata) {
        log.info("metadata: {}", metadata);

        ObjectMapper objectMapper = new ObjectMapper();
        MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);

        try {
            return objectMapper.readValue(metadata, mapType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<FileSpec> fileSpecsFor(MultipartFile[] files) {
        List<FileSpec> fileSpecs = new ArrayList<>();
        for (MultipartFile file : files) {
            fileSpecs.add(fileSpecFor(file));
        }
        return fileSpecs;
    }

    private FileSpec fileSpecFor(MultipartFile file) {
        try {
            return new FileSpec(file.getInputStream(), file.getOriginalFilename(), file.getSize());
        } catch (IOException e) {
            log.error("Exception during file store:", e);
            throw new RuntimeException("Failed to store files: " + e.getMessage());
        }
    }

}
