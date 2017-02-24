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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class AdminController {

    private final MetadataRepository metadataRepository;
    private final MediaStore mediaStore;

    public AdminController(MetadataRepository metadataRepository, MediaStore mediaStore) {
        this.metadataRepository = metadataRepository;
        this.mediaStore = mediaStore;
    }

    @PostMapping("/content-items")
    public ResponseEntity saveFileAndMetadata(@RequestParam("file") MultipartFile[] files,
                                              @RequestParam("metadata") String metadata,
                                              UriComponentsBuilder uriComponentsBuilder) throws IOException {

        Map<String, Object> verifiedMetadata = parseMetadata(metadata);
        Map<String, Object> fileList = mediaStore.storeFiles(fileSpecsFor(files), verifiedMetadata);

        String contentItemIdentifier = files[0].getOriginalFilename();

        String id = metadataRepository.save(new ContentItem(fileList, contentItemIdentifier, verifiedMetadata));
        log.info("Saved content item with id: {}", id);

        return new ResponseEntity<Void>(createLocationHeader(uriComponentsBuilder, id), HttpStatus.CREATED);
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
