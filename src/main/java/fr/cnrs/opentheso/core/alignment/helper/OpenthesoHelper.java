/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import javax.net.ssl.HttpsURLConnection;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.imports.rdf4j.nouvelle.ReadRDF4JNewGen;
import fr.cnrs.opentheso.core.json.helper.JsonHelper;
import fr.cnrs.opentheso.skosapi.SKOSDocumentation;
import fr.cnrs.opentheso.skosapi.SKOSLabel;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSXmlDocument;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.rio.RDFFormat;

/**
 *
 * @author miled.rousset
 */
public class OpenthesoHelper {

    private StringBuffer messages;
    // private ArrayList<NodeAlignment> listAlignValues;

    // les informations récupérées de Wikidata
    private ArrayList<SelectedResource> resourceOpenthesoTraductions;
    private ArrayList<SelectedResource> resourceOpenthesoDefinitions;
    private ArrayList<SelectedResource> resourceOpenthesoImages;

    public OpenthesoHelper() {
        messages = new StringBuffer();
    }

    /**
     * Alignement du thésaurus vers la source Wikidata en Sparql et en retour du
     * Json
     *
     * @param idC
     * @param idTheso
     * @param lexicalValue
     * @param idLang
     * @param query
     * @param source
     * @return
     */
    public ArrayList<NodeAlignment> queryOpentheso(String idC, String idTheso,
            String lexicalValue, String idLang,
            String query, String source) {

        if (query.trim().equals("") ) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }        
        
        ArrayList<NodeAlignment> listeAlign = new ArrayList<>();
        // construction de la requête de type (webservices Opentheso)
        HttpsURLConnection cons = null;
        HttpURLConnection con = null;
        BufferedReader br;
        try {
            lexicalValue = URLEncoder.encode(lexicalValue, "UTF-8");
            lexicalValue = lexicalValue.replaceAll(" ", "%20");
            query = query.replace("##lang##", idLang);
            query = query.replace("##value##", lexicalValue);       
            URL url = new URL(query);
            if(query.startsWith("https://")) {
                cons = (HttpsURLConnection) url.openConnection();
                cons.setRequestMethod("GET");
                cons.setRequestProperty("Accept", "application/rdf+xml");
                if (cons.getResponseCode() != 200){
                    if (cons.getResponseCode() != 202) {
                        messages.append(cons.getResponseMessage());
                        return null;
                    }
                }
                br = new BufferedReader(new InputStreamReader((cons.getInputStream())));                
            }
            else {
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/rdf+xml");
                if (con.getResponseCode() != 200){
                    if (con.getResponseCode() != 202) {
                        messages.append(con.getResponseMessage());
                        return null;
                    }
                }
                br = new BufferedReader(new InputStreamReader((con.getInputStream())));                  
            }

            String output;
            String xmlRecord = "";
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
            }
            if(cons != null)
                cons.disconnect();
            if(con != null)
                con.disconnect();
            
