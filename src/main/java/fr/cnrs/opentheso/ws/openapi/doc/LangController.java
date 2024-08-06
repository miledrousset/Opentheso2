/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.doc;

import fr.cnrs.opentheso.ws.openapi.helper.CustomMediaType;
import fr.cnrs.opentheso.ws.openapi.helper.LangHelper;
import fr.cnrs.opentheso.ws.openapi.helper.ResponseHelper;
import java.math.BigDecimal;
import java.util.Locale;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.json.JsonObjectBuilder;

/**
 *
 * @author julie
 */
@Path("/")
public class LangController {
    
    
    private String firstLetterCapital(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    @Path("/lang")
    @GET
    @Produces(CustomMediaType.APPLICATION_JSON_UTF_8)
    public Response getAvailablesLanguages() {
        
        JsonArrayBuilder builder = Json.createArrayBuilder();
        
        LangHelper helper = new LangHelper();
        for (String langCode : helper.availableLang()) {
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            Locale locale = new Locale(langCode);
            
            objectBuilder.add("code", langCode);
            objectBuilder.add("display", firstLetterCapital(locale.getDisplayLanguage(locale)));
            
            builder.add(objectBuilder.build());
        }
        
        return ResponseHelper.response(Response.Status.OK, builder.build().toString(), CustomMediaType.APPLICATION_JSON_UTF_8);
        
    }
    
}
