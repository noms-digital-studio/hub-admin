package uk.gov.justice.digital.noms.hub;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;
import uk.gov.justice.digital.noms.hub.ports.http.AdminController;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(AdminController.class);
    }
}
