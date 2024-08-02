/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.v1.routes.group;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.MessageHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.Objects;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;

/**
 * @author julie
 */
@Path("/group")
public class GroupController {

    @Path("/ark:/{naan}/{ark}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getGroupIdFromArk.summary}$",
            description = "${getGroupIdFromArk.description}$",
            tags = {"Group", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getGroupIdFromArk.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public Response getGroupIdFromArk(@Parameter(name = "naan", required = true, description = "${getGroupIdFromArk.naan.description}$") @PathParam("naan") String naan,
                                      @Parameter(name = "ark", required = true, description = "${getGroupIdFromArk.arkId.description}$") @PathParam("ark") String arkId) {

        String datas;
        String format = APPLICATION_JSON_UTF_8;
        try (HikariDataSource ds = connect()) {
            if (ds == null)
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Server unavailable", format);

            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.exportGroup(ds, naan + "/" + arkId, HeaderHelper.removeCharset(format));
        }
        return ResponseHelper.response(Response.Status.OK, Objects.requireNonNullElseGet(datas, () -> MessageHelper.emptyMessage(format)), format);
    }

}
