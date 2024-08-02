/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import java.net.HttpURLConnection;


/**
 *
 * @author miled.rousset
 */
public class GeoNamesHelper {

    private StringBuffer messages;
    // private ArrayList<NodeAlignment> listAlignValues;

    // les informations récupérées de Wikidata
    private ArrayList<SelectedResource> resourceTraductions;
    private ArrayList<SelectedResource> resourceDefinitions;
    private ArrayList<SelectedResource> resourceImages;

    public GeoNamesHelper() {
        messages = new StringBuffer();
    }

    /**
     * Alignement du thésaurus vers la source GeoNames en Json 
     *
     * @param idC
     * @param idTheso
     * @param value
     * @param lang
     * @param query
     * @param source
     * @return
     */
    public ArrayList<NodeAlignment> queryGeoNames(String idC, String idTheso,
            String value, String lang,
            String query, String source) {
        ArrayList<NodeAlignment> listeAlign = null;
        //http://api.geonames.org/searchJSON?q=saint%20clair&maxRows=10&lang=fr&username=opentheso
        //http://api.geonames.org/searchJSON?q=saint%20clair%20du%20rhone&maxRows=10&style=FULL&lang=fr&username=opentheso
        
        if (query.trim().equals("")) {
            return null;
        }
        if (value.trim().equals("")) {
            return null;
        }

        try {
            value = URLEncoder.encode(value, "UTF-8");
            query = query.replace("##lang##", lang);
            query = query.replace("##value##", value);
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                messages.append("Failed : HTTP error code : ");
                messages.append(conn.getResponseCode());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String records = "";
            while ((output = br.readLine()) != null) {
                records += output;
            }
//            byte[] bytes = records.getBytes();
//            records = new String(bytes, Charset.forName("UTF-8"));
            conn.disconnect();
            listeAlign = getValues(value, records, idC, idTheso, source);
            br.close();
        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;
        }
        return listeAlign;
    }

