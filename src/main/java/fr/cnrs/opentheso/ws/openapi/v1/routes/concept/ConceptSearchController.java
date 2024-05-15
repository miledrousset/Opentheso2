/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.getDatasFromArk;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.getContentTypeFromHeader;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Objects;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author julie
 */
@Path("/search")
public class ConceptSearchController {
    
    @Path("/ark")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${searchByArkId.summary}$",
            description = "${searchByArkId.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${search.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response searchByArkId(@Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchByArkId.q.description}$") @QueryParam("q") String idArk,
                                  @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchByArkId.lang.description}$") @QueryParam("lang") String lang,
                                  @Parameter(name = "showLabels", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "${searchByArkId.showLabels.description}$") @QueryParam("showLabels") String showLabelsString,
                                  @Context HttpHeaders headers) {

        if (lang == null) {
            lang = "";
        }
        String format = getContentTypeFromHeader(headers);
        String datas;
        boolean showLabels = showLabelsString != null && showLabelsString.equalsIgnoreCase("true");

        if (idArk == null) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No Ark ID specified", format);
        }

        datas = getDatasFromArk("", lang, idArk, showLabels);
        return ResponseHelper.response(Response.Status.OK, Objects.requireNonNullElseGet(datas, () -> emptyMessage(format)), format);

    }

    
}
