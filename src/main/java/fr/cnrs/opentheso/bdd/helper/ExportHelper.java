package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.skosapi.SKOSDiscussion;
import fr.cnrs.opentheso.skosapi.SKOSGPSCoordinates;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import fr.cnrs.opentheso.skosapi.SKOSRelation;
import fr.cnrs.opentheso.skosapi.SKOSResource;
import fr.cnrs.opentheso.skosapi.SKOSStatus;
import fr.cnrs.opentheso.skosapi.SKOSVote;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;
import javax.faces.context.FacesContext;
import org.apache.commons.lang3.StringUtils;

import static fr.cnrs.opentheso.bdd.helper.ConceptHelper.formatLinkTag;

public class ExportHelper {

    private final static String SEPERATEUR = "##";
    private final static String SUB_SEPERATEUR = "@";

    
    public List<SKOSResource> getAllFacettes(HikariDataSource ds, String idTheso,
            String baseUrl, String originalUri) throws Exception {

        List<SKOSResource> facettes = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * FROM opentheso_get_facettes('th20', 'test') as (id_facet VARCHAR, "
                        + "lexical_value VARCHAR, created timestamp with time zone, modified timestamp with time zone, "
                        + "lang VARCHAR, id_concept_parent VARCHAR, uri_value VARCHAR, membres TEXT)");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        SKOSResource sKOSResource = new SKOSResource(getUriForFacette(resultSet.getString("id_facet"), 
                                idTheso, originalUri), SKOSProperty.FACET);
                        sKOSResource.addRelation(resultSet.getString("id_facet"), resultSet.getString("uri_value"), SKOSProperty.superOrdinate);
                        sKOSResource.addLabel(resultSet.getString("lexical_value"), resultSet.getString("lang"), SKOSProperty.prefLabel);
                        sKOSResource.addDate(resultSet.getString("created"), SKOSProperty.created);
                        sKOSResource.addDate(resultSet.getString("modified"), SKOSProperty.modified);

                        String labelBrut = resultSet.getString("membres");
                        if (StringUtils.isNotEmpty(labelBrut)) {
                            String[] tabs = labelBrut.split(SEPERATEUR);
                            for (String tab : tabs) {
                                String[] element = tab.split(SUB_SEPERATEUR);
                                sKOSResource.addRelation(element[0], element[1], SKOSProperty.member);
                            }
                        }
                        
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

    public List<SKOSResource> getAllConcepts(HikariDataSource ds, String idTheso,
            String baseUrl, String idGroup, String originalUri) throws Exception {

        List<SKOSResource> concepts = new ArrayList<>();

        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery(getSQLRequest(idTheso, baseUrl, idGroup));
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        SKOSResource sKOSResource = new SKOSResource();
                        sKOSResource.setProperty(SKOSProperty.Concept);
                        sKOSResource.setUri(resultSet.getString("uri"));
                        sKOSResource.setLocalUri(resultSet.getString("local_uri"));

                        sKOSResource.addIdentifier(resultSet.getString("identifier"), SKOSProperty.identifier);

                        if (!StringUtils.isEmpty(resultSet.getString("ark_id"))) {
                            sKOSResource.setArkId(resultSet.getString("ark_id"));
                        }

                        setStatusOfConcept(resultSet.getString("type"), sKOSResource);

                        getLabels(resultSet.getString("prefLab"), sKOSResource, SKOSProperty.prefLabel);
                        getLabels(resultSet.getString("altLab"), sKOSResource, SKOSProperty.altLabel);
                        getLabels(resultSet.getString("altLab_hiden"), sKOSResource, SKOSProperty.hiddenLabel);

                        if (resultSet.getString("broader") == null || resultSet.getString("broader").isEmpty()) {
                            sKOSResource.getRelationsList().add(new SKOSRelation(idTheso, getUriFromId(idTheso, originalUri),
                                    SKOSProperty.topConceptOf));
                        }

                        addRelationsGiven(resultSet.getString("related"), sKOSResource);

                        addDocumentation(resultSet.getString("note"), sKOSResource, SKOSProperty.note);
                        addDocumentation(resultSet.getString("definition"), sKOSResource, SKOSProperty.definition);
                        addDocumentation(resultSet.getString("secopeNote"), sKOSResource, SKOSProperty.scopeNote);
                        addDocumentation(resultSet.getString("historyNote"), sKOSResource, SKOSProperty.historyNote);
                        addDocumentation(resultSet.getString("example"), sKOSResource, SKOSProperty.example);
                        addDocumentation(resultSet.getString("editorialNote"), sKOSResource, SKOSProperty.editorialNote);
                        addDocumentation(resultSet.getString("changeNote"), sKOSResource, SKOSProperty.changeNote);

                        addAlignementGiven(resultSet.getString("exactMatch"), sKOSResource, SKOSProperty.exactMatch);
                        addAlignementGiven(resultSet.getString("closeMatch"), sKOSResource, SKOSProperty.closeMatch);
                        addAlignementGiven(resultSet.getString("broadMatch"), sKOSResource, SKOSProperty.broadMatch);
                        addAlignementGiven(resultSet.getString("relatedmatch"), sKOSResource, SKOSProperty.relatedMatch);
                        addAlignementGiven(resultSet.getString("narrowMatch"), sKOSResource, SKOSProperty.narrowMatch);

                        addRelationsGiven(resultSet.getString("narrower"), sKOSResource);

                        if (resultSet.getString("broader") != null) {
                            addRelationsGiven(resultSet.getString("broader"), sKOSResource);
                        }

                        sKOSResource.addRelation(idTheso, getUriFromId(idTheso, originalUri), SKOSProperty.inScheme);

                        //addReplaced(resultSet.getString("replaces_by"), sKOSResource, SKOSProperty.isReplacedBy);
                        //addReplaced(resultSet.getString("replaces"), sKOSResource, SKOSProperty.replaces);
                        if (!StringUtils.isEmpty(resultSet.getString("notation"))) {
                            sKOSResource.addNotation(resultSet.getString("notation"));
                        }

                        addImages(sKOSResource, resultSet.getString("img"));
                        
                        addMembres(sKOSResource, resultSet.getString("membre"), resultSet.getString("IDENTIFIER"));

                        if (resultSet.getString("latitude") != null || resultSet.getString("longitude") != null) {
                            sKOSResource.setGPSCoordinates(new SKOSGPSCoordinates(
                                    resultSet.getDouble("latitude"), resultSet.getDouble("longitude")));
                        }

                        // createur et contributeur
                        if (resultSet.getString("creator") != null) {
                            sKOSResource.addCreator(resultSet.getString("creator"), SKOSProperty.creator);
                        }
                        if (resultSet.getString("contributor") != null) {
                            sKOSResource.addCreator(resultSet.getString("contributor"), SKOSProperty.contributor);
                        }

                        String created = resultSet.getString("created");
                        if (StringUtils.isNotEmpty(created)) {;
                            sKOSResource.addDate(created.substring(0, created.indexOf(" ")), SKOSProperty.created);
                        }

                        String modified = resultSet.getString("modified");
                        if (StringUtils.isNotEmpty(modified)) {
                            sKOSResource.addDate(modified.substring(0, modified.indexOf(" ")), SKOSProperty.modified);
                        }

                        ArrayList<String> first = new ArrayList<>();
                        first.add(resultSet.getString("identifier"));
                        ArrayList<ArrayList<String>> paths = new ArrayList<>();

                        paths = new ConceptHelper().getPathOfConceptWithoutGroup(ds,
                                resultSet.getString("identifier"), idTheso, first, paths);
                        ArrayList<String> pathFromArray = getPathFromArray(paths);
                        if (!pathFromArray.isEmpty()) {
                            sKOSResource.setPaths(pathFromArray);
                        }

                        concepts.add(sKOSResource);
                    }
                }
            }
        } catch (SQLException sqle) {
            System.out.println(">> " + sqle.getMessage());
        }

        return concepts;
    }

    private String getUriFromId(String id, String originalUri) {

        if (originalUri != null && !originalUri.isEmpty()) {
            return originalUri + "/" + id;
        } else {
            return getPath(originalUri) + "/" + id;
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

        return baseSQL + "') as x(URI text, TYPE varchar,  LOCAL_URI text, IDENTIFIER varchar, ARK_ID varchar, "
                + "prefLab varchar, altLab varchar, altLab_hiden varchar, definition text, example text, editorialNote text, changeNote text, "
                + "secopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, "
                + "closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, latitude double precision, longitude double precision, "
                + "membre text, created timestamp with time zone, modified timestamp with time zone, img text, creator text, contributor text);";
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

    private void addCandidatDiscussions(SKOSResource resource, String textBrut) {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                SKOSDiscussion skosDiscussion = new SKOSDiscussion();
                skosDiscussion.setMsg(element[0]);
                skosDiscussion.setIdUser(Integer.valueOf(element[1]));
                skosDiscussion.setDate(element[2]);
                resource.addMessage(skosDiscussion);
            }
        }
    }

    private void addCandidatVote(HikariDataSource ds, SKOSResource resource, String textBrut, String idTheso, String idConcept) {
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);

                SKOSVote skosVote = new SKOSVote();
                skosVote.setIdNote(element[2]);
                skosVote.setIdUser(Integer.parseInt(element[0]));
                skosVote.setIdThesaurus(idTheso);
                skosVote.setIdConcept(idConcept);
                skosVote.setTypeVote(element[1]);
                if (!StringUtils.isEmpty(element[2]) && !"null".equalsIgnoreCase(element[2])) {
                    String htmlTagsRegEx = "<[^>]*>";
                    NodeNote nodeNote = new NoteHelper().getNoteByIdNote(ds, Integer.parseInt(element[2]));
                    if (nodeNote != null) {
                        String str = ConceptHelper.formatLinkTag(nodeNote.getLexicalvalue());
                        skosVote.setValueNote(str.replaceAll(htmlTagsRegEx, ""));
                    }
                }
                resource.addVote(skosVote);
            }
        }
    }

    private void addImages(SKOSResource resource, String textBrut) {
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] images = textBrut.split(SEPERATEUR);
            for (String image : images) {
                resource.addImageUri(image);
            }
        }
    }

    private void setStatusOfConcept(String status, SKOSResource sKOSResource) {
        switch (status.toLowerCase()) {
            case "ca":
                sKOSResource.setStatus(SKOSProperty.candidate);
                break;
            case "dep":
                sKOSResource.setStatus(SKOSProperty.deprecated);
                break;
            default:
                sKOSResource.setStatus(SKOSProperty.Concept);
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
                return SKOSProperty.relatedHasPart;
            case "RPO":
                return SKOSProperty.relatedPartOf;
            case "RT":
                return SKOSProperty.related;
            case "NTG":
                return SKOSProperty.narrowerGeneric;
            case "NTP":
                return SKOSProperty.narrowerPartitive;
            case "NTI":
                return SKOSProperty.narrowerInstantial;
            case "NT":
                return SKOSProperty.narrower;
            case "BTG":
                return SKOSProperty.broaderGeneric;
            case "BTP":
                return SKOSProperty.broaderPartitive;
            case "BTI":
                return SKOSProperty.broaderInstantial;
            default:
                return SKOSProperty.broader;
        }
    }

    private void addAlignementGiven(String textBrut, SKOSResource sKOSResource, int type) throws SQLException {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            for (String tab : tabs) {
                sKOSResource.addMatch(tab, type);
            }
        }
    }

    private void addDocumentation(String textBrut, SKOSResource sKOSResource, int type) throws SQLException {

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPERATEUR);

            String htmlTagsRegEx = "<[^>]*>";

            for (String tab : tabs) {
                String[] element = tab.split(SUB_SEPERATEUR);
                String str = formatLinkTag(element[0]);
                sKOSResource.addDocumentation(str.replaceAll(htmlTagsRegEx, ""), element[1], type);
            }
        }
    }

    private void getLabels(String labelBrut, SKOSResource sKOSResource, int type) throws SQLException {

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
                sKOSResource.addRelation(idConcept, tab, SKOSProperty.memberOf);
            }
        }
    }

}
