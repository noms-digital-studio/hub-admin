package uk.gov.justice.digital.noms.hub.domain;

import lombok.Data;

@Data
public class ContentItem {
    private final String id;
    private final String title;
    private final String mediaUri;
    private final String filename;
    private final String category;

    public ContentItem(String title, String mediaUri, String filename, String category) {
        this(null, title, mediaUri, filename, category);
    }

    public ContentItem(String id, String title, String mediaUri, String filename, String category) {
        this.id = id;
        this.title = title;
        this.mediaUri = mediaUri;
        this.filename = filename;
        this.category = category;
    }
}
