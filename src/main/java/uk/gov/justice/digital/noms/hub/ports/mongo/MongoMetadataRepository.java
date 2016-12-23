package uk.gov.justice.digital.noms.hub.ports.mongo;

import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.noms.hub.domain.Article;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.util.UUID;

@Repository
public class MongoMetadataRepository implements MetadataRepository {

    @Override
    public UUID saveArticle(Article article) {
        return UUID.randomUUID();
    }
}
