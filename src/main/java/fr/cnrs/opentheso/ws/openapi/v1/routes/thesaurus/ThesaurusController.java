package fr.cnrs.opentheso.ws.openapi.v1.routes.thesaurus;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;

/**
 *
 * @author julie
 */
@Path("/thesaurus")
public class ThesaurusController {

    @Path("/")
    @GET
    @Produces({APPLICATION_JSON_UTF_8})
    @Operation(summary = "${getListAllPublicTheso.summary}$",
            description = "${getListAllPublicTheso.description}$",
            tags = {"Thesaurus"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "${getListAllPublicTheso.200.description}$", content = {
                            @Content(mediaType = APPLICATION_JSON_UTF_8)
                    }),
                    @ApiResponse(responseCode = "404", description = "${responses.theso.404.description}$")
            }
    )
    public Response getListAllPublicTheso() {
        String datasJson;

        try (HikariDataSource ds = connect()) {
            
            if (ds == null) {
                return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "Serveur unavailable", APPLICATION_JSON_UTF_8);
            }
            
            ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
            List<String> listPublicTheso = thesaurusHelper.getAllIdOfThesaurus(ds, false);

            NodeThesaurus nodeThesaurus;

            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
            for (String idTheso : listPublicTheso) {
                JsonObjectBuilder job = Json.createObjectBuilder();
                job.add("idTheso", idTheso);
                JsonArrayBuilder jsonArrayBuilderLang = Json.createArrayBuilder();

                nodeThesaurus = thesaurusHelper.getNodeThesaurus(ds, idTheso);
                for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {
                    JsonObjectBuilder jobLang = Json.createObjectBuilder();
                    jobLang.add("lang", thesaurus.getLanguage());
                    jobLang.add("title", thesaurus.getTitle());
                    jsonArrayBuilderLang.add(jobLang.build());
                }
                if (!nodeThesaurus.getListThesaurusTraduction().isEmpty()) {
                    job.add("labels", jsonArrayBuilderLang.build());
                }
                jsonArrayBuilder.add(job.build());
            }
            datasJson = jsonArrayBuilder.build().toString();
        } 

        if (datasJson != null) {
            return ResponseHelper.response(Response.Status.OK, datasJson,  APPLICATION_JSON_UTF_8);
        } else {
            return null;
        }
    }
}
