package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.api.D3jsHelper;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import fr.cnrs.opentheso.ws.openapi.helper.MessageHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.getContentTypeFromHeader;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.removeCharset;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

@Path("/concept/{idTheso}")
public class ConceptThesoController {

    @Path("/{idConcept}")
    @GET
    @Produces({APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "${getSkosFromidConcept.summary}$",
            description = "${getSkosFromidConcept.description}$",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getSkosFromidConcept.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
            @Content(mediaType = APPLICATION_JSON_UTF_8),
            @Content(mediaType = APPLICATION_RDF_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public Response getSkosFromidConcept(@Parameter(name = "idTheso", description = "${getSkosFromidConcept.idTheso.description}$", required = true) @PathParam("idTheso") String idThesaurus,
            @Parameter(name = "idConcept", description = "${getSkosFromidConcept.idConcept.description}$", required = true) @PathParam("idConcept") String idConcept,
            @Context HttpHeaders headers) {

        String format = getContentTypeFromHeader(headers);

        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Server unavailable", format);
            }
            RestRDFHelper helper = new RestRDFHelper();
            datas = helper.exportConceptFromId(ds, idConcept, idThesaurus, removeCharset(format));
        }

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "No data found with the thesaurus ID " + idThesaurus + " and concept ID " + idConcept, format);
        }

