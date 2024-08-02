package fr.cnrs.opentheso.bdd.helper.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.skosapi.SKOSProperty;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections4.CollectionUtils;

/**
 *
 * @author miledrousset
 */
public class DaoResourceHelper {

    private final static String SEPARATEUR = "##";
    private final static String SUB_SEPARATEUR = "@@";

    /**
     * retourne la liste de Top termes pour un thesaurus concept trié par alpha
     * la liste des champs retournés sont :
     * idConcept, notation, status, label, havechildren, uri, definitions, synonymes
     * @param ds
     * @param idTheso
     * @param idBT

     * @return 
     */
    public List<String> getConceptsTT(HikariDataSource ds, String idTheso, String idBT) {

        // on récupère les concepts fils et les facettes
        List<String> listIds = new ArrayList<>();
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from opentheso_get_narrowers_ignorefacet"
                        + "('" + idTheso + "','" + idBT + "'); "
                        //+ " as x(notation character varying, status character varying,  idConcept character varying);"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIds.add(resultSet.getString("idconcept2"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listIds;
    }         
    
    
    /**
     * retourne la liste de Top termes pour un thesaurus concept trié par alpha
     * la liste des champs retournés sont :
     * idConcept, notation, status, label, havechildren, uri, definitions, synonymes
     * @param ds
     * @param idTheso

     * @param idLang
     * @return 
     */
    public List<NodeConceptGraph> getConceptsTTForGraph(HikariDataSource ds, String idTheso, String idLang) {

        // on récupère les concepts fils et les facettes
        List<NodeConceptGraph> nodeConceptGraphs = new ArrayList<>();
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from opentheso_get_list_topterm_forgraph"
                        + "('" + idTheso + "', '" + idLang + "') "
                        + " as x(idconcept Character varying, local_uri text, status Character varying,  label VARCHAR, altlabel VARCHAR, definition text, havechildren boolean);"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeConceptGraph nodeConceptGraph = new NodeConceptGraph();
                        nodeConceptGraph.setIdConcept(resultSet.getString("idconcept"));

                        nodeConceptGraph.setIdThesaurus(idTheso);
                        nodeConceptGraph.setIdLang(idLang);
                        nodeConceptGraph.setTerm(true);
                        
                        nodeConceptGraph.setUri(resultSet.getString("local_uri"));
                        nodeConceptGraph.setStatusConcept(resultSet.getString("status"));                        
                        
                        if(StringUtils.isEmpty(resultSet.getString("label"))) {
                            nodeConceptGraph.setPrefLabel("");
                        } else {
                            nodeConceptGraph.setPrefLabel(resultSet.getString("label"));
                        }
                        
                        if(StringUtils.isEmpty(resultSet.getString("altlabel"))) {
                            nodeConceptGraph.setAltLabels(null);
                        } else {
                            nodeConceptGraph.setAltLabels(getLabelsWithoutSub(resultSet.getString("altlabel")));
                        }       
                        
                        if(StringUtils.isEmpty(resultSet.getString("definition"))) {
                            nodeConceptGraph.setDefinitions(null);
                        } else {
                            nodeConceptGraph.setDefinitions(getLabelsWithoutSub(resultSet.getString("definition")));
                        }                          
                        
                        nodeConceptGraph.setHaveChildren(resultSet.getBoolean("havechildren"));

                        nodeConceptGraphs.add(nodeConceptGraph);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        Collections.sort(nodeConceptGraphs);
        return nodeConceptGraphs;
    }       
    
    
    /**
     * retourne la liste de fils pour un concept trié par alpha
     * la liste des champs retournés sont :
     * idConcept, notation, status, label, havechildren, uri, definitions, synonymes
     * @param ds
     * @param idTheso
     * @param idConceptBT
     * @param idLang
     * @return 
     */
    public List<NodeConceptGraph> getConceptsNTForGraph(HikariDataSource ds, String idTheso,
            String idConceptBT, String idLang) {

        // on récupère les concepts fils et les facettes
        List<NodeConceptGraph> nodeConceptGraphs = new ArrayList<>();
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from opentheso_get_list_narrower_forgraph"
                        + "('" + idTheso + "', '" + idConceptBT + "', '" + idLang + "') "
                        + " as x(idconcept2 Character varying, local_uri text, status Character varying,  label VARCHAR, altlabel VARCHAR, definition text, havechildren boolean);"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeConceptGraph nodeConceptGraph = new NodeConceptGraph();
                        nodeConceptGraph.setIdConcept(resultSet.getString("idconcept2"));

                        nodeConceptGraph.setIdThesaurus(idTheso);
                        nodeConceptGraph.setIdLang(idLang);
                        nodeConceptGraph.setTerm(true);
                        
                        nodeConceptGraph.setUri(resultSet.getString("local_uri"));
                        nodeConceptGraph.setStatusConcept(resultSet.getString("status"));                        
                        
                        if(StringUtils.isEmpty(resultSet.getString("label"))) {
                            nodeConceptGraph.setPrefLabel("");
                        } else {
                            nodeConceptGraph.setPrefLabel(resultSet.getString("label"));
                        }
                        
                        if(StringUtils.isEmpty(resultSet.getString("altlabel"))) {
                            nodeConceptGraph.setAltLabels(null);
                        } else {
                            nodeConceptGraph.setAltLabels(getLabelsWithoutSub(resultSet.getString("altlabel")));
                        }       
                        
                        if(StringUtils.isEmpty(resultSet.getString("definition"))) {
                            nodeConceptGraph.setDefinitions(null);
                        } else {
                            nodeConceptGraph.setDefinitions(getLabelsWithoutSub(resultSet.getString("definition")));
                        }                          
                        
                        nodeConceptGraph.setHaveChildren(resultSet.getBoolean("havechildren"));

                        nodeConceptGraphs.add(nodeConceptGraph);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        Collections.sort(nodeConceptGraphs);
        return nodeConceptGraphs;
    }    
    
    
    /**
     * permet de récupérer les concept fils pour le concept à déplier
     * elle retourne : idConcept, notation, status, label, havechildren (trié) 
     * @param ds
     * @param idTheso
     * @param idConceptBT
     * @param idLang
     * @param isSortByNotation
     * @return 
     */
    public List<NodeConceptTree> getConceptsNTForTree(HikariDataSource ds, String idTheso,
            String idConceptBT, String idLang, boolean isSortByNotation) {

        // on récupère les concepts fils et les facettes
        List<NodeConceptTree> nodeConceptTrees = new ArrayList<>();
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from opentheso_get_list_narrower_fortree"
                        + "('" + idTheso + "', '" + idConceptBT + "', '" + idLang + "') "
                        + " as x(idconcept2 Character varying, notation Character varying,"
                        + " status Character varying,  label VARCHAR, havechildren boolean);"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeConceptTree nodeConceptTree = new NodeConceptTree();
                        nodeConceptTree.setIdConcept(resultSet.getString("idconcept2"));
                        if(isSortByNotation)
                            nodeConceptTree.setNotation(resultSet.getString("notation"));
                        else
                            nodeConceptTree.setNotation("");
                        nodeConceptTree.setIdThesaurus(idTheso);
                        nodeConceptTree.setIdLang(idLang);
                        nodeConceptTree.setTerm(true);
                        
                        if(StringUtils.isEmpty(resultSet.getString("label"))) {
                            nodeConceptTree.setTitle("");
                        } else {
                            nodeConceptTree.setTitle(resultSet.getString("label"));
                        }
                        
                        nodeConceptTree.setHaveChildren(resultSet.getBoolean("havechildren"));
                        nodeConceptTree.setStatusConcept(resultSet.getString("status"));
                        nodeConceptTrees.add(nodeConceptTree);
                    }
                }
                stmt.executeQuery("select * from opentheso_get_facets_of_concept"
                        + "('" + idTheso + "', '" + idConceptBT + "', '" + idLang + "') " 
                        + "as x(id_facet Character varying, libelle Character varying, have_members boolean);"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeConceptTree nodeConceptTree = new NodeConceptTree();
                        nodeConceptTree.setIdConcept(resultSet.getString("id_facet"));
                        nodeConceptTree.setNotation("");
                        nodeConceptTree.setIdThesaurus(idTheso);
                        nodeConceptTree.setIdLang(idLang);
                        nodeConceptTree.setFacet(true);
                        
                        if(StringUtils.isEmpty(resultSet.getString("libelle"))) {
                            nodeConceptTree.setTitle("");
                        } else {
                            nodeConceptTree.setTitle(resultSet.getString("libelle"));
                        }                        
                        
                        nodeConceptTree.setHaveChildren(resultSet.getBoolean("have_members"));
                        nodeConceptTree.setStatusConcept("");
                        nodeConceptTrees.add(nodeConceptTree);
                    }
                }                
            }
        } catch (SQLException ex) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(!isSortByNotation) {
            try {
                // on tente un tri naturel, en cas d'erreur, on applique un tri classique
                Collections.sort(nodeConceptTrees);
            } catch (Exception e) {
                // tri classique
                try {
                    Collections.sort(nodeConceptTrees, Comparator.comparing(p -> p.getTitle()));
                } catch (Exception ex) {
                }
            }
        }
        return nodeConceptTrees;
    }

    /**
     * pemret de récupérer une collection par langue
     *
     * @param ds
     * @param idTheso
     * @param idGroup
     * @param idLang
     * @return
     */
