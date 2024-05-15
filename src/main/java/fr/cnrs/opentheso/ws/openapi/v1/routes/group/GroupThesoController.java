package fr.cnrs.opentheso.ws.openapi.v1.routes.group;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.removeCharset;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import jakarta.ws.rs.QueryParam;

@Path("/group/{idTheso}")
public class GroupThesoController {
    
    @Path("/")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getAllGroupsFromTheso.summary}$",
            description = "${getAllGroupsFromTheso.description}$",
            tags = {"Group"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getAllGroupsFromTheso.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)}),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response getAllGroupsFromTheso(@Parameter(name = "idTheso", description = "${getAllGroupsFromTheso.idTheso.description}$", schema = @Schema(type = "string")) @PathParam("idTheso") String idTheso) {
        GroupHelper groupHelper = new GroupHelper();
        ArrayList<NodeGroupTraductions> nodeGroupTraductions;

        String datasJson;
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        try (HikariDataSource ds = connect()) {

            List<String> listIdGroupOfTheso = groupHelper.getListIdOfGroup(ds, idTheso);
            
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
                return ResponseHelper.response(Response.Status.OK, datasJson, APPLICATION_JSON_UTF_8);
            }
        }
        
        return ResponseHelper.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error", APPLICATION_JSON_UTF_8);
    }
    
    @Path("/{idGroup}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(
            summary = "${getGroupFromIdThesoIdGroup.summary}$",
            description = "${getGroupFromIdThesoIdGroup.description}$",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getGroupFromIdThesoIdGroup.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "${responses.group.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public Response getGroupFromIdThesoIdGroup(
            @Parameter(name = "idTheso", required = true, description = "${getGroupFromIdThesoIdGroup.idTheso.description}$") @PathParam("idTheso") String idTheso,
            @Parameter(name = "idGroup", required = true, description = "${getGroupFromIdThesoIdGroup.idGroup.description}$") @PathParam("idGroup") String idGroup,
            @Context HttpHeaders headers
            ) {
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String format;
        String datas;
        try (HikariDataSource ds = connect()) {
            format = HeaderHelper.getContentTypeFromHeader(headers);
            if (ds == null) return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", format);
            datas = restRDFHelper.exportGroup(ds, idTheso, idGroup, removeCharset(format));
        }
        if (datas == null) return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Group not found", format);
        return ResponseHelper.response(Response.Status.OK, datas, format);
    }

    @Path("/branch")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(
            summary = "${getAllBranchOfGroup.summary}$",
            description = "${getAllBranchOfGroup.description}$",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getAllBranchOfGroup.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "404", description = "${responses.group.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            }
    )
    public Response getAllBranchOfGroup(
            @Parameter(name = "idTheso", required = true, description = "${getAllBranchOfGroup.idTheso.description}$") @PathParam("idTheso") String idTheso,
            @Parameter(name = "idGroups", required = true, description = "${getAllBranchOfGroup.idGroups.description}$", example = "g1,g2,g3") @QueryParam("idGroups") String idGroups,
            @Context HttpHeaders headers
    ) {
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        
        if (idGroups == null) {
            return ResponseHelper.errorResponse(Response.Status.BAD_REQUEST, "No group id", format);
        }
        
        String[] groups = idGroups.split(",");
        
        if (groups.length == 0) {
            return ResponseHelper.errorResponse(Response.Status.BAD_REQUEST, "No group id", format);
        }
        
        String datas;
        
        try (HikariDataSource ds = connect()) {
            if (ds == null) return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", format);
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.brancheOfGroup(ds, groups, idTheso, removeCharset(format));
        }
        if (datas == null || datas.equals("{}")) return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Group not found", format);
        return ResponseHelper.response(Response.Status.OK, datas, format);
    }
    
    @Path("/branchtree")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(
            summary = "${getAllBranchOfGroupAsTree.summary}$",
            description = "${getAllBranchOfGroupAsTree.description}$",
            tags = {"Group"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getAllBranchOfGroupAsTree.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "404", description = "${responses.group.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            }
    )
    public Response getAllBranchOfGroupAsTree(
            @Parameter(name = "idTheso", required = true, description = "${getAllBranchOfGroupAsTree.idTheso.description}$") @PathParam("idTheso") String idTheso,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchAutocomplete.lang.description}$") @QueryParam("lang") String lang,
            @Parameter(name = "idGroups", required = true, description = "${getAllBranchOfGroupAsTree.idGroups.description}$", example = "g1,g2,g3") @QueryParam("idGroups") String idGroups,
            @Context HttpHeaders headers
    ) {
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        
        if (idGroups == null) {
            return ResponseHelper.errorResponse(Response.Status.BAD_REQUEST, "No group id", format);
        }
        
        String[] groups = idGroups.split(",");
        
        if (groups.length == 0) {
            return ResponseHelper.errorResponse(Response.Status.BAD_REQUEST, "No group id", format);
        }
        
        String datas;
        
        try (HikariDataSource ds = connect()) {
            if (ds == null) return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Service unavailable", format);
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.brancheOfGroupAsTree(ds, groups, idTheso, lang);
        }
        if (datas == null || datas.equals("{}")) return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Group not found", format);
        return ResponseHelper.response(Response.Status.OK, datas, format);
    }    
    
}
