/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.core.alignment.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.core.json.helper.JsonHelper;
import org.apache.commons.lang3.ObjectUtils;

/**
 *
 * @author miled.rousset
 */
public class IdRefHelper {

    private StringBuffer messages;
    
    
    public IdRefHelper() {
        messages = new StringBuffer();
    }
  
    /**
     * Alignement du thésaurus vers la source IdRef Titre Uniforme en REST et en retour du Json
     */
    public ArrayList<NodeAlignment> queryIdRefUniformtitle(String idC, String idTheso, String lexicalValue, String query, String source) {

        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }
        
        String[] splitValues = lexicalValue.split(" ");
        
        String value = "";
        for (String splitValue : splitValues) {
            if(value.isEmpty())
                value = splitValue;
            else
                value = value + " AND " + splitValue;
        }

        ArrayList<NodeAlignment> listeAlign;
        try {
            value = URLEncoder.encode(value,"UTF-8");            
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

            conn.disconnect();
            listeAlign = getValuesSubject(records, idC, idTheso, source);

        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }                
            
    /**
     * Alignement du thésaurus vers la source IdRef Sujets en REST et en retour du Json
     * @return 
     */
    public ArrayList<NodeAlignment> queryIdRefLieux(String idC, String idTheso, String lexicalValue, String query, String source) {

        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }
        
        String[] splitValues = lexicalValue.split(" ");
        
        String value = "";
        
