/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.api;

import fr.cnrs.opentheso.core.json.helper.JsonHelper;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
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

    /////// TEST TEST TEST /////////////       

    @Path("/testput")
    @PUT
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public String addArk(String content) {
        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jo;
        try {
            jo = jsonHelper.getJsonObject(content);
            return jo.toString();
        } catch (Exception e) {
            return "Erreur";
        }

    }

}
