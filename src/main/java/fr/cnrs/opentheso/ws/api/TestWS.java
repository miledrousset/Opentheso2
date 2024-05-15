/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author miledrousset
 */

@Path("/bonjour")
public class TestWS {
    
    
    /////// TEST TEST TEST /////////////
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String direBonjour() {
        return "Bonjour, tout le monde!";
    }    
    
    @Path("{idTheso}.{idConcept}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getConcept(@PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {
        
        String datas = idTheso + " / " + idConcept;
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();        
    }    
    /////// TEST TEST TEST /////////////       
    
    
}
