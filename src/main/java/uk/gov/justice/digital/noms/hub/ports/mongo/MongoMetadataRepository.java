package uk.gov.justice.digital.noms.hub.ports.mongo;

import org.springframework.stereotype.Repository;
import uk.gov.justice.digital.noms.hub.domain.ContentItem;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import java.util.UUID;

@Repository
public class MongoMetadataRepository implements MetadataRepository {

    @Override
    public UUID save(ContentItem contentItem) {
        return UUID.randomUUID();
    }
}
