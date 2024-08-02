package fr.cnrs.opentheso.ws.openapi.v1.routes.concept;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cnrs.opentheso.bdd.helper.PropositionApiHelper;
import fr.cnrs.opentheso.bean.proposition.model.PropositionFromApi;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;


@Path("/concepts/propositions")
public class PropositionsController {


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProposition(@Context HttpHeaders headers, String propositionJson) throws JsonProcessingException, SQLException {

        var apiKeyHelper = new ApiKeyHelper();
        var apiKey = headers.getHeaderString("API-KEY");

        var keyState = apiKeyHelper.checkApiKey(apiKey);

        if (keyState != ApiKeyState.VALID) {
            return apiKeyHelper.errorResponse(keyState);
        }

        var proposition = new ObjectMapper().readValue(propositionJson, PropositionFromApi.class);
        var userId = apiKeyHelper.getIdUser(apiKey);

        new PropositionApiHelper().createProposition(proposition, userId);
        return Response.status(Response.Status.CREATED).build();
    }
}
