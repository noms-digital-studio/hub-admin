package uk.gov.justice.digital.noms.hub.domain;

import java.util.List;

public interface MetadataRepository {
    String save(ContentItem contentItem);
    List<ContentItem> findAll();
}
