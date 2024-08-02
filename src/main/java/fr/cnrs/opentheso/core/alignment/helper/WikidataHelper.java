/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

//import com.bordercloud.sparql.Endpoint;
//import com.bordercloud.sparql.EndpointException;
import org.apache.jena.query.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.alignment.SelectedResource;
import fr.cnrs.opentheso.core.json.helper.JsonHelper;
import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import jakarta.json.Json;
import jakarta.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author miled.rousset
 */
@Data
public class WikidataHelper {

    private StringBuffer messages;
    // les informations récupérées de Wikidata
    private List<SelectedResource> resourceWikidataTraductions;
    private List<SelectedResource> resourceWikidataDefinitions;
    private List<SelectedResource> resourceWikidataImages;

    public WikidataHelper() {
        messages = new StringBuffer();
    }

    public ArrayList<NodeAlignment> queryWikidata_rest(String idC, String idTheso,
            String lexicalValue, String lang,
            String query, String source) {
        
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
            lexicalValue = URLEncoder.encode(lexicalValue, "UTF-8");
            lexicalValue = lexicalValue.replaceAll(" ", "%20");
            query = query.replaceAll("##value##", lexicalValue);
            query = query.replaceAll("##lang##", lang);            
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
            
        } catch (Exception e) {
        }
        return listeAlign;        
     }
    
    private ArrayList<NodeAlignment> getValuesFromJson(String jsonValue, String idConcept, String idTheso, String source){
        JsonObject object;
        JsonArray jsonArray;
        JsonObject value;   
       
        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();
        
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(jsonValue));
            object = jsonReader.readObject();
            jsonArray = object.getJsonArray("search");

            for (int i = 0; i < jsonArray.size(); i++) {
                NodeAlignment na = new NodeAlignment();
                na.setInternal_id_concept(idConcept);
                na.setInternal_id_thesaurus(idTheso);
                value = jsonArray.getJsonObject(i);
                // label ou Nom
                try {
                     na.setConcept_target(value.getJsonObject("match").getString("text"));
                } catch (Exception e) {
                    continue;
                }                    

                // description
                try {
                    if(value.getString("description") != null) {
                        na.setDef_target(value.getString("description"));
                    } else {
                        na.setDef_target("");
                    }
                } catch (Exception e) {
                    continue;
                }                

                na.setThesaurus_target(source);

                // URI
                try {
                    na.setUri_target(value.getString("concepturi"));
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
  
    /**
     * Alignement du thésaurus vers la source Wikidata en Sparql et en retour du
     * Json
     *
     * @param idC
     * @param idTheso
     * @param lexicalValue
     * @param lang
     * @param requete
     * @param source
     * @return
     */
    public List<NodeAlignment> queryWikidata_sparql(String idC, String idTheso, String lexicalValue, String lang,
            String requete, String source) {

        List<NodeAlignment> listAlignValues = new ArrayList<>();
        
        try {
            Endpoint sp = new Endpoint("https://query.wikidata.org/sparql", false);
            requete = requete.replaceAll("##value##", lexicalValue);
            requete = requete.replaceAll("##lang##", lang);

            HashMap<String, HashMap> rs = sp.query(requete);
            if (rs == null) {
                return null;
            }

            List<HashMap<String, Object>> rows_queryWikidata = (ArrayList) rs.get("result").get("rows");

            for (HashMap<String, Object> hashMap : rows_queryWikidata) {
                NodeAlignment na = new NodeAlignment();
                na.setInternal_id_concept(idC);
                na.setInternal_id_thesaurus(idTheso);

                // label ou Nom
                if (hashMap.get("itemLabel") != null) {
                    na.setConcept_target(hashMap.get("itemLabel").toString());
                } else {
                    continue;
                }

                // description
                if (hashMap.get("itemDescription") != null) {
                    na.setDef_target(hashMap.get("itemDescription").toString());
                } else {
                    na.setDef_target("");
                }

                na.setThesaurus_target(source);

                // URI
                if (hashMap.get("item") != null) {
                    na.setUri_target(hashMap.get("item").toString());
                } else {
                    continue;
                }
                listAlignValues.add(na);
            }
        } catch (EndpointException eex) {
            messages.append(eex.toString());
            System.err.println(eex.toString());
            return null;
        } catch (Exception e) {
            messages.append(requete);
            messages.append(e.toString());
            messages.append(" ou pas de connexion internet !! ");
            System.err.println(e.toString());
            return null;
        }
        return listAlignValues;
    }
    
    
    public void test(){
        // Définir l'URL du service SPARQL (par exemple, DBpedia)
        String sparqlEndpoint = "https://dbpedia.org/sparql";

        // Définir la requête SPARQL
        String sparqlQuery = "SELECT ?subject ?predicate ?object WHERE { ?subject ?predicate ?object } LIMIT 10";

        // Créer une QueryExecution pour exécuter la requête
        Query query = QueryFactory.create(sparqlQuery);
        try (QueryExecution qexec = QueryExecutionFactory.sparqlService(sparqlEndpoint, query)) {
            // Exécuter la requête et obtenir les résultats
            ResultSet results = qexec.execSelect();

            // Afficher les résultats
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                System.out.println(soln);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    

    /**
     * Cette fonction permet de récupérer les options de Wikidata Images,
     * alignements, traductions....ource
     *
     * @param selectedNodeAlignment
     * @param selectedOptions
     * @param thesaurusUsedLanguageWithoutCurrentLang
     * @param thesaurusUsedLanguage
     */
    public void setOptionsFromWikidata(NodeAlignment selectedNodeAlignment, List<String> selectedOptions,
            List<String> thesaurusUsedLanguageWithoutCurrentLang, List<String> thesaurusUsedLanguage) {
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
                    resourceWikidataTraductions = getTraductions(datas, entity, thesaurusUsedLanguageWithoutCurrentLang);
                    break;
                case "notes":
                    resourceWikidataDefinitions = getDescriptions(datas, entity, thesaurusUsedLanguage);
                    break;
                case "images":
                    resourceWikidataImages = getImages(datas, entity);
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
            List<String> languages) {
        ArrayList<SelectedResource> traductions = new ArrayList<>();

        JsonHelper jsonHelper = new JsonHelper();
        JsonObject jsonObject = jsonHelper.getJsonObject(jsonDatas);
        JsonObject jsonObject1;
        JsonValue jsonValue;

        String lang;
        String value;

        try {
            jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("labels");
        } catch (Exception e) {
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
    private List<SelectedResource> getDescriptions(String jsonDatas, String entity, List<String> languages) {

        ArrayList<SelectedResource> descriptions = new ArrayList<>();
        JsonObject jsonObject = new JsonHelper().getJsonObject(jsonDatas);

        JsonObject jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("descriptions");
        for (String language : languages) {
            try {
                SelectedResource selectedResource = new SelectedResource();
                JsonValue jsonValue = jsonObject1.getJsonObject(language).get("language");
                selectedResource.setIdLang(jsonValue.toString().replace("\"", ""));
                jsonValue = jsonObject1.getJsonObject(language).get("value");
                selectedResource.setGettedValue(jsonValue.toString().replace("\"", ""));
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
        JsonObject jsonObject1;
        JsonObject jsonObject2;
        JsonValue jsonValue;

        ArrayList<SelectedResource> imagesUrls = new ArrayList<>();

        try {
            jsonObject1 = jsonObject.getJsonObject("entities").getJsonObject(entity).getJsonObject("claims");//.getJsonObject("P18");
        } catch (Exception e) {
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

}
