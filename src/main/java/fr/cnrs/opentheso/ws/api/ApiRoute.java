
package fr.cnrs.opentheso.ws.api;

import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author miledrousset
 */

@ApplicationPath("/api")
public class ApiRoute extends ResourceConfig {
    public ApiRoute() {
        packages("fr.cnrs.opentheso.ws.api");
    }
}
