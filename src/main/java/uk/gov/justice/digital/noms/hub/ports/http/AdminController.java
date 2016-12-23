package uk.gov.justice.digital.noms.hub.ports.http;

import org.springframework.stereotype.Component;
import uk.gov.justice.digital.noms.hub.domain.MetadataRepository;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.UUID;

@Component
@Path("/articles")
@Produces(MediaType.APPLICATION_JSON)
public class AdminController {
    private MetadataRepository metadataRepository;

    public AdminController(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @POST
    public Response saveArticle(CreateArticleRequest createArticleRequest, @Context UriInfo uriInfo) {
        UUID uuid = metadataRepository.saveArticle(createArticleRequest.buildArticle());

        URI location = uriInfo.getAbsolutePathBuilder()
                .path("{id}")
                .resolveTemplate("id", uuid)
                .build();

        return Response.created(location).build();
    }
}
