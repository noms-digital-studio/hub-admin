package uk.gov.justice.digital.noms.hub.domain;

import java.util.Objects;

public class ContentItem {
    private final String title;
    private final String mediaUri;

    public ContentItem(String title, String mediaUri) {
        this.title = title;
        this.mediaUri = mediaUri;
    }

    public String getTitle() {
        return title;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentItem that = (ContentItem) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(mediaUri, that.mediaUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, mediaUri);
    }
}
