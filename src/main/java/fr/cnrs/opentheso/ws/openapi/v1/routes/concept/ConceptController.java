package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import static fr.cnrs.opentheso.ws.openapi.helper.ConceptHelper.directFetchConcept;
import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

/**
 * @author julie
 */
@Path("/concept/ark/fullpath/search")
public class ConceptController {

    @Path("/")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${searchJsonForWidgetArk.summary}$",
            description = "${searchJsonForWidgetArk.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${searchJsonForWidgetArk.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response searchJsonForWidgetArk(
            @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchJsonForWidgetArk.q.description}$", example = "66666/lkp6ure1g7b6,66666/lkubqlukv7i5") @QueryParam("q") String q,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchJsonForWidgetArk.lang.description}$", example = "fr") @QueryParam("lang") String lang,
            @Parameter(name = "full", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), description = "${searchJsonForWidgetArk.full.description}$") @QueryParam("full") String fullString
    ) {
        String[] idArks = q.split(",");
        if (idArks.length == 0) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No term specified", APPLICATION_JSON_UTF_8);
        }
        if (lang == null) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No lang specified", APPLICATION_JSON_UTF_8);
        }
        boolean full = fullString != null && fullString.equalsIgnoreCase("true");

        String fullFormat = full ? "full" : null;

        String datas;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Erreur interne du serveur", APPLICATION_JSON_UTF_8);
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.findDatasForWidgetByArk(ds, lang, idArks, fullFormat);
            if (datas == null) {
                return ResponseHelper.response(Response.Status.OK, emptyMessage(APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
            }
        }

        return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
    }

    /*
    @Path("/testput")
    @PUT
    @Consumes({APPLICATION_JSON_UTF_8})
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${searchJsonForWidgetArk.summary}$",
            description = "${searchJsonForWidgetArk.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${searchJsonForWidgetArk.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response testPut(String content
    ) {

        return ResponseHelper.response(Response.Status.OK, emptyMessage(APPLICATION_JSON_UTF_8), APPLICATION_JSON_UTF_8);
    }
*/
}