        return ResponseHelper.response(Response.Status.OK, datas, format);
    }

    @Path("/{idConcept}/labels")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(
            summary = "${getJsonFromIdConceptWithLabels.summary}$",
            description = "${getJsonFromIdConceptWithLabels.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${getJsonFromIdConceptWithLabels.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            },
            tags = {"Concept"}
    )
    public Response getJsonFromIdConceptWithLabels(
            @Parameter(name = "idTheso", description = "${getJsonFromIdConceptWithLabels.idTheso.description}$", required = true) @PathParam("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "${getJsonFromIdConceptWithLabels.idConcept.description}$", required = true) @PathParam("idConcept") String idConcept,
            @Parameter(name = "lang", description = "${getJsonFromIdConceptWithLabels.lang.description}$", required = false) @QueryParam("lang") String lang
    ) {

        String datas;
        String format = APPLICATION_JSON_UTF_8;
        if (lang == null || lang.isEmpty()) {
            lang = "fr";
        }
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", format);
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.getInfosOfConcept(ds,
                    idTheso, idConcept, lang);
        }

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "No concept with ID " + idConcept + " in " + idTheso, format);
        } else {
            return ResponseHelper.response(Response.Status.OK, datas, format);
        }

    }

    @Path("/{idConcept}/graph/")
    @GET
    @Produces({APPLICATION_JSON_LD_UTF_8})
    @Operation(
            summary = "${getDatasForGraph.summary}$",
            description = "${getDatasForGraph.description}$",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getDatasForGraph.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8)
        }),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$")
            }
    )
    public Response getDatasForGraph(
            @Parameter(name = "idTheso", description = "${getDatasForGraph.idTheso.description}$", required = true) @PathParam("idTheso") String idThesaurus,
            @Parameter(name = "idArk", description = "${getDatasForGraph.idArk.description}$", required = true) @PathParam("idConcept") String idConcept,
            @Parameter(name = "lang", description = "${getDatasForGraph.lang.description}$", required = true) @QueryParam("lang") String lang
    ) {
        String datas;
        try (HikariDataSource ds = connect()) {
            datas = new D3jsHelper().findDatasForGraph__(ds, idConcept, idThesaurus, lang);
            if (datas == null) {
                return ResponseHelper.response(Response.Status.NOT_FOUND, emptyMessage(APPLICATION_JSON_LD_UTF_8), APPLICATION_JSON_LD_UTF_8);
            } else {
                return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_LD_UTF_8);
            }
        }
    }
    
    @Path("/thesoGraph/")
    @GET
    @Produces({APPLICATION_JSON_LD_UTF_8})
    @Operation(
            summary = "${getDatasForGraphByTheso.summary}$",
            description = "${getDatasForGraph.description}$",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getDatasForGraph.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8)
        }),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$")
            }
    )
    public Response getDatasForGraphForThisTheso(
            @Parameter(name = "idTheso", description = "${getDatasForGraph.idTheso.description}$", required = true) @PathParam("idTheso") String idThesaurus,
            @Parameter(name = "lang", description = "${getDatasForGraph.lang.description}$", required = true) @QueryParam("lang") String lang
    ) {
        String datas;
        try (HikariDataSource ds = connect()) {
            datas = new D3jsHelper().findDatasForGraph__(ds, null, idThesaurus, lang);
            if (datas == null) {
                return ResponseHelper.response(Response.Status.NOT_FOUND, emptyMessage(APPLICATION_JSON_LD_UTF_8), APPLICATION_JSON_LD_UTF_8);
            } else {
                return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_LD_UTF_8);
            }
        }
    }    

    @Path("/{idConcept}/expansion")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_RDF_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(
            summary = "${getBrancheOfConcepts.summary}$",
            description = "${getBrancheOfConcepts.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${getBrancheOfConcepts.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8),
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
            @Content(mediaType = APPLICATION_RDF_UTF_8),
            @Content(mediaType = APPLICATION_TURTLE_UTF_8)}),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            },
            tags = {"Concept"}
    )
    public Response getBrancheOfConcepts(
            @Parameter(name = "idTheso", description = "${getBrancheOfConcepts.idTheso.description}$", required = true, example = "th3") @PathParam("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "${getBrancheOfConcepts.idConcept.description}$", required = true, example = "3") @PathParam("idConcept") String idConcept,
            @Parameter(name = "way", description = "${getBrancheOfConcepts.way.description}$", required = true,
                    schema = @Schema(type = "string", allowableValues = {"top", "down"})) @QueryParam("way") String way,
            @Context HttpHeaders headers
    ) {
        String datas;
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        
        if (idTheso == null || idConcept == null || (!way.equalsIgnoreCase("top") && !way.equalsIgnoreCase("down"))) {
            return ResponseHelper.errorResponse(Response.Status.BAD_REQUEST, "Missing parameter", format);
        }
        
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", format);
            }
            datas = getBranchOfConcepts(idConcept, idTheso, way, removeCharset(format));
        }

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Concept not found", format);
        } else {
            return ResponseHelper.response(Response.Status.OK, datas, format);
        }
    }

    private String getBranchOfConcepts(String idConcept, String idTheso, String way, String format) {
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

    @Path("/{idConcept}/narrower/{lang}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(
            summary = "${getNarrower.summary}$",
            description = "${getNarrower.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${getNarrower.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)}),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            },
            tags = {"Concept"}
    )
    public Response getNarrower(
            @Parameter(name = "idTheso", description = "${getNarrower.idTheso.description}$", required = true, example = "th3") @PathParam("idTheso") String idTheso,
            @Parameter(name = "idConcept", description = "${getNarrower.idConcept.description}$", required = true, example = "3") @PathParam("idConcept") String idConcept,
            @Parameter(name = "lang", description = "${getNarrower.lang.description}$", required = true, example = "fr") @PathParam("lang") String lang
    ) {
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", APPLICATION_JSON_UTF_8);
            }
            datas = new RestRDFHelper().getNarrower(ds, idTheso, idConcept, lang);
            if (StringUtils.isEmpty(datas)) {
                return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Concept not found", APPLICATION_JSON_UTF_8);
            } else {
                return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
            }
        }
    }

    @Path("/fromdate/{date}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_RDF_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(
            summary = "${getConceptsFromDate.summary}$",
            description = "${getConceptsFromDate.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${getConceptsFromDate.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8),
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
            @Content(mediaType = APPLICATION_RDF_UTF_8),
            @Content(mediaType = APPLICATION_TURTLE_UTF_8)
        }),
                @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            },
            tags = {"Concept"}
    )
    public Response getConceptsFromDate(
            @Parameter(name = "idTheso", description = "${getConceptsFromDate.idTheso.description}$", required = true, example = "th3") @PathParam("idTheso") String idTheso,
            @Parameter(name = "date", description = "${getConceptsFromDate.date.description}$", required = true, schema = @Schema(type = "string", format = "date"), example = "2014-07-21") @PathParam("date") String date,
            @Context HttpHeaders headers
    ) {
        String format = getContentTypeFromHeader(headers);
        String datas;
        try (HikariDataSource ds = connect()) {
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.getIdConceptFromDate(ds, idTheso, date, removeCharset(format));
        }

        if (datas == null) {
            return ResponseHelper.response(Response.Status.OK, emptyMessage(format), format);
        } else {
            return ResponseHelper.response(Response.Status.OK, datas, format);
        }
    }

    @Path("/ontome/{cidocClass}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getAllLinkedConceptsWithOntome.summary}$",
            description = "${getAllLinkedConceptsWithOntome.description}$",
            tags = {"Concept", "Ontome"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getAllLinkedConceptsWithOntome.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response getAllLinkedConceptsWithOntome(
            @Parameter(name = "idTheso", description = "${getAllLinkedConceptsWithOntome.idTheso.description}$", required = true, example = "th3") @PathParam("idTheso") String idTheso,
            @Parameter(name = "cidocClass", description = "${getAllLinkedConceptsWithOntome.cidocClass.description}$", required = true, example = "364") @PathParam("cidocClass") String cidocClass
    ) {
        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, MessageHelper.errorMessage("Service unavailable", APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            if (cidocClass == null || cidocClass.isEmpty()) {
                datas = restRDFHelper.getAllLinkedConceptsWithOntome__(ds, idTheso);
            } else {
                datas = restRDFHelper.getLinkedConceptWithOntome__(ds, idTheso, cidocClass);
            }
            if (datas == null) {
                return ResponseHelper.response(Response.Status.OK, MessageHelper.emptyMessage(APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
            }
            return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
        }
    }

}
