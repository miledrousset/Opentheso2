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

/**
 * REST Web Service
 *
 * @author miled.rousset
 */
//general path = /api
@Path("/concept")

public class SelectConcept {

    /**
     * Creates a new instance of resources La connexion est faite à chaque
     * question
     *
     */
    public SelectConcept() {
    }

    private HikariDataSource connect() {
        ConnexionRest connexionRest = new ConnexionRest();
        return connexionRest.getConnexion();
    }

    /**
     * Cette fonction permet de se replacer sur un concept dans le thésaurus 
     *
     * @param idTheso
     * @param idConcept
     * @param uriInfo
     * @return 
     */
    @Path("{idTheso}.{idConcept}")
    @GET
    @Produces("application/xml;charset=UTF-8")
    public Response getConcept(@PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept, 
            @Context UriInfo uriInfo) {

        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            ds.close();
            String path = uriInfo.getBaseUriBuilder().toString().replaceAll("/api/", "/");
            if (idTheso == null || idConcept == null) return null;
            if (idTheso.isEmpty() || idConcept.isEmpty()) return null;
            
            path = path + "?idc=" + idConcept +"&"+ "idt=" + idTheso;
            try {
                URI uri = new URI(path);//uriInfo.getBaseUriBuilder().path("bar").build();
                return Response.temporaryRedirect(uri).build();
            } catch (URISyntaxException ex) {
                Logger.getLogger(SelectConcept.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
