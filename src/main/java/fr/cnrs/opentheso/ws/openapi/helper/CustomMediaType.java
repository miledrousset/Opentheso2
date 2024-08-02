/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

import jakarta.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author julie
 */
public class CustomMediaType extends MediaType {
    
    public static final String APPLICATION_JSON_LD = "application/ld+json";
    public static final String APPLICATION_RDF = "application/rdf+xml";
    public static final String APPLICATION_TURTLE = "text/turtle"; 
    
    public static final String APPLICATION_JSON_LD_UTF_8 = "application/ld+json;charset=utf-8";
    public static final String APPLICATION_RDF_UTF_8 = "application/rdf+xml;charset=utf-8";
    public static final String APPLICATION_JSON_UTF_8 = APPLICATION_JSON +  ";charset=utf-8";
    public static final String APPLICATION_TURTLE_UTF_8 = "text/turtle;charset=utf-8";
    
    public static final List<String> ACCEPTED_HEADERS = Arrays.asList(APPLICATION_JSON_LD, APPLICATION_RDF, APPLICATION_TURTLE, APPLICATION_JSON);
}
