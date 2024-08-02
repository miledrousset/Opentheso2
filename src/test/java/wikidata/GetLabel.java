/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wikidata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class GetLabel {
    
    public GetLabel() {
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void queryWikidata() {
        String value = "école";

        
        HttpsURLConnection cons;
        BufferedReader br;
        try {
            value = URLEncoder.encode(value, "UTF-8");   
            String query = "https://www.wikidata.org/w/api.php?action=wbsearchentities&language=fr&search="+ value + "&format=json&limit=5";            
            URL url = new URL(query);

            cons = (HttpsURLConnection) url.openConnection();
            cons.setRequestMethod("GET");
            cons.setRequestProperty("Accept", "application/json");
            if (cons.getResponseCode() != 200){
                if (cons.getResponseCode() != 202) {
                    return;
                }
            }
            br = new BufferedReader(new InputStreamReader((cons.getInputStream())));                

            String output;
            String xmlRecord = "";
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
            }
//            byte[] bytes = xmlRecord.getBytes();
//            xmlRecord = new String(bytes, Charset.forName("UTF-8"));
            cons.disconnect();

     //       listeAlign = getValues(xmlRecord, idC, lang, idTheso, source);
            br.close();
            getValuesFromJson(xmlRecord);            
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
    }
    
    private void getValuesFromJson(String jsonValue){
        JsonObject object;
        JsonArray jsonArray;
    
        JsonObject value;   
        String id = "";
        String url = "";
        String label = "";  
        String description = "";
        
        JsonArray jsonArrayLabel; 
        
        
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonValue));
            object = jsonReader.readObject();
            jsonArray = object.getJsonArray("search");

            for (int i = 0; i < jsonArray.size(); i++) {
                value = jsonArray.getJsonObject(i);
                try {
                    id = value.getString("id");
                } catch (Exception e) {
                    continue;
                }   
                try {
                    description = value.getString("description");
                } catch (Exception e) {
                    continue;
                }
                try {
                    url = value.getString("concepturi");
                    url = url.replaceAll("http://", "https://");
                } catch (Exception e) {
                    continue;
                }                
                try {
                    label = value.getJsonObject("match").getString("text");
                } catch (Exception e) {
                    continue;
                }                 
            }            
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        
        /*
        JsonObject jsonObject = JsonObject.getJsonObject(jsonDatas);
        JsonArray jsonArrayNames;        
        jsonArrayNames = jsonValue.getJsonArray("nom_s");
        if(jsonArrayNames != null) {
            for (int j = 0; j < jsonArrayNames.size(); j++) {
                if(j == 0)
                    nom = jsonArrayNames.getString(j);
                else
                    nom = nom + "; " + jsonArrayNames.getString(j);
            }
        }*/
    }
    
}