        for (String splitValue : splitValues) {
            if(value.isEmpty())
                value = splitValue;
            else
                value = value + " AND " + splitValue;
        }

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Opentheso)

        try {
            value = URLEncoder.encode(value,"UTF-8");            
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

            conn.disconnect();
            listeAlign = getValuesSubject(records, idC, idTheso, source);

        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }     
    
    /**
     * Alignement du thésaurus vers la source IdRef Sujets en REST et en retour du Json
     */
    public ArrayList<NodeAlignment> queryIdRefSubject(String idC, String idTheso, String lexicalValue, String query, String source) {

        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }
        
        String [] splitValues = lexicalValue.split(" ");
        
        String value = "";
        
        for (String splitValue : splitValues) {
            if(value.isEmpty())
                value = splitValue;
            else
                value = value + " AND " + splitValue;
        }

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Opentheso)

        try {
            value = URLEncoder.encode(value,"UTF-8");            
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

            conn.disconnect();
            listeAlign = getValuesSubject(records, idC, idTheso, source);
        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }   
    
    
    private ArrayList<NodeAlignment> getValuesSubject(String jsonDatas, String idC, String idTheso, String source) {

        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();

        try {
            JsonObject jsonObject = new JsonHelper().getJsonObject(jsonDatas);
            if(jsonObject == null || jsonObject.getJsonObject("response") == null)
                return listAlignValues;

            JsonArray jsonArray = jsonObject.getJsonObject("response").getJsonArray("docs");

            for (int i = 0; i < jsonArray.size(); i++) {
                NodeAlignment na = new NodeAlignment();
                na.setInternal_id_concept(idC);
                na.setInternal_id_thesaurus(idTheso);
                na.setDef_target("");
                na.setThesaurus_target(source);

                JsonObject jsonObject1 = jsonArray.getJsonObject(i);
                na.setConcept_target(jsonObject1.getString("affcourt_z"));
                na.setUri_target("https://www.idref.fr/" + jsonObject1.getString("ppn_z"));
                na.setUri_target("https://www.idref.fr/" + jsonObject1.getString("ppn_z"));

                listAlignValues.add(na);
            }

            return listAlignValues;
        } catch(Exception ex) {
            return listAlignValues;
        }
    }
    
    /**
     * Alignement du thésaurus vers la source IdRef Personnes en REST et en retour du Json
     */
    public ArrayList<NodeAlignment> queryIdRefPerson(String idC, String idTheso, String lexicalValue, String query, String source) {

        if (query.trim().equals("")) {
            return null;
        }
        if (lexicalValue.trim().equals("")) {
            return null;
        }
        
        String[] splitValues = lexicalValue.split(" ");
        
        String value = "";
        
        for (String splitValue : splitValues) {
            if(value.isEmpty())
                value = splitValue;
            else
                value = value + " AND " + splitValue;
        }

        ArrayList<NodeAlignment> listeAlign;

        try {
            value = URLEncoder.encode(value,"UTF-8");            
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

            conn.disconnect();
            listeAlign = getValuesNames(records, idC, idTheso, source);

        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }       
    
    /**
     * Alignement du thésaurus vers la source IdRef Names en REST et en retour du Json
     */
    public ArrayList<NodeAlignment> queryIdRefNames(String idC, String idTheso, String nom, String prenom, String query, String source) {

        nom = nom.trim();
        prenom = prenom.trim();
        if (query.trim().equals("")) {
            return null;
        }
        if (nom.isEmpty()) {
            query = query.replace("nom_t:(##nom##)%20AND%20", "");
        }
        if (prenom.isEmpty()) {
            query = query.replace("%20AND%20prenom_t:(##prenom##)", "");
        } 
        
        /// il faut ici séparer les valeurs des noms et prenoms 
        // pour ajouter des AND entre les valeurs multiples
        
        if(!nom.isEmpty()) {
            nom = clearName(nom);
        }
        if(!prenom.isEmpty()) {
            prenom = clearName(prenom);
        }        

        ArrayList<NodeAlignment> listeAlign;
        // construction de la requête de type (webservices Opentheso)

        try {
            nom = URLEncoder.encode(nom,"UTF-8");
            prenom = URLEncoder.encode(prenom,"UTF-8");

            query = query.replace("##nom##", nom);
            query = query.replace("##prenom##", prenom);
            
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

            conn.disconnect();
            listeAlign = getValuesNames(records, idC, idTheso, source);
        } catch (MalformedURLException e) {
            messages.append(e.toString());
            return null;
        } catch (IOException e) {
            messages.append(e.toString());
            return null;            
        }
        return listeAlign;
    }
    
    private String clearName(String nom){
        nom = nom.replaceAll("\\[|\\]" , "");        
        
        nom = nom.replaceAll(";", " AND ");            
        nom = nom.replaceAll(" ", " AND ");
        nom = nom.replaceAll("_", " AND ");
        nom = nom.replaceAll("-", " AND ");
        
       
        return nom;
    }
    
    
    private ArrayList<NodeAlignment> getValuesNames(String jsonDatas, String idC, String idTheso, String source) {

        ArrayList<NodeAlignment> listAlignValues = new ArrayList<>();

        try {
            JsonObject jsonObject = new JsonHelper().getJsonObject(jsonDatas);

            if(jsonObject == null || jsonObject.getJsonObject("response") == null)
                return listAlignValues;

            JsonArray jsonArray = jsonObject.getJsonObject("response").getJsonArray("docs");

            if (ObjectUtils.isNotEmpty(jsonArray)) {

                for (int i = 0; i < jsonArray.size(); i++) {
                    NodeAlignment na = new NodeAlignment();
                    na.setInternal_id_concept(idC);
                    na.setInternal_id_thesaurus(idTheso);
                    na.setThesaurus_target(source);

                    JsonObject jsonObject1 = jsonArray.getJsonObject(i);
                    na.setConcept_target(jsonObject1.getString("affcourt_z"));
                    na.setUri_target("https://www.idref.fr/" + jsonObject1.getString("ppn_z"));

                    // description
                    na.setDef_target("Noms=" + getValue(jsonObject1, "nom_s") + "/ Prenoms= "
                            + getValue(jsonObject1, "prenom_s"));

                    listAlignValues.add(na);
                }
            }
            return listAlignValues;
        } catch (Exception ex) {
            return listAlignValues;
        }
    }

    private String getValue(JsonObject jsonObject, String filedName) {
        String value = "";
        JsonArray prenomS = jsonObject.getJsonArray(filedName);
        if(prenomS != null) {
            for (int j = 0; j < prenomS.size(); j++) {
                if(j == 0)
                    value = prenomS.getString(j);
                else
                    value = value+ "; " + prenomS.getString(j);
            }
        }
        return value;
    }

    public String getMessages() {
        return messages.toString();
    }

}
