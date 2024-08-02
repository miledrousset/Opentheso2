/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.json.helper;

import java.io.InputStream;
import java.io.StringReader;
import jakarta.json.Json;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;


/**
 *
 * @author miled.rousset
 */
public class JsonHelper {
    
    private JsonArrayBuilder jab;
    
    
    public JsonHelper() {
        jab = Json.createArrayBuilder();
    }
    
    public void addJsonData(String uri, String value) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uri", uri);
        builder.add("label", value);
        jab.add(builder);
    }
        
    public void addJsonDataFull(String uri, String value, String definition, boolean isAltLabel) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uri", uri);
        builder.add("label", value);
        builder.add("isAltLabel", isAltLabel);
        builder.add("definition", definition);        
        jab.add(builder);
    }

    public JsonArray getBuilder() {
        if(jab != null)
            return jab.build();
        else
            return null;
    }
    
    
    /**
     * permet de lire un fichier Json d'après une inputStream
     * @param is
     * @return 
     */
    public JsonObject getJsonObject(InputStream is) {
        JsonReader reader = Json.createReader(is);
        JsonObject jsonObject = reader.readObject();
        reader.close();
        return jsonObject;
    }
    
    /**
     * Permet de lire un texte en Json
     */
    public JsonObject getJsonObject(String jsonText) {
        JsonReader reader = Json.createReader(new StringReader(jsonText));
        JsonObject jsonObject = reader.readObject();
        reader.close();
        return jsonObject;
    }    
}
