/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.api;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.core.json.helper.JsonHelper;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;

/**
 * REST Web Service
 *
 * @author miled.rousset
 */
//general path = /api
@Path("/")
public class Rest_new {
    /**
     * Creates a new instance of resources La connexion est faite à chaque
     * question
     *
     */
    public Rest_new() {
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
    private boolean getStatusOfWebservices(HikariDataSource ds, String idTheso) {
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


    @Path("/ping")
    @GET
    @Produces("text/text;charset=UTF-8")
    public String testWS(){
        return "pong";
    }
    
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


    @Path("/post")
    @POST
    @Produces("text/text;charset=UTF-8")
    public String testPost(){
        return "pong";
    }

    /**
     * pour produire du RDF-SKOS
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromArk__(@PathParam("naan") String naan,
                                     @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du RDF-SKOS
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}.rdf")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromArk(@PathParam("naan") String naan,
                                   @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du Json
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromArk__(@PathParam("naan") String naan,
                                     @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Json
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromArk(@PathParam("naan") String naan,
                                   @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromArk__(@PathParam("naan") String naan,
                                       @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}.jsonld")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromArk(@PathParam("naan") String naan,
                                     @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromArk__(@PathParam("naan") String naan,
                                       @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/{naan}/{idArk}.ttl")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromArk(@PathParam("naan") String naan,
                                     @PathParam("idArk") String arkId) {

        if (naan == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (arkId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConcept(ds,
                naan + "/" + arkId,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /*
     * fin de la recherche par idArk
     */
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /* TEST TEST TEST DOI
     */
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /**
     * récupérer un concept par DOI et produire du REF-SKOS
     *
     * @param doi1
     * @param doi2
     * @param doiId
     * @return
     */
    @Path("/doi:{doi1}.{doi2}/{doiId}.rdf")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromDoi__(
            @PathParam("doi1") String doi1,
            @PathParam("doi2") String doi2,
            @PathParam("doiId") String doiId) {

        if (doi1 == null || doi2 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (doi1.isEmpty() || doi2.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (doiId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (doiId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptDoi(ds,
                "doi:" + doi1 + "." + doi2 + "/" + doiId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML).build();
    }
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////

    /* TEST TEST TEST DOI
     */
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /*
     * recherche par Id Handle
     * Partie pour la négociation de contenu
     * concernant les URI de type Handle avec header
     * curl -L --header "Accept: application/rdf+xml »
     * curl -L --header "Accept: text/turtle »
     * curl -L --header "Accept: application/json »
     * curl -L --header "Accept: application/ld+json »
     */
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /**
     * pour produire du RDF-SKOS
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromHandle(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du RDF-SKOS
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}.rdf")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromHandle__(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du Json
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromHandle(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Json
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromHandle__(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromHandle(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}.jsonld")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonldFromHandle__(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromHandle(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param hdl1
     * @param handleId
     * @param hdl3
     * @param hdl2
     * @return #MR
     */
    @Path("/{hdl1}.{hdl2}.{hdl3}/{handleId}.ttl")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromHandle__(
            @PathParam("hdl1") String hdl1,
            @PathParam("hdl2") String hdl2,
            @PathParam("hdl3") String hdl3,
            @PathParam("handleId") String handleId) {

        if (hdl1 == null || hdl2 == null || hdl3 == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (hdl1.isEmpty() || hdl2.isEmpty() || hdl3.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (handleId == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (handleId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptHdl(ds,
                hdl1 + "." + hdl2 + "." + hdl3 + "/" + handleId,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /*
     * fin de la recherche par idHandle
     */
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /*
     * Recherche par Id du concept
     */
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /**
     * pour produire du RDF-SKOS
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromIdConcept__(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML)
                .header("Access-Control-Allow-Origin", "*")
                .build();
//        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du RDF-SKOS
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.rdf")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getSkosFromIdConcept(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/rdf+xml");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML).build();
    }

    /**
     * pour produire du Json
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromIdConcept__(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Json
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromIdConcept(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //     return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonLdFromIdConcept__(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du JsonLd
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.jsonld")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getJsonLdFromIdConcept(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "application/ld+json");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromIdConcept__(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }

    /**
     * pour produire du Turtle
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.ttl")
    @GET
    @Produces("text/turtle;charset=UTF-8")
    public Response getTurtleFromIdConcept(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        if (idConcept.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.exportConceptFromId(ds,
                idConcept, idTheso,
                "text/turtle");
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN).build();
    }




/////////////////////////////////////////////////////
/////////////////////////////////////////////////////

    /*
     * Fin de la recherche par Id du concept
     */
/////////////////////////////////////////////////////
////////////////////////////////////////////////////
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /*
     * Trouver la valeur d'après un ID Ark concept
     */
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /**
     * pour produire du Json
     *
     * @param idLang
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("/preflabel.{idLang}/{naan}/{idArk}.json")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getPrefLabelJsonFromArk(
            @PathParam("idLang") String idLang,
            @PathParam("naan") String naan,
            @PathParam("idArk") String arkId) {

        if (naan == null || naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (arkId == null || arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        if (idLang == null || idLang.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.getPrefLabelFromArk(ds, naan, arkId, idLang);
        ds.close();
        if (datas == null) {
            return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
        }
        return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }

/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /*
     * Recherche par valeurs avec négociation de contenu
     */
/////////////////////////////////////////////////////
/////////////////////////////////////////////////////
    /**
     * Permet de rechercher une valeur en filtrant par theso et par langue avec
     * négociation de contenu //exp curl -L --header "Accept:
     * application/rdf+xml"
     * http://localhost:8083/opentheso/api/search?q="vase&lang=fr&theso=2" curl
     * http://localhost:8083/opentheso/api/search?q=notation:nota1&theso=1&format=json
     * curl -L --header "Accept: application/rdf+xml"
     * http://localhost:8083/opentheso/api/search?q=notation:nota1&theso=1
     *
     * @param uri
     * @param headers
     * @return
     *
     * /// options
     * https://pactols.frantiq.fr/opentheso/api/search?q=ark:/26678/pcrtVFfTq3JlGu&lang=fr&theso=TH_1&showLabels=true
     *
     */
    @Path("/search")
    @GET
    @Produces("application/rdf+xml,application/ld+json,application/json,text/turtle;charset=UTF-8")
    public Response searchRdf(@Context UriInfo uri, @Context HttpHeaders headers) {
        String value = null;
        String idLang = "";
        String idTheso = null;
        String format = null;
//        String group = "";
        String [] groups = null; // group peut être de la forme suivante pour multiGroup (G1,G2,G3)        

        String filter = null;
        boolean showLabels = false;
        String idArk;
        String match = null; // match=exact (pour limiter la recherche aux termes exactes) match=exactone (pour chercher les prefLable, s'il n'existe pas, on cherche sur les altLabels
        
        String datas;

        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
            if (entry.getKey().equalsIgnoreCase("accept")) {
                for (String header : entry.getValue()) {
                    switch (header.toLowerCase()) {
                        case "application/rdf+xml":
                            format= "rdf";
                            break;
                        case "application/ld+json":
                            format= "jsonld";
                            break;
                        case "application/json":
                            format= "json";
                            break;
                        case "text/turtle":
                            format= "turtle";
                            break;
                        default:
                            format= "rdf";
                            break;
                    }
                }
            }
        }

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("q")) {
                    value = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    groups = valeur.split(",");
                }                
         /*       if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                }*/
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
                if (e.getKey().equalsIgnoreCase("showLabels")) {
                    if(valeur.equalsIgnoreCase("true"))
                        showLabels = true;
                }
                if (e.getKey().equalsIgnoreCase("match")) {
                    if("exact".equalsIgnoreCase(valeur) || "exactone".equalsIgnoreCase(valeur))
                        match = valeur;
                }                  
            }
        }
        if (value == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }

        if (!value.contains("ark:/")) {
            if (idTheso == null) {
                return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
            }
        }

        // vérification du filtre pour savoir si la recherche concerne des champs spécifiques
        if (value.contains("notation:")) {
            /// rercherche par notation
            filter = "notation:";
        }
        if (value.contains("prefLabel:")) {
            /// rercherche par prefLabel
        }

        if (format == null) {
            format = "rdf";
        }

        /// rercherche par idArk
        if (value.contains("ark:/")) {
            try {
                idArk = value.substring(value.indexOf("ark:/")+5);
            } catch (Exception e) {
                return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
            }
            datas = getDatasFromArk(idTheso, idLang, idArk, showLabels);
            if (datas == null) {
                return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
            }
            return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        }


        /// autre recherche
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getDatas(idTheso, idLang, groups, value, format, filter, match);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
                //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML).build();
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getDatas(idTheso, idLang, groups, value, format, filter, match);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            case "turtle":
                format = "text/turtle";
                datas = getDatas(idTheso, idLang, groups, value, format, filter, match);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN).build();
                }
                // return Response.status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN).build();
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
            case "json":
                format = "application/json";
                datas = getDatas(idTheso, idLang, groups, value, format, filter, match);
                if (datas == null) {
                    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
                }
                //    return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();
        }
        //    return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        return Response
                .status(Response.Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }




    //////////////////////////////////////////////////
    //////////////////////////////////////////////////  
    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    private String getDatas(
            String idTheso, String idLang,
            String [] groups,
            String value,
            String format, String filter,
            String match) {
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            if (filter != null) {
                switch (filter) {
                    case "notation:":
                        value = value.substring(value.indexOf(":") + 1);
                        datas = restRDFHelper.findNotation(ds, idTheso, value, format);
                        ds.close();
                        return datas;
                }
            }   datas = restRDFHelper.findConcepts(ds,
                    idTheso, idLang, groups, value, format, match);
        }
        if (datas == null) {
            return null;
        }
        return datas;
    }

    //http://localhost:8082/opentheso2/api/search?q=ark:/26678/pcrt4gr80Hd4Bm
    private String getDatasFromArk(
            String idTheso,
            String idLang,
            String idArk,
            boolean showLabels) {

        HikariDataSource ds = null;
        String datas;
        try {
            ds = connect();
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            if(idLang == null || idLang.isEmpty()) {
                datas = restRDFHelper.exportConcept(ds,
                        idArk, "application/json");
            } else
                datas = restRDFHelper.exportConceptFromArkWithLang(ds,
                        idArk, idTheso, idLang, showLabels, "application/json");
            ds.close();
            return datas;
        } catch (Exception e) {
            if (ds != null) {
                ds.close();
            }
        }
        return null;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////
    //////////////Fonction qui permet de produire /////////////////////////////////////////  
    //////////////des données Json pour le widget Aïoli////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    /**
     * Permet de rechercher une valeur en filtrant par theso, groupe et langue
     *
     * "http://193.48.140.131:8083/opentheso/api/searchwidget?q=or&lang=fr&theso=TH_1"
     *
     * @param uri JSON
     * @return
     */
    @Path("/searchwidget")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response searchJsonForWidget(@Context UriInfo uri) {
        String value = null;
        String idLang = "";
        String idTheso = null;
        String [] groups = null; // group peut être de la forme suivante pour multiGroup (G1,G2,G3)
        String [] arkGroups = null; // group peut être de la forme suivante pour multiGroup (psrbfdfdjsfh,fdsfdsfsf,kdhfjsdfhjhf)        
        String format = null; // format = full (on renvoie les altLabel en plus)
        boolean match = false; // match=exact (pour limiter la recherche aux termes exactes)

        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("q")) {
                    value = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    groups = valeur.split(",");
                }
                if (e.getKey().equalsIgnoreCase("arkgroup")) {
                    arkGroups = valeur.split(",");
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
                if (e.getKey().equalsIgnoreCase("match")) {
                    match = true;
                }                 
            }
        }

        if (idTheso == null) {
            return Response.status(Response.Status.OK).entity(getJsonMessage("l'Id du Thesaurus est obligatoire")).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();            
        }
        if (value == null) {
            return Response.status(Response.Status.OK).entity(getJsonMessage("La question est vide")).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();  
        }
        if(arkGroups != null && arkGroups.length != 0){
            groups = getIdGroupFromArk(arkGroups, idTheso);
        }

        datas = getDatasForWidget(idTheso, idLang, groups, value, format, match);
        
        if (datas == null) {
            return Response.status(Response.Status.OK).entity(getJsonMessage("Pas de résultat")).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();  
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }    
    
    private String[] getIdGroupFromArk(String[] arkGroups, String idTheso) {
        String[] groups = new String[arkGroups.length];
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            /// récupération des IdGroup si arkGroup est renseigné
            GroupHelper groupHelper = new GroupHelper();
            int i=0;
            for (String arkGroup : arkGroups) {
                groups[i] = groupHelper.getIdGroupFromArkId(ds, arkGroup, idTheso);
                i++;
            }
            ds.close();
        }
        return groups;
    }    

    
    ///////////////////////////////////////////////////////////////////////////////////////
    //////////////Fonction qui permet de produire /////////////////////////////////////////  
    //////////////des données Json pour le widget Koha////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    /**
     * Permet de rechercher une valeur en filtrant par theso, groupe et langue
     *
     * "http://193.48.140.131:8083/opentheso/api/searchwidgetbyark?q=77777/abcdddor,76767/oreijezrnh&lang=fr"
     *
     * @param uri JSON
     * @return
     */
    @Path("/searchwidgetbyark")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response searchJsonForWidgetByArk(@Context UriInfo uri) {
        String [] idArks = null;
        String idLang = "";
        String datas;
        String format = null; // format = full (on renvoie les altLabel en plus)        

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("q")) {
                    idArks = valeur.split(",");
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }                  
            }
        }
        
        if(idArks == null || idArks.length == 0)
            return Response.status(Response.Status.OK).entity(getJsonMessage("l'Id Ark est obligatoire")).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build(); 
        
        datas = getDatasForWidgetByArk(idLang, idArks, format);
        if (datas == null) {
            return Response.status(Response.Status.OK).entity(getJsonMessage("Pas de résultat")).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build(); 
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }      
    
    private String getDatasForWidgetByArk(String idLang, String[] idArks, String format) {
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.findDatasForWidgetByArk(ds,
                    idLang, idArks, format);
            ds.close();
        }
        if (datas == null) {
            return null;
        }
        return datas;
    }    
    
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * fin de la recherche par valeur
     */
///////////////////////////////////////////////////// 
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Recherche par valeurs pour autocomplétion
     * on revoie que le prefLable et l'URI en Json
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////  
    /**
     * Permet de rechercher une valeur en filtrant par theso et par langue
     * retourne une liste des valeurs (prefLabel + Uri) pour les programmes qui
     * font de l'autocompletion exp :
     * http://193.48.140.131:8083/opentheso/api/autocomplete/or?theso=TH_1&lang=fr&group=6
     *
     * @param value
     * @param uri JSON
     * @return
     */
    @Path("/autocomplete/{value}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response searchAutocomplete(@PathParam("value") String value, @Context UriInfo uri) {
        String idLang = "";
        String idTheso = null;
        String [] groups = null; // group peut être de la forme suivante pour multiGroup (G1,G2,G3)
        String format = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    groups = valeur.split(",");
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }
        if (idTheso == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build(); 
        }
        if (value == null || value.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();               
        }

        String datas;
        if (format != null && format.equalsIgnoreCase("full")) {
            datas = getAutocompleteDatas(idTheso, idLang, groups, value, true);
        } else {
            datas = getAutocompleteDatas(idTheso, idLang, groups, value, false);
        }

        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();            
            
        }
        //      return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //  return Response.ok(skos.toString()).header("Access-Control-Allow-Origin", "*").build();
    }

    /**
     * Une autre forme d'appel pour l'autocomplétion adaptée aux plugins type
     * Omeka-S Permet de rechercher une valeur en filtrant par theso et par
     * langue retourne une liste des valeurs (prefLabel + Uri) pour les
     * programmes qui font de l'autocompletion exp :
     * http://localhost:8080/opentheso/api/autocomplete?theso=TH_1&value=vase&lang=fr&group=6
     * le format : pour définir s'il faut renvoyer plus des données (définition
     * ....)
     *
     * @param uri JSON
     * @return
     */
    @Path("/autocomplete")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response searchAutocomplete2(@Context UriInfo uri) {
        String idLang = "";
        String value = null;
        String idTheso = null;
        String [] groups = null;
        String format = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("value")) {
                    value = valeur;
                }
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    groups = valeur.split(",");
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }
        if (value == null) {
            //  return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
            return Response
                    .status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
        if (idTheso == null) {
            //    return Response.status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
            return Response
                    .status(Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        String datas;
        if (format != null && format.equalsIgnoreCase("full")) {
            datas = getAutocompleteDatas(idTheso, idLang, groups, value, true);
        } else {
            datas = getAutocompleteDatas(idTheso, idLang, groups, value, false);
        }

        if (datas == null) {
            //    return Response.status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
            return Response
                    .status(Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
        // return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    private String getAutocompleteDatas(String idTheso,
                                        String idLang, String[] groups,
                                        String value, boolean withNotes) {
        HikariDataSource ds = connect();
        if (ds == null) {
            return null;
        }
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.findAutocompleteConcepts(ds,
                idTheso, idLang, groups, value, withNotes);
        ds.close();
        if (datas == null) {
            return null;
        }
        return datas;
    }
    
    private String getDatasForWidget(String idTheso,
                                     String idLang, String[] groups, String value,
                                     String format, boolean match) {
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.findDatasForWidget(ds,
                    idTheso, idLang, groups, value, format, match);
            ds.close();
        }
        if (datas == null) {
            return null;
        }
        return datas;
    }

/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Fin de la recherche par valeurs pour autocomplétion
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////  
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * Fonctions avancées pour retourner une branche complète
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////      
    /**
     * Pour retourner une branche complète à partir d'un concept en SKOS mais en
     * remontant la branche par les BT (termes génériques)
     * http://localhost:8082/opentheso2/api/expansion/concept?id=30&theso=th1&way=down
     * http://localhost:8082/opentheso2/api/expansion/concept?id=30&theso=th1&way=down&format=json
     *
     * @param uri
     * @return
     */
    @Path("expansion/concept")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getBrancheOfConcepts(@Context UriInfo uri) {
        String idConcept = null;
        String idArk = null;        
        String idTheso = null;
        String way = null;
        String format = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("id")) {
                    idConcept = valeur;
                }
                if (e.getKey().equalsIgnoreCase("idark")) {
                    idArk = valeur;
                }                
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("way")) {
                    way = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }
        if (idTheso == null || idTheso.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();            
        }
        
        if(idConcept == null || idConcept.isEmpty()) {
            if(idArk == null || idArk.isEmpty()) {
                return Response
                        .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            } else {
                idConcept = getIdConceptFromArk(idArk, idTheso);
                if(idConcept == null)
                    return Response
                            .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                
            }
        }

        if (format == null) {
            format = "rdf";
        }
        String datas;

        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getBranchOfConcepts(idConcept, idTheso, way, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getBranchOfConcepts(idConcept, idTheso, way, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            case "turtle":
                format = "text/turtle";
                datas = getBranchOfConcepts(idConcept, idTheso, way, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.TEXT_PLAIN)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            case "json":
                format = "application/json";
                datas = getBranchOfConcepts(idConcept, idTheso, way, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
        }
        return Response
                .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                .header("Access-Control-Allow-Origin", "*")
                .build();         
    }
    
    private String getIdConceptFromArk(String idArk, String idtheso){
        String idConcept;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            ConceptHelper conceptHelper = new ConceptHelper();
            idConcept = conceptHelper.getIdConceptFromArkId(ds, idArk, idtheso);

        }
        return idConcept;
    }

    private String getBranchOfConcepts(String idConcept,
                                       String idTheso, String way, String format) {
        String datas = null;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            // sens de récupération des concepts vers le haut
            if (way.equalsIgnoreCase("top")) {
                datas = restRDFHelper.brancheOfConceptsTop(ds,
                        idConcept, idTheso, format);
            }   // sens de récupération des concepts vers le bas        
            if (way.equalsIgnoreCase("down")) {
                datas = restRDFHelper.brancheOfConceptsDown(ds,
                        idConcept, idTheso, format);
            }
        }
        if (datas == null) {
            return null;
        }
        return datas;
    }

    /**
     * Pour retourner une branche complète à partir d'un identifiant d'un groupe
     *
     * @param uri
     * @return
     */
    @Path("all/group")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getAllBrancheOfGroup(@Context UriInfo uri) {
        String [] groups = null; // group peut être de la forme suivante pour multiGroup (G1,G2,G3)
        String idTheso = null;
        String format = null;
        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("id")) {
                    groups = valeur.split(",");
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }

        if (idTheso == null || groups == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (format == null) {
            format = "rdf";
        }
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getAllBrancheOfGroup__(idTheso, groups, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                       
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                  
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getAllBrancheOfGroup__(idTheso, groups, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                    
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                  
            case "turtle":
                format = "text/turtle";
                datas = getAllBrancheOfGroup__(idTheso, groups, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                       
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                  
            case "json":
                format = "application/json";
                datas = getAllBrancheOfGroup__(idTheso, groups, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                      
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
        }
        
        return Response
                .status(Response.Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                .header("Access-Control-Allow-Origin", "*")
                .build();        
    }

    private String getAllBrancheOfGroup__(String idtheso,
                                          String [] groups, String format) {
        String datas;
        try (HikariDataSource ds = connect()) {
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.brancheOfGroup(ds, groups, idtheso, format);           
        }
        return datas;         
    }

    /**
     * Pour retourner un thesaurus complet à partir de son identifiant
     *
     * @param uri
     * @return
     */
    @Path("all/theso")
    @GET
    @Produces("application/rdf+xml;charset=UTF-8")
    public Response getAllTheso(@Context UriInfo uri) {
        String idTheso = null;
        String format = null;
        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("id")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }
            }
        }

        if (idTheso == null) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }
        if (format == null) {
            format = "rdf";
        }
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getAllTheso__(idTheso, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                        
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getAllTheso__(idTheso, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                  
            case "turtle":
                format = "text/turtle";
                datas = getAllTheso__(idTheso, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyTurtle()).type(MediaType.TEXT_PLAIN)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            case "json":
                format = "application/json";
                datas = getAllTheso__(idTheso, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                      
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
        }
        return Response
                .status(Response.Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                .header("Access-Control-Allow-Origin", "*")
                .build();          
    }

    private String getAllTheso__(String idtheso, String format) {
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.getTheso(ds, idtheso, format);
        }
        if (datas == null) {
            return null;
        }
        return datas;
    }
    
    /**
     * Pour retourner un thesaurus complet à partir de son identifiant
     * le thesaurus retourné ne comporte pas de relations, mais uniquement Id et value en Json
     * On peut préciser la langue 
     *
     * @param uri
     * @return
     */
    @Path("jsonlist/theso")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getAllIdValueTheso(@Context UriInfo uri) {
        String idTheso = null;
        String lang = null;
        String datas;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("id")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("lang")) {
                    lang = valeur;
                }
            }
        }

        if (idTheso == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();              
        }
      /*  if (lang == null || lang.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML).build();
        }*/
        
        datas = getAllIdValueTheso__(idTheso, lang);
        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();              
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();          
    }    

    private String getAllIdValueTheso__(String idtheso, String lang) {
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.getThesoIdValue(ds, idtheso, lang);
        }
        if (datas == null) {
            return null;
        }
        return datas;
    }    
    
