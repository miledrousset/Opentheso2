/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

import java.util.ArrayList;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import jakarta.json.Json;
import jakarta.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author miled.rousset
 */
public class OntomeHelper {

    private StringBuffer messages;
    // private ArrayList<NodeAlignment> listAlignValues;

    public OntomeHelper() {
        messages = new StringBuffer();
    }

    public ArrayList<NodeAlignment> queryOntomeHelper(String idC, String idTheso,
            String lexicalValue,
            String query, String source) {
        
       // query = "https://ontome.net/api/classes-type-descendants/label/##value##/json";
        
        if (query.trim().equals("") ) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }        

        if (query.trim().equals("") ) {
            return null;
        }
        HttpsURLConnection cons;
        BufferedReader br;
        ArrayList<NodeAlignment> listeAlign = null;
        
        try {
         //   lexicalValue = lexicalValue.replaceAll(" ", "%20");            
            lexicalValue = URLEncoder.encode(lexicalValue, "UTF-8");
            query = query.replaceAll("##value##", lexicalValue);
         
            URL url = new URL(query);

            cons = (HttpsURLConnection) url.openConnection();
            cons.setRequestMethod("GET");
            cons.setRequestProperty("Accept", "application/json");
            if (cons.getResponseCode() != 200){
                if (cons.getResponseCode() != 202) {
                    return null;
                }
            }
            br = new BufferedReader(new InputStreamReader((cons.getInputStream())));                

            String output;
            String xmlRecord = "";
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
            }
            cons.disconnect();
            br.close();
            listeAlign = getValuesFromJson(xmlRecord, idC, idTheso, source);
            
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return listeAlign;        
     }
    
    private ArrayList<NodeAlignment> getValuesFromJson(String jsonValue, String idConcept, String idTheso, String source){
        JsonArray jsonArray;
        JsonObject value;   
       
        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();
        
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonValue));
            jsonArray = jsonReader.readArray();
           // jsonArray = object.getJsonArray("search");

            for (int i = 0; i < jsonArray.size(); i++) {
                NodeAlignment na = new NodeAlignment();
                na.setInternal_id_concept(idConcept);
                na.setInternal_id_thesaurus(idTheso);
                value = jsonArray.getJsonObject(i);
                // label ou Nom
                try {
                     na.setConcept_target(value.getString("standardLabel") + " (" + value.getInt("id") + ")");
                } catch (Exception e) {
                    continue;
                }                    

                // description
                try {
                    if(value.getString("rootNamespaceLabel") != null) {
                        na.setDef_target(value.getString("rootNamespaceLabel") + " (" + value.getInt("rootNamespaceId") + ")");
                    } else {
                        na.setDef_target("");
                    }
                } catch (Exception e) {
                    continue;
                }                

                na.setThesaurus_target(source);

                // URI
                try {
                    na.setUri_target(value.getString("ontomeURI"));
                    na.setUri_target(na.getUri_target().replaceAll("http://", "https://"));
                } catch (Exception e) {
                    continue;
                }                  

                listAlignValues.add(na);                
            }            
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return listAlignValues;
    }
  

    

    public String getMessages() {
        return messages.toString();
    }

}
