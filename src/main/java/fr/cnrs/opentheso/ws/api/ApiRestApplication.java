package fr.cnrs.opentheso.ws.api;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;


@ApplicationPath("/api")
public class ApiRestApplication extends ResourceConfig {
    public ApiRestApplication() {
        packages("fr.cnrs.opentheso.ws.api");
    }
}
