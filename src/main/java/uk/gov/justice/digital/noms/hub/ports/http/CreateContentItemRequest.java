package uk.gov.justice.digital.noms.hub.ports.http;

import uk.gov.justice.digital.noms.hub.domain.ContentItem;

public class CreateContentItemRequest {
    private String title;

    public String getTitle() {
        return title;
    }

    ContentItem buildContentItem() {
        return new ContentItem(this.title);
    }
}
