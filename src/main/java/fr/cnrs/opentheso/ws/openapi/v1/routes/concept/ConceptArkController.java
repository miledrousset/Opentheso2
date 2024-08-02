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

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Objects;

import static fr.cnrs.opentheso.ws.openapi.helper.ConceptHelper.directFetchConcept;
import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

@Path("/concept/ark:/{naan}/{ark}")
public class ConceptArkController {


    @Path("/")
    @GET
    @Produces({APPLICATION_JSON_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_TURTLE_UTF_8, APPLICATION_RDF_UTF_8})
    @Operation(summary = "${getConceptByArk.summary}$",
            description = "${getConceptByArk.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getConceptByArk.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8),
                            @Content(mediaType = APPLICATION_RDF_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public Response getConceptByArk(
            @Parameter(name = "naan", description = "${getConceptByArk.naan.description}$", required = true, example = "66666") @PathParam("naan") String naan,
            @Parameter(name = "ark", description = "${getConceptByArk.idArk.description}$", required = true, example = "lkp6ure1g7b6") @PathParam("ark") String idArk,
            @Context HttpHeaders headers
    ) {
        String format = HeaderHelper.getContentTypeFromHeader(headers);
        return directFetchConcept(naan + "/" + idArk, format);
    }

    @Path("/childs")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getIdArkOfConceptNT.summary}$",
            description = "${getIdArkOfConceptNT.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getIdArkOfConceptNT.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${getIdArkOfConceptNT.400.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            })
    public Response getIdArkOfConceptNT(
            @Parameter(name = "naan", description = "${getIdArkOfConceptNT.naan.description}$", example = "66666") @PathParam("naan") String naan,
            @Parameter(name = "ark", description = "${getIdArkOfConceptNT.arkId.description}$", example = "lkhsq27fw3z6") @PathParam("ark") String arkLocalId
    ) {
        String arkId = naan + "/" + arkLocalId;

        String datas;
        RestRDFHelper restRDFHelper = new RestRDFHelper();

        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "No server connection", APPLICATION_JSON_UTF_8);
            }
            datas = restRDFHelper.getChildsArkId(ds, arkId);
        }

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.response(Response.Status.OK, emptyMessage(APPLICATION_JSON_UTF_8), datas);

        }
        return ResponseHelper.response(Response.Status.OK, datas, APPLICATION_JSON_UTF_8);
    }

    @Path("/prefLabel/{lang}")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(
            summary = "${getPrefLabelFromArk.summary}$",
            description = "${getPrefLabelFromArk.description}$",
            tags = {"Concept", "Ark"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getPrefLabelFromArk.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "404", description = "${responses.concept.404.description}$"),
                    @ApiResponse(responseCode = "503", description = "${responses.503.description}$")
            }
    )
    public Response getPrefLabelFromArk(
            @Parameter(name = "naan", description = "${getPrefLabelFromArk.naan.description}$", required = true, example = "66666") @PathParam("naan") String naan,
            @Parameter(name = "ark", description = "${getPrefLabelFromArk.idArk.description}$", required = true, example = "lkp6ure1g7b6") @PathParam("ark") String idArk,
            @Parameter(name = "lang", description = "${getPrefLabelFromArk.lang.description}$", required = true, example = "fr") @PathParam("lang") String lang
    ) {
        HikariDataSource ds = connect();
        if (ds == null) {
            return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "No server connection", APPLICATION_JSON_UTF_8);
        }

        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas = restRDFHelper.getPrefLabelFromArk(ds, naan, idArk, lang);
        ds.close();
        return ResponseHelper.response(Response.Status.OK, Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)), APPLICATION_JSON_UTF_8);
    }


}
