package fr.cnrs.opentheso.ws.openapi.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Map;

/**
 *
 * @author julie
 */
public class ResponseHelper {

    /**
     * Retrourne une Response au format JSON
     * @param status
     * @param entity
     * @param format
     * @return
     */
    public static Response response(Status status, Object entity, String format) {
        return Response
                .status(status)
                .entity(entity)
                .type(format)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }

    /**
     * Retourne une Response en cas d'erreur avec le format souhaité
     * @param status
     * @param message
     * @param format
     * @return
     */
    public static Response errorResponse(Status status, String message, String format) {
        return response(status, MessageHelper.errorMessage(message, format), format);
    }

    /**
     * Retourne une réponse de statut en JSON
     * @param status
     * @param message
     * @return
     */
    public static Response createStatusResponse(Response.Status status, String message) {
        String jsonResponse = String.format("{\"code\": %d, \"message\": \"%s\"}", status.getStatusCode(), message);
        return Response.status(status)
                .entity(jsonResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String createJsonFromMap(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting map to JSON", e);
        }
    }

    public static Response createJsonResponse(Response.Status status, Map<String, Object> data) {
        String jsonResponse = createJsonFromMap(data);
        return Response.status(status)
                .entity(jsonResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
    
}
