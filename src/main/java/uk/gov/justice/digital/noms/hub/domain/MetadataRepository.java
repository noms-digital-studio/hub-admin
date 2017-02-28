package uk.gov.justice.digital.noms.hub.domain;

import java.util.List;
import java.util.Optional;

public interface MetadataRepository {
    String save(ContentItem contentItem);
    List<ContentItem> findAll(String filter);
    Optional<ContentItem> findById(String id);
}
