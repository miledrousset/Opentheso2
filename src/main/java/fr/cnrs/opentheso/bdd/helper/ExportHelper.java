package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import jakarta.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;

import fr.cnrs.opentheso.bdd.tools.StringPlus;
import org.jsoup.Jsoup;


public class ExportHelper {

    private final static String SEPERATEUR = "##";
    private final static String SUB_SEPERATEUR = "@@";

    
    public List<SKOSResource> getAllFacettes(HikariDataSource ds, String idTheso, String baseUrl, 
            String originalUri, NodePreference nodePreference) throws Exception {
        
        List<SKOSResource> facettes = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * FROM opentheso_get_facettes('" + idTheso + "', '" + baseUrl + "') as (id_facet VARCHAR, "
                        + "lexical_value VARCHAR, created timestamp with time zone, modified timestamp with time zone, "
                        + "lang VARCHAR, id_concept_parent VARCHAR, uri_value VARCHAR, "
                        + "definition text, example text, editorialNote text, changeNote text, "
                        + " secopeNote text, note text, historyNote text "
                        + ")");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        SKOSResource sKOSResource = new SKOSResource(getUriForFacette(resultSet.getString("id_facet"), 
                                idTheso, originalUri), SKOSProperty.FACET);
                        
                        sKOSResource.addRelation(resultSet.getString("id_facet"), resultSet.getString("uri_value"), SKOSProperty.SUPER_ORDINATE);
                        sKOSResource.setIdentifier(resultSet.getString("id_facet"));
                        List<String> members = new FacetHelper().getAllMembersOfFacet(ds, resultSet.getString("id_facet"), idTheso);
                        for (String idConcept : members) {
                            NodeUri nodeUri = new ConceptHelper().getNodeUriOfConcept(ds, idConcept, idTheso);
                            sKOSResource.addRelation(nodeUri.getIdConcept(), getUriFromNodeUri(idTheso, originalUri, 
                                    idConcept, nodePreference, nodeUri),  SKOSProperty.MEMBER);
                        }
        
                        sKOSResource.addLabel(resultSet.getString("lexical_value"), resultSet.getString("lang"), SKOSProperty.PREF_LABEL);
                        sKOSResource.addDate(resultSet.getString("created"), SKOSProperty.CREATED);
                        sKOSResource.addDate(resultSet.getString("modified"), SKOSProperty.MODIFIED);
        
                        addDocumentation(resultSet.getString("definition"), sKOSResource, SKOSProperty.DEFINITION);
                        addDocumentation(resultSet.getString("note"), sKOSResource, SKOSProperty.NOTE);
                        addDocumentation(resultSet.getString("editorialNote"), sKOSResource, SKOSProperty.EDITORIAL_NOTE);
                        addDocumentation(resultSet.getString("secopeNote"), sKOSResource, SKOSProperty.SCOPE_NOTE);
                        addDocumentation(resultSet.getString("historyNote"), sKOSResource, SKOSProperty.HISTORY_NOTE);
                        addDocumentation(resultSet.getString("example"), sKOSResource, SKOSProperty.EXAMPLE);
                        addDocumentation(resultSet.getString("changeNote"), sKOSResource, SKOSProperty.CHANGE_NOTE);                        
                        
                        facettes.add(sKOSResource);
                    }
                }
            }
        }

        return facettes;
    }
    
    
    private String getUriForFacette(String idFacet, String idTheso, String originalUri){
        if(FacesContext.getCurrentInstance() == null) {
            return originalUri;
        }
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        path = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        
        return path + "/?idf=" + idFacet + "&idt=" +idTheso;
    }

    /**
     * Nouvelle version pour récupérer un thésaurus entier en utilisant les 
     * requêtes plpgsql 
     * @param ds
     * @param idTheso
     * @param baseUrl
     * @param idGroup
     * @param originalUri
     * @param nodePreference
     * @return
     * @throws Exception 
     */
    public List<SKOSResource> getAllConcepts(HikariDataSource ds, String idTheso,
            String baseUrl, String idGroup, String originalUri, NodePreference nodePreference, boolean filterHtmlCharacter) throws Exception {

        List<SKOSResource> concepts = new ArrayList<>();
        String [] contributors;
        String note;
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery(getSQLRequest(idTheso, baseUrl, idGroup));
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        SKOSResource sKOSResource = new SKOSResource();
                        sKOSResource.setProperty(SKOSProperty.CONCEPT);
                        sKOSResource.setUri(resultSet.getString("uri"));
                        sKOSResource.setLocalUri(resultSet.getString("local_uri"));

                        sKOSResource.addIdentifier(resultSet.getString("identifier"), SKOSProperty.IDENTIFIER);

                        if (!StringUtils.isEmpty(resultSet.getString("ark_id"))) {
                            sKOSResource.setArkId(resultSet.getString("ark_id"));
                        }

                        setStatusOfConcept(resultSet.getString("type"), sKOSResource);

                        getLabels(resultSet.getString("prefLab"), sKOSResource, SKOSProperty.PREF_LABEL);
                        getLabels(resultSet.getString("altLab_hiden"), sKOSResource, SKOSProperty.HIDDEN_LABEL);
                        getLabels(resultSet.getString("altLab"), sKOSResource, SKOSProperty.ALT_LABEL);

                        if (resultSet.getString("broader") == null || resultSet.getString("broader").isEmpty()) {
                            sKOSResource.getRelationsList().add(new SKOSRelation(idTheso, getUriThesoFromId(idTheso, originalUri, nodePreference),
                                    SKOSProperty.TOP_CONCEPT_OF));
                        }

                        addRelationsGiven(resultSet.getString("related"), sKOSResource);
                        
                        note = resultSet.getString("definition");
                        if(StringUtils.isNotEmpty(note)){
                            if(filterHtmlCharacter)
                                note = Jsoup.parse(note).text();
                            addDocumentation(note, sKOSResource, SKOSProperty.DEFINITION);
                        }
                        
                        note = resultSet.getString("note");
                        if(StringUtils.isNotEmpty(note)){
                            if(filterHtmlCharacter)
                                note = Jsoup.parse(note).text();
                            addDocumentation(note, sKOSResource, SKOSProperty.NOTE);
                        }        
                        
                        note = resultSet.getString("editorialNote");
                        if(StringUtils.isNotEmpty(note)){
                            if(filterHtmlCharacter)
                                note = Jsoup.parse(note).text();
                            addDocumentation(note, sKOSResource, SKOSProperty.EDITORIAL_NOTE);
                        }   
                        note = resultSet.getString("secopeNote");
                        if(StringUtils.isNotEmpty(note)){
                            if(filterHtmlCharacter)
                                note = Jsoup.parse(note).text();
                            addDocumentation(note, sKOSResource, SKOSProperty.SCOPE_NOTE);
                        }           
                        note = resultSet.getString("historyNote");
                        if(StringUtils.isNotEmpty(note)){
                            if(filterHtmlCharacter)
                                note = Jsoup.parse(note).text();
                            addDocumentation(note, sKOSResource, SKOSProperty.HISTORY_NOTE);
                        }   
                        note = resultSet.getString("example");
                        if(StringUtils.isNotEmpty(note)){
                            if(filterHtmlCharacter)
                                note = Jsoup.parse(note).text();
                            addDocumentation(note, sKOSResource, SKOSProperty.EXAMPLE);
                        }         
                        note = resultSet.getString("changeNote");
                        if(StringUtils.isNotEmpty(note)){
                            if(filterHtmlCharacter)
                                note = Jsoup.parse(note).text();
                            addDocumentation(note, sKOSResource, SKOSProperty.CHANGE_NOTE);
                        }                         

                        addAlignementGiven(resultSet.getString("broadMatch"), sKOSResource, SKOSProperty.BROAD_MATCH);
                        addAlignementGiven(resultSet.getString("closeMatch"), sKOSResource, SKOSProperty.CLOSE_MATCH);
                        addAlignementGiven(resultSet.getString("exactMatch"), sKOSResource, SKOSProperty.EXACT_MATCH);
                        addAlignementGiven(resultSet.getString("narrowMatch"), sKOSResource, SKOSProperty.NARROWER_MATCH);
                        addAlignementGiven(resultSet.getString("relatedmatch"), sKOSResource, SKOSProperty.RELATED_MATCH);

                        addRelationsGiven(resultSet.getString("narrower"), sKOSResource);

                        if (resultSet.getString("broader") != null) {
                            addRelationsGiven(resultSet.getString("broader"), sKOSResource);
                        }

                        sKOSResource.addRelation(idTheso, getUriThesoFromId(idTheso, originalUri, nodePreference), SKOSProperty.INSCHEME);
                        
                        addReplaced(resultSet.getString("replaces"), sKOSResource, SKOSProperty.REPLACES);
                        
                        addReplaced(resultSet.getString("replaced_by"), sKOSResource, SKOSProperty.IS_REPLACED_BY);
                        
                        if (!StringUtils.isEmpty(resultSet.getString("notation"))) {
                            sKOSResource.addNotation(resultSet.getString("notation"));
                        }

                        addImages(sKOSResource, resultSet.getString("img"));
                        
                        addMembres(sKOSResource, resultSet.getString("membre"), resultSet.getString("IDENTIFIER"));

                        addFacets(sKOSResource, resultSet.getString("facets"), idTheso, originalUri);
                        
                        addExternalResources(sKOSResource, resultSet.getString("externalResources"));

                        addGps(sKOSResource, resultSet.getString("gpsData"));
                        
                        if (resultSet.getString("creator") != null) {
                            sKOSResource.addAgent(resultSet.getString("creator"), SKOSProperty.CREATOR);
                        }
                        
                        if(!StringUtils.isEmpty(resultSet.getString("contributor"))){
                            contributors = resultSet.getString("contributor").split(SEPERATEUR);
                            for (String contributor : contributors) {
                                sKOSResource.addAgent(contributor, SKOSProperty.CONTRIBUTOR);
                            }
                        }

                        String created = resultSet.getString("created");
                        if (StringUtils.isNotEmpty(created)) {
                            sKOSResource.addDate(created.substring(0, created.indexOf(" ")), SKOSProperty.CREATED);
                        }

                        String modified = resultSet.getString("modified");
                        if (StringUtils.isNotEmpty(modified)) {
                            sKOSResource.addDate(modified.substring(0, modified.indexOf(" ")), SKOSProperty.MODIFIED);
                        }

                    /*    ArrayList<String> first = new ArrayList<>();
                        first.add(resultSet.getString("identifier"));
                        ArrayList<ArrayList<String>> paths = new ArrayList<>();

                        paths = new ConceptHelper().getPathOfConceptWithoutGroup(ds,
                                resultSet.getString("identifier"), idTheso, first, paths);
                        ArrayList<String> pathFromArray = getPathFromArray(paths);
                        if (!pathFromArray.isEmpty()) {
                            sKOSResource.setPaths(pathFromArray);
                        }*/

                        concepts.add(sKOSResource);
                    //System.out.println(">> " + "Ajout d'un concept " + sKOSResource.getIdentifier());                         
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println(">> " + sqle.getMessage());
        }

        return concepts;
    }

    private void addGps(SKOSResource sKOSResource, String str) {
        if (StringUtils.isNotEmpty(str)) {
            String[] tabs = str.split(SEPERATEUR);

            List<SKOSGPSCoordinates> tmp = new ArrayList<>();
            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                tmp.add(new SKOSGPSCoordinates(Double.parseDouble(element[0]), Double.parseDouble(element[1])));
            }
            sKOSResource.setGpsCoordinates(tmp);
        }
    }

    private String getUriFromId(String id, String originalUri, NodePreference nodePreference) {
        if(nodePreference.isOriginalUriIsArk()) {
            return nodePreference.getOriginalUri()+ "/" + nodePreference.getIdNaan() + "/" + id;
        }
        if(nodePreference.isOriginalUriIsHandle()) {
            return "https://hdl.handle.net/" + id;
        }
        
        if (originalUri != null && !originalUri.isEmpty()) {
            return originalUri + "/" + id;
        } else {
            return getPath(originalUri) + "/" + id;
        }
    }
    
    private String getUriThesoFromId(String id, String originalUri, NodePreference nodePreference) {
        if(nodePreference.isOriginalUriIsArk()) {
            return nodePreference.getOriginalUri()+ "/" + nodePreference.getIdNaan() + "/" + id;
        }
        if(nodePreference.isOriginalUriIsHandle()) {
            return "https://hdl.handle.net/" + id;
        }
        
        if (originalUri != null && !originalUri.isEmpty()) {
            return originalUri + "/?idt=" + id;
        } else {
            return getPath(originalUri) + "/?idt=" + id;
        }
    }    

    private String getPath(String originalUri) {
        if (FacesContext.getCurrentInstance() == null) {
            return originalUri;
        }

        return FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin")
                + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
    }

    private String getSQLRequest(String idTheso, String baseUrl, String idGroup) {
        String baseSQL = "SELECT * FROM ";
        if (StringUtils.isEmpty(idGroup)) {
            baseSQL = baseSQL + "opentheso_get_concepts('" + idTheso + "', '" + baseUrl;
        } else {
            baseSQL = baseSQL + "opentheso_get_concepts_by_group('" + idTheso + "', '" + baseUrl + "', '" + idGroup;
        }
        return baseSQL + "') as x(URI text, TYPE varchar, LOCAL_URI text, IDENTIFIER varchar, ARK_ID varchar, "
                + "prefLab varchar, altLab varchar, altLab_hiden varchar, definition text, example text, editorialNote text, changeNote text, "
                + "secopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, "
                + "closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, gpsData text, "
                + "membre text, created timestamp with time zone, modified timestamp with time zone, img text, creator text, contributor text, "
                + "replaces text, replaced_by text, facets text, externalResources text);";
    }

    private ArrayList<String> getPathFromArray(ArrayList<ArrayList<String>> paths) {
        String pathFromArray = "";
        ArrayList<String> allPath = new ArrayList<>();
        for (ArrayList<String> path : paths) {
            for (String string1 : path) {
                if (pathFromArray.isEmpty()) {
                    pathFromArray = string1;
                } else {
                    pathFromArray = pathFromArray + SEPERATEUR + string1;
                }
            }
            allPath.add(pathFromArray);
            pathFromArray = "";
        }
        return allPath;
    }

    private void addImages(SKOSResource resource, String textBrut) {
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] images = textBrut.split(SEPERATEUR);
            ArrayList<NodeImage> nodeImages = new ArrayList<>();
            for (String image : images) {
                String[] imageDetail = image.split(SUB_SEPERATEUR);
               // if(imageDetail.length != 4) return;
                
                NodeImage nodeImage = new NodeImage();
                nodeImage.setImageName(imageDetail[0]);
                nodeImage.setCopyRight(imageDetail[1]);
                nodeImage.setUri(imageDetail[2]);
                if(imageDetail.length >= 4)
                    nodeImage.setCreator(imageDetail[3]);
                nodeImages.add(nodeImage);
            }
            resource.setNodeImage(nodeImages);
        }
    }

    private void addFacets(SKOSResource resource, String textBrut, String idTheso, String originalUri) {
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] idFacettes = textBrut.split(SEPERATEUR);
            for (String idFacette : idFacettes) {
                String url = getPath(originalUri)+ "/?idf=" + idFacette + "&idt=" +idTheso;
                resource.addRelation(idFacette, url, SKOSProperty.SUB_ORDINATE_ARRAY);
            }
        }
    }
    private void addExternalResources(SKOSResource resource, String textBrut) {
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] externalResources = textBrut.split(SEPERATEUR);
            for (String externalResource : externalResources) {
                resource.addExternalResource(externalResource);
            }
        }
    }    

    private void setStatusOfConcept(String status, SKOSResource sKOSResource) {
        switch (status.toLowerCase()) {
            case "ca":
                sKOSResource.setStatus(SKOSProperty.CANDIDATE);
                break;
            case "dep":
                sKOSResource.setStatus(SKOSProperty.DEPRECATED);
                break;
            default:
                sKOSResource.setStatus(SKOSProperty.CONCEPT);
                break;
        }

    }

    private void addRelationsGiven(String textBrut, SKOSResource sKOSResource) {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                sKOSResource.addRelation(element[2], element[0], getType(element[1]));
            }
        }
    }

    private int getType(String role) {
        switch (role) {
            case "RHP":
                return SKOSProperty.RELATED_HAS_PART;
            case "RPO":
                return SKOSProperty.RELATED_PART_OF;
            case "RT":
                return SKOSProperty.RELATED;
            case "NTG":
                return SKOSProperty.NARROWER_GENERIC;
            case "NTP":
                return SKOSProperty.NARROWER_PARTITIVE;
            case "NTI":
                return SKOSProperty.NARROWER_INSTANTIAL;
            case "NT":
                return SKOSProperty.NARROWER;
            case "BTG":
                return SKOSProperty.BROADER_GENERIC;
            case "BTP":
                return SKOSProperty.BROADER_PARTITIVE;
            case "BTI":
                return SKOSProperty.BROADER_INSTANTIAL;
            default:
                return SKOSProperty.BROADER;
        }
    }

    private void addReplaced(String textBrut, SKOSResource sKOSResource, int type) throws SQLException {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                sKOSResource.addReplaces(tab, type);
            }
        }
    }

    private void addAlignementGiven(String textBrut, SKOSResource sKOSResource, int type) throws SQLException {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                sKOSResource.addMatch(tab.trim(), type);
            }
        }
    }

    private void addDocumentation(String textBrut, SKOSResource sKOSResource, int type) throws SQLException {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            String htmlTagsRegEx = "<[^>]*>";

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                String str = new StringPlus().normalizeStringForXml(element[0]);
                sKOSResource.addDocumentation(str, element[1], type);
            }
        }
    }

    private void getLabels(String labelBrut, SKOSResource sKOSResource, int type) throws SQLException {
        if("16238".equalsIgnoreCase(sKOSResource.getIdentifier())){
            System.out.println("concept : " + sKOSResource.getIdentifier() + "  " + labelBrut);
        }
        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                sKOSResource.addLabel(element[0], element[1], type);
            }
        }
    }

    private void addMembres(SKOSResource sKOSResource, String textBrut, String idConcept) {
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                sKOSResource.addRelation(idConcept, tab, SKOSProperty.MEMBER_OF);
            }
        }
    }
    
    private String getUriFromNodeUri(String idTheso, String originalUri, String idConcept, 
                NodePreference nodePreference, NodeUri nodeUri) {
              
        if(nodePreference.isOriginalUriIsArk() && nodeUri.getIdArk()!= null && !StringUtils.isEmpty(nodeUri.getIdArk())) {
            return originalUri + '/' + nodeUri.getIdArk();
        }
        else if(nodePreference.isOriginalUriIsArk() && (nodeUri.getIdArk() == null || StringUtils.isEmpty(nodeUri.getIdArk())) ) {
            return getPath(originalUri) + "/?idc=" + idConcept + "&idt=" + idTheso;      
        } else if(nodePreference.isOriginalUriIsHandle() && !StringUtils.isEmpty(nodeUri.getIdHandle())) {
            return "https://hdl.handle.net/" + nodeUri.getIdHandle();
        } else if (nodePreference.isOriginalUriIsDoi() && !StringUtils.isEmpty(nodeUri.getIdDoi())) {
            return "https://doi.org/" + nodeUri.getIdDoi();
        } else if (!StringUtils.isEmpty(originalUri)) {
            return originalUri + "/?idc=" + idConcept + "&idt=" + idTheso;
        } else {
            return getPath(originalUri) + "/?idc=" + idConcept + "&idt=" + idTheso;
        }
    }

}
