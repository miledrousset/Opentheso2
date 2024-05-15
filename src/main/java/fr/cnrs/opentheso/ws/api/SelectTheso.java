/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.api;

import com.zaxxer.hikari.HikariDataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;

/**
 * REST Web Service
 *
 * @author miled.rousset
 */
//general path = /api
@Path("/theso")

public class SelectTheso {

    /**
     * Creates a new instance of resources La connexion est faite à chaque
     * question
     *
     */
    public SelectTheso() {
    }

    private HikariDataSource connect() {
        ConnexionRest connexionRest = new ConnexionRest();
        return connexionRest.getConnexion();
    }

    /**
     * Cette fonction permet de se diriger vers le bon thésaurus en passant par
     * son nom VIA REST ceci permet de gérer les noms de domaines et filtrer les
     * thésaurus dans un parc important
     *
     * @param name
     * @param uriInfo
     * @return 
     */
    @Path("{theso}")
    @GET
    @Produces("application/xml;charset=UTF-8")
    public Response getThesoUri(@PathParam("theso") String name, @Context UriInfo uriInfo) {
        String idTheso;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            PreferencesHelper preferencesHelper = new PreferencesHelper();
            idTheso = preferencesHelper.getIdThesaurusFromName(ds, name);
            ds.close();
            String path = uriInfo.getBaseUriBuilder().toString().replaceAll("/api/", "/");
            if (idTheso == null) return null;
            if (idTheso.isEmpty()) return null;            

            path = path + "?idt=" + idTheso;
            try {
                URI uri = new URI(path);//uriInfo.getBaseUriBuilder().path("bar").build();
                return Response.temporaryRedirect(uri).build();
            } catch (URISyntaxException ex) {
                Logger.getLogger(SelectTheso.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
