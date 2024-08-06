package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Objects;

import static fr.cnrs.opentheso.ws.openapi.helper.ConceptHelper.directFetchConcept;
import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

@Path("/concept/handle/{handle}/{idHandle}")
public class ConceptHandleController {
    
    @Path("/")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "${getConceptByHandle.summary}$",
            description = "${getConceptByHandle.description}$",
            tags = {"Concept"},
            responses = {
                @ApiResponse(responseCode = "200", description = "${getConceptByHandle.200.description}$", content = {
            @Content(mediaType = APPLICATION_JSON_UTF_8),
            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
            @Content(mediaType = APPLICATION_RDF_UTF_8)
        }),
                @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public Response getConceptByHandle(
            @Parameter(name = "handle", description = "${getConceptByHandle.handle.description}$", required = true) @PathParam("handle") String handle,
            @Parameter(name = "idHandle", description = "${getConceptByHandle.idHandle.description}$", required = true) @PathParam("idHandle") String idHandle,
            @Context HttpHeaders headers
    ) {
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            String datas = restRDFHelper.exportConceptHdl(ds,
                    handle + "/" + idHandle,
                format);            
            return ResponseHelper.response(Response.Status.OK, datas, format);
        }        
    }    

}
