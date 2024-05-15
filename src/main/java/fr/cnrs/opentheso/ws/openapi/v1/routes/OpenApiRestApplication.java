package fr.cnrs.opentheso.ws.openapi.v1.routes;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;


@ApplicationPath("/openapi/v1")
public class OpenApiRestApplication extends ResourceConfig {
    public OpenApiRestApplication() {
        packages("fr.cnrs.opentheso.ws.openapi.v1.routes");
    }
}