    private ArrayList<NodeAlignment> getValues(
            String value,
            String jsonDatas,
            String idC, String idTheso, String source) {
        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();

        String uriGeonmaes = "https://www.geonames.org/";
        JsonObject jsonObject;
        String toponymName = "";
        String uri = null;
        String infos = "";
        String longitude = "0.0";
        String latitude = "0.0";
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonDatas))) {

            JsonArray jsonArray = jsonReader.readObject().getJsonArray("geonames");

            for (int i = 0; i < jsonArray.size(); ++i) {
                jsonObject = jsonArray.getJsonObject(i);
                try {
                    toponymName = jsonObject.getString("toponymName");
                    uri = uriGeonmaes + jsonObject.getInt("geonameId");
                    infos = infos + jsonObject.getString("countryCode") + ", ";
                    infos = infos + jsonObject.getString("fclName") + " ; ";

                    longitude = jsonObject.getString("lng");
                    latitude = jsonObject.getString("lat");

                    infos = infos + "long: " + longitude + " ; ";
                    infos = infos + "lat:" + latitude;                        
                } catch (Exception e) {
                }
                 
                
       //         uri = uri.replaceAll("http://", "https://");

                if(uri!= null) {
                    NodeAlignment na = new NodeAlignment();
                    //si le titre est équivalent, on le place en premier
                    if (value.trim().equalsIgnoreCase(toponymName.trim())) {
                        na.setConcept_target(toponymName);
                        na.setDef_target(infos);
                        na.setInternal_id_concept(idC);
                        na.setInternal_id_thesaurus(idTheso);
                        na.setThesaurus_target(source);
                        na.setUri_target(uri);
                        na.setLng(Double.parseDouble(longitude));
                        na.setLat(Double.parseDouble(latitude));                    
                        listAlignValues.add(0, na);
                    } else {
                        na.setConcept_target(toponymName);
                        na.setDef_target(infos);
                        na.setInternal_id_concept(idC);
                        na.setInternal_id_thesaurus(idTheso);
                        na.setThesaurus_target(source);
                        na.setUri_target(uri);
                        na.setLng(Double.parseDouble(longitude));
                        na.setLat(Double.parseDouble(latitude));
                        listAlignValues.add(na);
                    }
                }
            }
        } catch (Exception e) {
            messages.append(e.toString());
            return null;
        }
        return listAlignValues;
    }

    /**
     * Cette fonction permet de récupérer les options de Geonames Images,
     * alignements, traductions....
     *
     * @param selectedNodeAlignment
     * @param selectedOptions
     * @param thesaurusUsedLanguageWithoutCurrentLang
     */
    public void setOptions(NodeAlignment selectedNodeAlignment, List<String> selectedOptions, List<String> thesaurusUsedLanguageWithoutCurrentLang) {

        if (selectedNodeAlignment == null) {
            return;
        }

        CurlHelper curlHelper = new CurlHelper();
        curlHelper.setHeader1("Accept");
        curlHelper.setHeader2("application/json");
        
        //http://api.geonames.org/getJSON?geonameId=3014728&username=opentheso

        String idGeoNames = selectedNodeAlignment.getUri_target().substring(selectedNodeAlignment.getUri_target().lastIndexOf("/") + 1);
        String uriGeonamesFull = "http://api.geonames.org/getJSON?geonameId=" + idGeoNames + "&username=opentheso";
        String datas = curlHelper.getDatasFromUriHttp(uriGeonamesFull);
        
        for (String selectedOption : selectedOptions) {
            switch (selectedOption) {
                case "langues":
                    resourceTraductions = getTraductions(datas, thesaurusUsedLanguageWithoutCurrentLang);
                    break;
                case "notes":
                    resourceDefinitions = null;
                    //resourceDefinitions = getDescriptions(datas, thesaurusUsedLanguage);
                    break;
                case "images":
                    resourceImages = null;
                    //resourceImages = getImages(datas);
                    break;                    
            }
        }
    }

    /**
     * récupération des traductions
     *
     * @param jsonDatas
     * @param languages
     * @return
     */
    private ArrayList<SelectedResource> getTraductions(String jsonDatas, List<String> languages) {

        ArrayList<SelectedResource> traductions = new ArrayList<>();

        String lang;
        String value;  
        boolean isPreferredName;
        
        // lecture du fichier Json des langues
        JsonArray dataArray;
        JsonObject dataObject;
        try (JsonReader reader = Json.createReader(new StringReader(jsonDatas))) {
            dataObject = reader.readObject();
            dataArray= dataObject.getJsonArray("alternateNames");
            
            for(int i = 0; i < dataArray.size(); ++i) {
                dataObject = dataArray.getJsonObject(i);

                try {
                    lang = dataObject.getString("lang");
                    value = dataObject.getString("name");   
                    isPreferredName = dataObject.getBoolean("isPreferredName"); 
                } catch (Exception e) {
                    continue;
                }


                if(lang == null || value == null || lang.isEmpty() || value.isEmpty() || !isPreferredName)  continue;

                if(languages.contains(lang)) {
                    SelectedResource selectedResource = new SelectedResource();
                    selectedResource.setIdLang(lang);
                    selectedResource.setGettedValue(value);
                    traductions.add(selectedResource);
                }
            }
        }
        return traductions;
    }

    /**
     * permet de récupérer les descriptions de wikidata
     *
     * @param jsonDatas
     * @param entity
     * @param languages
     * @return
     */
    private ArrayList<SelectedResource> getDescriptions(
            String jsonDatas,
            ArrayList<String> languages) {
        
       
        ArrayList<SelectedResource> descriptions = new ArrayList<>();
        String lang;
        String value;        
        
        // lecture du fichier Json des notes
        JsonArray dataArray;
        JsonObject dataObject;
        try (JsonReader reader = Json.createReader(new StringReader(jsonDatas))) {
            dataObject = reader.readObject();
            dataArray= dataObject.getJsonArray("alternateNames");
            for (int i = 0; i < dataArray.size(); ++i) {
                dataObject = dataArray.getJsonObject(i);
                
                SelectedResource selectedResource = new SelectedResource();
                lang = dataObject.getString("language");
                value = dataObject.getString("string");

                if(lang == null || value == null || lang.isEmpty() || value.isEmpty())  continue;

                if(languages.contains(lang)) {
                    selectedResource.setIdLang(lang);
                    selectedResource.setGettedValue(value);
                    descriptions.add(selectedResource);
                }
            }
        }
        return descriptions;
    }

    public String getMessages() {
        return messages.toString();
    }

    public ArrayList<SelectedResource> getResourceTraductions() {
        return resourceTraductions;
    }

    public ArrayList<SelectedResource> getResourceDefinitions() {
        return resourceDefinitions;
    }

    public ArrayList<SelectedResource> getResourceImages() {
        return resourceImages;
    }

}