/////////////////////////////////////////////////////    
///////////////////////////////////////////////////// 
    /*
     * inforamtions sur le thésaurus
     */
///////////////////////////////////////////////////// 
/////////////////////////////////////////////////////      
    /**
     * Pour retourner la liste des thésaurus publics ou la liste des collections
     * d'un thésaurus
     * https://pactols.frantiq.fr/opentheso/api/info/list?theso=all
     * https://pactols.frantiq.fr/opentheso/api/info/list?theso=th1&group=all
     * https://pactols.frantiq.fr/opentheso/api/info/list?theso=th1&topconcept=all
     *
     * @param uri
     * @return
     */
    @Path("info/list")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getlistAllPublicTheso(@Context UriInfo uri) {
        String idTheso = null;
        String group = null;
        String topconcept = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    group = valeur;
                    if (!group.trim().equalsIgnoreCase("all")) {
                        group = null;
                    }
                }
                if (e.getKey().equalsIgnoreCase("topconcept")) {
                    topconcept = valeur;
                    if (!topconcept.trim().equalsIgnoreCase("all")) {
                        topconcept = null;
                    }
                }
            }
        }

        if (idTheso == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }

        String datas = null;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return Response
                        .status(Response.Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            }
            if (group == null && topconcept == null) {
                if (!idTheso.equalsIgnoreCase("all")) {
                    return Response
                            .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                datas = getlistAllPublicTheso__(ds);
            } else {
                if (group != null && group.equalsIgnoreCase("all")) {
                    datas = getlistAllGroupOfTheso__(ds, idTheso);
                }
                if (topconcept != null && topconcept.equalsIgnoreCase("all")) {
                    datas = getlistAllTopConceptOfTheso__(ds, idTheso);
                }
            }
        }

        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();        
    }

    private String getlistAllPublicTheso__(HikariDataSource ds) {
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        List<String> listPublicTheso = thesaurusHelper.getAllIdOfThesaurus(ds, false);

        NodeThesaurus nodeThesaurus;

        String datasJson;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idTheso : listPublicTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idTheso", idTheso);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeThesaurus = thesaurusHelper.getNodeThesaurus(ds, idTheso);
            for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", thesaurus.getLanguage());
                jobLang.add("title", thesaurus.getTitle());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeThesaurus.getListThesaurusTraduction().isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }
        datasJson = jsonArrayBuilder.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    private String getlistAllGroupOfTheso__(HikariDataSource ds, String idTheso) {
        GroupHelper groupHelper = new GroupHelper();
        List<String> listIdGroupOfTheso = groupHelper.getListIdOfGroup(ds, idTheso);

        ArrayList<NodeGroupTraductions> nodeGroupTraductions;

        String datasJson;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idGroup : listIdGroupOfTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idGroup", idGroup);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeGroupTraductions = groupHelper.getAllGroupTraduction(ds, idGroup, idTheso);
            for (NodeGroupTraductions nodeGroupTraduction : nodeGroupTraductions) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", nodeGroupTraduction.getIdLang());
                jobLang.add("title", nodeGroupTraduction.getTitle());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeGroupTraductions.isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }
        datasJson = jsonArrayBuilder.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    private String getlistAllTopConceptOfTheso__(HikariDataSource ds, String idTheso) {
        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();

        List<String> listIdTopConceptOfTheso = conceptHelper.getAllTopTermOfThesaurus(ds, idTheso);

        ArrayList<NodeTermTraduction> nodeTermTraductions;

        String datasJson;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (String idConcept : listIdTopConceptOfTheso) {
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add("idConcept", idConcept);
            JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

            nodeTermTraductions = termHelper.getAllTraductionsOfConcept(ds, idConcept, idTheso);
            for (NodeTermTraduction nodeTermTraduction : nodeTermTraductions) {
                JsonObjectBuilder jobLang = Json.createObjectBuilder();
                jobLang.add("lang", nodeTermTraduction.getLang());
                jobLang.add("title", nodeTermTraduction.getLexicalValue());
                jsonArrayBuilderLang.add(jobLang.build());
            }
            if (!nodeTermTraductions.isEmpty()) {
                job.add("labels", jsonArrayBuilderLang.build());
            }
            jsonArrayBuilder.add(job.build());
        }
        datasJson = jsonArrayBuilder.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    /**
     * Pour retourner la liste des langues d'un thésaurus
     * https://pactols.frantiq.fr/opentheso/api/info/listlang?theso=TH_1&lang=all
     *
     * @param uri
     * @return
     */
    @Path("info/listLang")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getlistLangOfTheso(@Context UriInfo uri) {
        String idTheso = null;
        String lang = null;
        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }

            }
        }
        if (idTheso == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();            
        }
        if (idTheso.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();              
        }

        HikariDataSource ds = connect();
        if (ds == null) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();              
        }

        String datas = getlistLangOfTheso__(ds, idTheso);
        ds.close();

        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();          
    }

    private String getlistLangOfTheso__(HikariDataSource ds, String idTheso) {
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        ArrayList<String> listLangOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurus(ds, idTheso);

        String datasJson;

        JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

        for (String idLang : listLangOfTheso) {
            JsonObjectBuilder jobLang = Json.createObjectBuilder();
            jobLang.add("lang", idLang);
            jsonArrayBuilderLang.add(jobLang.build());
        }
        datasJson = jsonArrayBuilderLang.build().toString();

        if (datasJson != null) {
            return datasJson;
        } else {
            return null;
        }
    }

    /**
     * Pour retourner la dernière date de modification
     *
     * @param uri
     * @return
     */
    @Path("info/lastupdate")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getInfoLastUpdate(@Context UriInfo uri) {
        String idTheso = null;
        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
            }
        }
        if (idTheso == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }
        HikariDataSource ds = connect();
        if (ds == null) {
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();              
        }

        String datas = getInfoLastUpdate__(ds, idTheso);
        ds.close();

        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();              
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();          
    }

    private String getInfoLastUpdate__(HikariDataSource ds, String idTheso) {
        ConceptHelper conceptHelper = new ConceptHelper();
        Date date = conceptHelper.getLastModification(ds, idTheso);
        if (date == null) {
            return messageEmptyJson();
        }

        return "{\"lastUpdate\": \"" + date.toString() + "\"}";
    }

        /**
     * Pour retourner les concepts modifiés à partir de la date donnée
     * format de la date 2022-01-01
     * @param uri
     * @return
     */
    @Path("/getchangesfrom")
    @GET
    @Produces("application/rdf+xml,application/ld+json,application/json,text/turtle;charset=UTF-8")
    public Response getConceptsFrom(@Context UriInfo uri) {
        String fromDate = null;
        String idTheso = null;      
        String format = null;
        
        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }                
                if (e.getKey().equalsIgnoreCase("date")) {
                    fromDate = valeur;
                }
                if (e.getKey().equalsIgnoreCase("format")) {
                    format = valeur;
                }                
            }
        }
        if (fromDate == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }
        if (idTheso == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }
        if (format == null) {
            format = "rdf";
        }     
        String datas;
        
        switch (format) {
            case "rdf": {
                format = "application/rdf+xml";
                datas = getLastConcepts__(idTheso, fromDate, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_XML)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                  
            }
            case "jsonld":
                format = "application/ld+json";
                datas = getLastConcepts__(idTheso, fromDate, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                     
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            case "turtle":
                format = "text/turtle";
                datas = getLastConcepts__(idTheso, fromDate, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                       
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.TEXT_PLAIN)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                
            case "json":
                format = "application/json";
                datas = getLastConcepts__(idTheso, fromDate, format);
                if (datas == null) {
                    return Response
                            .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                            .header("Access-Control-Allow-Origin", "*")
                            .build();                    
                }
                return Response
                        .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
        }
        return Response
                .status(Response.Status.BAD_REQUEST).entity(messageEmptySkos()).type(MediaType.APPLICATION_XML)
                .header("Access-Control-Allow-Origin", "*")
                .build();          
    }

    private String getLastConcepts__(String idTheso, String fromDate, String format) {

        String datas;
        try (HikariDataSource ds = connect()) {
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.getIdConceptFromDate(ds, idTheso, fromDate, format);           
        }
        return datas;
    }


    
    


    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////// Fonctions spécifiques pour Ontome  //////////////////////////////////////  
    ////////récupération des Toptermes qui sont liés à une classe CIDOC-CRM////////////////
    ///////////////////////////////////////////////////////////////////////////////////////
    /**
     * Pour retourner les concepts de la branche qui est liée à une classe CIDOC-CRM pour Ontome
     * le lien se fait par l'alignement en ExactMatch
     * Si la classe est renseignée, on retourne uniquement le concept en question
     * http://localhost:8082/opentheso2/api/ontome/linkedConcept?theso=th1&class=56
     * http://localhost:8082/opentheso2/api/ontome/linkedConcept?theso=th1
     * @param uri
     * @return
     */
    @Path("ontome/linkedConcept/")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getAllLinkedConceptsWithOntome(@Context UriInfo uri) {
        String idTheso = null;
        String cidocClass = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("class")) {
                    cidocClass = valeur;
                }
            }
        }

        if (idTheso == null || idTheso.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }

        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return Response.status(Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON).build();
            }   RestRDFHelper restRDFHelper = new RestRDFHelper();
            if(cidocClass == null || cidocClass.isEmpty()) {
                datas = restRDFHelper.getAllLinkedConceptsWithOntome__(ds, idTheso);
            } else {
                datas = restRDFHelper.getLinkedConceptWithOntome__(ds, idTheso, cidocClass);
            }
        }

        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();            
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }




    /////////////////////////////////////////////////////////////////////////////////////// 
    ///////////////////////////////////////////////////////////////////////////////////////    
    /////////////////////////////////////////////////////////////////////////////////////// 
    ///////////////////////////////////////////////////////////////////////////////////////    





    ///////////////////////////////////////////////////////////////////////////////////////
    ///////////// Fonctions qui  permettent de naviguer dans le thésaurus /////////////////  
    //////////////récupération des Toptermes et les fils à la demande//////////////////////
    ////////////// sert pour le widget à distance /////////////////////////////////////////    
    ///////////////////////////////////////////////////////////////////////////////////////

    //http://localhost:8082/opentheso2/api/topterm?theso=th19&lang=fr
    /**
     * permet de récupérer les TopTerms d'un thésaurus dans une langue donnée
     * en précisant l'Id du thésaurus et la langue sont obligatoires,
     * @param uri
     * @return
     */
    @Path("/topterm")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getTopterms(@Context UriInfo uri) {
        String idLang = null;
        String idTheso = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
            }
        }
        if (idTheso == null || idLang == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();              
        }

        String datas = getTopterms__(idTheso, idLang);

        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();            
        }

        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
    private String getTopterms__(String idTheso, String idLang) {
        String datas;
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        try (HikariDataSource ds = connect()) {
            datas = restRDFHelper.getTopTerms(ds, idTheso, idLang);
        }
        return datas;
    }

    //http://localhost:8082/opentheso2/api/narrower?theso=th19&id=300&lang=fr
    /**
     * permet de récupérer les TopTerms d'un thésaurus dans une langue donnée
     * en précisant l'Id du thésaurus, id du concept et la langue sont obligatoires,
     * @param uri
     * @return
     */
    @Path("/narrower")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getNarrower(@Context UriInfo uri) {
        String idLang = null;
        String idTheso = null;
        String idConcept = null;

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("id")) {
                    idConcept = valeur;
                }
            }
        }
        if (idTheso == null || idConcept == null || idLang == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();            
        }

        String datas = getNarrower__(idTheso, idConcept, idLang);

        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }

        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
    private String getNarrower__(String idTheso, String idConcept, String idLang) {
        String datas;
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        try (HikariDataSource ds = connect()) {
            datas = restRDFHelper.getNarrower(ds, idTheso, idConcept, idLang);
        }
        return datas;
    }



    /**
     * Pour récupérer les informations sur un concept
     * au format Json, les identifiants sont remplacés par les labels
     *
     * @param idTheso
     * @param idConcept
     * @return #MR
     */
    @Path("/{idTheso}.{idConcept}.labels")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getJsonFromIdConceptWithLabels(
            @PathParam("idTheso") String idTheso,
            @PathParam("idConcept") String idConcept) {

        if (idConcept == null) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }
        if (idConcept.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return Response
                        .status(Response.Status.SERVICE_UNAVAILABLE).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build();                 
            }   RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.getInfosOfConcept(ds,
                    idTheso, idConcept, "fr");
        }
        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
        //     return Response.status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON).build();
    }


    /**
     * Permet de retourner les Ids Ark fils pour un concept
     * 
     * /ark/allchilds?ark=un_ark 
     * http://localhost:8080/opentheso2/api/ark/allchilds?ark=67717/T124-66
     * {
        count: 10,
        arks: [ark1, ark2, ..., ark10]
       }
     * @param uri JSON
     * @return
     */
    @Path("/ark/allchilds")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getIdArkOfConceptNT(@Context UriInfo uri) {
        String idArk = null;
        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("ark")) {
                    idArk = valeur;
                }
            }
        }
        if (StringUtils.isEmpty(idArk)) {
            return Response
                    .status(Response.Status.OK).entity(getJsonMessage("The id ark is missing")).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build(); 
        }

        String datas;
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return Response
                        .status(Response.Status.OK).entity(getJsonMessage("No connection")).type(MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", "*")
                        .build(); 
            }
            datas = restRDFHelper.getChildsArkId(ds, idArk);
        }    

        if (StringUtils.isEmpty(datas)) {
            return Response
                    .status(Response.Status.OK).entity(getJsonMessage("No datas")).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();            
            
        }
        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }    
    
    
    









    ///////////////////////////////////////////////////////////////////////////////////////
    //////////////Fonction qui permet de produire /////////////////////////////////////////  
    //////////////des données Json pour le graphe D3js ////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////


    //http://localhost:8082/opentheso2/api/graph?theso=th19&id=266607&group=g12&lang=fr

    /**
     * permet de récupérer les données d'un thésaurus pour le graphe géré avec D3js
     * en précisant l'Id du thésaurus (obligatoire),
     * l'id du concept de départ,
     * l'id de la collection,
     * l'id de la langue
     * @param uri
     * @return
     */
    @Path("/graph")
    @GET
    @Produces("application/ld+json;charset=UTF-8")
    public Response getDatasForGraph(@Context UriInfo uri) {
        String idLang = "";
        String idConcept = null;
        String idTheso = null;
        String idGroup = "";

        for (Map.Entry<String, List<String>> e : uri.getQueryParameters().entrySet()) {
            for (String valeur : e.getValue()) {
                if (e.getKey().equalsIgnoreCase("id")) {
                    idConcept = valeur;
                }
                if (e.getKey().equalsIgnoreCase("lang")) {
                    idLang = valeur;
                }
                if (e.getKey().equalsIgnoreCase("theso")) {
                    idTheso = valeur;
                }
                if (e.getKey().equalsIgnoreCase("group")) {
                    idGroup = valeur;
                }
            }
        }
        if (idTheso == null || idTheso.isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();             
        }

        String datas = getDatasForGraph__(idTheso, idConcept, idLang, idGroup);

        if (datas == null) {
            return Response
                    .status(Response.Status.OK).entity(messageEmptyJson()).type(MediaType.APPLICATION_JSON)
                    .header("Access-Control-Allow-Origin", "*")
                    .build();         }

        return Response
                .status(Response.Status.OK).entity(datas).type(MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    private String getDatasForGraph__(String idTheso, String idConcept, String idLang, String idGroup) {
        String datas;
        try (HikariDataSource ds = connect()) {
            datas = new D3jsHelper().findDatasForGraph__(ds, idConcept, idTheso, idLang);
        }
        return datas;
    }




    
    
    
    
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////Permet de traduire les URI ARK par Opentheso////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * pour faire la redirection entre un IdArk et l'URL Opentheso
     *
     * @param naan
     * @param arkId
     * @return #MR
     */
    @Path("ark:/{naan}/{idArk}")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getUriFromArk(@PathParam("naan") String naan,
                                     @PathParam("idArk") String arkId) {
        if (naan == null || naan.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(getJsonMessage("NAAN not specified")).type(MediaType.APPLICATION_JSON).build();
        }

        if (arkId == null || arkId.isEmpty()) {
            return Response.status(Status.BAD_REQUEST).entity(getJsonMessage("Ark not specified")).type(MediaType.APPLICATION_JSON).build();
        }
        String webUrl;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return Response.status(Status.BAD_REQUEST).entity(getJsonMessage("No connection")).type(MediaType.APPLICATION_JSON).build();
            }

            RestRDFHelper restRDFHelper = new RestRDFHelper();
            webUrl = restRDFHelper.getUrlFromIdArk(ds, naan, arkId);
            if(webUrl == null) {
                return Response.status(Status.BAD_REQUEST).entity(getJsonMessage("Ark ID does not exist")).type(MediaType.APPLICATION_JSON).build();
            }
            URI uri = new URI(webUrl);
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(Rest_new.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Response.status(Status.BAD_REQUEST).entity(getJsonMessage("Internal error")).type(MediaType.APPLICATION_JSON).build();
    }    
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////FIN traduire les URI ARK par Opentheso//////////////////////////////
////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////






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
    
    private String getJsonMessage(String message){
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("message", message);
        return job.build().toString();
    }

    private String messageEmptyTurtle() {
        String message = "";

        return message;
    }
}