/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.v1.routes;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/redirect")
public class RedirectController {
    
    @Path("/ark:/{naan}/{idArk}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getUriFromArk.summary}$",
            description = "${getUriFromArk.description}$",
            tags = {"Ark"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getUriFromArk.200.description}$"),
                @ApiResponse(responseCode = "307", description = "${getUriFromArk.307.description}$"),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                @ApiResponse(responseCode = "404", description = "${getUriFromArk.404.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response getUriFromArk(
            @Parameter(name = "naan", in = ParameterIn.PATH, schema = @Schema(type = "string"), description = "${getUriFromArk.naan.description}$") @PathParam("naan") String naan,
            @Parameter(name = "idArk", in = ParameterIn.PATH, schema = @Schema(type = "string"), description = "${getUriFromArk.idArk.description}$") @PathParam("idArk") String arkId
    ) {
        String webUrl;
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "No database connection", APPLICATION_JSON_UTF_8);
            }

            RestRDFHelper restRDFHelper = new RestRDFHelper();
            webUrl = restRDFHelper.getUrlFromIdArk(ds, naan, arkId);
            if (webUrl == null) {
                return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Ark ID not found", APPLICATION_JSON_UTF_8);
            }
            URI uri = new URI(webUrl);
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException ex) {
            Logger.getLogger(RedirectController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ResponseHelper.errorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error", APPLICATION_JSON_UTF_8);
    }
    
}
