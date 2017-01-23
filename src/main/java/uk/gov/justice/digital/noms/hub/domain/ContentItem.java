package uk.gov.justice.digital.noms.hub.domain;

import lombok.Data;

@Data
public class ContentItem {
    private final String title;
    private final String mediaUri;
    private final String filename;

    public ContentItem(String title, String mediaUri, String filename) {
        this.title = title;
        this.mediaUri = mediaUri;
        this.filename = filename;
    }
}
