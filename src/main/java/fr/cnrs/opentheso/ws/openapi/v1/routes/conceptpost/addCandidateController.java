package fr.cnrs.opentheso.ws.openapi.v1.routes.conceptpost;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.CandidateHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyState;
import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Data;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;


@Path("/candidate")
public class addCandidateController {
    /**
     * Route qui permet d'ajouter un candidat à partir d'un JSON
     * @param headers
     * @param candidate
     * @return
     */
    @Path("")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "${addCandidate.summary}$",
            description = "${addCandidate.description}$",
            tags = {"Concept Write"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON représentant le candidat à ajouter",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = Candidate.class),
                            examples = @ExampleObject(
                                    name = "${addCandidate.schemaTitle}$",
                                    value = "{ \"title\": \"Sample Title\", \"definition\": \"Sample Description\", \"thesoId\": \"th2\", \"source\": \"Sample Source\" }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Allo", content = {
                            @Content(mediaType = MediaType.APPLICATION_JSON)
                    }),
                    @ApiResponse(responseCode = "400", description = "400"),
                    @ApiResponse(responseCode = "404", description = "404"),
                    @ApiResponse(responseCode = "503", description = "503")
            },
            security = {
                    @SecurityRequirement(name = "API-KEY")
            }
    )
    public Response addCandidate(@Context HttpHeaders headers, String candidate) throws JsonProcessingException {

        var apiKeyHelper = new ApiKeyHelper();
        var apiKey = headers.getHeaderString("API-KEY");

        var keyState = apiKeyHelper.checkApiKey(apiKey);

        if (keyState != ApiKeyState.VALID){
            return apiKeyHelper.errorResponse(keyState);
        }
        
        try (HikariDataSource ds = connect()) {
            var userId = apiKeyHelper.getIdUser(apiKey);
            JsonNode candidateJson = new ObjectMapper().readTree(candidate);
            if (ds == null) {
                return ResponseHelper.response(Response.Status.NOT_FOUND, null, CustomMediaType.APPLICATION_JSON_UTF_8);
            }

            Map<String, Object> successResponse = new HashMap<>();
            if (!new CandidateHelper().saveCandidat(candidate, userId)){
                return ResponseHelper.createStatusResponse(Response.Status.BAD_REQUEST, "Bad JSON format.");
            }
            successResponse.put("candidate", candidateJson);
            return ResponseHelper.createJsonResponse(Response.Status.OK, successResponse);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public class Candidate {

        @Schema(description = "Title of the candidate", example = "Sample Title", requiredMode = Schema.RequiredMode.REQUIRED)
        public Element terme;

        @Schema(description = "Thesaurus ID", example = "th123", requiredMode = Schema.RequiredMode.REQUIRED)
        public String thesoId;

        @Schema(description = "collection ID", example = "g123", requiredMode = Schema.RequiredMode.REQUIRED)
        public String collectionId;

        @Schema(description = "Description or definition of the candidate", example = "Sample Description", requiredMode = Schema.RequiredMode.REQUIRED)
        public Element definition;

        @Schema(description = "Note of the candidate", example = "Sample note", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public Element note;

        @Schema(description = "Source of the candidate", example = "Sample Source", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String source;

        @Schema(description = "lang", example = "fr", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public List<Element> synonymes;

        @Schema(description = "comment", example = "Simple Comment", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public String comment;
    }

    @Data
    public class Element {
        private String value;
        private String lang;
    }
}
