/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.api;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

/**
 * REST Web Service
 *
 * @author miled.rousset
 */

//general path = /api

@Path("/group")
public class RestGroup {
    /**
     * Creates a new instance of resources La connexion est faite à chaque
     * question
     *
     */
    public RestGroup() {
    }
    
    private HikariDataSource connect() {
        ConnexionRest connexionRest = new ConnexionRest();
        return connexionRest.getConnexion();
    }

  

    /**
     * Permet de lire les préférences d'un thésaurus pour savoir si le
     * webservices est activé ou non
     *
     * @param idTheso
     */
    private boolean getStatusOfWebservices(HikariDataSource ds , String idTheso) {
        return new PreferencesHelper().isWebservicesOn(ds, idTheso);
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * recherche par Id Ark
     * Partie pour la négociation de contenu 
     * concernant les URI de type ARK avec header 
     * curl -L --header "Accept: application/rdf+xml »
     * curl -L --header "Accept: text/turtle »
     * curl -L --header "Accept: application/json »
     * curl -L --header "Accept: application/ld+json »
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////    

    /**
     *  pour produire du RDF-SKOS
     * @param naan
     * @param arkId
     * @return 
     * #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromArk__(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {
        HikariDataSource ds = connect();
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        if(naan == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(naan.isEmpty()){
            ds.close();            
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId.isEmpty()) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();        
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                naan + "/" + arkId,
                "application/rdf+xml");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
    }    
    
    
    /**
     *  pour produire du RDF-SKOS
     * @param naan
     * @param arkId
     * @return 
     * #MR
     */
    @Path("/{naan}/{idArk}.rdf")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromArk(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {
        HikariDataSource ds = connect();
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        if(naan == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(naan.isEmpty()){
            ds.close();            
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId.isEmpty()) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();        
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                naan + "/" + arkId,
                "application/rdf+xml");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();
    }
    
    /**
     * pour produire du Json
     * @param naan
     * @param arkId
     * @return 
     * #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromArk__(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {
        HikariDataSource ds = connect();        
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        if(naan == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(naan.isEmpty()){
            ds.close();            
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId.isEmpty()) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();        
        }       

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                naan + "/" + arkId,
                "application/json");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }    
    
    /**
     * pour produire du Json
     * @param naan
     * @param arkId
     * @return 
     * #MR
     */
    @Path("/{naan}/{idArk}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromArk(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {
        HikariDataSource ds = connect();        
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        if(naan == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(naan.isEmpty()){
            ds.close();            
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId.isEmpty()) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();        
        }       

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                naan + "/" + arkId,
                "application/json");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }
    
    /**
     * pour produire du Json
     * @param naan
     * @param arkId
     * @return 
     * #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromArk__(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {
        HikariDataSource ds = connect();        
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        if(naan == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(naan.isEmpty()){
            ds.close();            
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId.isEmpty()) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();        
        }      

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                naan + "/" + arkId,
                "application/ld+json");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }
    
    /**
     * pour produire du Json
     * @param naan
     * @param arkId
     * @return 
     * #MR
     */
    @Path("/{naan}/{idArk}.jsonld")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromArk(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {
        HikariDataSource ds = connect();        
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        if(naan == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(naan.isEmpty()){
            ds.close();            
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId.isEmpty()) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();        
        }      

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                naan + "/" + arkId,
                "application/ld+json");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }    
    
    /**
     * pour produire du Turtle
     * @param naan
     * @param arkId
     * @return 
     * #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromArk__(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {
        HikariDataSource ds = connect();        
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        if(naan == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(naan.isEmpty()){
            ds.close();            
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId.isEmpty()) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();        
        }      

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                naan + "/" + arkId,
                "text/turtle");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }    
    
    /**
     * pour produire du Turtle
     * @param naan
     * @param arkId
     * @return 
     * #MR
     */
    @Path("/{naan}/{idArk}.ttl")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromArk(@PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {
        HikariDataSource ds = connect();        
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        if(naan == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(naan.isEmpty()){
            ds.close();            
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId == null) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if(arkId.isEmpty()) {
            ds.close();
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();        
        }      

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                naan + "/" + arkId,
                "text/turtle");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * fin de la recherche par idArk
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////       

    
    /**
     *  Permet d'exporter un group par Identifiant interne en précisant le thésaurus
     * //exp
     * curl -L --header "Accept: application/rdf+xml" "http://localhost:8083/opentheso/api/group/?id=5&theso=TH_1"
     * @param uri
     * SKOS
     * @return 
     */
    @Path("/")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response searchJsonLd(@Context UriInfo uri) {
        String idGroup = null;
        String idTheso = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if(e.getKey().equalsIgnoreCase("id")) 
                    idGroup = valeur;
                if(e.getKey().equalsIgnoreCase("theso")) 
                    idTheso = valeur;                 
            }
        }
        if(idTheso == null || idGroup == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if(ds == null)
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();


        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportGroup(ds,
                idTheso, idGroup, "application/rdf+xml");
        ds.close();
        if(datas == null) {
            return Response.status(Status.NO_CONTENT).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.ACCEPTED).entity(datas).type(MediaType.APPLICATION_XML).build();     
    }     
    
    


   
    private String messageEmptySkos() {
        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<rdf:RDF\n"
                + "	xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">"
                + "</rdf:RDF>";

        return message;
    }

    private String messageEmptyJson() {
        String message = "{\n"
                + "}";

        return message;
    }
   
}
