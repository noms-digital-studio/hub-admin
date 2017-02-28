package uk.gov.justice.digital.noms.hub.ports.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;

import java.util.Map;

@Data
public class ContentItemRequest {
    private final String id;
    private final Map<String, Object> files;
    private final String filename;
    private final String timestamp;
    private final Map<String, Object> metadata;

    @JsonCreator
    public ContentItemRequest() {
        id = null;
        files = null;
        filename = null;
        timestamp = null;
        metadata = null;
    }

    public ContentItemRequest(ContentItem source){
        this.id = source.getId();
        this.files = source.getFiles();
        this.filename = source.getFilename();
        this.timestamp = source.getTimestamp();
        this.metadata = source.getMetadata();
    }

    public ContentItem toContentItem(){
        return new ContentItem(id, files, filename, timestamp, metadata);
    }
}
