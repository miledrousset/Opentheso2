package fr.cnrs.opentheso.ws.ark;

import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.datas.Qualifier;
import java.util.ArrayList;
import fr.cnrs.opentheso.bdd.tools.FileUtilities;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

public final class NodeJson2 {

    private String token;
    private String ark;
    private String naan;    
    private String type; // prefix
    private String urlTarget;
    private String title;
    private String creator;
    private boolean useHandle;
    private String modificationDate;
    private ArrayList <DcElement> dcElements;    
    private ArrayList<Qualifier> qualifiers;    
    
    public NodeJson2() {
    }
    
    /**
     * Permet de retourner les valeurs déifnies sous le format Json String
     * @return 
     */
    public String getJsonString() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("token", token);
        job.add("ark", ark);
        job.add("naan", naan);
        job.add("type", type);
        job.add("urlTarget", urlTarget);
        job.add("title", title);
        job.add("creator", creator);
        job.add("useHandle", useHandle);
        job.add("modificationDate", new FileUtilities().getDate());
        
        job.add("dcElements", getStringFromDcElements());        
        job.add("qualifiers", getStringFromQualifiers()); 

        return job.build().toString();
    }
    
    /**
     * Pour la mise à jour des URI uniquement pour Ark
     * @return 
     */
    public String getJsonUriString() {
        String arkString = "{"
                + "\"token\":\""+ token + "\","
                + "\"ark\":\""+ ark + "\","
                + "\"naan\":\"" + naan + "\","
                + "\"urlTarget\":\""+ urlTarget + "\","
                + "\"useHandle\":" + useHandle
                + "}";
        return arkString;
    }    
    
    private JsonArray getStringFromDcElements(){
       JsonArrayBuilder jsonArrayBuilderDC = Json.createArrayBuilder();
        if(dcElements == null) {
            return jsonArrayBuilderDC.build();
        }
        for (DcElement dcElement : dcElements) {
            JsonObjectBuilder jobLine = Json.createObjectBuilder();
            jobLine.add("name", dcElement.getName());
            jobLine.add("value", dcElement.getValue());
            jobLine.add("language", dcElement.getLanguage());
            jsonArrayBuilderDC.add(jobLine.build());
        }
        return jsonArrayBuilderDC.build();
    }
    
    private JsonArray getStringFromQualifiers(){
       JsonArrayBuilder jsonArrayBuilderQual = Json.createArrayBuilder();
        if(qualifiers == null) {
            return jsonArrayBuilderQual.build();
        }
        for (Qualifier qualifier : qualifiers) {
            JsonObjectBuilder jobLine = Json.createObjectBuilder();
            jobLine.add("qualifier", qualifier.getQualifier());
            jobLine.add("url_target", qualifier.getUrlTarget());
            jsonArrayBuilderQual.add(jobLine.build());
        }
        return jsonArrayBuilderQual.build();        
    }    
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getArk() {
        return ark;
    }

    public void setArk(String ark) {
        this.ark = ark;
    }

    public String getNaan() {
        return naan;
    }

    public void setNaan(String naan) {
        this.naan = naan;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrlTarget() {
        return urlTarget;
    }

    public void setUrlTarget(String urlTarget) {
        this.urlTarget = urlTarget;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isUseHandle() {
        return useHandle;
    }

    public void setUseHandle(boolean useHandle) {
        this.useHandle = useHandle;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public ArrayList<DcElement> getDcElements() {
        return dcElements;
    }

    public void setDcElements(ArrayList<DcElement> dcElements) {
        this.dcElements = dcElements;
    }

    public ArrayList<Qualifier> getQualifiers() {
        return qualifiers;
    }

    public void setQualifiers(ArrayList<Qualifier> qualifiers) {
        this.qualifiers = qualifiers;
    }

}