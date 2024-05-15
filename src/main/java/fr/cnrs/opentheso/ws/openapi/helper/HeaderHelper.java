/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Julien LINGET
 */
public class HeaderHelper {
    
    /**
     * Parcours les headers et récupère le premier type accepté, renvoie JSON par défaut
     * @param headers - Headers de la requête HTTP
     * @return Le premier type supporté apparaissant dans la requête, JSON si aucun type supporté n'a été passé en header.
     */
    public static String getContentTypeFromHeader(HttpHeaders headers) {
        
        MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        Map<String, List<String>> baseRequestHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        baseRequestHeaders.putAll(requestHeaders);
       
        for (String contentType : baseRequestHeaders.get("accept")) {
            if (CustomMediaType.ACCEPTED_HEADERS.contains(removeCharset(contentType.toLowerCase()))) {
                return contentType;
            }
        }
        
        return CustomMediaType.APPLICATION_JSON_UTF_8;
     
    }
    
    /**
     * Retire l'option ;charset= de la chaine de caractère contentType
     * @param contentType Type de donnée passé en header HTTP avec la précision du charset
     * @return Le type de donnée en minuscule sans la précision du charset
     */
    public static String removeCharset(String contentType) {
        if(StringUtils.isEmpty(contentType)) return null;
        
        contentType = contentType.toLowerCase();
        int indexCharset = contentType.indexOf(";charset=");
        if (indexCharset == -1) {
            return contentType;
        }
        return contentType.substring(0, indexCharset);
    }
    
}
