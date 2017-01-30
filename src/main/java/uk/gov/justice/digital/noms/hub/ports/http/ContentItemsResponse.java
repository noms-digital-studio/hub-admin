package uk.gov.justice.digital.noms.hub.ports.http;

import lombok.Data;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;

import java.util.List;

@Data
public class ContentItemsResponse {
    private List<ContentItem> contentItems;

    public ContentItemsResponse(List<ContentItem> contentItems) {
        this.contentItems = contentItems;
    }
}
