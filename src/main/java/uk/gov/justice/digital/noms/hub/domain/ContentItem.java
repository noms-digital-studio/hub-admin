package uk.gov.justice.digital.noms.hub.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ContentItem {
    private final String id;
    private final Map<String, Object> files;
    private final String filename;
    private final String timestamp;
    private final Map<String, Object> metadata;

    public ContentItem(Map<String, Object> files, String filename, Map<String, Object> metadata) {
        this(null, files, filename, null, metadata);
    }

    public ContentItem(String id, Map<String, Object> files, String filename, String timestamp, Map<String, Object> metadata) {
        this.id = id;
        this.files = files;
        this.filename = filename;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
}
