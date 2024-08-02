package fr.cnrs.opentheso.core.alignment.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import lombok.Data;


/**
 *
 * @author miled.rousset
 */
@Data
public class GemetHelper {

    private StringBuffer messages = new StringBuffer();

    // les informations récupérées de Wikidata
    private List<SelectedResource> resourceTraductions;
    private List<SelectedResource> resourceDefinitions;
    private List<SelectedResource> resourceImages;


    /**
     * Alignement du thésaurus vers la source Wikidata en Sparql et en retour du Json
     */
    public List<NodeAlignment> queryGemet(String idC, String idTheso, String value, String lang, String query, String source) {

        List<NodeAlignment> listeAlign;

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
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
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
            byte[] bytes = records.getBytes();
            records = new String(bytes, Charset.forName("UTF-8"));
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

    private List<NodeAlignment> getValues(String value, String jsonDatas, String idC, String idTheso, String source) {

        List<NodeAlignment> listAlignValues = new ArrayList<>();

        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonDatas))) {

            JsonArray jsonArray = jsonReader.readArray();

            for (int i = 0; i < jsonArray.size(); ++i) {

                String title = jsonArray.getJsonObject(i).getJsonObject("preferredLabel").getString("string");
                String uri = jsonArray.getJsonObject(i).getString("uri").replaceAll("http://", "https://");

                NodeAlignment na = new NodeAlignment();
                //si le titre est équivalent, on le place en premier
                if (value.trim().equalsIgnoreCase(title.trim())) {
                    na.setConcept_target(title);
                    na.setDef_target("");
                    na.setInternal_id_concept(idC);
                    na.setInternal_id_thesaurus(idTheso);
                    na.setThesaurus_target(source);
                    na.setUri_target(uri);
                    listAlignValues.add(0, na);
                } else {
                    na.setConcept_target(title);
                    na.setDef_target("");
                    na.setInternal_id_concept(idC);
                    na.setInternal_id_thesaurus(idTheso);
                    na.setThesaurus_target(source);
                    na.setUri_target(uri);
                    listAlignValues.add(na);
                }
            }
        } catch (Exception e) {
            messages.append(e.toString());
            return null;
        }
        return listAlignValues;
    }

    /**
     * Cette fonction permet de récupérer les options de Wikidata Images, alignements, traductions....ource
     */
    public void setOptions(NodeAlignment selectedNodeAlignment, List<String> selectedOptions,
            List<String> thesaurusUsedLanguageWithoutCurrentLang, List<String> thesaurusUsedLanguage) {

        if (selectedNodeAlignment == null) {
            return;
        }

        // uri traductions
        String uriLang = "https://www.eionet.europa.eu/gemet/getAllTranslationsForConcept?concept_uri="
                + selectedNodeAlignment.getUri_target().replaceAll("https://", "http://")
                + "&property_uri=http://www.w3.org/2004/02/skos/core%23prefLabel";
        // uri défintions
        String uriDefinition = "https://www.eionet.europa.eu/gemet/getAllTranslationsForConcept?concept_uri="
                + selectedNodeAlignment.getUri_target().replaceAll("https://", "http://")
                + "&property_uri=http://www.w3.org/2004/02/skos/core%23definition";
        // pas de négociation de contenu, donc récupération via requete Get 

        CurlHelper curlHelper = new CurlHelper();
        curlHelper.setHeader1("Accept");
        curlHelper.setHeader2("application/json");

        for (String selectedOption : selectedOptions) {
            switch (selectedOption) {
                case "langues":
                    resourceTraductions = getTraductions(curlHelper.getDatasFromUriHttps(uriLang),
                            thesaurusUsedLanguageWithoutCurrentLang);
                    break;
                case "notes":
                    resourceDefinitions = getDescriptions(curlHelper.getDatasFromUriHttps(uriDefinition),
                            thesaurusUsedLanguage);
            }
        }
    }

    /**
     * récupération des traductions
     */
    private List<SelectedResource> getTraductions(String jsonDatas, List<String> languages) {

        List<SelectedResource> traductions = new ArrayList<>();
        
        // lecture du fichier Json des langues
        try (JsonReader reader = Json.createReader(new StringReader(jsonDatas))) {
            JsonArray dataArray = reader.readArray();
            for (int i = 0; i < dataArray.size(); ++i) {
                JsonObject dataObject = dataArray.getJsonObject(i);
                String lang = dataObject.getString("language");
                String value = dataObject.getString("string");

                if(lang == null || value == null || lang.isEmpty() || value.isEmpty())  continue;

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
     */
    private List<SelectedResource> getDescriptions(String jsonDatas, List<String> languages) {

        List<SelectedResource> descriptions = new ArrayList<>();
        
        // lecture du fichier Json des langues
        try (JsonReader reader = Json.createReader(new StringReader(jsonDatas))) {
            JsonArray dataArray = reader.readArray();
            for (int i = 0; i < dataArray.size(); ++i) {
                JsonObject dataObject = dataArray.getJsonObject(i);
                SelectedResource selectedResource = new SelectedResource();
                String lang = dataObject.getString("language");
                String value = dataObject.getString("string");

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

}
