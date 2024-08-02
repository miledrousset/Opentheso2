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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.json.helper.JsonHelper;

/**
 *
 * @author miled.rousset
 */
public class GettyAATHelper {

    private StringBuffer messages;
    // private ArrayList<NodeAlignment> listAlignValues;

    // les informations récupérées de Wikidata
    private ArrayList<SelectedResource> resourceAATTraductions;
    private ArrayList<SelectedResource> resourceAATDefinitions;
    private ArrayList<SelectedResource> resourceAATImages;

    public GettyAATHelper() {
        messages = new StringBuffer();
    }

    /**
     * Alignement du thésaurus vers la source Wikidata en Sparql et en retour du
     * Json
     *
     * @param idC
     * @param idTheso
     * @param lexicalValue
     * @param lang
     * @param query
     * @param source
     * @return
     */
    public ArrayList<NodeAlignment> queryAAT(String idC, String idTheso,
            String lexicalValue, String lang,
            String query, String source) {

        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Getty)

        try {
            lexicalValue = URLEncoder.encode(lexicalValue, "UTF-8");
            query = query.replace("##value##", lexicalValue);
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/xml");

            if (conn.getResponseCode() != 200) {
                messages.append(conn.getResponseMessage());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String output;
            String xmlRecord = "";
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
            }
            conn.disconnect();

            listeAlign = getValues(xmlRecord, idC, idTheso, source);
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

    private ArrayList<NodeAlignment> getValues(String xmlDatas,
            String idC, String idTheso, String source) {

        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();

        String uri = "http://vocab.getty.edu/page/aat/";

        try {
            String localName = "";
            String text;
            String originalText;
            String id;
            //    try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader r = factory.createXMLStreamReader(new StringReader(xmlDatas));
            NodeAlignment nodeAlignment = new NodeAlignment();
            while (r.hasNext()) {
                int event = r.next();
                if (event == r.START_ELEMENT) {
                    if (r.hasName()) {
                        localName = r.getLocalName();
                    }
                }
                if (event == r.CHARACTERS) {
                    // le term dans la langue source du Getty
                    if (localName.equalsIgnoreCase("preferred_term")) {
                        originalText = new String(r.getTextCharacters(), r.getTextStart(), r.getTextLength());
                        if (!originalText.trim().isEmpty()) {
                            nodeAlignment.setDef_target(originalText);
                        }
                    }
                    // uri du concept
                    if (localName.equalsIgnoreCase("subject_id")) {
                        id = new String(r.getTextCharacters(), r.getTextStart(), r.getTextLength());
                        if (!id.trim().isEmpty()) {
                            nodeAlignment.setUri_target(uri + id);
                        }
                    }
                    // le texte recherché avec la langue en cours 
                    if (localName.equalsIgnoreCase("term")) {
                        text = new String(r.getTextCharacters(), r.getTextStart(), r.getTextLength());
                        if (!text.trim().isEmpty()) {
                            nodeAlignment.setConcept_target(text);
                        }
                    }
                }
                if (event == r.END_ELEMENT) {
                    if (r.hasName()) {
                        localName = r.getLocalName();
                    }
                    if (localName.equalsIgnoreCase("subject")) {
                        nodeAlignment.setInternal_id_concept(idC);
                        nodeAlignment.setInternal_id_thesaurus(idTheso);
                        nodeAlignment.setThesaurus_target(source);
                        listAlignValues.add(nodeAlignment);
                        nodeAlignment = new NodeAlignment();
                    }
                }
            }

        } catch (XMLStreamException ex) {
            messages.append(ex.toString());
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
    public void setOptionsFromAAT(
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
                    resourceAATTraductions = getTraductions(datas, entity, thesaurusUsedLanguageWithoutCurrentLang);
                    break;
                case "notes":
                    resourceAATDefinitions = getDescriptions(datas, entity, thesaurusUsedLanguage);
                    break;
                case "images":
                    resourceAATImages = getImages(datas, entity);
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

    public ArrayList<SelectedResource> getResourceAATTraductions() {
        return resourceAATTraductions;
    }

    public ArrayList<SelectedResource> getResourceAATDefinitions() {
        return resourceAATDefinitions;
    }

    public ArrayList<SelectedResource> getResourceAATImages() {
        return resourceAATImages;
    }

}
