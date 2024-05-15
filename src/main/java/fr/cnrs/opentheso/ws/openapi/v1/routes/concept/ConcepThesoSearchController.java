package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Objects;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.*;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.*;
import fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper;
import static fr.cnrs.opentheso.ws.openapi.helper.HeaderHelper.getContentTypeFromHeader;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

@Path("/concept/{idTheso}/search")
public class ConcepThesoSearchController {

    @Path("/")
    @GET
    @Produces({APPLICATION_RDF_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(summary = "${search.summary}$",
            description = "${search.description}$",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${search.200.description}$", content = {
                            @Content(mediaType = APPLICATION_RDF_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response search(@Parameter(name = "idTheso", description = "${search.idTheso.description}$", required = true) @PathParam("idTheso") String idTheso,
                           @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${search.q.description}$") @QueryParam("q") String q,
                           @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${search.lang.description}$") @QueryParam("lang") String lang,
                           @Parameter(name = "group", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${search.group.description}$") @QueryParam("group") String group,
                           @Parameter(name = "match", in = ParameterIn.QUERY, schema = @Schema(type = "string", allowableValues = {"exact", "exactone"}), required = false,
                                   description = "${search.match.description}$") @QueryParam("match") String match,
                           @Context HttpHeaders headers) {

        return searchFilter(idTheso, headers, q, lang, group, match, null);

    }

    @Path("/notation")
    @GET
    @Produces({APPLICATION_RDF_UTF_8, APPLICATION_JSON_LD_UTF_8, APPLICATION_JSON_UTF_8, APPLICATION_TURTLE_UTF_8})
    @Operation(summary = "${searchNotation.summary}$",
            description = "${searchNotation.description}$",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${search.200.description}$", content = {
                            @Content(mediaType = APPLICATION_RDF_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_LD_UTF_8),
                            @Content(mediaType = APPLICATION_JSON_UTF_8),
                            @Content(mediaType = APPLICATION_TURTLE_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response searchNotation(@Parameter(name = "idTheso", description = "${search.idTheso.description}$", required = true) @PathParam("idTheso") String idTheso,
                                   @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${search.q.description}$") @QueryParam("q") String q,
                                   @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${search.lang.description}$") @QueryParam("lang") String lang,
                                   @Parameter(name = "group", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${search.group.description}$") @QueryParam("group") String group,
                                   @Parameter(name = "match", in = ParameterIn.QUERY, schema = @Schema(type = "string", allowableValues = {"exact", "exactone"}), required = false,
                                           description = "${search.match.description}$") @QueryParam("match") String match,
                                   @Context HttpHeaders headers) {
        return searchFilter(idTheso, headers, q, lang, group, match, "notation:");
    }

    private Response searchFilter(String idTheso, HttpHeaders headers, String q, String lang, String groupsString, String match, String filter) {

        String format = getContentTypeFromHeader(headers);
        String datas;
        String[] groups = null;

        if (q == null) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No term specified", format);
        }

        if (lang == null) {
            lang = "";
        }

        if (groupsString != null) {
            groups = groupsString.split(",");
        }

        if (match != null && !match.equalsIgnoreCase("exact") && !match.equalsIgnoreCase("exactone")) {
            match = null;
        }

        datas = getDatas(idTheso, lang, groups, q, format, filter, match);

        if (StringUtils.isEmpty(datas)) {
            return ResponseHelper.response(Response.Status.OK, emptyMessage(format), format);
        } else {
            return ResponseHelper.response(Response.Status.OK, datas, format);
        }
    }

    @Path("/fullpath")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${searchJsonForWidget.summary}$",
            description = "${searchJsonForWidget.description}$",
            tags = {"Concept"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${search.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "400", description = "${responses.400.description}$"),
                    @ApiResponse(responseCode = "500", description = "${responses.500.description}$")
            })
    public Response searchJsonForWidget(
            @Parameter(name = "idTheso", description = "${search.idTheso.description}$", required = true, example = "th3") @PathParam("idTheso") String idTheso,
            @Parameter(name = "q", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${searchJsonForWidget.q.description}$", example = "Lyon") @QueryParam("q") String q,
            @Parameter(name = "lang", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = true, description = "${search.lang.description}$", example = "fr") @QueryParam("lang") String lang,
            @Parameter(name = "group", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchJsonForWidget.group.description}$") @QueryParam("group") String groupStrings,
            @Parameter(name = "arkgroup", in = ParameterIn.QUERY, schema = @Schema(type = "string"), required = false, description = "${searchJsonForWidget.arkgroup.description}$") @QueryParam("arkgroup") String arkgroupStrings,
            @Parameter(name = "full", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "${searchJsonForWidget.full.description}$") @QueryParam("full") String fullString,
            @Parameter(name = "exactMatch", in = ParameterIn.QUERY, schema = @Schema(type = "boolean"), required = false, description = "${searchJsonForWidget.exactMatch.description}$") @QueryParam("exactMatch") String exactMatchString
    ) {
        if (q == null) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No term specified", APPLICATION_JSON_UTF_8);
        }
        if (lang == null) {
            return ResponseHelper.response(Response.Status.BAD_REQUEST, "No lang specified", APPLICATION_JSON_UTF_8);
        }
        boolean full = fullString != null && fullString.equalsIgnoreCase("true");
        String[] groups = null;
        if (groupStrings != null) {
            groups = groupStrings.split(",");
        }
        boolean exactMatch = exactMatchString != null && exactMatchString.equalsIgnoreCase("true");

        if (arkgroupStrings != null) {
            groups = getIdGroupFromArk(arkgroupStrings.split(","), idTheso);
        }

        String fullFormat = full ? "full" : null;

        String datas = getDatasForWidget(idTheso, lang, groups, q, fullFormat, exactMatch);
        return ResponseHelper.response(Response.Status.OK, Objects.requireNonNullElseGet(datas, () -> emptyMessage(APPLICATION_JSON_UTF_8)), APPLICATION_JSON_UTF_8);
    }
    
    private String getDatasForWidget(String idTheso, String idLang, String[] groups, String value, String format, boolean match) {
        String datas;

        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            RestRDFHelper restRDFHelper = new RestRDFHelper();
            datas = restRDFHelper.findDatasForWidget(ds, idTheso, idLang, groups, value, HeaderHelper.removeCharset(format), match);

            return datas;
        }
        catch (Exception e) {
            System.out.print(e.getMessage());
                }
        return null;
    }

    private String[] getIdGroupFromArk(String[] arkGroups, String idTheso) {
        String[] groups = new String[arkGroups.length];
        try (HikariDataSource ds = connect()) {
            if (ds == null) {
                return null;
            }
            /// récupération des IdGroup si arkGroup est renseigné
            GroupHelper groupHelper = new GroupHelper();
            int i = 0;
            for (String arkGroup : arkGroups) {
                groups[i] = groupHelper.getIdGroupFromArkId(ds, arkGroup, idTheso);
                i++;
            }
        }
        return groups;
    }
}

