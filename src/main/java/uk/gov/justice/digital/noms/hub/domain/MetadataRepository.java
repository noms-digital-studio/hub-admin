package uk.gov.justice.digital.noms.hub.domain;

import java.util.UUID;

public interface MetadataRepository {
    UUID saveArticle(Article article);
}