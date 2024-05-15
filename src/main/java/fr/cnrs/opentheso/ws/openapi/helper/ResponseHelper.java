package fr.cnrs.opentheso.ws.openapi.helper;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 *
 * @author julie
 */
public class ResponseHelper {
    
    public static Response response(Status status, Object entity, String format) {
        return Response
                .status(status)
                .entity(entity)
                .type(format)
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
    
    public static Response errorResponse(Status status, String message, String format) {
        return response(status, MessageHelper.errorMessage(message, format), format);
    }
    
}
