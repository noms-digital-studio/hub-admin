package uk.gov.justice.digital.noms.hub.domain;

import lombok.Data;

@Data
public class ContentItem {
    private final String title;
    private final String mediaUri;
    private final String filename;
    private String category;

    public ContentItem(String title, String mediaUri, String filename, String category) {
        this.title = title;
        this.mediaUri = mediaUri;
        this.filename = filename;
        this.category = category;
    }
}
