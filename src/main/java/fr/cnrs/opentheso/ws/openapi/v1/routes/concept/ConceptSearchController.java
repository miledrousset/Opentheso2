package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;

import static fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType.APPLICATION_JSON_UTF_8;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.getAutocompleteDatas;
import static fr.cnrs.opentheso.ws.openapi.helper.MessageHelper.emptyMessage;

@Path("/concept/search")
public class ConceptSearchController {

    @GET
    @Path("/{idThesaurus}/{input}")
    public Response searchAutocompleteV2(@PathParam("input") String input,
                                         @PathParam("idThesaurus") String idThesaurus,
                                         @QueryParam("lang") String lang,
                                         @QueryParam("group") String idGroup) throws JsonProcessingException {

        var concepts = new SearchHelper().searchConceptWSV2(input, lang, idGroup, idThesaurus);
        return ResponseHelper.response(Response.Status.OK, new ObjectMapper().writeValueAsString(concepts), APPLICATION_JSON_UTF_8);
    }

    @GET
    @Path("/groups/{idThesaurus}/{idLang}")
    public Response getGroupsByThesaurus(@PathParam("idThesaurus") String idThesaurus, @PathParam("idLang") String idLang) {

        try (var ds = connect()) {
            var groups = new GroupHelper().getListRootConceptGroup(ds, idThesaurus, idLang, true);
            return ResponseHelper.response(Response.Status.OK, new ObjectMapper().writeValueAsString(groups), APPLICATION_JSON_UTF_8);
        } catch (JsonProcessingException e) {
            return ResponseHelper.response(Response.Status.OK, List.of(), APPLICATION_JSON_UTF_8);
        }
    }
}
