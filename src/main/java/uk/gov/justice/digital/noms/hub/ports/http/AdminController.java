package uk.gov.justice.digital.noms.hub.ports.http;

import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.UUID;

public class AdminController {
    MetadataRepository metadataRepository;

    public AdminController(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    public Response saveArticle(CreateArticleRequest createArticleRequest, @Context UriInfo uriInfo) {
        UUID uuid = metadataRepository.saveArticle(createArticleRequest.buildArticle());

        URI location = uriInfo.getAbsolutePathBuilder()
                .path("{id}")
                .resolveTemplate("id", uuid)
                .build();

        return Response.created(location).build();
    }
}