/*    public NodeFullConcept getFullCollection(HikariDataSource ds, String idTheso, String idGroup, String idLang) {
        NodeFullConcept nodeFullConcept = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from opentheso_get_concept('" + idTheso + "', '" + idConcept + "', '" + idLang + "')"
                        + "as x(URI text, conceptType varchar, localUri text, identifier varchar, permalinkId varchar,"
                        + "prefLabel varchar, altLabel varchar, hidenlabel varchar,"
                        + "prefLabel_trad varchar, altLabel_trad varchar, hiddenLabel_trad varchar, definition text, example text, editorialNote text, changeNote text,"
                        + "scopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, "
                        + "closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, gpsData text,"
                        + " membre text, created timestamp with time zone, modified timestamp with time zone, images text, creator text, contributor text,"
                        + "replaces text, replaced_by text, facets text, externalResources text);"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeFullConcept = new NodeFullConcept();

                        nodeFullConcept.setUri(resultSet.getString("URI"));

                        // type de concept 
                        nodeFullConcept.setResourceType(SKOSProperty.CONCEPT);

                        // Status du concept 
                        nodeFullConcept.setResourceStatus(SKOSProperty.CONCEPT);
                        if ("DEP".equalsIgnoreCase(resultSet.getString("conceptType"))) {
                            nodeFullConcept.setResourceStatus(SKOSProperty.DEPRECATED);
                        }
                        if ("CA".equalsIgnoreCase(resultSet.getString("conceptType"))) {
                            nodeFullConcept.setResourceStatus(SKOSProperty.CANDIDATE);
                        }

                        // identifiants
                        nodeFullConcept.setIdentifier(resultSet.getString("identifier"));
                        nodeFullConcept.setPermanentId(resultSet.getString("permalinkId"));
                        nodeFullConcept.setNotation(resultSet.getString("notation"));

                        // dates
                        nodeFullConcept.setCreated(resultSet.getString("created"));
                        nodeFullConcept.setModified(resultSet.getString("modified"));

                        // users
                        nodeFullConcept.setCreatorName(resultSet.getString("creator"));

                        nodeFullConcept.setContributorName(getContributors(resultSet.getString("contributor")));

                        // selected prefLabel (langue en cours)
                        nodeFullConcept.setPrefLabel((getLabel(resultSet.getString("prefLabel"), idLang)));
                        // selected altLabel
                        nodeFullConcept.setAltLabels(getLabels(resultSet.getString("altLabel")));
                        // selected hiddenLabel
                        nodeFullConcept.setHiddenLabels(getLabels(resultSet.getString("hidenlabel")));

                        // labels traductions
                        nodeFullConcept.setPrefLabelsTraduction(getLabelsTraduction(resultSet.getString("prefLabel_trad")));
                        nodeFullConcept.setAltLabelTraduction(getLabelsTraduction(resultSet.getString("altLabel_trad")));
                        nodeFullConcept.setHiddenLabelTraduction(getLabelsTraduction(resultSet.getString("hiddenLabel_trad")));

                        // notes
                        nodeFullConcept.setDefinitions(getNotes(resultSet.getString("definition")));
                        nodeFullConcept.setExamples(getNotes(resultSet.getString("example")));
                        nodeFullConcept.setEditorialNotes(getNotes(resultSet.getString("editorialNote")));
                        nodeFullConcept.setChangeNotes(getNotes(resultSet.getString("changeNote")));
                        nodeFullConcept.setScopeNotes(getNotes(resultSet.getString("scopeNote")));
                        nodeFullConcept.setNotes(getNotes(resultSet.getString("note")));
                        nodeFullConcept.setHistoryNotes(getNotes(resultSet.getString("historyNote")));

                        // relations 
                        nodeFullConcept.setBroaders(getRelations(resultSet.getString("broader")));
                        nodeFullConcept.setNarrowers(getRelations(resultSet.getString("narrower")));
                        nodeFullConcept.setRelateds(getRelations(resultSet.getString("related")));

                        // alignements
                        nodeFullConcept.setExactMatchs(getAlignments(resultSet.getString("exactMatch")));
                        nodeFullConcept.setCloseMatchs(getAlignments(resultSet.getString("closeMatch")));
                        nodeFullConcept.setBroadMatchs(getAlignments(resultSet.getString("broadMatch")));
                        nodeFullConcept.setRelatedMatchs(getAlignments(resultSet.getString("relatedMatch")));
                        nodeFullConcept.setNarrowMatchs(getAlignments(resultSet.getString("narrowMatch")));

                        // GPS
                        nodeFullConcept.setGps(getGps(resultSet.getString("gpsData")));

                        // membres, les collections dont le concept est membre
                        nodeFullConcept.setMembres(getFromIdLabel(resultSet.getString("membre")));

                        // images 
                        nodeFullConcept.setImages(getImages(resultSet.getString("images")));

                        // replaces
                        nodeFullConcept.setReplaces(getFromIdLabel(resultSet.getString("replaces")));
                        //replaceBy
                        nodeFullConcept.setReplacedBy(getFromIdLabel(resultSet.getString("replaced_by")));

                        //Facettes
                        nodeFullConcept.setFacets(getFromIdLabel(resultSet.getString("facets")));

                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeFullConcept;
    }    */
    
    /**
     * pemret de récupérer la liste des relations NT
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idLang
     * @param step
     * @param offset
     * @return
     */
    public List<ConceptRelation> getListNT(HikariDataSource ds, String idTheso, String idConcept, String idLang, int offset, int step) {
        if (ds.isClosed()) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, "HikariDataSource is closed.");
            return null;
        }
        List<ConceptRelation> conceptRelations = null;
        
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM opentheso_get_next_nt('" + idTheso + "', '" + idConcept + "', '" + idLang + "'," + offset + "," + step + ");"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        conceptRelations = getRelations(resultSet.getString("narrower"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conceptRelations;
    }    
    
    /**
     * pemret de récupérer un concept complet par langue
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idLang
     * @param step
     * @param offset
     * @return
     */
    public NodeFullConcept getFullConcept(HikariDataSource ds, String idTheso, String idConcept, String idLang, int offset, int step) {
        NodeFullConcept nodeFullConcept = null;
        if (ds.isClosed()) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, "HikariDataSource is closed.");
            return null;
        }

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from opentheso_get_concept('" + idTheso + "', '" + idConcept + "', '" + idLang + "'," + offset + "," + step + ")"
                        + " as x(URI text, resourceType varchar, localUri text, identifier varchar, permalinkId varchar,"
                        + "prefLabel varchar, altLabel varchar, hidenlabel varchar,"
                        + "prefLabel_trad varchar, altLabel_trad varchar, hiddenLabel_trad varchar, definition text, example text, editorialNote text, changeNote text,"
                        + "scopeNote text, note text, historyNote text, notation varchar, narrower text, broader text, related text, exactMatch text, "
                        + "closeMatch text, broadMatch text, relatedMatch text, narrowMatch text, gpsData text,"
                        + " membre text, created timestamp with time zone, modified timestamp with time zone, images text, creator text, contributor text,"
                        + "replaces text, replaced_by text, facets text, externalResources text, conceptType text);"
                );
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeFullConcept = new NodeFullConcept();

                        nodeFullConcept.setUri(resultSet.getString("URI"));

                        // type de resource (ConceptScheme, Concept, Collection, ThesaurusArray, FoafImage)
                        nodeFullConcept.setResourceType(SKOSProperty.CONCEPT);

                        // type de concept :  Qualifier, Subject, Place, people ...
                        nodeFullConcept.setConceptType(resultSet.getString("conceptType"));
                        
                        // Status du concept 
                        nodeFullConcept.setResourceStatus(SKOSProperty.CONCEPT);
                        if ("DEP".equalsIgnoreCase(resultSet.getString("resourceType"))) {
                            nodeFullConcept.setResourceStatus(SKOSProperty.DEPRECATED);
                        }
                        if ("CA".equalsIgnoreCase(resultSet.getString("resourceType"))) {
                            nodeFullConcept.setResourceStatus(SKOSProperty.CANDIDATE);
                        }

                        // identifiants
                        nodeFullConcept.setIdentifier(resultSet.getString("identifier"));
                        nodeFullConcept.setPermanentId(resultSet.getString("permalinkId"));
                        nodeFullConcept.setNotation(resultSet.getString("notation"));

                        // dates
                        nodeFullConcept.setCreated(resultSet.getString("created"));
                        nodeFullConcept.setModified(resultSet.getString("modified"));

                        // users
                        nodeFullConcept.setCreatorName(resultSet.getString("creator"));

                        nodeFullConcept.setContributorName(getContributors(resultSet.getString("contributor")));

                        // selected prefLabel (langue en cours)
                        nodeFullConcept.setPrefLabel((getLabel(resultSet.getString("prefLabel"), idLang)));
                        // selected altLabel
                        nodeFullConcept.setAltLabels(getLabels(resultSet.getString("altLabel"), idLang));
                        // selected hiddenLabel
                        nodeFullConcept.setHiddenLabels(getLabels(resultSet.getString("hidenlabel"), idLang));

                        // labels traductions
                        nodeFullConcept.setPrefLabelsTraduction(getLabelsTraduction(resultSet.getString("prefLabel_trad")));
                        nodeFullConcept.setAltLabelTraduction(getLabelsTraduction(resultSet.getString("altLabel_trad")));
                        nodeFullConcept.setHiddenLabelTraduction(getLabelsTraduction(resultSet.getString("hiddenLabel_trad")));

                        // notes
                        nodeFullConcept.setDefinitions(getNotes(resultSet.getString("definition")));
                        nodeFullConcept.setExamples(getNotes(resultSet.getString("example")));
                        nodeFullConcept.setEditorialNotes(getNotes(resultSet.getString("editorialNote")));
                        nodeFullConcept.setChangeNotes(getNotes(resultSet.getString("changeNote")));
                        nodeFullConcept.setScopeNotes(getNotes(resultSet.getString("scopeNote")));
                        nodeFullConcept.setNotes(getNotes(resultSet.getString("note")));
                        nodeFullConcept.setHistoryNotes(getNotes(resultSet.getString("historyNote")));

                        // relations 
                        nodeFullConcept.setBroaders(getRelations(resultSet.getString("broader")));
                        nodeFullConcept.setNarrowers(getRelations(resultSet.getString("narrower")));
                        nodeFullConcept.setRelateds(getRelations(resultSet.getString("related")));

                        // alignements
                        nodeFullConcept.setExactMatchs(getAlignments(resultSet.getString("exactMatch")));
                        nodeFullConcept.setCloseMatchs(getAlignments(resultSet.getString("closeMatch")));
                        nodeFullConcept.setBroadMatchs(getAlignments(resultSet.getString("broadMatch")));
                        nodeFullConcept.setRelatedMatchs(getAlignments(resultSet.getString("relatedMatch")));
                        nodeFullConcept.setNarrowMatchs(getAlignments(resultSet.getString("narrowMatch")));

                        // externalResources
                        nodeFullConcept.setExternalResources(getExternalResources(resultSet.getString("externalResources")));
                        
                        // GPS
                        nodeFullConcept.setGps(getGps(resultSet.getString("gpsData")));

                        // membres, les collections dont le concept est membre
                        nodeFullConcept.setMembres(getFromIdLabel(resultSet.getString("membre")));

                        // images 
                        nodeFullConcept.setImages(getImages(resultSet.getString("images")));

                        // replaces
                        nodeFullConcept.setReplaces(getFromIdLabel(resultSet.getString("replaces")));
                        //replaceBy
                        nodeFullConcept.setReplacedBy(getFromIdLabel(resultSet.getString("replaced_by")));

                        //Facettes
                        nodeFullConcept.setFacets(getFromIdLabel(resultSet.getString("facets")));

                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DaoResourceHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeFullConcept;
    }
    
    private List<String> getExternalResources(String textBrut) {
        List<String> resources = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    resources.add(tab);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(resources)) {
            return resources;
        }
        return null;
    }    

    private List<String> getContributors(String textBrut) {
        List<String> contributors = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    contributors.add(tab);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(contributors)) {
            return contributors;
        }
        return null;
    }

    private List<ConceptImage> getImages(String textBrut) {
        List<ConceptImage> conceptImages = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptImage conceptImage = new ConceptImage();
                    if (StringUtils.isEmpty(element[0]) || "null".equalsIgnoreCase(element[0])) {
                        conceptImage.setImageName("");
                    } else {
                        conceptImage.setImageName(element[0]);
                    }
                    conceptImage.setCopyRight(element[1]);
                    conceptImage.setUri(element[2]);
                    if(element.length > 3)
                        conceptImage.setCreator(element[3]);
                    conceptImages.add(conceptImage);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptImages)) {
            return conceptImages;
        }
        return null;
    }

    private List<ConceptIdLabel> getFromIdLabel(String textBrut) {
        List<ConceptIdLabel> membres = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptIdLabel membre = new ConceptIdLabel();
                    membre.setUri(element[0]);
                    membre.setIdentifier(element[1]);
                    membre.setLabel(element[2]);
                    membres.add(membre);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(membres)) {
            Collections.sort(membres);
            return membres;
        }
        return null;
    }

    private List<ResourceGPS> getGps(String textBrut) {
        List<ResourceGPS> resourceGPSs = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ResourceGPS resourceGPS = new ResourceGPS();
                    resourceGPS.setLatitude(Double.valueOf(element[0]));
                    resourceGPS.setLongitude(Double.valueOf(element[1]));
                    resourceGPS.setPosition(Integer.parseInt(element[2]));
                    resourceGPSs.add(resourceGPS);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(resourceGPSs)) {
            return resourceGPSs;
        }
        return null;
    }

    private List<String> getAlignments(String textBrut) {
        List<String> uris = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    uris.add(tab);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(uris)) {
            return uris;
        }
        return null;
    }

    private List<ConceptRelation> getRelations(String textBrut) {
        List<ConceptRelation> conceptRelations = new ArrayList<>();
        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);
            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptRelation conceptRelation = new ConceptRelation();
                    conceptRelation.setUri(element[0]);
                    conceptRelation.setRole(element[1]);
                    conceptRelation.setIdConcept(element[2]);
                    conceptRelation.setLabel(element[3]);
                    conceptRelations.add(conceptRelation);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptRelations)) {
            Collections.sort(conceptRelations);
            return conceptRelations;
        }
        return null;
    }

    private List<ConceptNote> getNotes(String textBrut) {
        List<ConceptNote> conceptNotes = new ArrayList<>();
        StringPlus stringPlus = new StringPlus();

        if (StringUtils.isNotEmpty(textBrut)) {
            String[] tabs = textBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptNote conceptNote = new ConceptNote();
                    conceptNote.setIdNote(Integer.parseInt(element[0]));
                    conceptNote.setLabel(stringPlus.normalizeStringForXml(element[1]));
                    conceptNote.setIdLang(element[2]);
                    if(element.length > 3)
                        conceptNote.setNoteSource(element[3]);
                    conceptNotes.add(conceptNote);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptNotes)) {
            return conceptNotes;
        }
        return null;
    }

    private ConceptLabel getLabel(String labelBrut, String idLang) {
        ConceptLabel conceptLabel = null;

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    conceptLabel = new ConceptLabel();
                    conceptLabel.setIdLang(idLang);
                    conceptLabel.setLabel(element[0]);
                    conceptLabel.setIdTerm(element[1]);
                    conceptLabel.setId(Integer.parseInt(element[2]));
                } catch (Exception e) {
                }
            }
        }
        return conceptLabel;
    }
    
    private List<String> getLabelsWithoutSub(String labelBrut) {
        List<String> labels = new ArrayList<>();

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    labels.add(tab);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(labels)) {
            return labels;
        }
        return null;
    }    

    private List<ConceptLabel> getLabels(String labelBrut, String idLang) {
        List<ConceptLabel> conceptLabels = new ArrayList<>();

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptLabel conceptLabel = new ConceptLabel();
                    conceptLabel.setIdLang(idLang);
                    conceptLabel.setLabel(element[0]);
                    conceptLabel.setIdTerm(element[1]);                    
                    if(element.length > 2)
                        conceptLabel.setId(Integer.parseInt(element[2]));
                    
                    conceptLabels.add(conceptLabel);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptLabels)) {
            Collections.sort(conceptLabels);
            return conceptLabels;
        }
        return null;
    }

    private List<ConceptLabel> getLabelsTraduction(String labelBrut) {
        List<ConceptLabel> conceptLabels = new ArrayList<>();

        if (StringUtils.isNotEmpty(labelBrut)) {
            String[] tabs = labelBrut.split(SEPARATEUR);

            for (String tab : tabs) {
                try {
                    String[] element = tab.split(SUB_SEPARATEUR);
                    ConceptLabel conceptLabel = new ConceptLabel();
                    conceptLabel.setIdTerm(element[0]);
                    conceptLabel.setLabel(element[1]);
                    conceptLabel.setIdLang(element[2]);
                    conceptLabel.setId(Integer.parseInt(element[3]));
                    if(element.length > 4 )
                        conceptLabel.setCodeFlag(element[4]);
                    conceptLabels.add(conceptLabel);
                } catch (Exception e) {
                }
            }
        }
        if (CollectionUtils.isNotEmpty(conceptLabels)) {
            Collections.sort(conceptLabels);
            return conceptLabels;
        }
        return null;
    }

    //private NodeFullConcept daoGetFullConcept(HikariDataSource ds, String IdTheso, List<String> idConcepts){
}
