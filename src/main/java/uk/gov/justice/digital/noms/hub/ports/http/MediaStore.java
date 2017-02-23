package uk.gov.justice.digital.noms.hub.ports.http;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.digital.noms.hub.domain.MediaRepository;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class MediaStore {

    private final MediaRepository mediaRepository;

    public MediaStore(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public Map<String, Object> storeFiles(MultipartFile[] files, Map<String, Object> verifiedMetadata) {
        List<String> fileLabels = getFileLabels(verifiedMetadata);

        try {
            return save(files, fileLabels);
        } catch (IOException e) {
            log.error("Exception during file store:", e);
            throw new RuntimeException("Failed to store files: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getFileLabels(Map<String, Object> verifiedMetadata) {
        if (verifiedMetadata.containsKey("fileLabels")) {
            return (List) verifiedMetadata.remove("fileLabels");
        }
        return Collections.emptyList();
    }

    private Map<String, Object> save(MultipartFile[] files, List<String> fileLabels) throws IOException {

        if (files.length != fileLabels.size()) {
            log.error("Mismatched files: {} and file labels: {}", files.length, fileLabels.size());
            throw new RuntimeException("Mismatched files and file labels");
        }

        if (files.length < 1) {
            log.error("No files received");
            throw new RuntimeException("No files received");
        }


        List<String> fileUris = store(files);

        return buildFileList(fileLabels, fileUris);
    }

    private List<String> store(MultipartFile[] files) throws IOException {

        List<String> fileUris = new ArrayList<>();

        for (MultipartFile file : files) {
            logFileParameters(file);
            String id = mediaRepository.save(file.getInputStream(), file.getOriginalFilename(), file.getSize());
            fileUris.add(id);
        }

        return fileUris;
    }

    private void logFileParameters(MultipartFile file) {
        log.debug("filename: " + file.getOriginalFilename());
        log.debug("file size: " + file.getSize());
    }

    private Map<String, Object> buildFileList(List<String> fileLabels, List<String> fileUris) {
        Map<String, Object> fileList = new HashMap<>();

        for (int i = 0; i < fileLabels.size(); i++) {
            fileList.put(fileLabels.get(i), fileUris.get(i));
        }
        logFileList(fileList);

        return fileList;
    }

    private void logFileList(Map<String, Object> fileList) {
        for (Map.Entry<String, Object> e : fileList.entrySet()) {
            log.debug("File list entry: '{}':'{}'", e.getKey(), e.getValue());
        }
    }
}
