package uk.gov.justice.digital.noms.hub.domain;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ContentItem {
    private final String id;
    private final String mediaUri;
    private final String filename;
    private final String timestamp;
    private final Map<String, Object> metadata;

    public ContentItem(String mediaUri, String filename, Map<String, Object> metadata) {
        this(null, mediaUri, filename, null, metadata);
    }

    public ContentItem(String id, String mediaUri, String filename, String timestamp, Map<String, Object> metadata) {
        this.id = id;
        this.mediaUri = mediaUri;
        this.filename = filename;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }
}
