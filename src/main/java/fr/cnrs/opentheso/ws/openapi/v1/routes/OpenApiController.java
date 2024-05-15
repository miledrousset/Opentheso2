/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.openapi.v1.routes;

import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import fr.cnrs.opentheso.ws.openapi.helper.LangHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import fr.cnrs.opentheso.ws.openapi.v1.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * REST Web Service
 *
 * @author Julien LINGET
 */
//general path = /openapi/v1
@Path("/")
public class OpenApiController extends BaseOpenApiResource {

    @Context
    ServletConfig config;

    @Context
    Application app;
   
    @Path("/ping")
    @GET
    @Produces({CustomMediaType.APPLICATION_JSON_UTF_8})
    @Operation(summary = "${testWS.summary}$",
            description = "${testWS.description}$",
            responses = {
                @ApiResponse(responseCode = "200", description = "${testWS.200.description}$", content = {
            @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8)
        }),
                @ApiResponse(responseCode = "400", description = "${responses.400.description}$")
            },
            tags = {"Test"})
    public Response testWS() {
        String message = "{\"message\": \"pongV1\"}";

        return Response
                .status(Response.Status.OK)
                .entity(message)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @Path("/testAuth")
    @GET
    @Produces(CustomMediaType.APPLICATION_JSON_UTF_8)
    @Operation(summary = "${testAuth.summary}$",
            description = "${testAuth.description}$",
            tags = {"Test"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${testAuth.200.description}$", content = {
                    @Content(mediaType = CustomMediaType.APPLICATION_JSON_UTF_8)
                }),
                @ApiResponse(responseCode = "401", description = "${testAuth.401.description}$"),
                @ApiResponse(responseCode = "403", description = "${testAuth.403.description}$"),
                @ApiResponse(responseCode = "500", description = "${responses.500.description}$"),
                @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            },
            security = {
                @SecurityRequirement(name = "CLE-API-EXEMPLE")
            }
    )
    public Response testAuth(@HeaderParam("CLE-API-EXEMPLE") String cleAPI) {
        ApiKeyHelper helper = new ApiKeyHelper();
        ApiKeyState keyState = helper.checkApiKeyExistance(cleAPI);
        if (keyState != ApiKeyState.VALID) {
            return helper.errorResponse(keyState);
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("valid", true);
        builder.add("key", cleAPI);
        
        Response myResponse = ResponseHelper.response(Response.Status.OK, builder.build().toString(), CustomMediaType.APPLICATION_JSON_UTF_8);
        
        return myResponse;
    }
    
    private String changeURL(UriInfo uriInfo, String scheme) {
        if (scheme == null) return uriInfo.getBaseUri().toString();
        String urlWithoutScheme = uriInfo.getBaseUri().toString().split("://")[1];
        return scheme + "://" + urlWithoutScheme;
    }
}