            listeAlign = getValues(xmlRecord, idC, idLang, idTheso, source);
            br.close();
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return listeAlign;
    }

    private ArrayList<NodeAlignment> getValues(String xmlDatas,
            String idC, String idLang, String idTheso, String source) {

        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();
        
        InputStream inputStream;
        SKOSXmlDocument sxd;
        try {
            inputStream = new ByteArrayInputStream(xmlDatas.getBytes("UTF-8"));
            sxd = new ReadRDF4JNewGen().readRdfFlux(inputStream, RDFFormat.RDFXML, idLang);
            //ReadRdf4j readRdf4j = new ReadRdf4j(inputStream, 0, false, idLang); /// read XML SKOS
            //sxd = readRdf4j.getsKOSXmlDocument();

            for (SKOSResource resource : sxd.getConceptList()) {
                NodeAlignment na = new NodeAlignment();
                na.setInternal_id_concept(idC);
                na.setInternal_id_thesaurus(idTheso);
                na.setThesaurus_target(source);//"Pactols");
                na.setUri_target(resource.getUri());
                for(SKOSLabel label : resource.getLabelsList()) {
                    switch (label.getProperty()) {
                        case SKOSProperty.PREF_LABEL:
                            if(label.getLanguage().equals(idLang)) {
                                na.setConcept_target(label.getLabel());
                            }
                            break;
                        case SKOSProperty.ALT_LABEL:
                            if(label.getLanguage().equals(idLang)) {
                                if(StringUtils.isEmpty(na.getConcept_target_alt())) {
                                    na.setConcept_target_alt(label.getLabel());
                                } else {
                                    na.setConcept_target_alt(na.getConcept_target_alt() + ";" + label.getLabel());
                                }
                            }
                            break;                                
                        default:
                            break;
                    }
                }

                for(SKOSDocumentation sd : resource.getDocumentationsList()) {
                    if(sd.getProperty() == SKOSProperty.DEFINITION && sd.getLanguage().equals(idLang)) {
                        na.setDef_target(sd.getText());
                    }
                }
                listAlignValues.add(na);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(OpenthesoHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {            
            Logger.getLogger(OpenthesoHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listAlignValues;
    }
    
    /**
     * Cette fonction permet de récupérer les options du Getty AAT (images,
     * alignements, traductions....)
     *
     * @param selectedNodeAlignment
     * @param selectedOptions
     * @param thesaurusUsedLanguageWithoutCurrentLang
     * @param thesaurusUsedLanguage
     */
    public void setOptionsFromOpentheso(
            NodeAlignment selectedNodeAlignment,
            List<String> selectedOptions,
            ArrayList<String> thesaurusUsedLanguageWithoutCurrentLang,
            ArrayList<String> thesaurusUsedLanguage) {
        if (selectedNodeAlignment == null) {
            return;
        }
        CurlHelper curlHelper = new CurlHelper();
        curlHelper.setHeader1("Accept");
        curlHelper.setHeader2("application/json");

        String uri = selectedNodeAlignment.getUri_target();//."https://www.wikidata.org/entity/Q178401";//"https://www.wikidata.org/entity/Q178401";//Q7748";Q324926
        String datas = curlHelper.getDatasFromUriHttps(uri);
        String entity = uri.substring(uri.lastIndexOf("/") + 1);

        for (String selectedOption : selectedOptions) {
            switch (selectedOption) {
                case "langues":
                    resourceOpenthesoTraductions = getTraductions(datas, entity, thesaurusUsedLanguageWithoutCurrentLang);
                    break;
                case "notes":
                    resourceOpenthesoDefinitions = getDescriptions(datas, entity, thesaurusUsedLanguage);
                    break;
                case "images":
                    resourceOpenthesoImages = getImages(datas, entity);
                    break;
            }
        }
    }

    /**
     * récupération des traductions
     *
     * @param jsonDatas
     * @param entity
     * @param languages
     * @return
     */
    private ArrayList<SelectedResource> getTraductions(
            String jsonDatas, String entity,
            ArrayList<String> languages) {
        ArrayList<SelectedResource> traductions = new ArrayList<>();

        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;
        JsonValue jsonValue;

        String lang;
        String value;

        try {
            jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("labels");
        } catch (Exception e) {
            //System.err.println(e.toString());
            return null;
        }
        for (String language : languages) {
            try {
                SelectedResource selectedResource = new SelectedResource();
                jsonValue = jsonObject1.getJsonObject(language).get("language");
                lang = jsonValue.toString().replace("\"", "");
                selectedResource.setIdLang(lang);
                jsonValue = jsonObject1.getJsonObject(language).get("value");
                value = jsonValue.toString().replace("\"", "");
                selectedResource.setGettedValue(value);
                traductions.add(selectedResource);
            } catch (Exception e) {
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
            String jsonDatas, String entity,
            ArrayList<String> languages) {
        ArrayList<SelectedResource> descriptions = new ArrayList<>();
        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;
        JsonValue jsonValue;

        String lang;
        String value;

        try {
            jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("descriptions");
        } catch (Exception e) {
            //System.err.println(e.toString());
            return null;
        }

        for (String language : languages) {
            try {
                SelectedResource selectedResource = new SelectedResource();
                jsonValue = jsonObject1.getJsonObject(language).get("language");
                lang = jsonValue.toString().replace("\"", "");
                selectedResource.setIdLang(lang);
                jsonValue = jsonObject1.getJsonObject(language).get("value");
                value = jsonValue.toString().replace("\"", "");
                selectedResource.setGettedValue(value);
                descriptions.add(selectedResource);
            } catch (Exception e) {
            }
        }
        return descriptions;
    }

    /**
     * permet de récupérer les images de Wikidata
     *
     * @param jsonDatas
     * @param entity
     * @return
     */
    private ArrayList<SelectedResource> getImages(String jsonDatas, String entity) {
        // pour construire l'URL de Wikimedia, il faut ajouter 
        // http://commons.wikimedia.org/wiki/Special:FilePath/
        // puis le nom de l'image

        String fixedUrl = "https://commons.wikimedia.org/wiki/Special:FilePath/";

        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);

        //    JsonObject test = jsonObject.getJsonObject("entities");
        JsonObject jsonObject1;

        JsonObject jsonObject2;
        JsonValue jsonValue;

        ArrayList<SelectedResource> imagesUrls = new ArrayList<>();

        try {
            jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("claims");//.getJsonObject("P18");
        } catch (Exception e) {
            //System.err.println(e.toString());
            return null;
        }

        try {
            JsonArray jsonArray = jsonObject1.getJsonArray("P18");
            for (int i = 0; i < jsonArray.size(); i++) {
                SelectedResource selectedResource = new SelectedResource();
                jsonObject2 = jsonArray.getJsonObject(i);
                jsonValue = jsonObject2.getJsonObject("mainsnak").getJsonObject("datavalue").get("value");
                selectedResource.setGettedValue(fixedUrl + jsonValue.toString().replace("\"", ""));
                imagesUrls.add(selectedResource);
            }

        } catch (Exception e) {
        }
        return imagesUrls;
    }

    public String getMessages() {
        return messages.toString();
    }

    public ArrayList<SelectedResource> getResourceOpenthesoTraductions() {
        return resourceOpenthesoTraductions;
    }

    public ArrayList<SelectedResource> getResourceOpenthesoDefinitions() {
        return resourceOpenthesoDefinitions;
    }

    public ArrayList<SelectedResource> getResourceOpenthesoImages() {
        return resourceOpenthesoImages;
    }



}
