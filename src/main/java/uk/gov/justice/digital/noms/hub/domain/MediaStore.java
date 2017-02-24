package uk.gov.justice.digital.noms.hub.domain;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class MediaStore {

    private final MediaRepository mediaRepository;

    public MediaStore(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    public Map<String, Object> storeFiles(List<FileSpec> files, Map<String, Object> verifiedMetadata) {
        return save(files, fileLabelsFrom(verifiedMetadata));
    }

    @SuppressWarnings("unchecked")
    private List<String> fileLabelsFrom(Map<String, Object> verifiedMetadata) {
        if (verifiedMetadata.containsKey("fileLabels")) {
            return (List) verifiedMetadata.remove("fileLabels");
        }
        return Collections.emptyList();
    }

    private Map<String, Object> save(List<FileSpec> files, List<String> fileLabels) {

        if (files.size() != fileLabels.size()) {
            log.error("Mismatched files: {} and file labels: {}", files.size(), fileLabels.size());
            throw new RuntimeException("Mismatched files and file labels");
        }

        if (files.size() < 1) {
            log.error("No files received");
            throw new RuntimeException("No files received");
        }

        List<String> fileUris = store(files);

        return buildFileList(fileLabels, fileUris);
    }

    private List<String> store(List<FileSpec> files) {

        List<String> fileUris = new ArrayList<>();

        for (FileSpec fileSpec : files) {
            logFileParameters(fileSpec);
            String id = mediaRepository.save(fileSpec.getInputStream(), fileSpec.getFilename(), fileSpec.getSize());
            fileUris.add(id);
        }

        return fileUris;
    }

    private void logFileParameters(FileSpec file) {
        log.debug("filename: " + file.getFilename());
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
