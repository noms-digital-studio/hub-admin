package uk.gov.justice.digital.noms.hub.domain;

import java.util.Objects;

public class ContentItem {
    private final String title;

    public ContentItem(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentItem contentItem = (ContentItem) o;
        return Objects.equals(title, contentItem.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
