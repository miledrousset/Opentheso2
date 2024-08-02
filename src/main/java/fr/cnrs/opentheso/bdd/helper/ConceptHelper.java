/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.HierarchicalRelationship;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.dao.DaoResourceHelper;
import fr.cnrs.opentheso.bdd.helper.dao.NodeFullConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeConceptArkId;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeConceptType;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeDeprecated;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeHieraRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeMetaData;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTree;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptExport;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptSearch;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearch;
import fr.cnrs.opentheso.bdd.helper.nodes.status.NodeStatus;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.candidat.dao.CandidatDao;
import fr.cnrs.opentheso.bean.candidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.importexport.outils.HTMLLinkElement;
import fr.cnrs.opentheso.bean.importexport.outils.HtmlLinkExtraction;
import fr.cnrs.opentheso.bean.toolbox.statistique.ConceptStatisticData;
import fr.cnrs.opentheso.ws.api.NodeDatas;
import fr.cnrs.opentheso.ws.ark.ArkHelper2;
import fr.cnrs.opentheso.ws.handle.HandleHelper;
import fr.cnrs.opentheso.ws.handlestandard.HandleService;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import net.handle.hdllib.HandleException;
import org.apache.commons.collections4.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class ConceptHelper {

    private final Log log = LogFactory.getLog(ConceptHelper.class);

    //identifierType  1=numericId ; 2=alphaNumericId
    private NodePreference nodePreference;
    private String message = "";

    /**
     * ************************************************************
     * /**************************************************************
     * Nouvelles fonctions stables auteur Miled Rousset
     * /**************************************************************
     * /*************************************************************
     */
    /**
     * permet de changer les valeurs d'un type de concept dans la table
     * ConceptType
     *
     * @param ds
     * @param idThesaurus
     * @param nodeConceptType
     * @return
     */
    public boolean applyChangeForConceptType(HikariDataSource ds,
            String idThesaurus, NodeConceptType nodeConceptType) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept_type set "
                        + " label_fr = '" + nodeConceptType.getLabel_fr() + "',"
                        + " label_en = '" + nodeConceptType.getLabel_en() + "',"
                        + " reciprocal = " + nodeConceptType.isReciprocal() + ","
                        + " id_theso = '" + idThesaurus + "'"
                        + " WHERE code ='" + nodeConceptType.getCode() + "'"
                        + " AND id_theso ='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating type of concept : " + nodeConceptType.getCode(), sqle);
        }
        return false;
    }

    /**
     * permet de déplacer le concept vers un autre thésaurus
     *
     * @param ds
     * @param idConceptToMove
     * @param idThesoFrom
     * @param idThesoTarget
     * @param idUser
     * @return
     */
    public boolean moveConceptToAnotherTheso(HikariDataSource ds,
            String idConceptToMove, String idThesoFrom, String idThesoTarget, int idUser) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        " update concept set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update thesaurus_array set id_thesaurus = '" + idThesoTarget + "' where id_concept_parent = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update thesaurus_array set id_thesaurus = '" + idThesoTarget + "' where id_concept_parent = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update concept_historique set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update term set id_thesaurus = '" + idThesoTarget + "' from preferred_term where term.id_term = preferred_term.id_term"
                        + " and term.id_thesaurus = preferred_term.id_thesaurus and preferred_term.id_concept = '" + idConceptToMove + "' and term.id_thesaurus = '" + idThesoFrom + "';"
                        + " update non_preferred_term set id_thesaurus = '" + idThesoTarget + "' from preferred_term where non_preferred_term.id_term = preferred_term.id_term"
                        + " and non_preferred_term.id_thesaurus = preferred_term.id_thesaurus and preferred_term.id_concept = '" + idConceptToMove + "' and non_preferred_term.id_thesaurus = '" + idThesoFrom + "';"
                        + " update note set id_thesaurus = '" + idThesoTarget + "' from preferred_term where note.id_term = preferred_term.id_term"
                        + " and note.id_thesaurus = preferred_term.id_thesaurus and preferred_term.id_concept = '" + idConceptToMove + "' and note.id_thesaurus = '" + idThesoFrom + "';"
                        + " update note set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update note_historique set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update preferred_term set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update concept_candidat set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update candidat_status set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update candidat_messages set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update candidat_vote set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update concept_group_concept set idthesaurus = '" + idThesoTarget + "' where idconcept = '" + idConceptToMove + "' and idthesaurus = '" + idThesoFrom + "';"
                        + " update hierarchical_relationship set id_thesaurus = '" + idThesoTarget + "' where id_concept1 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update hierarchical_relationship set id_thesaurus = '" + idThesoTarget + "' where id_concept2 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update hierarchical_relationship_historique set id_thesaurus = '" + idThesoTarget + "' where id_concept1 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update hierarchical_relationship_historique set id_thesaurus = '" + idThesoTarget + "' where id_concept2 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update concept_term_candidat set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update alignement set internal_id_thesaurus = '" + idThesoTarget + "' where internal_id_concept = '" + idConceptToMove + "' and internal_id_thesaurus = '" + idThesoFrom + "';"
                        + " update proposition set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update concept_replacedby set id_thesaurus = '" + idThesoTarget + "' where id_concept1 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update concept_replacedby set id_thesaurus = '" + idThesoTarget + "' where id_concept2 = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update gps set id_theso = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_theso = '" + idThesoFrom + "';"
                        + " update concept_facet set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update external_resources set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update external_images set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update proposition set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                        + " update concept_dcterms set id_thesaurus = '" + idThesoTarget + "' where id_concept = '" + idConceptToMove + "' and id_thesaurus = '" + idThesoFrom + "';"
                );
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while moving concept : " + idThesoTarget, sqle);
        }
        return false;
    }

    /**
     * permet de supprimer un type de concept
     *
     * @param ds
     * @param idThesaurus
     * @param nodeConceptType
     * @return
     */
    public boolean deleteConceptType(HikariDataSource ds,
            String idThesaurus, NodeConceptType nodeConceptType) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from concept_type where "
                        + " code = '" + nodeConceptType.getCode() + "'"
                        + " AND id_theso ='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting type of concept : " + nodeConceptType.getCode(), sqle);
        }
        return false;
    }

    /**
     * Permet d'ajouter un nouveau type de concept
     *
     * @param ds
     * @param idThesaurus
     * @param nodeConceptType
     * @return
     */
    public boolean addNewConceptType(HikariDataSource ds,
            String idThesaurus, NodeConceptType nodeConceptType) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("insert into concept_type (code, label_fr, label_en, reciprocal, id_theso) values ("
                        + "'" + nodeConceptType.getCode() + "',"
                        + "'" + nodeConceptType.getLabel_fr() + "',"
                        + "'" + nodeConceptType.getLabel_en() + "',"
                        + nodeConceptType.isReciprocal() + ","
                        + "'" + idThesaurus + "'"
                        + ")");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while adding type of concept : " + nodeConceptType.getCode(), sqle);
        }
        return false;
    }

    public boolean isConceptTypeExist(HikariDataSource ds, String idTheso, NodeConceptType nodeConceptType) {
        boolean existe = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select code from concept_type where " + "code = '" + nodeConceptType.getCode() + "'"
                        + " and id_theso = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if concept_type exist : " + nodeConceptType.getCode(), sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de changer le type du concept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param type
     * @param idUser
     * @return
     */
    public boolean setConceptType(HikariDataSource ds,
            String idThesaurus,
            String idConcept,
            String type,
            int idUser) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set concept_type = '" + type + "'"
                        + " WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating type of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Permet de retourner la liste de types de concepts date de type 2021-02-01
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<NodeConceptType> getAllTypesOfConcept(HikariDataSource ds, String idTheso) {
        ArrayList<NodeConceptType> nodeConceptTypes = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from concept_type where id_theso in ('" + idTheso + "', 'all')"
                        + " order by "
                        + " CASE unaccent(lower(code))"
                        + " WHEN 'concept' THEN 1"
                        + " END");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeConceptType nodeConceptType = new NodeConceptType();
                        nodeConceptType.setCode(resultSet.getString("code"));
                        nodeConceptType.setLabel_fr(resultSet.getString("label_fr"));
                        nodeConceptType.setLabel_en(resultSet.getString("label_en"));
                        nodeConceptType.setReciprocal(resultSet.getBoolean("reciprocal"));
                        if ("all".equalsIgnoreCase(resultSet.getString("id_theso"))) {
                            nodeConceptType.setPermanent(true);
                        } else {
                            nodeConceptType.setPermanent(false);
                        }

                        nodeConceptTypes.add(nodeConceptType);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All types of concepts ", sqle);
        }
        return nodeConceptTypes;
    }

    /**
     * Permet de retourner la liste des concepts à partir d'une date donnée date
     * de type 2021-02-01
     *
     * @param ds
     * @param idTheso
     * @param date
     * @return
     */
    public ArrayList<String> getIdConceptFromDate(HikariDataSource ds, String idTheso, String date) {
        ArrayList<String> ids = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept"
                        + " where "
                        + " concept.id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " concept.status != 'CA'"
                        + " and"
                        + " concept.modified BETWEEN '" + date + "' and now();");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        ids.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting concepts from date ", sqle);
        }
        return ids;
    }

    /**
     * permet de récupérer les concepts dépréciés
     *
     * @param ds
     * @param idTheso
     * @param idLang
     * @return #MR
     */
    public ArrayList<NodeDeprecated> getAllDeprecatedConceptOfThesaurus(HikariDataSource ds, String idTheso, String idLang) {
        ArrayList<NodeDeprecated> nodeDeprecateds = new ArrayList<>();
        DeprecateHelper deprecateHelper = new DeprecateHelper();
        ArrayList<NodeIdValue> replacesValues;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept, concept.modified, users.username, term.lexical_value"
                        + " from term, concept, preferred_term, users"
                        + " where  "
                        + " concept.contributor = users.id_user"
                        + " and"
                        + " concept.id_concept = preferred_term.id_concept "
                        + " and concept.id_thesaurus = preferred_term.id_thesaurus "
                        + " and preferred_term.id_term = term.id_term "
                        + " and preferred_term.id_thesaurus = term.id_thesaurus "
                        + " and concept.id_thesaurus = '" + idTheso + "'"
                        + " and term.lang = '" + idLang + "'"
                        + " and concept.status = 'DEP'"
                        + " order by unaccent(lower(lexical_value))");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeDeprecated nodeDeprecated = new NodeDeprecated();

                        nodeDeprecated.setDeprecatedId(resultSet.getString("id_concept"));
                        nodeDeprecated.setDeprecatedLabel(resultSet.getString("lexical_value"));
                        nodeDeprecated.setModified(resultSet.getDate("modified"));
                        nodeDeprecated.setUserName(resultSet.getString("username"));
                        nodeDeprecateds.add(nodeDeprecated);
                    }
                }
                for (NodeDeprecated nodeDeprecated : nodeDeprecateds) {
                    replacesValues = deprecateHelper.getAllReplacedBy(ds, idTheso, nodeDeprecated.getDeprecatedId(), idLang);
                    boolean first = true;
                    for (NodeIdValue replacesValue : replacesValues) {
                        if (first) {
                            nodeDeprecated.setReplacedByLabel(replacesValue.getValue());
                            nodeDeprecated.setReplacedById(replacesValue.getId());
                        } else {
                            nodeDeprecated.setReplacedByLabel(nodeDeprecated.getDeprecatedLabel() + "##" + replacesValue.getValue());
                            nodeDeprecated.setReplacedById(nodeDeprecated.getDeprecatedId() + "##" + replacesValue.getId());
                        }
                        first = false;
                    }
                }
                return nodeDeprecateds;
            }
        } catch (SQLException sqle) {
            log.error("Error while getting deprecated values : " + idTheso, sqle);
        }
        return null;
    }

    /**
     * permet de retourner un noeud de données optimisées pour l'affichage du
     * graphe D3Js
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idLang
     * @return
     */
    public NodeDatas getConceptForGraph(HikariDataSource ds,
            String idConcept, String idTheso, String idLang) {
        NodeDatas nodeDatas = new NodeDatas();
        String label = getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
        if (label == null || label.isEmpty()) {
            nodeDatas.setName("(" + idConcept + ")");
        } else {
            nodeDatas.setName(label);
        }
        nodeDatas.setUrl(getUri(idConcept, idTheso));
        nodeDatas.setDefinition(new NoteHelper().getDefinition(ds, idConcept, idTheso, idLang));
        nodeDatas.setSynonym(new TermHelper().getNonPreferredTermsLabel(ds, idConcept, idTheso, idLang));
        return nodeDatas;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept en s'adaptant au
     * format défini pour le thésaurus
     *
     * @return
     */
    private String getUri(String idConcept, String idTheso) {
        if (idConcept == null || idTheso == null) {
            return "";
        }
        return nodePreference.getCheminSite() + "?idc=" + idConcept + "&idt=" + idTheso;
    }

    /**
     * Cette fonction permet de récupérer la liste des concepts suivant l'id du
     * Concept-Père et le thésaurus sous forme de classe NodeConceptTree (sans
     * les relations) elle fait le tri alphabétique ou par notation
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param isSortByNotation
     * @param idLang
     * @return
     */
    public ArrayList<NodeConceptTree> getListConcepts(HikariDataSource ds, String idConcept, String idThesaurus,
            String idLang, boolean isSortByNotation) {

        ResultSet resultSet = null;
        ArrayList<NodeConceptTree> nodeConceptTree = null;
        String query;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    if (isSortByNotation) {
                        /// Notation Sort 
                        query = "SELECT concept.notation, hierarchical_relationship.id_concept2"
                                + " FROM concept, hierarchical_relationship"
                                + " WHERE "
                                + " concept.id_concept = hierarchical_relationship.id_concept2 AND"
                                + " concept.id_thesaurus = hierarchical_relationship.id_thesaurus AND"
                                + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' AND"
                                + " hierarchical_relationship.id_concept1 = '" + idConcept + "' AND"
                                + " hierarchical_relationship.role ILIKE 'NT%'"
                                + " and concept.status != 'CA'"
                                + " ORDER BY"
                                + " concept.notation ASC limit 2000";
                    } else {
                        // alphabétique Sort
                        query = "select id_concept2 from hierarchical_relationship, concept"
                                + " where concept.id_thesaurus = hierarchical_relationship.id_thesaurus"
                                + " and concept.id_concept = hierarchical_relationship.id_concept2"
                                + " and hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                                + " and id_concept1 = '" + idConcept + "'"
                                + " and role LIKE 'NT%'"
                                + " and concept.status != 'CA' limit 2000";
                    }

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeConceptTree = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                            nodeConceptTree1.setIdConcept(resultSet.getString("id_concept2"));
                            if (isSortByNotation) {
                                nodeConceptTree1.setNotation(resultSet.getString("notation"));
                            }

                            nodeConceptTree1.setIdThesaurus(idThesaurus);
                            nodeConceptTree1.setIdLang(idLang);
                            nodeConceptTree1.setTerm(true);
                            nodeConceptTree.add(nodeConceptTree1);
                        }
                    }
                    if (nodeConceptTree != null) {
                        for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {
                            query = "SELECT term.lexical_value, concept.status"
                                    + " FROM concept, preferred_term, term"
                                    + " WHERE concept.id_concept = preferred_term.id_concept AND"
                                    + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                                    + " preferred_term.id_term = term.id_term AND"
                                    + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                                    + " concept.id_concept = '" + nodeConceptTree1.getIdConcept() + "' AND"
                                    + " term.lang = '" + idLang + "' AND"
                                    + " term.id_thesaurus = '" + idThesaurus + "';";

                            stmt.executeQuery(query);
                            resultSet = stmt.getResultSet();
                            if (resultSet != null) {
                                resultSet.next();
                                if (resultSet.getRow() == 0) {
                                    nodeConceptTree1.setTitle("");
                                    nodeConceptTree1.setStatusConcept("");
                                } else {
                                    nodeConceptTree1.setTitle(resultSet.getString("lexical_value"));
                                    if (resultSet.getString("status") == null) {
                                        nodeConceptTree1.setStatusConcept("");
                                    } else {
                                        nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                                    }
                                }
                                nodeConceptTree1.setHaveChildren(
                                        haveChildren(ds, idThesaurus, nodeConceptTree1.getIdConcept())
                                );
                            }
                        }
                    }
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting ListConcept of Concept : " + idConcept, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * Cas où le concept_père a des facettes :
     *
     * permet de récupérer la liste des concepts suivant l'id du Concept-Père et
     * le thésaurus sous forme de classe NodeConceptTree (sans les relations)
     * elle fait le tri alphabétique ou par notation
     *
     * En ignorant les concepts qui sont rangés dans les facettes du
     * Concept-Père, on les récupère à la suite quand on clique sur les facettes
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param isSortByNotation
     * @return
     */
    public ArrayList<NodeConceptTree> getListConceptsIgnoreConceptsInFacets(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang, boolean isSortByNotation) {

        // check pour choix de tri entre alphabétique sur terme ou sur notation  
        ResultSet resultSet = null;
        ArrayList<NodeConceptTree> nodeConceptTree = null;
        String query;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try {
                    if (isSortByNotation) {
                        /// Notation Sort 
                        query = "SELECT concept.notation, hierarchical_relationship.id_concept2"
                                + " FROM concept, hierarchical_relationship"
                                + " WHERE "
                                + " concept.id_concept = hierarchical_relationship.id_concept2 AND"
                                + " concept.id_thesaurus = hierarchical_relationship.id_thesaurus AND"
                                + " hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' AND"
                                + " hierarchical_relationship.id_concept1 = '" + idConcept + "' AND"
                                + " hierarchical_relationship.role ILIKE 'NT%'"
                                + " and concept.status != 'CA'"
                                + " and id_concept2 not in (select id_concept from concept_facet, thesaurus_array "
                                + " where"
                                + " concept_facet.id_facet = thesaurus_array.id_facet"
                                + " and concept_facet.id_thesaurus = thesaurus_array.id_thesaurus"
                                + " and thesaurus_array.id_concept_parent = '" + idConcept + "'"
                                + " and thesaurus_array.id_thesaurus = '" + idThesaurus + "')"
                                + " ORDER BY"
                                + " concept.notation ASC;";
                    } else {
                        // alphabétique Sort
                        query = "select id_concept2 from hierarchical_relationship, concept"
                                + " where concept.id_thesaurus = hierarchical_relationship.id_thesaurus"
                                + " and concept.id_concept = hierarchical_relationship.id_concept2"
                                + " and hierarchical_relationship.id_thesaurus = '" + idThesaurus + "'"
                                + " and id_concept1 = '" + idConcept + "'"
                                + " and role LIKE 'NT%'"
                                + " and concept.status != 'CA'"
                                + " and id_concept2 not in (select id_concept from concept_facet, thesaurus_array "
                                + " where"
                                + " concept_facet.id_facet = thesaurus_array.id_facet"
                                + " and concept_facet.id_thesaurus = thesaurus_array.id_thesaurus"
                                + " and thesaurus_array.id_concept_parent = '" + idConcept + "'"
                                + " and thesaurus_array.id_thesaurus = '" + idThesaurus + "')";
                    }

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    if (resultSet != null) {
                        nodeConceptTree = new ArrayList<>();
                        while (resultSet.next()) {
                            NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                            nodeConceptTree1.setIdConcept(resultSet.getString("id_concept2"));
                            if (isSortByNotation) {
                                nodeConceptTree1.setNotation(resultSet.getString("notation"));
                            }

                            nodeConceptTree1.setIdThesaurus(idThesaurus);
                            nodeConceptTree1.setIdLang(idLang);
                            nodeConceptTree1.setTerm(true);
                            nodeConceptTree.add(nodeConceptTree1);
                        }
                    }
                    if(nodeConceptTree != null){
                        for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {
                            query = "SELECT term.lexical_value, concept.status"
                                    + " FROM concept, preferred_term, term"
                                    + " WHERE concept.id_concept = preferred_term.id_concept AND"
                                    + " concept.id_thesaurus = preferred_term.id_thesaurus AND"
                                    + " preferred_term.id_term = term.id_term AND"
                                    + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                                    + " concept.id_concept = '" + nodeConceptTree1.getIdConcept() + "' AND"
                                    + " term.lang = '" + idLang + "' AND"
                                    + " term.id_thesaurus = '" + idThesaurus + "';";

                            stmt.executeQuery(query);
                            resultSet = stmt.getResultSet();
                            if (resultSet != null) {
                                resultSet.next();
                                if (resultSet.getRow() == 0) {
                                    nodeConceptTree1.setTitle("");
                                    nodeConceptTree1.setStatusConcept("");
                                } else {
                                    nodeConceptTree1.setTitle(resultSet.getString("lexical_value"));
                                    if (resultSet.getString("status") == null) {
                                        nodeConceptTree1.setStatusConcept("");
                                    } else {
                                        nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                                    }
                                }
                                nodeConceptTree1.setHaveChildren(
                                        haveChildren(ds, idThesaurus, nodeConceptTree1.getIdConcept())
                                );
                            }
                        }
                    }
                } finally {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting ListConcept of Concept : " + idConcept, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * Cettte fonction permet de retourner la liste des TopConcept avec IdArk et
     * handle
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public ArrayList<NodeUri> getAllTopConcepts(HikariDataSource ds, String idTheso) {

        ArrayList<NodeUri> NodeUris = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_concept, id_ark, id_handle, id_doi FROM concept"
                        + " WHERE id_thesaurus = '" + idTheso + "'"
                        + " AND top_concept = true and status !='CA'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUri nodeUri = new NodeUri();
                        if ((resultSet.getString("id_ark") == null) || (resultSet.getString("id_ark").trim().isEmpty())) {
                            nodeUri.setIdArk("");
                        } else {
                            nodeUri.setIdArk(resultSet.getString("id_ark"));
                        }
                        if ((resultSet.getString("id_handle") == null) || (resultSet.getString("id_handle").trim().isEmpty())) {
                            nodeUri.setIdHandle("");
                        } else {
                            nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        }
                        if ((resultSet.getString("id_doi") == null) || (resultSet.getString("id_doi").trim().isEmpty())) {
                            nodeUri.setIdDoi("");
                        } else {
                            nodeUri.setIdDoi(resultSet.getString("id_doi"));
                        }
                        nodeUri.setIdConcept(resultSet.getString("id_concept"));
                        NodeUris.add(nodeUri);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of TT of theso : " + idTheso, sqle);
        }
        return NodeUris;
    }

    /**
     * permet de récupérer les tops concepts par langue, cette focntion ne prend
     * pas en compte quand le concept n'existe pas dans la langue demandée
     *
     * @param ds
     * @param idTheso
     * @param idLang
     * @return
     */
    public List<NodeTree> getTopConceptsWithTermByTheso(HikariDataSource ds, String idTheso, String idLang) {

        List<NodeTree> nodes = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT distinct(concept.id_concept), term.lexical_value "
                        + " FROM concept, term, preferred_term "
                        + " WHERE concept.id_concept = preferred_term.id_concept"
                        + " AND concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " AND preferred_term.id_thesaurus = term.id_thesaurus"
                        + " AND preferred_term.id_term = term.id_term"
                        + " AND concept.id_thesaurus = '" + idTheso + "' "
                        + " AND concept.top_concept = true "
                        + " AND concept.status != 'CA'"
                        + " AND term.lang = '" + idLang + "'"
                        + " order by term.lexical_value");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTree nodeTree = new NodeTree();
                        nodeTree.setIdConcept(resultSet.getString("id_concept"));
                        nodeTree.setPreferredTerm(resultSet.getString("lexical_value"));
                        nodes.add(nodeTree);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of TT of theso : " + idTheso, sqle);
        }
        return nodes;
    }

    public List<NodeTree> getListChildrenOfConceptWithTerm(HikariDataSource ds, String idConcept, String idLang, String idThesaurus) {
        List<NodeTree> nodes = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT distinct(hierarchical_relationship.id_concept2), term.lexical_value "
                        + "FROM hierarchical_relationship, term, preferred_term "
                        + "WHERE hierarchical_relationship.id_concept2 = preferred_term.id_concept "
                        + "AND hierarchical_relationship.id_thesaurus = preferred_term.id_thesaurus "
                        + "AND preferred_term.id_term = term.id_term "
                        + "AND preferred_term.id_thesaurus = term.id_thesaurus "
                        + "AND hierarchical_relationship.id_thesaurus = '" + idThesaurus + "' "
                        + "AND hierarchical_relationship.id_concept1 = '" + idConcept + "' "
                        + "AND hierarchical_relationship.role LIKE 'NT%' "
                        + "AND term.lang = '" + idLang + "' "
                        + "ORDER BY term.lexical_value");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTree nodeTree = new NodeTree();
                        nodeTree.setIdConcept(resultSet.getString("id_concept2"));
                        nodeTree.setPreferredTerm(resultSet.getString("lexical_value"));
                        nodes.add(nodeTree);
                    }
                } catch (SQLException sqle) {
                    log.error("Error while getting Liste of TT of theso : " + idThesaurus, sqle);
                }
            } catch (SQLException sqle) {
                log.error("Error while getting Liste of TT of theso : " + idThesaurus, sqle);
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste of TT of theso : " + idThesaurus, sqle);
        }
        return nodes;
    }

    /**
     * Cettte fonction permet de retourner la liste des types de concepts
     *
     * @param ds
     * @return
     */
    public ArrayList<String> getAllTypeConcept(HikariDataSource ds) {

        ArrayList<String> allTypeConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT concept_type.code FROM concept_type");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        allTypeConcept.add(resultSet.getString("code"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting all types : ", sqle);
        }
        return allTypeConcept;
    }

    /**
     * Cette fonction permet de mettre à jour le type de concept
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param type
     * @return
     */
    public boolean updateTypeOfConcept(HikariDataSource ds, String idConcept, String idTheso, String type) {
        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("update concept set concept_type = '" + type + "'"
                        + " WHERE idthesaurus='" + idTheso + "'"
                        + " AND idconcept='" + idConcept + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while testing if haveChildren of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de déplacer une Branche
     */
    public boolean moveBranchFromConceptToConcept(HikariDataSource ds, String idConcept, ArrayList<String> idOldBTsToDelete,
            String idNewConceptBT, String idThesaurus, int idUser) {

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            if (idOldBTsToDelete.size() < 2 && !idOldBTsToDelete.isEmpty()) {
                if (idOldBTsToDelete.get(0).equalsIgnoreCase(idNewConceptBT)) {
                    return true;
                }
            }

            for (String idOldBT : idOldBTsToDelete) {
                if (!new RelationsHelper().deleteRelationBT(conn, idConcept, idThesaurus, idOldBT, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
            }

            if (!new RelationsHelper().addRelationBT(conn, idConcept, idThesaurus, idNewConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Cette fonction permet de déplacer un concept/Branche de la racine vers un
     * concept dans le thésaurus
     */
    public boolean moveBranchFromRootToConcept(HikariDataSource ds, String idConcept, String idNewConceptBT,
            String idThesaurus, int idUser) {

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            if (!new RelationsHelper().addRelationBT(conn, idConcept, idThesaurus, idNewConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            conn.commit();
            conn.close();

            return setNotTopConcept(ds, idConcept, idThesaurus);
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // Cette fonction permet de déplacer une Branche vers la racine, elle devient topterme
    public boolean moveBranchFromConceptToRoot(HikariDataSource ds, String idConcept, String idOldConceptBT,
            String idThesaurus, int idUser) {

        try (Connection conn = ds.getConnection()) {

            conn.setAutoCommit(false);

            if (!new RelationsHelper().deleteRelationBT(conn, idConcept, idThesaurus, idOldConceptBT, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            conn.close();
            return setTopConcept(ds, idConcept, idThesaurus);

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // Cette fonction permet de mettre à jour la notation pour un concept
    public boolean updateNotation(HikariDataSource ds, String idConcept, String idTheso, String notation) {

        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set notation ='" + notation + "'"
                        + " WHERE id_concept ='" + idConcept + "' AND id_thesaurus='" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    //Cette fonction permet de récupérer la liste des Ids of Topconcepts d'un thésaurus
    public ArrayList<String> getAllTopTermOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> listIdOfTopConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and top_concept = true and status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdOfTopConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All Ids of TopConcept : " + idThesaurus, sqle);
        }
        return listIdOfTopConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Topconcepts suivant l'id
     * du thésaurus sous forme de classe NodeConceptTree (sans les relations) La
     * liste est triée
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param isSortByNotation
     * @return
     */
    public ArrayList<NodeConceptTree> getListOfTopConcepts(HikariDataSource ds, String idThesaurus,
            String idLang, boolean isSortByNotation) {

        ArrayList<NodeConceptTree> nodeConceptTree = null;
        String query;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                if (isSortByNotation) {
                    query = "SELECT concept.notation, concept.status, concept.id_concept"
                            + " FROM concept WHERE"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND"
                            + " concept.top_concept = true and status != 'CA'"
                            + " ORDER BY concept.notation ASC limit 2000";
                } else {
                    query = "SELECT concept.status, concept.id_concept"
                            + " FROM concept WHERE"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND"
                            + " concept.top_concept = true and status != 'CA' limit 2000";
                }

                stmt.executeQuery(query);

                try (ResultSet resultSet = stmt.getResultSet()) {
                    nodeConceptTree = new ArrayList<>();
                    while (resultSet.next()) {
                        NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                        nodeConceptTree1.setIdConcept(resultSet.getString("id_concept"));
                        if (isSortByNotation) {
                            nodeConceptTree1.setNotation(resultSet.getString("notation"));
                        }
                        nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                        nodeConceptTree1.setIdThesaurus(idThesaurus);
                        nodeConceptTree1.setIdLang(idLang);
                        nodeConceptTree1.setTopTerm(true);
                        nodeConceptTree.add(nodeConceptTree1);
                    }
                    for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {

                        stmt.executeQuery("SELECT term.lexical_value"
                                + " FROM preferred_term, term"
                                + " WHERE preferred_term.id_term = term.id_term "
                                + " AND preferred_term.id_thesaurus = term.id_thesaurus "
                                + " AND term.lang = '" + idLang + "' "
                                + " AND preferred_term.id_concept = '" + nodeConceptTree1.getIdConcept() + "' "
                                + " AND term.id_thesaurus = '" + idThesaurus + "'");

                        try (ResultSet resultSet2 = stmt.getResultSet()) {
                            resultSet2.next();
                            if (resultSet2.getRow() == 0) {
                                nodeConceptTree1.setTitle("(" + nodeConceptTree1.getIdConcept() + ")");
                            } else {
                                nodeConceptTree1.setTitle(resultSet2.getString("lexical_value"));
                            }
                            nodeConceptTree1.setHaveChildren(
                                    haveChildren(ds, idThesaurus, nodeConceptTree1.getIdConcept())
                            );
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting TopConcept sorted of theso  : " + idThesaurus, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue le résultat est allégé
     * pour l'adapter à la recherche
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public NodeConceptSearch getConceptForSearch(HikariDataSource ds,
            String idConcept, String idThesaurus, String idLang) {
        NodeConceptSearch nodeConceptSerach = new NodeConceptSearch();

        TermHelper termHelper = new TermHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        GroupHelper groupHelper = new GroupHelper();

        nodeConceptSerach.setIdTheso(idThesaurus);
        nodeConceptSerach.setCurrentLang(idLang);
        nodeConceptSerach.setIdConcept(idConcept);
        nodeConceptSerach.setIsDeprecated(isDeprecated(ds, idConcept, idThesaurus));

        //récupération du PrefLabel
        nodeConceptSerach.setPrefLabel(getLexicalValueOfConcept(ds, idConcept, idThesaurus, idLang));

        //récupération des traductions
        nodeConceptSerach.setNodeTermTraductions(termHelper.getTraductionsOfConcept(ds, idConcept, idThesaurus, idLang));

        //récupération des termes génériques
        nodeConceptSerach.setNodeBT(relationsHelper.getListBT(ds, idConcept, idThesaurus, idLang));

        //récupération des termes spécifiques
        nodeConceptSerach.setNodeNT(relationsHelper.getListNT(ds, idConcept, idThesaurus, idLang, 21, 0));

        //récupération des termes associés
        nodeConceptSerach.setNodeRT(relationsHelper.getListRT(ds, idConcept, idThesaurus, idLang));

        //    String idTerm = termHelper.getIdTermOfConcept(ds, idConcept, idThesaurus);
        //récupération des Non Prefered Term
        nodeConceptSerach.setNodeEM(termHelper.getNonPreferredTerms(ds, idConcept, idThesaurus, idLang));
        nodeConceptSerach.setNodeConceptGroup(groupHelper.getListGroupOfConcept(ds, idThesaurus, idConcept, idLang));

        return nodeConceptSerach;
    }
 
    /**
     * permet de trouver les idConcepts en partant d'un label
     *
     * @param ds
     * @param idTheso
     * @param label
     * @param idLang
     * @return
     */
    public ArrayList<String> getIdConceptsFromLabel(HikariDataSource ds,
            String idTheso, String label, String idLang) {
        ArrayList<String> conceptLabels = new ArrayList<>();
        StringPlus stringPlus = new StringPlus();
        label = stringPlus.convertString(label);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept from concept, preferred_term, term "
                        + " where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and"
                        + " concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " term.id_thesaurus = '" + idTheso + "'"
                        + " and "
                        + " term.lang = '" + idLang + "'"
                        + " and"
                        + " lower(term.lexical_value) = lower('" + label + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        conceptLabels.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcepts from labels of theso : " + idTheso, sqle);
        }
        return conceptLabels;
    }

    /**
     * permet de trouver un idConcept en partant d'un label
     *
     * @param ds
     * @param idTheso
     * @param label
     * @param idLang
     * @return
     */
    public String getOneIdConceptFromLabel(HikariDataSource ds,
            String idTheso, String label, String idLang) {
        String conceptId = null;
        StringPlus stringPlus = new StringPlus();
        label = stringPlus.convertString(label);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept from concept, preferred_term, term "
                        + " where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and"
                        + " concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " term.id_thesaurus = '" + idTheso + "'"
                        + " and "
                        + " term.lang = '" + idLang + "'"
                        + " and concept.status != 'DEP'"
                        + " and concept.status != 'CA'"
                        + " and"
                        + " lower(term.lexical_value) = lower('" + label + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        conceptId = resultSet.getString("id_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcepts from labels of theso : " + idTheso, sqle);
        }
        return conceptId;
    }

    /**
     * permet de trouver les idConcepts en partant d'un label
     *
     * @param ds
     * @param idTheso
     * @param label
     * @param idLang
     * @return
     */
    public ArrayList<String> getIdConceptsFromAltLabel(HikariDataSource ds,
            String idTheso, String label, String idLang) {
        ArrayList<String> conceptLabels = new ArrayList<>();
        StringPlus stringPlus = new StringPlus();
        label = stringPlus.convertString(label);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept from concept, preferred_term, non_preferred_term "
                        + " where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and"
                        + " concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = non_preferred_term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + " and"
                        + " non_preferred_term.id_thesaurus = '" + idTheso + "'"
                        + " and "
                        + " non_preferred_term.lang = '" + idLang + "'"
                        + " and"
                        + " lower(non_preferred_term.lexical_value) = lower('" + label + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        conceptLabels.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcepts from AltLabels of theso : " + idTheso, sqle);
        }
        return conceptLabels;
    }

    /**
     * permet de trouver un seul idConcept en partant d'un altLabel
     *
     * @param ds
     * @param idTheso
     * @param label
     * @param idLang
     * @return
     */
    public String getOneIdConceptFromAltLabel(HikariDataSource ds,
            String idTheso, String label, String idLang) {
        String conceptId = null;
        StringPlus stringPlus = new StringPlus();
        label = stringPlus.convertString(label);
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept from concept, preferred_term, non_preferred_term "
                        + " where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and"
                        + " concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = non_preferred_term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = non_preferred_term.id_thesaurus"
                        + " and"
                        + " non_preferred_term.id_thesaurus = '" + idTheso + "'"
                        + " and "
                        + " non_preferred_term.lang = '" + idLang + "'"
                        + " and concept.status != 'DEP'"
                        + " and"
                        + " lower(non_preferred_term.lexical_value) = lower('" + label + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        conceptId = resultSet.getString("id_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcepts from AltLabels of theso : " + idTheso, sqle);
        }
        return conceptId;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept en partant de son label C'est la fonction qui permet de récupérer
     * les doublons
     *
     * @param ds
     * @param label
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public NodeConceptSearch getConceptForSearchFromLabel(HikariDataSource ds,
            String label, String idThesaurus, String idLang) {

        TermHelper termHelper = new TermHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        GroupHelper groupHelper = new GroupHelper();

        String conceptId = getOneIdConceptFromLabel(ds, idThesaurus, label, idLang);
        if (StringUtils.isEmpty(conceptId)) {
            conceptId = getOneIdConceptFromAltLabel(ds, idThesaurus, label, idLang);
        }
        if (StringUtils.isEmpty(conceptId)) {
            return null;
        }

        NodeConceptSearch nodeConceptSearch = new NodeConceptSearch();

        nodeConceptSearch.setIdTheso(idThesaurus);
        nodeConceptSearch.setCurrentLang(idLang);
        nodeConceptSearch.setIdConcept(conceptId);
        nodeConceptSearch.setIsDeprecated(isDeprecated(ds, conceptId, idThesaurus));

        //récupération du PrefLabel
        nodeConceptSearch.setPrefLabel(getLexicalValueOfConcept(ds, conceptId, idThesaurus, idLang));

        //récupération des traductions
        nodeConceptSearch.setNodeTermTraductions(termHelper.getTraductionsOfConcept(ds, conceptId, idThesaurus, idLang));

        //récupération des termes génériques
        nodeConceptSearch.setNodeBT(relationsHelper.getListBT(ds, conceptId, idThesaurus, idLang));

        //récupération des termes spécifiques
        nodeConceptSearch.setNodeNT(relationsHelper.getListNT(ds, conceptId, idThesaurus, idLang, 21, 0));

        //récupération des termes associés
        nodeConceptSearch.setNodeRT(relationsHelper.getListRT(ds, conceptId, idThesaurus, idLang));

        String idTerm = termHelper.getIdTermOfConcept(ds, conceptId, idThesaurus);

        nodeConceptSearch.setNodeEM(termHelper.getNonPreferredTerms(ds, conceptId, idThesaurus, idLang));

        nodeConceptSearch.setNodeConceptGroup(groupHelper.getListGroupOfConcept(ds, idThesaurus, conceptId, idLang));

        return nodeConceptSearch;
    }

    /**
     * permet de retourner la liste des Top concepts pour un group donné retour
     * au format de NodeIdValue (informations pour construire l'arbre
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param idGroup
     * @param isSortByNotation
     * @return
     */
    public ArrayList<NodeIdValue> getListTopConceptsOfGroup(HikariDataSource ds,
            String idThesaurus, String idLang, String idGroup, boolean isSortByNotation) {

        ArrayList<NodeIdValue> tabIdValues = new ArrayList<>();

        String lexicalValue;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                if (isSortByNotation) {
                    ArrayList<NodeIdValue> tabIdConcepts = new ArrayList<>();
                    stmt.executeQuery("SELECT DISTINCT concept.id_concept, concept.notation"
                            + " FROM concept, concept_group_concept"
                            + " WHERE"
                            + " concept.id_concept = concept_group_concept.idconcept AND"
                            + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                            + " concept.status != 'CA' and"
                            + " concept.top_concept = true and"
                            + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "') limit 2001;");
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValue.setNotation(resultSet.getString("notation"));
                            tabIdConcepts.add(nodeIdValue);
                        }
                        for (NodeIdValue nodeIdValue1 : tabIdConcepts) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            lexicalValue = getLexicalValueOfConcept(ds, nodeIdValue1.getId(), idThesaurus, idLang);
                            if (lexicalValue == null || lexicalValue.isEmpty()) {
                                nodeIdValue.setValue("__" + nodeIdValue1.getId());
                            } else {
                                nodeIdValue.setValue(lexicalValue);
                            }
                            nodeIdValue.setId(nodeIdValue1.getId());
                            nodeIdValue.setNotation(nodeIdValue1.getNotation());
                            tabIdValues.add(nodeIdValue);
                        }
                    }

                } else {
                    ArrayList<String> tabIdConcepts = new ArrayList<>();
                    stmt.executeQuery("SELECT DISTINCT concept.id_concept, concept.notation"
                            + " FROM concept, concept_group_concept"
                            + " WHERE"
                            + " concept.id_concept = concept_group_concept.idconcept AND"
                            + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                            + " concept.status != 'CA' and"
                            + " concept.top_concept = true and"
                            + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "') limit 2001;");
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        while (resultSet.next()) {
                            tabIdConcepts.add(resultSet.getString("id_concept"));
                        }
                        for (String idConcept : tabIdConcepts) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            lexicalValue = getLexicalValueOfConcept(ds, idConcept, idThesaurus, idLang);
                            if (lexicalValue == null || lexicalValue.isEmpty()) {
                                nodeIdValue.setValue("__" + idConcept);
                            } else {
                                nodeIdValue.setValue(lexicalValue);
                            }
                            nodeIdValue.setId(idConcept);
                            tabIdValues.add(nodeIdValue);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(tabIdValues);
        }

        return tabIdValues;
    }

    /**
     * permet de retourner la liste des concepts pour un group donné retour au
     * format de NodeConceptTree (informations pour construire l'arbre
     *
     * @param ds
     * @param idThesaurus
     * @param idLang
     * @param idGroup
     * @param isSortByNotation
     * @return
     */
    public ArrayList<NodeIdValue> getListConceptsOfGroup(HikariDataSource ds,
            String idThesaurus, String idLang, String idGroup, boolean isSortByNotation) {

        ArrayList<NodeIdValue> tabIdValues = new ArrayList<>();
        String lexicalValue;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                if (isSortByNotation) {
                    ArrayList<NodeIdValue> tabIdConcepts = new ArrayList<>();
                    stmt.executeQuery("SELECT DISTINCT concept.id_concept, concept.notation"
                            + " FROM concept, concept_group_concept"
                            + " WHERE"
                            + " concept.id_concept = concept_group_concept.idconcept AND"
                            + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                            + " concept.status != 'CA' and"
                            + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "') limit 2001;");
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        while (resultSet.next()) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(resultSet.getString("id_concept"));
                            nodeIdValue.setNotation(resultSet.getString("notation"));
                            tabIdConcepts.add(nodeIdValue);
                        }
                        for (NodeIdValue nodeIdValue1 : tabIdConcepts) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            lexicalValue = getLexicalValueOfConcept(ds, nodeIdValue1.getId(), idThesaurus, idLang);
                            if (lexicalValue == null || lexicalValue.isEmpty()) {
                                nodeIdValue.setValue("__" + nodeIdValue1.getId());
                            } else {
                                nodeIdValue.setValue(lexicalValue);
                            }
                            nodeIdValue.setId(nodeIdValue1.getId());
                            nodeIdValue.setNotation(nodeIdValue1.getNotation());
                            tabIdValues.add(nodeIdValue);
                        }
                    }

                } else {
                    ArrayList<String> tabIdConcepts = new ArrayList<>();
                    stmt.executeQuery("SELECT DISTINCT concept.id_concept, concept.notation"
                            + " FROM concept, concept_group_concept"
                            + " WHERE"
                            + " concept.id_concept = concept_group_concept.idconcept AND"
                            + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                            + " concept.status != 'CA' and"
                            + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "') limit 2001;");
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        while (resultSet.next()) {
                            tabIdConcepts.add(resultSet.getString("id_concept"));
                        }
                        for (String idConcept : tabIdConcepts) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            lexicalValue = getLexicalValueOfConcept(ds, idConcept, idThesaurus, idLang);
                            if (lexicalValue == null || lexicalValue.isEmpty()) {
                                nodeIdValue.setValue("__" + idConcept);
                            } else {
                                nodeIdValue.setValue(lexicalValue);
                            }
                            nodeIdValue.setId(idConcept);
                            tabIdValues.add(nodeIdValue);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(tabIdValues);
        }

        return tabIdValues;
    }

    /**
     * permet de retourner la liste des concepts pour un group donné
     */
    public ArrayList<NodeUri> getListConceptsOfGroup(HikariDataSource ds, String idThesaurus, String idGroup) {

        String query = "SELECT DISTINCT concept.id_concept,"
                + " concept.id_ark, concept.id_handle, concept.id_doi"
                + " FROM concept, concept_group_concept"
                + " WHERE"
                + " concept.id_concept = concept_group_concept.idconcept AND"
                + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                + " concept.status != 'CA' AND "
                + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')";

        return getConceptDetails(ds, query, idThesaurus);
    }

    /**
     * permet de retourner la liste des concepts sans group
     */
    public ArrayList<NodeUri> getListConceptsWithoutGroup(HikariDataSource ds, String idThesaurus) {

        String query = "SELECT DISTINCT concept.id_concept, concept.id_ark, concept.id_handle FROM concept "
                + "WHERE id_thesaurus = '" + idThesaurus + "' "
                + "AND id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE id_thesaurus = '" + idThesaurus + "')";

        return getConceptDetails(ds, query, idThesaurus);
    }

    private ArrayList<NodeUri> getConceptDetails(HikariDataSource ds, String query, String idThesaurus) {

        ArrayList<NodeUri> nodeUris = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery(query);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUri nodeUri = new NodeUri();
                        nodeUri.setIdConcept(resultSet.getString("id_concept"));
                        nodeUri.setIdArk(resultSet.getString("id_ark"));
                        nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        nodeUri.setIdDoi(resultSet.getString("id_doi"));
                        nodeUris.add(nodeUri);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return nodeUris;
    }

    /**
     * permet de retourner la liste des concepts pour un group donné retour au
     * format de NodeConceptTree (informations pour construire l'arbre
     */
    public int getCountOfConceptsOfGroup(HikariDataSource ds, String idThesaurus, String idGroup) {

        int count = 0;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT count(concept.id_concept)"
                        + " FROM concept, concept_group_concept"
                        + " WHERE"
                        + " concept.id_concept = concept_group_concept.idconcept AND"
                        + " concept.id_thesaurus = concept_group_concept.idthesaurus AND"
                        + " concept.id_thesaurus = '" + idThesaurus + "' AND "
                        + " concept.status != 'CA' AND "
                        + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return count;
    }

    /**
     * permet de retourner le nombre des concepts dans un thesaurus rattaché à
     * aucun groupe
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public int getCountOfConceptsSansGroup(HikariDataSource ds, String idThesaurus) {

        int count = 0;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT count(id_concept) FROM concept "
                        + " WHERE id_thesaurus = '" + idThesaurus + "' "
                        + " AND concept.status != 'CA'"
                        + " AND id_concept NOT IN (SELECT idconcept FROM concept_group_concept WHERE idthesaurus = '" + idThesaurus + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return count;
    }

    /**
     * permet de mettre à jour la date du concept quand il y a une modification
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param contributor
     */
    public void updateDateOfConcept(HikariDataSource ds, String idTheso, String idConcept, int contributor) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set modified = current_date, contributor = " + contributor + " WHERE id_concept ='" + idConcept + "'"
                        + " AND id_thesaurus='" + idTheso + "'");
            }
        } catch (SQLException sqle) {
            log.error("Error while updating date of concept : " + idConcept, sqle);
        }
    }

    /**
     * Permet de retourner la date de la dernière modification sur un thésaurus
     *
     * @param ds
     * @param idTheso
     * @return
     */
    public Date getLastModification(HikariDataSource ds, String idTheso) {

        Date date = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select modified from concept where id_thesaurus = '"
                        + idTheso + "' and status != 'CA' and modified IS NOT NULL order by modified DESC limit 1 ");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        date = resultSet.getDate("modified");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return date;
    }

    /**
     * Permet de retourner la date de la dernière modification sur un thésaurus
     *
     * @param ds
     * @param idTheso
     * @param idLang
     * @return
     */
    public ArrayList<NodeIdValue> getLastModifiedConcept(HikariDataSource ds, String idTheso, String idLang) {

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_concept, term.lexical_value from concept, preferred_term, term"
                        + " where"
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and"
                        + " concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " concept.id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " term.lang = '" + idLang + "'"
                        + " and concept.status != 'CA' and concept.modified IS not null  order by concept.modified DESC, term.lexical_value limit 10");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("id_concept"));
                        nodeIdValue.setValue(resultSet.getString("lexical_value"));
                        nodeIdValues.add(nodeIdValue);
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return nodeIdValues;
    }

    /**
     * Permet de retourner la liste des concepts qui ont plusieurs groupes en
     * même temps
     */
    public ArrayList<String> getConceptsHavingMultiGroup(HikariDataSource ds, String idTheso) {

        ArrayList<String> listIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idconcept from concept_group_concept where "
                        + " idthesaurus = '" + idTheso + "' "
                        + " group by idconcept having count(idconcept) > 1");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdConcept.add(resultSet.getString("idconcept"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listIdConcept;
    }

    /**
     * Permet de retourner la liste des concepts qui ont uniquement un seul BT
     */
    public ArrayList<String> getConceptsHavingOneBT(HikariDataSource ds, String idTheso) {

        ArrayList<String> listIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1 from hierarchical_relationship where"
                        + " id_thesaurus = '" + idTheso + "' and role ilike 'BT%'"
                        + " group by id_concept1 having count(id_concept1) = 1");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdConcept.add(resultSet.getString("id_concept1"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listIdConcept;
    }

    /**
     * Permet de retourner la liste des concepts qui ont uniquement un seul BT
     * mais en filtrant par group
     */
    public ArrayList<String> getConceptsHavingOneBTByGroup(HikariDataSource ds, String idTheso, String idGroup) {

        ArrayList<String> listIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1, count(id_concept1) from hierarchical_relationship, concept_group_concept where"
                        + " concept_group_concept.idthesaurus = hierarchical_relationship.id_thesaurus AND"
                        + " concept_group_concept.idconcept = hierarchical_relationship.id_concept1 AND"
                        + " id_thesaurus = '" + idTheso + "' and role ilike 'BT%' AND"
                        + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')"
                        + " group by id_concept1 having count(id_concept1) = 1");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdConcept.add(resultSet.getString("id_concept1"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listIdConcept;
    }

    /**
     * Permet de retourner la liste des concepts qui ont plusieurs BT en même
     * temps
     */
    public ArrayList<String> getConceptsHavingMultiBT(HikariDataSource ds, String idTheso) {

        ArrayList<String> listIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1 from hierarchical_relationship where"
                        + " id_thesaurus = '" + idTheso + "' and role ilike 'BT%'"
                        + " group by id_concept1 having count(id_concept1) > 1");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdConcept.add(resultSet.getString("id_concept1"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listIdConcept;
    }

    /**
     * Permet de retourner la liste des concepts qui ont plusieurs BT en même
     * temps mais en filtrant par group
     */
    public ArrayList<String> getConceptsHavingMultiBTByGroup(HikariDataSource ds, String idTheso, String idGroup) {

        ArrayList<String> listIdConcept = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept1, count(id_concept1) from hierarchical_relationship, concept_group_concept where"
                        + " concept_group_concept.idthesaurus = hierarchical_relationship.id_thesaurus AND"
                        + " concept_group_concept.idconcept = hierarchical_relationship.id_concept1 AND"
                        + " id_thesaurus = '" + idTheso + "' and role ilike 'BT%' AND"
                        + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')"
                        + " group by id_concept1 having count(id_concept1) > 1");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdConcept.add(resultSet.getString("id_concept1"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listIdConcept;
    }

    /**
     * permet de retourner la liste des idConcept d'un thésaurus qui n'ont pas
     * d'identifiant numérique
     */
    public ArrayList<String> getAllNonNumericId(HikariDataSource ds, String idTheso) {

        ArrayList<String> listIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_concept like '%crt%'"
                        + " and id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listIdConcept;
    }

    public HashMap<String, String> getIdsAndValuesOfConcepts(HikariDataSource ds, ArrayList<String> idsToGet,
            String idLang, String idTheso) {
        HashMap<String, String> idsAndValues = new LinkedHashMap<>();
        String label;
        for (String idConcept : idsToGet) {
            label = getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
            if (label != null) {
                if (!label.isEmpty()) {
                    idsAndValues.put(idConcept, label);
                }
            }
        }
        return idsAndValues;
    }

    public ArrayList<NodeIdValue> getIdsAndValuesOfConcepts2(HikariDataSource ds, ArrayList<String> idsToGet,
            String idLang, String idTheso) {

        ArrayList<NodeIdValue> idsAndValues = new ArrayList<>();
        String label;
        for (String idConcept : idsToGet) {
            label = getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
            if (label != null) {
                if (!label.isEmpty()) {
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(idConcept);
                    nodeIdValue.setValue(label);
                    idsAndValues.add(nodeIdValue);
                }
            }
        }
        return idsAndValues;
    }

    ///// nouvelle focntion pour retourner les Ids concepts d'une branche
    public List<String> getIdsOfBranch2(HikariDataSource ds, String idTheso, String idConceptDeTete) {

        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();

        List<String> lisIds = new ArrayList<>();

        lisIds = getIdsOfBranch2__(ds, daoResourceHelper, idTheso, idConceptDeTete, lisIds);

        return lisIds;
    }

    private List<String> getIdsOfBranch2__(HikariDataSource ds, DaoResourceHelper daoResourceHelper,
            String idTheso, String idConceptDeTete, List<String> lisIds) {

        // pour éviter les boucles à l'infini
        if (lisIds.contains(idConceptDeTete)) {
            return lisIds;
        }

        lisIds.add(idConceptDeTete);

        List<String> listIdsOfConceptChildren = daoResourceHelper.getConceptsTT(ds, idTheso, idConceptDeTete);

        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranch2__(ds, daoResourceHelper, idTheso, listIdsOfConceptChildren1, lisIds);
        }
        return lisIds;
    }

    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en
     * partant du concept en paramètre
     *
     * @param hd
     * @param idConceptDeTete
     * @param idTheso
     * @return
     */
    public ArrayList<String> getIdsOfBranch(HikariDataSource hd, String idConceptDeTete, String idTheso) {
        ArrayList<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranch__(hd, idConceptDeTete, idTheso, lisIds);
        return lisIds;
    }

    private ArrayList<String> getIdsOfBranch__(HikariDataSource hd, String idConceptDeTete,
            String idTheso, ArrayList<String> lisIds) {

        if (lisIds.contains(idConceptDeTete)) {
            return lisIds;
        }

        lisIds.add(idConceptDeTete);

        ArrayList<String> listIdsOfConceptChildren = getListChildrenOfConcept(hd, idConceptDeTete, idTheso);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranch__(hd, listIdsOfConceptChildren1, idTheso, lisIds);
        }
        return lisIds;
    }

    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en
     * partant du concept en paramètre, elle évite les boucles à l'infini
     *
     * @param hd
     * @param idConceptDeTete
     * @param idTheso
     * @return
     */
    public ArrayList<String> getIdsOfBranchWithoutLoop(HikariDataSource hd, String idConceptDeTete, String idTheso) {
        ArrayList<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranchWithoutLoop__(hd, idConceptDeTete, idTheso, lisIds);
        return lisIds;
    }

    private ArrayList<String> getIdsOfBranchWithoutLoop__(HikariDataSource hd, String idConceptDeTete,
            String idTheso, ArrayList<String> lisIds) {

        if (lisIds.contains(idConceptDeTete)) {
            return lisIds;
        }

        lisIds.add(idConceptDeTete);

        ArrayList<String> listIdsOfConceptChildren = getListChildrenOfConcept(hd, idConceptDeTete, idTheso);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranchWithoutLoop__(hd, listIdsOfConceptChildren1, idTheso, lisIds);
        }
        return lisIds;
    }

    /**
     * Cette fonction permet de retrouver tous tes identifiants d'une branche en
     * partant du concept en paramètre avec limit pour le nombre de résultat
     *
     * @param hd
     * @param idConceptDeTete
     * @param idTheso
     * @param limit
     * @return
     */
    public ArrayList<String> getIdsOfBranchLimited(HikariDataSource hd, String idConceptDeTete, String idTheso, int limit) {
        ArrayList<String> lisIds = new ArrayList<>();
        lisIds = getIdsOfBranchLimited__(hd, idConceptDeTete, idTheso, lisIds, limit);
        return lisIds;
    }

    private ArrayList<String> getIdsOfBranchLimited__(HikariDataSource hd, String idConceptDeTete,
            String idTheso, ArrayList<String> lisIds, int limit) {

        if (lisIds.size() > limit) {
            return lisIds;
        }
        lisIds.add(idConceptDeTete);

        ArrayList<String> listIdsOfConceptChildren = getListChildrenOfConcept(hd, idConceptDeTete, idTheso);
        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            getIdsOfBranchLimited__(hd, listIdsOfConceptChildren1, idTheso, lisIds, limit);
        }
        return lisIds;
    }

    /**
     * Permet de retourner un Id numérique et unique pour le Concept
     */
    private String getNumericConceptId(HikariDataSource ds) {

        String idConcept = null;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select nextval('concept__id_seq') from concept__id_seq");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        int idNumerique = resultSet.getInt(1);
                        idNumerique++;
                        idConcept = "" + (idNumerique);
                        // si le nouveau Id existe, on l'incrémente
                        while (isIdExiste(conn, idConcept)) {
                            idConcept = "" + (++idNumerique);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idConcept;
    }

    /**
     * Permet de retourner un Id numérique et unique pour le Concept
     */
    public String getNumericConceptId(Connection conn) {
        String idConcept = getNumericConceptId__(conn);
        while (isIdExiste(conn, idConcept)) {
            idConcept = getNumericConceptId__(conn);
        }
        return idConcept;
    }

    /**
     * Permet de retourner un Id numérique et unique pour le Concept
     */
    private String getNumericConceptId__(Connection conn) {
        String idConcept = null;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select nextval('concept__id_seq') from concept__id_seq");
            try (ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    int idNumerique = resultSet.getInt(1);
                    idConcept = "" + (idNumerique);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idConcept;
    }

    private String getAlphaNumericId(Connection conn) {
        ToolsHelper toolsHelper = new ToolsHelper();
        String id = toolsHelper.getNewId(15, false, false);
        while (isIdExiste(conn, id)) {
            id = toolsHelper.getNewId(15, false, false);
        }
        return id;
    }

    private String getAlphaNumericId(HikariDataSource ds) {
        ToolsHelper toolsHelper = new ToolsHelper();
        String id = toolsHelper.getNewId(15, false, false);
        while (isIdExiste(ds, id)) {
            id = toolsHelper.getNewId(15, false, false);
        }
        return id;
    }

    /**
     * focntion qui permet de récupérer le Delta des Id concepts créés ou
     * modifiéés le format de la date est (yyyy-MM-dd)
     */
    public ArrayList<String> getConceptsDelta(HikariDataSource ds, String idTheso, String date) {

        ArrayList<String> ids = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "select id_concept from concept where "
                        + " id_thesaurus = '" + idTheso + "'"
                        + " and (created > '" + date + "'"
                        + " or modified > '" + date + "')";

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        ids.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting delta of Concept  : " + idTheso, sqle);
        }
        return ids;
    }

    /**
     * Cette fonction regenère un identifiant Ark pour un concept donné
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idLang
     * @return
     */
    public boolean generateArkId(HikariDataSource ds, String idTheso, String idConcept, String idLang) {

        ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
        if (!arkHelper2.login()) {
            message = "Erreur de connexion !!";
            return false;
        }

        NodeMetaData nodeMetaData;
        Concept concept;
        String privateUri;

        if (nodePreference == null) {
            message = ("Erreur: Veuillez paramétrer les préférences pour ce thésaurus !!");
            return false;
        }
        if (!nodePreference.isUseArk()) {
            message = "Erreur: Veuillez activer Ark dans les préférences !!";
            return false;
        }
        nodeMetaData = initNodeMetaData();
        if (nodeMetaData == null) {
            message = "Erreur: pas de méta-données";
            return false;
        }
        concept = getThisConcept(ds, idConcept, idTheso);
        if (concept == null) {
            message = "Erreur: ce concept n'existe pas : " + idConcept;
            return false;
        }
        nodeMetaData.setTitle(getLexicalValueOfConcept(ds, idConcept, idTheso, idLang));
        nodeMetaData.setSource(nodePreference.getPreferredName());
        nodeMetaData.setCreator(concept.getCreatorName());

        privateUri = "?idc=" + idConcept + "&idt=" + idTheso;

        /// test de tous les cas de figure pour la création d'un idArk
        if (concept.getIdArk() == null || concept.getIdArk().isEmpty()) {
            // cas où on a déja un identifiant Ark en local, donc on doit vérifier :
            // - si l'idArk est présent sur le serveur, on applique une mise à jour de l'URL
            // - si l'idArk n'est pas présent sur le serveur, il y a 2 cas :
            //      - on vérifie si l'URL liée au Ark fourni existe sur le serveur, alors on retourne une erreur (il y a confusion)
            //      - si l'URL n'existe pas sur le serveur, alors on procède à une création d'un identifiant Ark
            // 
            if (!arkHelper2.addArk(privateUri, nodeMetaData)) {
                message = arkHelper2.getMessage();
                message = arkHelper2.getMessage() + "  idConcept = " + idConcept;
                Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échoué ici : " + idConcept);
                return false;
            }
            if (!updateArkIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdArk())) {
                return false;
            }
            if (nodePreference.isGenerateHandle()) {
                if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                    return false;
                }
            }
        } else {
            // ark existe dans Opentheso, on vérifie si Ark est présent sur le serveur Ark 
            if (arkHelper2.isArkExistOnServer(concept.getIdArk())) {
                // ark existe sur le serveur, alors on applique une mise à jour
                // pour l'URL et les métadonnées

                if (!arkHelper2.updateArk(concept.getIdArk(), privateUri, nodeMetaData)) {
                    message = arkHelper2.getMessage();
                    message = arkHelper2.getMessage() + "  idConcept = " + idConcept;
                    return false;
                }
                if (nodePreference.isGenerateHandle()) {
                    if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                        return false;
                    }
                }
            } else {
                // création d'un identifiant Ark avec en paramètre l'ID Ark existant sur Opentheso
                // + (création de l'ID Handle avec le serveur Ark de la MOM)
                if (!arkHelper2.addArkWithProvidedId(concept.getIdArk(), privateUri, nodeMetaData)) {
                    message = arkHelper2.getMessage();
                    message = arkHelper2.getMessage() + "  idConcept = " + idConcept;
                    return false;
                }
                if (!updateArkIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdArk())) {
                    return false;
                }
                if (nodePreference.isGenerateHandle()) {
                    if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Cette fonction regenerer tous les idArk des concepts fournis en paramètre
     * cette action se fait en une seule fois, ne prends en charge que les
     * métadonnées obligatoires traitement rapide
     *
     * @param ds
     * @param idTheso
     * @param idConcepts
     * @param idLang
     * @return
     */
    public ArrayList<NodeIdValue> generateArkIdFast(HikariDataSource ds, String idTheso, ArrayList<String> idConcepts, String idLang) {

        if (nodePreference.isUseArkLocal()) {
            generateArkIdLocal(ds, idTheso, idConcepts);
            return null;
        }

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();

        ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
        if (!arkHelper2.login()) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur de connexion !!");
            nodeIdValues.add(nodeIdValue);
            message = "Erreur de connexion !!";
            return nodeIdValues;
        }

        Concept concept;

        if (nodePreference == null) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur: Veuillez paramétrer les préférences pour ce thésaurus !!");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }
        if (!nodePreference.isUseArk()) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur: Veuillez activer Ark dans les préférences !!");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }

        JsonArrayBuilder jsonArrayBuilderMetas = Json.createArrayBuilder();

        JsonObjectBuilder joDatas = Json.createObjectBuilder();
        if (arkHelper2.getToken() == null) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setValue("Erreur: token non fourni");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }

        joDatas.add("token", arkHelper2.getToken());

        for (String idConcept : idConcepts) {
            concept = getThisConcept(ds, idConcept, idTheso);
            if (concept == null) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(idConcept);
                nodeIdValue.setValue("Erreur: ce concept n'existe pas");
                nodeIdValues.add(nodeIdValue);
                continue;
            }
            JsonObjectBuilder jo = Json.createObjectBuilder();
            jo.add("idConcept", concept.getIdConcept());
            jo.add("ark", concept.getIdArk());

            jo.add("naan", nodePreference.getIdNaan());
            jo.add("type", nodePreference.getPrefixArk());
            jo.add("urlTarget", nodePreference.getCheminSite() + "?idc=" + idConcept + "&idt=" + idTheso);
            jo.add("title", getLexicalValueOfConcept(ds, idConcept, idTheso, idLang));
            jo.add("creator", concept.getCreatorName());

            jsonArrayBuilderMetas.add(jo.build());
        }
        joDatas.add("arks", jsonArrayBuilderMetas.build());

        String jsonResult = arkHelper2.addBatchArk(joDatas.build().toString());

        JsonArray jsonArray;
        JsonObject jsonObject;
        String idConcept = null;
        String idArk;
        try {
            JsonReader reader = Json.createReader(new StringReader(jsonResult));
            jsonArray = reader.readArray();
            System.out.println("/////////////////// traitement des mises à jour dans Opentheso /////////////////////");
            for (int i = 0; i < jsonArray.size(); ++i) {
                jsonObject = jsonArray.getJsonObject(i);
                try {
                    idConcept = jsonObject.getString("idConcept");
                    idArk = jsonObject.getString("idArk");
                    if (StringUtils.isEmpty(idConcept) || StringUtils.isEmpty(idArk)) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(idConcept);
                        nodeIdValue.setValue("Error: id Ark ou Concept vide : " + idArk);
                        nodeIdValues.add(nodeIdValue);
                    } else {
                        if (StringUtils.contains(idArk, "Error:")) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(idConcept);
                            nodeIdValue.setValue(idArk);
                            nodeIdValues.add(nodeIdValue);
                        } else {
                            if (!updateArkIdOfConcept(ds, idConcept, idTheso, idArk)) {
                                NodeIdValue nodeIdValue = new NodeIdValue();
                                nodeIdValue.setId(idConcept);
                                nodeIdValue.setValue("Error: erreur de mise à jour de Ark dans Opentheso : " + idArk);
                                nodeIdValues.add(nodeIdValue);
                            } else {
                                NodeIdValue nodeIdValue = new NodeIdValue();
                                nodeIdValue.setId(idConcept);
                                nodeIdValue.setValue(idArk);
                                nodeIdValues.add(nodeIdValue);
                            }
                        }
                    }
                } catch (Exception e) {
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(idConcept);
                    nodeIdValue.setValue(e.toString());
                    nodeIdValues.add(nodeIdValue);
                }
            }
        } catch (Exception e) {
        }
        return nodeIdValues;
    }

    /**
     * Cette fonction regenère tous les idArk des concepts fournis en paramètre
     *
     * @param ds
     * @param idTheso
     * @param idConcepts
     * @param idLang
     * @return
     */
    public ArrayList<NodeIdValue> generateArkId(HikariDataSource ds, String idTheso, ArrayList<String> idConcepts, String idLang) {
        if (nodePreference.isUseArkLocal()) {
            generateArkIdLocal(ds, idTheso, idConcepts);
            return null;
        }

        ArrayList<NodeIdValue> nodeIdValues = new ArrayList<>();
        ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
        if (!arkHelper2.login()) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur de connexion !!");
            nodeIdValues.add(nodeIdValue);
            message = "Erreur de connexion !!";
            return nodeIdValues;
        }

        NodeMetaData nodeMetaData;
        Concept concept;
        String privateUri;

        if (nodePreference == null) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur: Veuillez paramétrer les préférences pour ce thésaurus !!");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }
        if (!nodePreference.isUseArk()) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId("");
            nodeIdValue.setValue("Erreur: Veuillez activer Ark dans les préférences !!");
            nodeIdValues.add(nodeIdValue);
            return nodeIdValues;
        }

        for (String idConcept : idConcepts) {
            nodeMetaData = initNodeMetaData();
            if (nodeMetaData == null) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(idConcept);
                nodeIdValue.setValue("Erreur: pas de méta-données");
                nodeIdValues.add(nodeIdValue);
                return nodeIdValues;
            }
            concept = getThisConcept(ds, idConcept, idTheso);
            if (concept == null) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(idConcept);
                nodeIdValue.setValue("Erreur: ce concept n'existe pas");
                nodeIdValues.add(nodeIdValue);
                continue;
            }
            nodeMetaData.setTitle(getLexicalValueOfConcept(ds, idConcept, idTheso, idLang));
            nodeMetaData.setSource(nodePreference.getPreferredName());
            nodeMetaData.setCreator(concept.getCreatorName());

            privateUri = "?idc=" + idConcept + "&idt=" + idTheso;

            /// test de tous les cas de figure pour la création d'un idArk
            if (concept.getIdArk() == null || concept.getIdArk().isEmpty()) {
                // cas où on a déja un identifiant Ark en local, donc on doit vérifier :
                // - si l'idArk est présent sur le serveur, on applique une mise à jour de l'URL
                // - si l'idArk n'est pas présent sur le serveur, il y a 2 cas :
                //      - on vérifie si l'URL liée au Ark fourni existe sur le serveur, alors on retourne une erreur (il y a confusion)
                //      - si l'URL n'existe pas sur le serveur, alors on procède à une création d'un identifiant Ark
                // 
                if (!arkHelper2.addArk(privateUri, nodeMetaData)) {
                    message = arkHelper2.getMessage() + "  idConcept = " + idConcept;
                    Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échoué ici : " + idConcept);

                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(idConcept);
                    nodeIdValue.setValue("Erreur: La création Ark a échoué: " + arkHelper2.getMessage());
                    nodeIdValues.add(nodeIdValue);
                    continue;
                }
                if (!updateArkIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdArk())) {
                    NodeIdValue nodeIdValue = new NodeIdValue();
                    nodeIdValue.setId(idConcept);
                    nodeIdValue.setValue("Erreur: La mise à jour du concept dans Opentheso a échoué");
                    nodeIdValues.add(nodeIdValue);
                    continue;
                }
                if (nodePreference.isGenerateHandle()) {
                    if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(idConcept);
                        nodeIdValue.setValue("Erreur: La mise à jour Handle du concept dans Opentheso a échoué");
                        nodeIdValues.add(nodeIdValue);
                    }
                }
            } else {
                // ark existe dans Opentheso, on vérifie si Ark est présent sur le serveur Ark 
                if (arkHelper2.isArkExistOnServer(concept.getIdArk())) {
                    // ark existe sur le serveur, alors on applique une mise à jour
                    // pour l'URL et les métadonnées

                    if (!arkHelper2.updateArk(concept.getIdArk(), privateUri, nodeMetaData)) {
                        message = arkHelper2.getMessage() + "  idConcept = " + idConcept;
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(idConcept);
                        nodeIdValue.setValue("Erreur: Ark existe sur le serveur OpenArk, mais la mise à jour a échoué : " + arkHelper2.getMessage());
                        nodeIdValues.add(nodeIdValue);
                        continue;
                    }
                    if (nodePreference.isGenerateHandle()) {
                        if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(idConcept);
                            nodeIdValue.setValue("Erreur: La mise à jour Handle du concept dans Opentheso a échoué");
                            nodeIdValues.add(nodeIdValue);
                        }
                    }
                } else {
                    // création d'un identifiant Ark avec en paramètre l'ID Ark existant sur Opentheso
                    // + (création de l'ID Handle avec le serveur Ark de la MOM)
                    if (!arkHelper2.addArkWithProvidedId(concept.getIdArk(), privateUri, nodeMetaData)) {
                        message = arkHelper2.getMessage() + "  idConcept = " + idConcept;
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(idConcept);
                        nodeIdValue.setValue("Erreur: Ark n'existe pas sur le serveur OpenArk, mais la mise à jour a échoué : " + arkHelper2.getMessage());
                        nodeIdValues.add(nodeIdValue);
                        continue;
                    }
                    if (!updateArkIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdArk())) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(idConcept);
                        nodeIdValue.setValue("Erreur: La mise à jour du concept dans Opentheso a échoué");
                        nodeIdValues.add(nodeIdValue);
                        continue;
                    }
                    if (nodePreference.isGenerateHandle()) {
                        if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper2.getIdHandle())) {
                            NodeIdValue nodeIdValue = new NodeIdValue();
                            nodeIdValue.setId(idConcept);
                            nodeIdValue.setValue("Erreur: La mise à jour Handle du concept dans Opentheso a échoué");
                            nodeIdValues.add(nodeIdValue);
                        }
                    }
                }
            }
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idConcept);
            nodeIdValue.setValue("OK");
            nodeIdValues.add(nodeIdValue);
        }
        return nodeIdValues;
    }

    /**
     * Cette fonction regenère tous les idArk des concepts fournis en paramètre
     *
     * @param ds
     * @param idTheso
     * @param idConcepts
     * @return
     */
    public boolean updateUriArk(HikariDataSource ds, String idTheso, ArrayList<String> idConcepts) {

        ArkHelper2 arkHelper2 = new ArkHelper2(nodePreference);
        if (!arkHelper2.login()) {
            message = "Erreur de connexion !!";
            return false;
        }

        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }
        String privateUri;
        String idArk;

        for (String idConcept : idConcepts) {
            if (idConcept == null || idConcept.isEmpty()) {
                continue;
            }
            // Mise à jour de l'URI 
            idArk = getIdArkOfConcept(ds, idConcept, idTheso);
            if (idArk == null || idArk.isEmpty()) {
                continue;
            }

            privateUri = "?idc=" + idConcept + "&idt=" + idTheso;
            if (!arkHelper2.updateUriArk(idArk, privateUri)) {
                message = arkHelper2.getMessage();
                message = arkHelper2.getMessage() + "  idConcept = " + idConcept;
                return false;
            }
        }
        return true;
    }

    /**
     * Cette fonction permet de générer les idArk en local
     *
     * @param ds
     * @param idTheso
     * @param idConcepts
     * @return
     */
    public boolean generateArkIdLocal(HikariDataSource ds, String idTheso, ArrayList<String> idConcepts) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArkLocal()) {
            return false;
        }

        ToolsHelper toolsHelper = new ToolsHelper();
        String idArk;
        ConceptHelper conceptHelper = new ConceptHelper();
        for (String idConcept : idConcepts) {
            idArk = conceptHelper.getIdArkOfConcept(ds, idConcept, idTheso);
            if (StringUtils.isEmpty(idArk)) {
                idArk = toolsHelper.getNewId(nodePreference.getSizeIdArkLocal(), nodePreference.isUppercase_for_ark(), true);
                idArk = nodePreference.getNaanArkLocal() + "/" + nodePreference.getPrefixArkLocal() + idArk;
            }
            if (!updateArkIdOfConcept(ds, idConcept, idTheso, idArk)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Permet de : - Vérifier si l'identifiant Ark existe sur le serveur Arkéo -
     * S'il existe, on le met à jour pour l'URL - s'il n'existe pas, on le créé
     *
     * à utiliser avec précaution pour maintenance
     */
    /**
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idArk
     * @return
     */
    /*    public boolean updateArkId(HikariDataSource ds, String idTheso, String idConcept, String idArk) {

        ArkHelper arkHelper = new ArkHelper(nodePreference);
        if (!arkHelper.login()) {
            return false;
        }

        NodeMetaData nodeMetaData;
        Concept concept;
        String privateUri;

        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseArk()) {
            return false;
        }

        if (idArk == null || idArk.isEmpty()) {
            return false;
        }

        nodeMetaData = getNodeMetaData(ds, idConcept,
                nodePreference.getSourceLang(), idTheso);
        if (nodeMetaData == null) {
            return false;
        }
        concept = getThisConcept(ds, idConcept, idTheso);
        if (concept == null) {
            return false;
        }

        privateUri = "?idc=" + idConcept + "&idt=" + idTheso;

        if (arkHelper.isArkExistOnServer(idArk)) {
            // ark existe sur le serveur, alors on applique une mise à jour
            // pour l'URL et les métadonnées
            if (!arkHelper.updateArk(idArk, privateUri, nodeMetaData)) {
                message = arkHelper.getMessage();
                return false;
            }
            if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper.getIdHandle())) {
                return false;
            }
        } else {
            // création d'un identifiant Ark avec en paramètre l'ID Ark existant sur Opentheso
            // + (création de l'ID Handle avec le serveur Ark de la MOM)

            // on vérifie d'abord si idHandle existe sur handle.net, alors il faut le supprimer avant
            if (arkHelper.isHandleExistOnServer(idArk.replaceAll("/", "."))) {
                if (!arkHelper.deleteHandle(idArk.replaceAll("/", "."), privateUri, nodeMetaData)) {
                    message = arkHelper.getMessage();
                    return false;
                }
            }

            if (!arkHelper.addArkWithProvidedId(idArk, privateUri, nodeMetaData)) {
                message = arkHelper.getMessage();
                return false;
            }
            if (!updateHandleIdOfConcept(ds, idConcept, idTheso, arkHelper.getIdHandle())) {
                return false;
            }
        }
        return true;
    }*/
    /**
     * Pour préparer les données pour la création d'un idArk
     */
    private NodeMetaData initNodeMetaData() {
        /*        NodeConcept nodeConcept;
        nodeConcept = getConcept(ds, idConcept, idTheso, idLang, 21, 0);
        if (nodeConcept == null) {
            return null;
        }*/
        NodeMetaData nodeMetaData = new NodeMetaData();
        //     nodeMetaData.setCreator(nodeConcept.getTerm().getSource());
        //     nodeMetaData.setTitle(nodeConcept.getTerm().getLexical_value());
        nodeMetaData.setDcElementsList(new ArrayList<>());
        return nodeMetaData;
    }

    /**
     * Cette fonction permet d'ajouter un Concept à la table Concept, en avec
     * RollBack
     */
    private String addConceptInTableNew(HikariDataSource ds, String idTheso, String idConcept,
            String idArk, String status, String notation, boolean isTopConcept) {

        if (notation == null) {
            notation = "";
        }
        if (status == null) {
            status = "";
        }

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                // Si l'idConcept = null, c'est un nouveau concept sans Id fourni
                if (idConcept == null) {
                    if (nodePreference.getIdentifierType() == 1) { // identifiants types alphanumérique
                        ToolsHelper toolsHelper = new ToolsHelper();
                        idConcept = toolsHelper.getNewId(10, false, false);
                        while (isIdExiste(ds, idConcept)) {
                            idConcept = toolsHelper.getNewId(10, false, false);
                        }
                    } else {
                        idConcept = getNumericConceptId(ds);
                    }
                }
                stmt.executeUpdate("Insert into concept "
                        + "(id_concept, id_thesaurus, id_ark, status, notation, top_concept)"
                        + " values ("
                        + "'" + idConcept + "'"
                        + ",'" + idTheso + "'"
                        + ",'" + idArk + "'"
                        + ",'" + status + "'"
                        + ",'" + notation + "'"
                        + "," + isTopConcept
                        + ")");
            }
        } catch (SQLException sqle) {
            log.error("Error while adding new Concept : " + sqle);
            idConcept = null;
        }
        return idConcept;
    }

    /**
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @return
     */
    public boolean isHaveIdArk(HikariDataSource ds, String idTheso, String idConcept) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_ark from concept where id_concept = '" + idConcept + "'"
                        + " and id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        String idArk = resultSet.getString("id_ark");
                        if (idArk == null || idArk.isEmpty()) {
                            return false;
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return false;
    }

    /**
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @return
     */
    public boolean isHaveNotation(HikariDataSource ds, String idTheso, String idConcept) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select notation from concept where id_concept = '" + idConcept + "'"
                        + " and id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        String notation = resultSet.getString("notation");
                        if (notation == null || notation.isEmpty()) {
                            return false;
                        }
                        return true;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non
     */
    public boolean isIdExiste(HikariDataSource ds, String idConcept) {

        boolean existe = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where " + "id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non dans un
     * thésaurus en particulier
     */
    public boolean isIdExiste(HikariDataSource ds, String idConcept, String idTheso) {

        boolean existe = false;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where " + "lower(id_concept) = lower('"
                        + idConcept + "') and id_thesaurus = '" + idTheso + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet d'ajouter un Concept complet à la base avec le
     * libellé et les relations Si l'opération échoue, elle envoi un NULL et ne
     * modifie pas la base de données
     *
     * @param ds
     * @param idParent
     * @param relationType
     * @param concept
     * @param term
     * @param idUser
     * @return
     */
    public String addConcept(HikariDataSource ds, String idParent, String relationType,
            Concept concept, Term term, int idUser) {

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            if (idParent == null) {
                concept.setTopConcept(true);
            }
            String idConcept = addConceptInTable(conn, concept, idUser);
            if (idConcept == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            if (concept.getIdGroup() != null && !concept.getIdGroup().isEmpty()) {
                new GroupHelper().addConceptGroupConcept(ds, concept.getIdGroup(), concept.getIdConcept(), concept.getIdThesaurus());
            }
            String idTerm = new TermHelper().addTerm(conn, term, idConcept, idUser);
            if (idTerm == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            term.setId_term(idTerm);
            /**
             * ajouter le lien hiérarchique avec le concept partent sauf si ce
             * n'est pas un TopConcept
             */
            if (!concept.isTopConcept()) {
                String inverseRelation = "BT";
                if (relationType == null) {
                    relationType = "NT";
                }
                switch (relationType) {
                    case "NT":
                        inverseRelation = "BT";
                        break;
                    case "NTG":
                        inverseRelation = "BTG";
                        break;
                    case "NTP":
                        inverseRelation = "BTP";
                        break;
                    case "NTI":
                        inverseRelation = "BTI";
                        break;
                }

                HierarchicalRelationship hierarchicalRelationship = new HierarchicalRelationship();
                hierarchicalRelationship.setIdConcept1(idParent);
                hierarchicalRelationship.setIdConcept2(idConcept);
                hierarchicalRelationship.setIdThesaurus(concept.getIdThesaurus());
                hierarchicalRelationship.setRole(relationType);

                if (!addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                    conn.rollback();
                    conn.close();
                    return null;
                }
                hierarchicalRelationship.setIdConcept1(idConcept);
                hierarchicalRelationship.setIdConcept2(idParent);
                hierarchicalRelationship.setIdThesaurus(concept.getIdThesaurus());
                hierarchicalRelationship.setRole(inverseRelation);

                if (!addLinkHierarchicalRelation(conn, hierarchicalRelationship, idUser)) {
                    conn.rollback();
                    conn.close();
                    return null;
                }
            }
            if (nodePreference != null) {
                // création de l'identifiant Handle
                if (nodePreference.isUseHandle()) {
                    if (!addIdHandle(conn, idConcept, concept.getIdThesaurus())) {
                        conn.rollback();
                        conn.close();
                        message = message + "La création Handle a échouée";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Handle a échoué");
                    }
                }
            }
            conn.commit();
            conn.close();

            if (nodePreference != null) {
                // Si on arrive ici, c'est que tout va bien 
                // alors c'est le moment de récupérer le code ARK
                if (nodePreference.isUseArk()) {
                    if (!generateArkId(ds, concept.getIdThesaurus(), idConcept, term.getLang())) {
                        //    conn.rollback();
                        //    conn.close();
                        message = message + "La création Ark a échoué";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échoué");
                    }
                }
                if (nodePreference.isUseArkLocal()) {
                    ArrayList<String> idConcepts = new ArrayList<>();
                    idConcepts.add(idConcept);
                    if (!generateArkIdLocal(ds, concept.getIdThesaurus(), idConcepts)) {
                        message = message + "La création du Ark local a échoué";
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création du Ark local a échoué");
                    }
                }
            }

            return idConcept;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Cette fonction permet de supprimer un Concept avec ses relations et
     * traductions
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idUser
     * @return
     */
    public boolean deleteConcept(HikariDataSource ds,
            String idConcept, String idTheso, int idUser) {

        RelationsHelper relationsHelper = new RelationsHelper();
        TermHelper termHelper = new TermHelper();
        NoteHelper noteHelper = new NoteHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        return deleteConcept__(ds, idConcept, idTheso, idUser,
                termHelper, relationsHelper, noteHelper, alignmentHelper);
    }

    private boolean deleteConcept__(HikariDataSource ds, String idConcept, String idThesaurus, int idUser,
            TermHelper termHelper, RelationsHelper relationsHelper, NoteHelper noteHelper, AlignmentHelper alignmentHelper) {
        String idTerm = termHelper.getIdTermOfConcept(ds, idConcept, idThesaurus);
        if (idTerm == null) {
            return true;
        }
        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.setAutoCommit(false);

            if (!termHelper.deleteTerm(conn, idTerm, idThesaurus, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!relationsHelper.deleteAllRelationOfConcept(conn, idConcept, idThesaurus, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!noteHelper.deleteNotes(conn, idConcept, idThesaurus)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!alignmentHelper.deleteAlignmentOfConcept(conn, idConcept, idThesaurus)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (!deleteConceptFromTable(conn, idConcept, idThesaurus, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            // supprime l'appartenance du concept à des facettes
            if (!deleteConceptFromFacets(conn, idThesaurus, idConcept)) {
                conn.rollback();
                conn.close();
                return false;
            }
            if (!deleteConceptReplacedby(conn, idThesaurus, idConcept)) {
                conn.rollback();
                conn.close();
                return false;
            }
            // supprime les facettes qui sont attachées à ce concept
            if (!deleteFacets(ds, idThesaurus, idConcept)) {
                conn.rollback();
                conn.close();
                return false;
            }
            if (!deleteAllGroupOfConcept(ds, idConcept, idThesaurus, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }

            if (nodePreference != null) {
                // Si on arrive ici, c'est que tout va bien 
                // alors c'est le moment de supprimer le code ARK
                if (nodePreference.isUseArk()) {
                    // suppression de l'identifiant ARK
                }
                // suppression de l'identifiant Handle
                if (nodePreference.isUseHandle()) {
                    String idHandle = getIdHandleOfConcept(ds, idConcept, idThesaurus);
                    if (!deleteIdHandle(conn, idConcept, idHandle, idThesaurus)) {
                        conn.rollback();
                        conn.close();
                        Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La suppression du Handle a échouée");
                        return false;
                    }
                }
            }
            conn.commit();
            conn.close();
            return true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                try {
                    conn.rollback();
                    conn.close();
                } catch (SQLException ex1) {
                    Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            return false;
        }
    }

    /**
     * Cette fonction permet de supprimer un Concept avec ses relations et
     * traductions, notes, alignements, ...pas de controle s'il a des fils,
     * c'est une suppression définitive
     *
     * @param ds
     * @param idConceptTop
     * @param idUser
     * @param idTheso
     * @return
     */
    public boolean deleteBranchConcept(HikariDataSource ds,
            String idConceptTop, String idTheso, int idUser) {

        TermHelper termHelper = new TermHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        NoteHelper noteHelper = new NoteHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        List<String> idConcepts = getIdsOfBranch2(
                ds,
                idTheso,
                idConceptTop
        );

        // test si les concepts fils ont une poly-hiérarchie, on refuse la suppression (qui peut supprimer plusieurs branches 
        for (String idConcept : idConcepts) {
            if (relationsHelper.isConceptHaveManyRelationBT(ds, idConcept, idTheso)) {
                message = idConcept;
                return false;
            }
        }

        // supprimer les concepts        
        for (String idConcept : idConcepts) {
            if (!deleteConcept__(ds,
                    idConcept, idTheso, idUser,
                    termHelper, relationsHelper, noteHelper, alignmentHelper)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Cette fonction permet de supprimer tous les concepts d'une collection les
     * Concepts avec les relations et traductions, notes, alignements, ...pas de
     * controle s'il a des fils, c'est une suppression définitive
     *
     * @param ds
     * @param idGroup
     * @param idUser
     * @param idTheso
     * @return
     */
    public boolean deleteBranchCollectionConcept(HikariDataSource ds,
            String idGroup, String idTheso, int idUser) {

        TermHelper termHelper = new TermHelper();
        RelationsHelper relationsHelper = new RelationsHelper();
        NoteHelper noteHelper = new NoteHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        ArrayList<String> idConcepts = getAllIdConceptOfThesaurusByGroup(
                ds,
                idTheso,
                idGroup);

        // supprimer les concepts
        for (String idConcept : idConcepts) {
            if (!deleteConcept__(ds,
                    idConcept, idTheso, idUser,
                    termHelper, relationsHelper, noteHelper, alignmentHelper)) {
                return false;
            }
        }
        return true;
    }

    /**
     * permet de supprimer l'appertenance d'un concept à un groupe
     *
     * @param ds
     * @param idConcept
     * @param idGroup
     * @param idThesaurus
     * @param idUser
     * @return
     */
    public boolean deleteGroupOfConcept(HikariDataSource ds,
            String idConcept, String idGroup, String idThesaurus, int idUser) {

        boolean status = false;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from concept_group_concept where idthesaurus ='"
                        + idThesaurus + "' and idconcept ='" + idConcept + "' and idgroup ='" + idGroup + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting groupe of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * permet de supprimer tous les groupes du concept (cas de suppression du
     * concept)
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return
     */
    public boolean deleteAllGroupOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, int idUser) {

        boolean status = false;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("delete from concept_group_concept where idthesaurus ='"
                        + idThesaurus + "' and idconcept ='" + idConcept + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while deleting all groupe of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de supprimer le concept par ID de la table Concept
     *
     * @param conn
     * @param idConcept
     * @param idThesaurus
     * @param idUser
     * @return
     */
    private boolean deleteConceptFromTable(Connection conn, String idConcept, String idThesaurus, int idUser) {
        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("delete from concept where id_thesaurus ='" + idThesaurus
                    + "' and id_concept ='" + idConcept + "'");
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while deleting Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter une traduction à un terme
     */
    public boolean addConceptTraduction(HikariDataSource ds, Term term, int idUser) {

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            if (!new TermHelper().addTermTraduction(conn, term, idUser)) {
                conn.rollback();
                conn.close();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Cette fonction permet d'ajouter une relation à la table
     * hierarchicalRelationship
     */
    public boolean addLinkHierarchicalRelation(Connection conn, HierarchicalRelationship hierarchicalRelationship, int idUser) {

        try (Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("Insert into hierarchical_relationship"
                    + "(id_concept1, id_thesaurus, role, id_concept2)"
                    + " values ("
                    + "'" + hierarchicalRelationship.getIdConcept1() + "'"
                    + ",'" + hierarchicalRelationship.getIdThesaurus() + "'"
                    + ",'" + hierarchicalRelationship.getRole() + "'"
                    + ",'" + hierarchicalRelationship.getIdConcept2() + "')");
            new RelationsHelper().addRelationHistorique(conn,
                    hierarchicalRelationship.getIdConcept1(), hierarchicalRelationship.getIdThesaurus(),
                    hierarchicalRelationship.getIdConcept2(), hierarchicalRelationship.getRole(),
                    idUser, "ADD");
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                System.out.println(sqle.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Cette fonction permet d'ajouter un Concept à la table Concept, en
     * paramètre un objet Classe Concept
     *
     * @param conn
     * @param concept
     * @param idUser
     * @return
     */
    public String addConceptInTable(Connection conn, Concept concept, int idUser) {

        String idConcept = null;
        String idArk = "";
        int idSequenceConcept = -1;

        if (concept.getNotation() == null) {
            concept.setNotation("");
        }

        try (Statement stmt = conn.createStatement()) {
            if (concept.getIdConcept() == null) {
                if (nodePreference.getIdentifierType() == 1) { // identifiants types alphanumérique
                    idConcept = getAlphaNumericId(conn);
                    concept.setIdConcept(idConcept);
                } else {
                    idConcept = getNumericConceptId(conn);
                    concept.setIdConcept(idConcept);
                    if (idConcept != null) {
                        idSequenceConcept = Integer.parseInt(idConcept);
                    }
                }
            } else {
                idConcept = concept.getIdConcept();
            }
            if (idConcept == null) {
                return null;
            }
            if (idSequenceConcept == -1) {
                stmt.executeUpdate("Insert into concept (id_concept, id_thesaurus, id_ark, created ,status, notation, top_concept, creator)"
                        + " values ("
                        + "'" + idConcept + "'"
                        + ",'" + concept.getIdThesaurus() + "'"
                        + ",'" + idArk + "'"
                        + ", current_date"
                        + ",'" + concept.getStatus() + "'"
                        + ",'" + concept.getNotation() + "'"
                        + "," + concept.isTopConcept()
                        + "," + idUser + ")");
            } else {
                stmt.executeUpdate("Insert into concept (id, id_concept, id_thesaurus, id_ark, created, status, notation, top_concept, creator)"
                        + " values ("
                        + idSequenceConcept
                        + ",'" + idConcept + "'"
                        + ",'" + concept.getIdThesaurus() + "'"
                        + ",'" + idArk + "'"
                        + ", current_date"
                        + ",'" + concept.getStatus() + "'"
                        + ",'" + concept.getNotation() + "'"
                        + "," + concept.isTopConcept()
                        + "," + idUser + ")");
            }
            /**
             * Ajout des informations dans la table Concept
             */
            if (!addConceptHistorique(conn, concept, idUser)) {
                stmt.close();
                return null;
            }
        } catch (SQLException sqle) {
            // Log exception
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + idConcept, sqle);
            }
            idConcept = null;
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non
     */
    public boolean isIdExiste(Connection conn, String idConcept) {

        boolean existe = false;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_concept from concept where " + "id_concept = '" + idConcept + "'");
            try (ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet.next()) {
                    existe = resultSet.getRow() != 0;
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if id exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept existe ou non
     */
    public boolean isNotationExist(HikariDataSource ds, String idThesaurus, String notation) {

        boolean existe = false;

        if (notation.isEmpty()) {
            return false;
        }

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and notation ilike '" + notation.trim() + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getRow() != 0;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if Notation exist : " + notation, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept a un createur
     *
     * @param ds
     * @param idThesaurus
     * @param idConcept
     * @return
     */
    public boolean isHaveCreator(HikariDataSource ds, String idThesaurus, String idConcept) {
        boolean existe = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select creator from concept where id_thesaurus = '" + idThesaurus + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if ((resultSet.getInt("creator") != -1) && (resultSet.getInt("creator") != 0)) {
                            existe = true;
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if creator exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si l'ID du concept a un contributeur
     *
     * @param ds
     * @param idThesaurus
     * @param idConcept
     * @return
     */
    public boolean isHaveContributor(HikariDataSource ds, String idThesaurus, String idConcept) {
        boolean existe = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select contributor from concept where id_thesaurus = '" + idThesaurus + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if ((resultSet.getInt("contributor") != -1) && (resultSet.getInt("contributor") != 0)) {
                            existe = true;
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while asking if contributor exist : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet d'ajouter l'historique d'un concept
     */
    public boolean addConceptHistorique(Connection conn, Concept concept, int idUser) {
        boolean status = false;
        String idArk = "";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("Insert into concept_historique (id_concept, id_thesaurus, id_ark, status, notation, top_concept, id_group, id_user)"
                    + " values ('" + concept.getIdConcept() + "','" + concept.getIdThesaurus() + "','" + idArk + "'"
                    + ",'" + concept.getStatus() + "','" + concept.getNotation() + "'," + concept.isTopConcept()
                    + ",'" + concept.getIdGroup() + "','" + idUser + "')");
            status = true;
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + concept.getIdConcept(), sqle);
            } else {
                status = true;
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de récupérer l'historique d'un concept
     */
    public ArrayList<Concept> getConceptHisoriqueAll(HikariDataSource ds, String idConcept, String idThesaurus) {

        ArrayList<Concept> listeConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT modified, status, notation, top_concept, id_group, username from concept_historique, users where id_thesaurus = '"
                        + idThesaurus + "' and id_concept = '" + idConcept + "'"
                        + " and concept_historique.id_user=users.id_user"
                        + " order by modified DESC");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            Concept c = new Concept();
                            c.setIdConcept(idConcept);
                            c.setIdThesaurus(idThesaurus);
                            c.setModified(resultSet.getDate("modified"));
                            c.setStatus(resultSet.getString("status"));
                            c.setNotation(resultSet.getString("notation"));
                            c.setTopConcept(resultSet.getBoolean("top_concept"));
                            c.setIdGroup(resultSet.getString("id_group"));
                            c.setUserName(resultSet.getString("username"));
                            listeConcept.add(c);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting historique of Concept : " + idConcept, sqle);
        }
        return listeConcept;
    }

    /**
     * Cette fonction permet de récupérer l'historique d'un concept à une date
     * précise
     */
    public ArrayList<Concept> getConceptHisoriqueFromDate(HikariDataSource ds,
            String idConcept, String idThesaurus, java.util.Date date) {

        ArrayList<Concept> listeConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT modified, status, notation, top_concept, id_group, username from concept_historique, users where id_thesaurus = '"
                        + idThesaurus + "' and id_concept = '" + idConcept + "'"
                        + " and concept_historique.id_user=users.id_user"
                        + " and modified <= '" + date + "' order by modified DESC");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            Concept c = new Concept();
                            c.setIdConcept(idConcept);
                            c.setIdThesaurus(idThesaurus);
                            c.setModified(resultSet.getDate("modified"));
                            c.setStatus(resultSet.getString("status"));
                            c.setNotation(resultSet.getString("notation"));
                            c.setTopConcept(resultSet.getBoolean("top_concept"));
                            c.setIdGroup(resultSet.getString("id_group"));
                            c.setUserName(resultSet.getString("username"));
                            listeConcept.add(c);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting date historique of Concept : " + idConcept, sqle);
        }
        return listeConcept;
    }

    public boolean addIdHandle(Connection conn, String idConcept, String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        String privateUri;
        
        if(nodePreference.isUseHandleWithCertificat()) {        
            privateUri = "?idc=" + idConcept + "&idt=" + idThesaurus;
            HandleHelper handleHelper = new HandleHelper(nodePreference);

            String idHandle = handleHelper.addIdHandle(privateUri);
            if (idHandle == null) {
                message = handleHelper.getMessage();
                return false;
            }
            return updateHandleIdOfConcept(conn, idConcept, idThesaurus, idHandle);            
        } else {
            // cas de Handle Standard 
            ToolsHelper toolsHelper = new ToolsHelper();

            HandleService hs = HandleService.getInstance();
            hs.applyNodePreference(nodePreference);
            hs.connectHandle();         
            privateUri = nodePreference.getCheminSite() + "?idc=" + idConcept + "&idt=" + idThesaurus;
            String idHandle = toolsHelper.getNewId(25, false, false);
            idHandle = hs.getPrivatePrefix() + idHandle;
            try {
                if(!hs.createHandle(idHandle, privateUri)){
                    message = hs.getMessage();
                    System.out.println(hs.getResponseMsg());
                    return false;
                }
            } catch (UnsupportedEncodingException | HandleException ex) {
                System.out.println(ex.getMessage());
            }
            idHandle = hs.getPrefix() + "/" + idHandle;                
            if (!updateHandleIdOfConcept(conn, idConcept,
                    idThesaurus, idHandle)) {
                return false;
            }    
            return true;
        }
    }

    public boolean generateIdHandle(HikariDataSource conn, String idConcept, String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        String privateUri;
            if(nodePreference.isUseHandleWithCertificat()) {   
            privateUri = "?idc=" + idConcept + "&idt=" + idThesaurus;
            HandleHelper handleHelper = new HandleHelper(nodePreference);

            String idHandle = handleHelper.addIdHandle(privateUri);
            if (idHandle == null) {
                message = handleHelper.getMessage();
                return false;
            }
            return updateHandleIdOfConcept(conn, idConcept, idThesaurus, idHandle);            
        } else {
            // cas de Handle Standard 
            ToolsHelper toolsHelper = new ToolsHelper();

            HandleService hs = HandleService.getInstance();
            hs.applyNodePreference(nodePreference);
            hs.connectHandle();         
            privateUri = nodePreference.getCheminSite() + "?idc=" + idConcept + "&idt=" + idThesaurus;
            String idHandle = toolsHelper.getNewId(25, false, false);
            idHandle = hs.getPrivatePrefix() + idHandle;
            try {
                if(!hs.createHandle(idHandle, privateUri)){
                    message = hs.getMessage();
                    System.out.println(hs.getResponseMsg());
                    return false;
                }
            } catch (UnsupportedEncodingException | HandleException ex) {
                System.out.println(ex.getMessage());
            }
            idHandle = hs.getPrefix() + "/" + idHandle;                
            if (!updateHandleIdOfConcept(conn, idConcept,
                    idThesaurus, idHandle)) {
                return false;
            }    
            return true;                
        }       
    }

    /**
     * permet de générer les identifiants Handle des concepts en paramètres
    * 
    * @param conn
    * @param idConcepts
    * @param idThesaurus
    * @return 
    */
    public boolean generateHandleId(HikariDataSource conn, ArrayList<String> idConcepts, String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        String privateUri;
        
        if(nodePreference.isUseHandleWithCertificat()) {
            // cas de chez Huma-Num
            HandleHelper handleHelper = new HandleHelper(nodePreference);
            String idHandle;
            for (String idConcept : idConcepts) {
                privateUri = "?idc=" + idConcept + "&idt=" + idThesaurus;
                idHandle = handleHelper.addIdHandle(privateUri);
                if (idHandle == null) {
                    message = handleHelper.getMessage();
                    return false;
                }
                if (!updateHandleIdOfConcept(conn, idConcept,
                        idThesaurus, idHandle)) {
                    return false;
                }
            }
            return true;
        } else {
            // cas de Handle Standard 
            ToolsHelper toolsHelper = new ToolsHelper();

            HandleService hs = HandleService.getInstance();
            hs.applyNodePreference(nodePreference);
            hs.connectHandle();            
            
            for (String idConcept : idConcepts) {
                privateUri = nodePreference.getCheminSite() + "?idc=" + idConcept + "&idt=" + idThesaurus;
                String idHandle = toolsHelper.getNewId(25, false, false);
                idHandle = hs.getPrivatePrefix() + idHandle;
                try {
                    if(!hs.createHandle(idHandle, privateUri)){
                        message = hs.getMessage();
                        System.out.println(hs.getResponseMsg());
                        return false;
                    }
                } catch (UnsupportedEncodingException | HandleException ex) {
                    System.out.println(ex.getMessage());
                }
                idHandle = hs.getPrefix() + "/" + idHandle;                
                if (!updateHandleIdOfConcept(conn, idConcept,
                        idThesaurus, idHandle)) {
                    return false;
                }
            }
            return true;            
        }
    }

    /**
     * Permet de supprimer un identifiant Handle de la table Concept et de la
     * plateforme (handle.net) via l'API REST
     */
    private boolean deleteIdHandle(Connection conn, String idConcept, String idHandle, String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        if(nodePreference.isUseHandleWithCertificat()) {
            HandleHelper handleHelper = new HandleHelper(nodePreference);
            if (!handleHelper.deleteIdHandle(idHandle, idThesaurus)) {
                message = handleHelper.getMessage();
                return false;
            }
            return updateHandleIdOfConcept(conn, idConcept,
                    idThesaurus, "");            
        }        
        else {
            // cas de Handle Standard 

            HandleService hs = HandleService.getInstance();
            hs.applyNodePreference(nodePreference);
            hs.connectHandle(); 
            try {
                hs.deleteHandle(idHandle);
            } catch (HandleException ex) {
                System.out.println(ex.toString());
            }    
            return updateHandleIdOfConcept(conn, idConcept,
                    idThesaurus, "");             
        }
    }

    /**
     * Permet de supprimer tous les identifiants Handle de la table Concept et
     * de la plateforme (handle.net) via l'API REST pour un thésaurus donné
     * suite à une suppression d'un thésaurus
     * @param ds
     * @param idThesaurus
     * @return 
     */
    public boolean deleteAllIdHandle(HikariDataSource ds,
            String idThesaurus) {
        if (nodePreference == null) {
            return false;
        }
        if (!nodePreference.isUseHandle()) {
            return false;
        }
        ArrayList<String> tabIdHandle = getAllIdHandleOfThesaurus(ds, idThesaurus);
        
        if(nodePreference.isUseHandleWithCertificat()) {
            HandleHelper handleHelper = new HandleHelper(nodePreference);
            if (!handleHelper.deleteAllIdHandle(tabIdHandle)) {
                message = handleHelper.getMessage();
                return false;
            }
            message = handleHelper.getMessage();
            return true;            
        } else {
            HandleService hs = HandleService.getInstance();
            hs.applyNodePreference(nodePreference);
            hs.connectHandle(); 
            for (String idHandle : tabIdHandle) {
                try {
                    hs.deleteHandle(idHandle);
                } catch (HandleException ex) {
                    System.out.println(ex.toString());
                }                  
            }
            return true;
        }
    }

    /**
     * Cette fonction permet d'ajouter un domaine à un Concept dans la table
     * Concept, en paramètre un objet Classe Concept
     */
    public boolean addNewGroupOfConcept(Connection conn, Concept concept, int idUser) {

        boolean status = false;

        try {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                if (!addConceptHistorique(conn, concept, idUser)) {
                    conn.rollback();
                    conn.close();
                    return false;
                }
                stmt.executeUpdate("Insert into concept_group_concept "
                        + "(idgroup, idthesaurus, idconcept)"
                        + " values ("
                        + "'" + concept.getIdConcept() + "'"
                        + ",'" + concept.getIdThesaurus() + "'"
                        + ",'" + concept.getIdArk() + "'"
                        + ",'" + concept.getStatus() + "'"
                        + ",'" + concept.getNotation() + "'"
                        + "," + concept.isTopConcept()
                        + ",'" + concept.getIdGroup() + "')");
                status = true;
                conn.commit();
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + concept.getIdConcept(), sqle);
            }
        }
        return status;
    }

    public boolean addNewGroupOfConcept(HikariDataSource ds, String idconcept, String idgroup, String idthesaurus) {

        boolean status = false;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO concept_group_concept (idgroup, idthesaurus, idconcept) VALUES ('"
                        + idgroup + "', '" + idthesaurus + "', '" + idconcept + "');");
                status = true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept to Group : " + idgroup, sqle);
            }
        }
        return status;
    }

    /**
     * Cette fonction permet d'insérrer un Concept dans la table Concept avec un
     * idConcept existant (Import) avec Rollback
     *
     * @param ds
     * @param concept
     * @param idUser
     * @return
     */
    public boolean insertConceptInTable(HikariDataSource ds, Concept concept, int idUser) {

        String created;
        String modified;
        if (concept.getCreated() == null) {
            created = null;
        } else {
            created = "'" + concept.getCreated() + "'";
        }
        if (concept.getModified() == null) {
            modified = null;
        } else {
            modified = "'" + concept.getModified() + "'";
        }
        if (concept.getStatus() == null) {
            concept.setStatus("D");
        }
        if (concept.getIdArk() == null) {
            concept.setIdArk("");
        }
        if (concept.getIdHandle() == null) {
            concept.setIdHandle("");
        }
        if (concept.getNotation() == null) {
            concept.setNotation("");
        }

        UserHelper userHelper = new UserHelper();
        concept.setCreator(userHelper.getIdUserFromPseudo(ds, concept.getCreatorName()));
        concept.setContributor(userHelper.getIdUserFromPseudo(ds, concept.getContributorName()));

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("Insert into concept "
                        + "(id_concept, id_thesaurus, id_ark, created, modified, status, notation, top_concept, id_handle, id_doi, creator, contributor)"
                        + " values ("
                        + "'" + concept.getIdConcept() + "'"
                        + ",'" + concept.getIdThesaurus() + "'"
                        + ",'" + concept.getIdArk() + "'"
                        + "," + created
                        + "," + modified
                        + ",'" + concept.getStatus() + "'"
                        + ",'" + concept.getNotation() + "'"
                        + "," + concept.isTopConcept()
                        + ",'" + concept.getIdHandle() + "'"
                        + ",'" + concept.getIdDoi() + "'"
                        + "," + concept.getCreator()
                        + "," + concept.getContributor()
                        + ")");
                return true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while adding Concept : " + concept.getIdConcept(), sqle);
            } else {
                return true;
            }
        }
        return false;
    }

    public NodeStatus getNodeStatus(HikariDataSource ds, String idConcept, String idThesaurus) {
        NodeStatus nodeStatus = new NodeStatus();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT * FROM candidat_status WHERE id_concept = '" + idConcept + "';");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeStatus.setIdConcept(resultSet.getString("id_concept"));
                        nodeStatus.setIdStatus(resultSet.getString("id_status"));
                        nodeStatus.setDate(resultSet.getString("date"));
                        nodeStatus.setIdUser(resultSet.getString("id_user"));
                        nodeStatus.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        nodeStatus.setMessage(resultSet.getString("message"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting NodeStatus  du Concept : " + idConcept, sqle);
        }
        return nodeStatus;
    }

    public boolean setNodeStatus(HikariDataSource ds, String idConcept, String idThesaurus, String idStatus,
            String date, int idUser, String message) {

        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("INSERT INTO candidat_status(id_concept, id_status, date, id_user, id_thesaurus, message) VALUES ('"
                        + idConcept + "', '" + idStatus + "', '" + date + "', " + idUser + ", '" + idThesaurus + "', '" + message + "');");
                status = true;
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                log.error("Error while setNodeStatus du Concept : " + idConcept, sqle);
            } else {
                status = true;
            }
        }
        return status;
    }

    /**
     * Cette fonction permet de récupérer un Concept par son id et son thésaurus
     * sous forme de classe Concept (sans les relations) ni le Terme
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public Concept getThisConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        Concept concept = null;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from concept where id_thesaurus = '" + idThesaurus + "'"
                        + " and id_concept = '" + idConcept + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        concept = new Concept();
                        concept.setIdConcept(idConcept);
                        concept.setIdThesaurus(idThesaurus);
                        concept.setIdArk(resultSet.getString("id_ark"));
                        concept.setIdHandle(resultSet.getString("id_handle"));
                        concept.setIdDoi(resultSet.getString("id_doi"));
                        concept.setCreated(resultSet.getDate("created"));
                        concept.setModified(resultSet.getDate("modified"));
                        concept.setStatus(resultSet.getString("status"));
                        concept.setNotation(resultSet.getString("notation"));
                        concept.setTopConcept(resultSet.getBoolean("top_concept"));
                        concept.setCreator(resultSet.getInt("creator"));
                        concept.setContributor(resultSet.getInt("contributor"));
                        concept.setIdGroup("");//resultSet.getString("idgroup"));
                        concept.setConceptType(resultSet.getString("concept_type").toLowerCase());
                    }
                }
                UserHelper userHelper = new UserHelper();
                if (concept != null) {
                    String contributor = userHelper.getNameUser(ds, concept.getContributor());
                    String creator = userHelper.getNameUser(ds, concept.getCreator());
                    if (contributor != null && !contributor.isEmpty()) {
                        concept.setContributorName(contributor);
                    } else {
                        concept.setContributorName("");
                    }
                    if (creator != null && !creator.isEmpty()) {
                        concept.setCreatorName(creator);
                    } else {
                        concept.setCreatorName("");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Concept : " + idConcept, sqle);
        }
        return concept;
    }

    /**
     * Cette fonction permet de récupérer la date de modificatin du Concept
     */
    public Date getModifiedDateOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        Date date = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select modified from concept where id_thesaurus = '"
                        + idThesaurus + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    if (resultSet.getRow() != 0) {
                        date = resultSet.getDate("modified");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting modified date of Concept : " + idConcept, sqle);
        }
        return date;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     */
    public ArrayList<String> getAllIdConceptOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and concept.status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Temporaire à supprimer par la suite ou à faire évoluer pour un export par
     * utilisateur
     */
    public ArrayList<String> getAllIdConceptOfThesaurusByUser(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select DISTINCT id_concept from concept\n"
                        + "where \n"
                        + "concept.id_thesaurus = 'th17'\n"
                        + "and\n"
                        + "concept.status != 'CA'\n"
                        + "and \n"
                        + "( \n"
                        + "	(concept.creator in (78,83)) \n"
                        + "	or   \n"
                        + "	(concept.contributor in (78,83))\n"
                        + ")");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Temporaire à supprimer par la suite ou à faire évoluer pour un export par
     * utilisateur
     */
    public ArrayList<String> getAllIdConceptOfThesaurusByUser2(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select DISTINCT internal_id_concept from alignement\n"
                        + "where \n"
                        + "internal_id_thesaurus = 'th17'\n"
                        + "and \n"
                        + "alignement.author in (78,83)");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * qui n'ont pas de group, pour permettre de retrouver les groupes manquants
     */
    public ArrayList<String> getAllIdConceptOfThesaurusWithoutGroup(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus ='" + idThesaurus + "'"
                        + " and concept.status != 'CA' and id_concept not in (select idconcept from"
                        + " concept_group_concept where idthesaurus = '" + idThesaurus + "')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus without Group : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer le nombre de concepts d'un thésaurus
     */
    public int getConceptCountOfThesaurus(HikariDataSource ds, String idThesaurus) {

        int count = 0;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select count(id_concept) from concept where id_thesaurus = '"
                        + idThesaurus + "' and concept.status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) != 0) {
                            count = resultSet.getInt(1);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting count of all IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return count;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id Handle d'un thésaurus
     */
    public ArrayList<String> getAllIdHandleOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> tabId = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_handle from concept where id_thesaurus = '" + idThesaurus + "'"
                        + " and (id_handle != null or id_handle != '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if (resultSet.getString("id_handle") != null) {
                            if (!resultSet.getString("id_handle").isEmpty()) {
                                tabId.add(resultSet.getString("id_handle"));
                            }
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdHandle of Thesaurus : " + idThesaurus, sqle);
        }
        return tabId;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id Ark d'un thésaurus
     */
    public ArrayList<String> getAllIdArkOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> tabId = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_ark from concept where id_thesaurus = '" + idThesaurus + "'"
                        + " and (id_ark != null or id_ark != '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if (resultSet.getString("id_ark") != null) {
                            if (!resultSet.getString("id_ark").isEmpty()) {
                                tabId.add(resultSet.getString("id_ark"));
                            }
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdHandle of Thesaurus : " + idThesaurus, sqle);
        }
        return tabId;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id Ark d'un thésaurus
     * sous forme de MAP avec idConcept + idArk
     */
    public HashMap<String, String> getAllIdArkOfThesaurusMap(HikariDataSource ds, String idThesaurus) {

        HashMap<String, String> tabIds = new LinkedHashMap<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept, id_ark from concept where id_thesaurus = '"
                        + idThesaurus + "' and (id_ark != null or id_ark != '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        if (resultSet.getString("id_ark") != null) {
                            if (!resultSet.getString("id_ark").isEmpty()) {
                                tabIds.put(resultSet.getString("id_concept"), resultSet.getString("id_ark"));
                            }
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdHandle of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIds;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * qui n'ont pas d'identifiants Ark
     *
     * @param ds
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> getAllIdConceptOfThesaurusWithoutArk(HikariDataSource ds, String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and (id_ark = '' or id_ark = null) and status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus without Ark : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * qui n'ont pas d'identifiants Handle
     */
    public ArrayList<String> getAllIdConceptOfThesaurusWithoutHandle(HikariDataSource ds,
            String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and (id_handle = '' or id_handle = null)");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus without Handle : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer le total des Id concept d'un thésaurus
     * en filtrant par Domaine/Group
     */
    public int getCountConceptOfThesaurusByLang(HikariDataSource ds,
            String idThesaurus, String idLang) {

        int count = -1;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT count(concept.id_concept) "
                        + " FROM concept, term, preferred_term "
                        + " WHERE "
                        + " concept.id_concept = preferred_term.id_concept"
                        + " and"
                        + " concept.id_thesaurus = preferred_term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and"
                        + " preferred_term.id_term = term.id_term"
                        + " AND"
                        + " concept.id_thesaurus = '" + idThesaurus + "' "
                        + " AND"
                        + " concept.status != 'CA'"
                        + " and term.lang = '" + idLang + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt("count");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return count;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * en filtrant par plusieurs domaines/Groupes
     *
     * @param ds
     * @param idThesaurus
     * @param idGroups
     * @return
     */
    public ArrayList<String> getAllIdConceptOfThesaurusByMultiGroup(HikariDataSource ds,
            String idThesaurus, String[] idGroups) {

        ArrayList<String> tabIdConcept = new ArrayList<>();
        String multiValuesGroup = "";
        // filter by group
        if (idGroups != null && idGroups.length != 0) {
            String groupSearch = "";
            for (String idGroup : idGroups) {
                if (groupSearch.isEmpty()) {
                    groupSearch = "lower('" + idGroup + "')";
                } else {
                    groupSearch = groupSearch + ",lower('" + idGroup + "')";
                }
            }
            multiValuesGroup = " and lower(concept_group_concept.idgroup) in (" + groupSearch + ")";
        }

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT concept.id_concept "
                        + " FROM concept, concept_group_concept "
                        + " WHERE "
                        + " concept.id_concept = concept_group_concept.idconcept"
                        + " AND"
                        + " concept.id_thesaurus = concept_group_concept.idthesaurus "
                        + " AND"
                        + " concept.id_thesaurus = '" + idThesaurus + "' "
                        + " AND"
                        + " concept.status != 'CA' "
                        + multiValuesGroup);

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by multiGroups : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * en filtrant par Domaine/Group
     */
    public ArrayList<String> getAllIdConceptOfThesaurusByGroup(HikariDataSource ds,
            String idThesaurus, String idGroup) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT concept.id_concept "
                        + " FROM concept, concept_group_concept "
                        + " WHERE "
                        + " concept.id_concept = concept_group_concept.idconcept"
                        + " AND"
                        + " concept.id_thesaurus = concept_group_concept.idthesaurus "
                        + " AND"
                        + " concept.id_thesaurus = '" + idThesaurus + "' "
                        + " AND"
                        + " concept.status != 'CA' "
                        + " AND"
                        + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "')");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus by Group : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * retourne tous les concepts d'un Groupe pour un thésaurus Permet de
     * retourner une ArrayList de String (idConcept) par thésaurus et par groupe
     * / ou null si rien
     */
    public ArrayList<String> getListConceptIdOfGroup(HikariDataSource ds, String idGroup,
            String idThesaurus) {

        ArrayList tabIdConceptGroup = null;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idconcept from concept_group_concept where idthesaurus = '"
                        + idThesaurus + "' and idgroup = '" + idGroup + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    tabIdConceptGroup = new ArrayList();
                    while (resultSet.next()) {
                        tabIdConceptGroup.add(resultSet.getString("idconcept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List Id or Concepts of group : " + idGroup, sqle);
        }
        return tabIdConceptGroup;
    }

    /**
     * Permet de retourner tous les identifiants BT pour un concept donné dans
     * le même groupe cette fonction permet de connaitre la polyhierarchie d'un
     * concept dans un domaine
     */
    public ArrayList<String> getAllBTOfConceptOfThisGroup(HikariDataSource ds,
            String idConcept, String idGroup, String idTheso) {

        ArrayList<String> tabIdBT = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept2 from hierarchical_relationship, concept_group_concept"
                        + " where"
                        + " concept_group_concept.idthesaurus = hierarchical_relationship.id_thesaurus AND"
                        + " concept_group_concept.idconcept = hierarchical_relationship.id_concept1 AND"
                        + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "') AND"
                        + " hierarchical_relationship.role = 'BT' AND"
                        + " hierarchical_relationship.id_concept1 = '" + idConcept + "' AND"
                        + " hierarchical_relationship.id_thesaurus = '" + idTheso + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        tabIdBT.add(resultSet.getString("id_concept2"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdBT of this Group of Concept : " + idConcept, sqle);
        }
        return tabIdBT;
    }

    /**
     * Cette fonction permet de récupérer la liste des Id concept d'un thésaurus
     * (cette fonction sert pour la génération des identifiants pour Wikidata)
     */
    public ArrayList<NodeConceptArkId> getAllConceptArkIdOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<NodeConceptArkId> nodeConceptArkIds = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept, id_ark from concept where id_thesaurus = '"
                        + idThesaurus + "' order by id_concept ASC");
                try (ResultSet resultSet = stmt.getResultSet()) {

                    while (resultSet.next()) {
                        NodeConceptArkId nodeConceptArkId = new NodeConceptArkId();
                        nodeConceptArkId.setIdConcept(resultSet.getString("id_concept"));
                        nodeConceptArkId.setIdArk(resultSet.getString("id_ark"));
                        nodeConceptArkIds.add(nodeConceptArkId);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All Id of Concept _ Ark of Thesaurus : " + idThesaurus, sqle);
        }
        return nodeConceptArkIds;
    }

    public ArrayList<String> getAllIdConceptOfThesaurus(Connection conn, String idThesaurus) {

        ArrayList<String> tabIdConcept = new ArrayList<>();

        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("select id_concept from concept where id_thesaurus = '" + idThesaurus + "'");
            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    tabIdConcept.add(resultSet.getString("id_concept"));
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting All IdConcept of Thesaurus : " + idThesaurus, sqle);
        }
        return tabIdConcept;
    }

    /**
     * Cette fonction permet d'exporter tous les concepts d'un thésaurus et les
     * charger dans la classe No
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param nodeConceptExports
     * @return
     */
    public ArrayList<NodeConceptExport> exportAllConcepts(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<NodeConceptExport> nodeConceptExports) {

        ConceptHelper conceptHelper = new ConceptHelper();

        ArrayList<String> listIdsOfConceptChildren = conceptHelper.getListChildrenOfConcept(ds, idConcept, idThesaurus);

        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idConcept, idThesaurus, false, false);

        //    System.out.println("IdConcept = " + idConcept);
        /// attention il y a un problème ici, il faut vérifier pourquoi nous avons un Concept Null
        if (nodeConcept == null || nodeConcept.getConcept() == null) {
            System.err.println("Attention Null proche de = : " + idConcept);
            return null;
        }

        nodeConceptExports.add(nodeConcept);

        for (String listIdsOfConceptChildren1 : listIdsOfConceptChildren) {
            nodeConcept = conceptHelper.getConceptForExport(ds, listIdsOfConceptChildren1, idThesaurus, false, false);
            nodeConceptExports.add(nodeConcept);
            if (!nodeConcept.getNodeListOfNT().isEmpty()) {
                for (int j = 0; j < nodeConcept.getNodeListOfNT().size(); j++) {

                    exportAllConcepts(ds,
                            nodeConcept.getNodeListOfNT().get(j).getUri().getIdConcept(),
                            idThesaurus, nodeConceptExports);
                }
            }
        }
        return nodeConceptExports;
    }

    /**
     * Cette fonction permet de récupérer le nom d'un Concept sinon renvoie une
     * chaine vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @return
     */
    public String getLexicalValueOfConcept(HikariDataSource ds, String idConcept, String idThesaurus, String idLang) {

        String lexicalValue = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lexical_value from term, preferred_term where"
                        + " preferred_term.id_term = term.id_term AND"
                        + " preferred_term.id_thesaurus = term.id_thesaurus"
                        + " and term.id_thesaurus = '" + idThesaurus + "'"
                        + " and preferred_term.id_concept = '" + idConcept + "'"
                        + " and term.lang = '" + idLang + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        lexicalValue = resultSet.getString("lexical_value");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting LexicalValue of Concept : " + idConcept, sqle);
        }
        return lexicalValue.trim();
    }

    /**
     * Cette fonction permet de récupérer l'identifiant Ark sinon renvoie une
     * chaine vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public String getIdArkOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        String ark = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_ark from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        ark = resultSet.getString("id_ark").trim();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idArk of Concept : " + idConcept, sqle);
        }
        return ark;
    }

    /**
     * Cette fonction permet de récupérer la notation sinon renvoie une chaine
     * vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public String getNotationOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        String notation = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select notation from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        notation = resultSet.getString("notation") == null ? "" : resultSet.getString("notation").trim();
                        //notation = resultSet.getString("notation").trim();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting notation of Concept : " + idConcept, sqle);
        }
        return notation;
    }

    /**
     * Cette fonction permet de récupérer la notation sinon renvoie une chaine
     * vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public String getTypeOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        String conceptType = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept_type from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        conceptType = resultSet.getString("concept_type") == null ? "" : resultSet.getString("concept_type").trim();
                        //notation = resultSet.getString("notation").trim();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting notation of Concept : " + idConcept, sqle);
        }
        return conceptType;
    }

    /**
     * Cette fonction permet de récupérer les identifiants d'un concept idArk,
     * idHandle, idConcept sous forme de nodeUri
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public NodeUri getNodeUriOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        NodeUri nodeUri = new NodeUri();
        nodeUri.setIdConcept(idConcept);

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_ark, id_handle from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        nodeUri.setIdArk(resultSet.getString("id_ark"));
                        nodeUri.setIdHandle(resultSet.getString("id_handle"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting nodeUri of Concept : " + idConcept, sqle);
        }
        return nodeUri;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant Handle sinon renvoie une
     * chaine vide
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public String getIdHandleOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        String handle = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_handle from concept where id_thesaurus = '"
                        + idThesaurus + "' and id_concept = '" + idConcept + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        handle = resultSet.getString("id_handle");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idHandle of Concept : " + idConcept, sqle);
        }
        return handle;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idArk
     *
     * @param ds
     * @param arkId
     * @param idTheso
     * @return
     */
    public String getIdConceptFromArkId(HikariDataSource ds, String arkId, String idTheso) {
        String idConcept = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where"
                        + " id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " REPLACE(concept.id_ark, '-', '') ilike REPLACE('" + arkId + "', '-', '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept").trim();
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcept by idArk : " + arkId, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après
     * l'idArk
     */
    public String getIdConceptFromHandleId(HikariDataSource ds, String handleId) {
        String idConcept = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_handle ilike '" + handleId + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcept by idArk : " + handleId, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Concept d'après l'id
     * du concept !!!! ATTENTION !!!! l'id du concept peut se trouver dans
     * plusieurs thésaurus différents donc on ne retourne que le premier.
     */
    public String getIdThesaurusFromIdConcept(HikariDataSource ds, String idConcept) {
        String idThesaurus = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from concept where id_concept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idThesaurus by idConcept: " + idConcept, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du thésaurus d'après
     * l'idArk
     */
    public String getIdThesaurusFromArkId(HikariDataSource ds, String arkId) {
        String idThesaurus = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from concept where " +
                        " REPLACE(concept.id_ark, '-', '') = REPLACE('" + arkId + "', '-', '')");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idThesaurus by idArk : " + arkId, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du thésaurus d'après
     * l'idHandle
     */
    public String getIdThesaurusFromHandleId(HikariDataSource ds, String handleId) {

        String idThesaurus = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_thesaurus from concept where id_handle = '" + handleId + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idThesaurus = resultSet.getString("id_thesaurus");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idThesaurus by idArk : " + handleId, sqle);
        }
        return idThesaurus;
    }

    /**
     * Cette fonction permet de récupérer l'identifiant du Group d'un Concept
     */
    public String getGroupIdOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        String idGroup = null;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idgroup from concept_group_concept where idthesaurus = '"
                        + idThesaurus + "' and idconcept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        if (resultSet.next()) {
                            idGroup = resultSet.getString("idgroup");
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Id of group of Concept : " + idConcept, sqle);
        }
        return idGroup;
    }

    /**
     * Cette fonction permet de récupérer les identifiants des Group d'un
     * Concept
     */
    public ArrayList<String> getListGroupIdOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {
        ArrayList<String> idGroup = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select idgroup from concept_group_concept where idthesaurus = '"
                        + idThesaurus + "' and idconcept = '" + idConcept + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroup.add(resultSet.getString("idgroup"));
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Id of group of Concept : " + idConcept, sqle);
        }
        return idGroup;
    }

    /**
     * Cette fonction permet de récupérer les identifiants des Group d'un
     * Concept dont il est le fils direct
     */
    public ArrayList<String> getListGroupParentIdOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        ArrayList<String> idGroup = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_group from concept where id_thesaurus = '" + idThesaurus
                        + "' and id_concept = '" + idConcept + "' and top_concept=true");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroup.add(resultSet.getString("id_group"));
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Id of group of Concept : " + idConcept, sqle);
        }
        return idGroup;
    }

    public ArrayList<String> getListGroupParentIdOfGroup(HikariDataSource ds,
            String idGRoup, String idThesaurus) {

        ArrayList<String> idGroupParentt = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_group1 from relation_group where id_thesaurus = '"
                        + idThesaurus + "' and id_group2 = '" + idGRoup + "' and relation='sub'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroupParentt.add(resultSet.getString("id_group1"));
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Id of group of Concept : " + idGRoup, sqle);
        }
        return idGroupParentt;
    }

    public ArrayList<String> getListGroupChildIdOfGroup(HikariDataSource ds, String idGRoup, String idThesaurus) {

        ArrayList<String> idGroupParentt = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_group2 from relation_group where id_thesaurus = '"
                        + idThesaurus + "' and id_group1 = '" + idGRoup + "' and relation='sub'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroupParentt.add(resultSet.getString("id_group2"));
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Id of group of Concept : " + idGRoup, sqle);
        }
        return idGroupParentt;
    }

    /**
     * Cette fonction permet de récupérer les identifiants des Group des parents
     * d'un concept SAUF les groupes du parent passé en paramètre
     */
    public ArrayList<String> getListGroupIdParentOfConceptOtherThan(HikariDataSource ds,
            ArrayList<String> idConceptParent, String idThesaurus, String idNoGroup) {

        ArrayList<String> idGroup = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "SELECT DISTINCT id_group from concept where id_thesaurus = '"
                        + idThesaurus + "' and (";
                for (String s : idConceptParent) {
                    query += "id_concept = '" + s + "' or ";
                }
                query = query.substring(0, query.length() - 4);
                query += ") and id_concept != '" + idNoGroup + "'";
                stmt.executeQuery(query);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            idGroup.add(resultSet.getString("id_group"));
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting Id group of parent of Concept", sqle);
        }
        return idGroup;
    }

    /**
     * Cettte fonction permet de retourner la liste des TopConcept avec IdArk et
     * handle pour un groupe
     */
    public ArrayList<NodeUri> getListIdsOfTopConceptsForExport(HikariDataSource ds,
            String idGroup, String idThesaurus) {

        ArrayList<NodeUri> NodeUris = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept, id_ark, id_handle from concept"
                        + " left join concept_group_concept on id_concept = idconcept"
                        + " and id_thesaurus = idthesaurus where id_thesaurus = '"
                        + idThesaurus + "' and idgroup = '" + idGroup + "' and top_concept = true");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeUri nodeUri = new NodeUri();
                        if ((resultSet.getString("id_ark") == null) || (resultSet.getString("id_ark").trim().isEmpty())) {
                            nodeUri.setIdArk("");
                        } else {
                            nodeUri.setIdArk(resultSet.getString("id_ark"));
                        }
                        if ((resultSet.getString("id_handle") == null) || (resultSet.getString("id_handle").trim().isEmpty())) {
                            nodeUri.setIdHandle("");
                        } else {
                            nodeUri.setIdHandle(resultSet.getString("id_handle"));
                        }
                        nodeUri.setIdConcept(resultSet.getString("id_concept"));

                        NodeUris.add(nodeUri);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Liste ID of TT of Group with ark and handle : " + idGroup, sqle);
        }
        return NodeUris;
    }

    /**
     * Cette fonction permet de récupérer la liste des Ids of Topconcepts
     * suivant l'id du groupe et le thésaurus
     */
    public ArrayList<String> getListIdsOfTopConceptsByGroup(HikariDataSource ds,
            String idGroup, String idThesaurus) {

        ArrayList<String> listIdOfTopConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept from concept where id_thesaurus = '"
                        + idThesaurus + "' and id_concept IN (SELECT idconcept FROM concept_group_concept WHERE idgroup = '"
                        + idGroup + "' AND idthesaurus = '" + idThesaurus + "') and top_concept = true");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdOfTopConcept.add(resultSet.getString("id_concept"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Ids of TopConcept of Group : " + idGroup, sqle);
        }
        return listIdOfTopConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Ids of Topconcepts pour
     * un thésaurus
     */
    public ArrayList<NodeTT> getAllListIdsOfTopConcepts(HikariDataSource ds, String idThesaurus) {

        ArrayList<NodeTT> listIdOfTopConcept = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String query = "select id_concept,"
                        + "id_ark, id_handle, idgroup from concept left join concept_group_concept on id_concept = idconcept and id_thesaurus = idthesaurus where id_thesaurus = '"
                        + idThesaurus + "'"
                        + " and top_concept = true";
                stmt.executeQuery(query);
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTT nodeTT = new NodeTT();
                        nodeTT.setIdConcept(resultSet.getString("id_concept"));
                        nodeTT.setIdArk(resultSet.getString("id_ark"));
                        nodeTT.setIdArk(resultSet.getString("id_handle"));
                        nodeTT.setIdGroup(resultSet.getString("idgroup"));
                        listIdOfTopConcept.add(nodeTT);
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Ids of TopConcept of thésaurus : " + idThesaurus, sqle);
            listIdOfTopConcept = null;
        }
        return listIdOfTopConcept;
    }

    /**
     * Cette fonction permet de récupérer la liste des Topconcepts suivant l'id
     * du groupe et le thésaurus sous forme de classe NodeConceptTree (sans les
     * relations)
     */
    public ArrayList<NodeConceptTree> getListTopConcepts(HikariDataSource ds,
            String idGroup, String idThesaurus, String idLang, boolean isSortByNotation) {

        ResultSet resultSet = null;
        ArrayList<NodeConceptTree> nodeConceptTree = null;
        String query;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                if (isSortByNotation) {
                    query = "SELECT concept.notation, concept.status, concept.id_concept"
                            + " FROM concept, concept_group_concept WHERE"
                            + " concept_group_concept.idconcept = concept.id_concept AND"
                            + " concept_group_concept.idthesaurus = concept.id_thesaurus AND"
                            + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "') AND"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND"
                            + " concept.top_concept = true"
                            + " ORDER BY concept.notation ASC";
                } else {
                    query = "SELECT concept.status, concept.id_concept"
                            + " FROM concept, concept_group_concept WHERE"
                            + " concept_group_concept.idconcept = concept.id_concept AND"
                            + " concept_group_concept.idthesaurus = concept.id_thesaurus AND"
                            + " lower(concept_group_concept.idgroup) = lower('" + idGroup + "') AND"
                            + " concept.id_thesaurus = '" + idThesaurus + "' AND"
                            + " concept.top_concept = true";
                }

                stmt.executeQuery(query);
                resultSet = stmt.getResultSet();
                nodeConceptTree = new ArrayList<>();
                while (resultSet.next()) {
                    NodeConceptTree nodeConceptTree1 = new NodeConceptTree();
                    nodeConceptTree1.setIdConcept(resultSet.getString("id_concept"));
                    if (isSortByNotation) {
                        nodeConceptTree1.setNotation(resultSet.getString("notation"));
                    }
                    nodeConceptTree1.setStatusConcept(resultSet.getString("status"));
                    nodeConceptTree1.setIdThesaurus(idThesaurus);
                    nodeConceptTree1.setIdLang(idLang);
                    nodeConceptTree1.setTopTerm(true);
                    nodeConceptTree.add(nodeConceptTree1);
                }
                for (NodeConceptTree nodeConceptTree1 : nodeConceptTree) {
                    query = "SELECT term.lexical_value FROM"
                            + " preferred_term, term WHERE"
                            + " preferred_term.id_term = term.id_term AND"
                            + " preferred_term.id_thesaurus = term.id_thesaurus AND"
                            + " term.lang = '" + idLang + "' AND"
                            + " preferred_term.id_concept = '" + nodeConceptTree1.getIdConcept() + "' AND"
                            + " term.id_thesaurus = '" + idThesaurus + "'";

                    stmt.executeQuery(query);
                    resultSet = stmt.getResultSet();
                    resultSet.next();
                    if (resultSet.getRow() == 0) {
                        nodeConceptTree1.setTitle("");
                    } else {
                        nodeConceptTree1.setTitle(resultSet.getString("lexical_value"));
                    }
                    nodeConceptTree1.setHaveChildren(
                            haveChildren(ds, idThesaurus, nodeConceptTree1.getIdConcept())
                    );
                }
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while getting TopConcept of Group : " + idGroup, sqle);
        }
        if (!isSortByNotation) {
            Collections.sort(nodeConceptTree);
        }
        return nodeConceptTree;
    }

    /**
     * Cette fonction permet de rendre un Concept de type Topconcept
     */
    public boolean setNotTopConcept(HikariDataSource ds, String idConcept, String idThesaurus) {

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set top_concept = false WHERE id_concept ='" + idConcept
                        + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de rendre un Concept de type Topconcept
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean setTopConcept(HikariDataSource ds, String idConcept, String idThesaurus) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set top_concept = true WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de mettre à jour le createur
     *
     * @param ds
     * @param idThesaurus
     * @param idConcept
     * @param idCreator
     * @return
     */
    public boolean setCreator(HikariDataSource ds, String idThesaurus, String idConcept, int idCreator) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set creator = " + idCreator
                        + " WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating creator of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de mettre à jour le contributeur
     *
     * @param ds
     * @param idThesaurus
     * @param idConcept
     * @param idContributor
     * @return
     */
    public boolean setContributor(HikariDataSource ds, String idThesaurus, String idConcept, int idContributor) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set contributor = " + idContributor
                        + " WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idThesaurus + "'");
                return true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating contributor of concept : " + idConcept, sqle);
        }
        return false;
    }

    /**
     * Cette fonction permet de savoir si le Concept est déprécié
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public boolean isDeprecated(HikariDataSource ds, String idConcept, String idThesaurus) {
        boolean existe = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select status from concept where id_concept = '"
                        + idConcept + "' and id_thesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if (resultSet.getString("status").equalsIgnoreCase("dep")) {
                            existe = true;
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Asking if TopConcept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Concept est un TopConcept
     */
    public boolean isTopConcept(HikariDataSource ds, String idConcept, String idThesaurus, String idGroup) {
        boolean existe = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select top_concept from concept where id_concept = '"
                        + idConcept + "' and id_thesaurus = '" + idThesaurus
                        + "' and id_group = '" + idGroup + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getBoolean("top_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Asking if TopConcept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de savoir si le Concept est un TopConcept sans
     * définir le group (pour permettre de nettoyer les orphelins)
     */
    public boolean isTopConcept(HikariDataSource ds, String idConcept, String idThesaurus) {
        boolean existe = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select top_concept from concept where id_concept = '" + idConcept
                        + "' and id_thesaurus = '" + idThesaurus + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        existe = resultSet.getBoolean("top_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while Asking if TopConcept : " + idConcept, sqle);
        }
        return existe;
    }

    /**
     * Cette fonction permet de récupérer les Ids des concepts suivant l'id du
     * Concept-Père et le thésaurus sous forme de classe tableau avec tri par
     * label
     *
     * @param ds
     * @param idConcept
     * @param idLang
     * @param idThesaurus
     * @return
     */
    public ArrayList<NodeIdValue> getListChildrenOfConceptSorted(HikariDataSource ds, String idConcept, String idLang, String idThesaurus) {

        //   ArrayList<String> listIdsOfConcept = getListChildrenOfConcept(ds, idConcept, idThesaurus);
        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
        List<NodeConceptTree> nodeConceptTrees = daoResourceHelper.getConceptsNTForTree(ds, idThesaurus, idConcept, idLang, false);

        ArrayList<NodeIdValue> listIdsTemp = new ArrayList<>();

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            if (StringUtils.isEmpty(nodeConceptTree.getTitle())) {
                nodeIdValue.setId(nodeConceptTree.getIdConcept());
                nodeIdValue.setValue(nodeConceptTree.getIdConcept());
            } else {
                nodeIdValue.setId(nodeConceptTree.getIdConcept());
                nodeIdValue.setValue(nodeConceptTree.getTitle());
            }
            listIdsTemp.add(nodeIdValue);
        }
        return listIdsTemp;

        /*
        String label;
        for (String idC : listIdsOfConcept) {
            label = getLexicalValueOfConcept(ds, idC, idThesaurus, idLang);
            NodeIdValue nodeIdValue = new NodeIdValue();

            if (label == null || label.isEmpty()) {
                nodeIdValue.setId(idC);
                nodeIdValue.setValue(idC);
            } else {
                nodeIdValue.setId(idC);
                nodeIdValue.setValue(label);
            }
            listIdsTemp.add(nodeIdValue);
        }
        Collections.sort(listIdsTemp);
        return listIdsTemp;*/
    }

    /**
     * Cette fonction permet de récupérer les Ids des concepts suivant l'id du
     * Concept-Père et le thésaurus sous forme de classe tableau pas de tri
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return
     */
    public ArrayList<String> getListChildrenOfConcept(HikariDataSource ds, String idConcept, String idThesaurus) {
        ArrayList<String> listIdsOfConcept = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select id_concept2 from hierarchical_relationship where id_thesaurus = '"
                        + idThesaurus + "' and id_concept1 = '" + idConcept + "' and role LIKE 'NT%'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdsOfConcept.add(resultSet.getString("id_concept2"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List of Id of Concept : " + idConcept, sqle);
        }
        return listIdsOfConcept;
    }

    /**
     * Cette fonction permet de récupérer les IdArk des concepts suivant l'idArk
     * du Concept-Père et le thésaurus
     *
     * @param ds
     * @param idArk
     * @return
     */
    public ArrayList<String> getListChildrenOfConceptByArk(HikariDataSource ds, String idArk) {
        ArrayList<String> listIdsArks = new ArrayList<>();

        String idTheso = getIdThesaurusFromArkId(ds, idArk);
        String idConcept = getIdConceptFromArkId(ds, idArk, idTheso);

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select concept.id_ark  from hierarchical_relationship, concept"
                        + " where"
                        + " concept.id_concept = hierarchical_relationship.id_concept2"
                        + " and"
                        + " concept.id_thesaurus = hierarchical_relationship.id_thesaurus"
                        + " and"
                        + " hierarchical_relationship.id_thesaurus = '" + idTheso + "'"
                        + " and"
                        + " hierarchical_relationship.id_concept1 = '" + idConcept + "'"
                        + " and"
                        + " hierarchical_relationship.role LIKE 'NT%'"
                        + " and"
                        + " concept.status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        listIdsArks.add(resultSet.getString("id_ark"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting List of Id of Concept : " + idConcept, sqle);
        }
        return listIdsArks;
    }

    private ArrayList<NodeHieraRelation> getRelations(ArrayList<NodeHieraRelation> nodeHieraRelations,
            ArrayList<String> relations) {

        ArrayList<NodeHieraRelation> nodeHieraRelations1 = new ArrayList<>();
        for (NodeHieraRelation nodeHieraRelation : nodeHieraRelations) {
            if (relations.contains(nodeHieraRelation.getRole())) {
                nodeHieraRelations1.add(nodeHieraRelation);
            };
        }
        return nodeHieraRelations1;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue On récupère aussi les
     * IdArk si Ark est actif
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param isArkActive
     * @param isCandidatExport
     * @return
     */
    public NodeConceptExport getConceptForExport(HikariDataSource ds,
            String idConcept, String idThesaurus, boolean isArkActive, boolean isCandidatExport) {

        NodeConceptExport nodeConceptExport = new NodeConceptExport();
        TermHelper termHelper = new TermHelper();
        NoteHelper noteHelper = new NoteHelper();
        ImagesHelper imagesHelper = new ImagesHelper();

        String htmlTagsRegEx = "<[^>]*>";

        // les relations BT, NT, RT
        ArrayList<NodeHieraRelation> nodeListRelations = new RelationsHelper().getAllRelationsOfConcept(ds, idConcept, idThesaurus);

        nodeConceptExport.setNodeListOfBT(getRelations(nodeListRelations, nodeConceptExport.getRelationsBT()));
        nodeConceptExport.setNodeListOfNT(getRelations(nodeListRelations, nodeConceptExport.getRelationsNT()));
        nodeConceptExport.setNodeListIdsOfRT(getRelations(nodeListRelations, nodeConceptExport.getRelationsRT()));

        //récupération du Concept        
        Concept concept = getThisConcept(ds, idConcept, idThesaurus);
        if (concept == null) {
            return null;
        }
        nodeConceptExport.setConcept(concept);

        //récupération les aligenemnts 
        nodeConceptExport.setNodeAlignmentsList(new AlignmentHelper().getAllAlignmentOfConceptNew(ds, idConcept, idThesaurus));

        //récupération des traductions        
        nodeConceptExport.setNodeTermTraductions(termHelper.getAllTraductionsOfConcept(ds, idConcept, idThesaurus));

        //récupération des Non Prefered Term        
        nodeConceptExport.setNodeEM(termHelper.getAllNonPreferredTerms(ds, idConcept, idThesaurus));

        //récupération des Groupes ou domaines 
        nodeConceptExport.setNodeListIdsOfConceptGroup(new GroupHelper().getListGroupOfConceptArk(ds, idThesaurus, idConcept));

        //récupération des notes du Terme
        //    String idTerm = termHelper.getIdTermOfConcept(ds, idConcept, idThesaurus);

        /*ArrayList<NodeNote> noteTerm = noteHelper.getListNotesAllLang(ds, idTerm, idThesaurus);
        if (isCandidatExport) {
            for (NodeNote note : noteTerm) {
                String str = formatLinkTag(note.getLexicalvalue());
                note.setLexicalvalue(str.replaceAll(htmlTagsRegEx, ""));
            }
        }
        nodeConceptExport.setNodeNoteTerm(noteTerm);
         */
        ArrayList<NodeNote> nodeNotes = noteHelper.getListNotesAllLang(ds, idConcept, idThesaurus);
        if (isCandidatExport) {
            for (NodeNote note : nodeNotes) {
                String str = formatLinkTag(note.getLexicalvalue());
                note.setLexicalvalue(str.replaceAll(htmlTagsRegEx, ""));
            }
        }
        nodeConceptExport.setNodeNotes(nodeNotes);

        //récupération des coordonnées GPS
        List<NodeGps> nodeGps = new GpsHelper().getCoordinate(ds, idConcept, idThesaurus);
        if (CollectionUtils.isNotEmpty(nodeGps)) {
            nodeConceptExport.setNodeGps(nodeGps);
        }

        ArrayList<NodeImage> nodeImages = imagesHelper.getExternalImages(ds, idConcept, idThesaurus);
        if (nodeImages != null) {
            nodeConceptExport.setNodeImages(nodeImages);
        }

        if (isCandidatExport) {
            nodeConceptExport.setMessages(new MessageDao().getAllMessagesByCandidat(ds, idConcept, idThesaurus));
            nodeConceptExport.setVotes(new CandidatDao().getAllVotesByCandidat(ds, idConcept, idThesaurus));
        }

        // pour les facettes
        List<String> idFacettes = new FacetHelper().getAllIdFacetsOfConcept(ds, idConcept, idThesaurus);
        if (!idFacettes.isEmpty()) {
            nodeConceptExport.setListFacetsOfConcept(idFacettes);
        }

        /// pour les concepts dépréciés 
        DeprecateHelper deprecateHelper = new DeprecateHelper();
        nodeConceptExport.setReplacedBy(deprecateHelper.getAllReplacedByWithArk(ds, idThesaurus, idConcept));
        nodeConceptExport.setReplaces(deprecateHelper.getAllReplacesWithArk(ds, idThesaurus, idConcept));

        return nodeConceptExport;
    }

    public static String formatLinkTag(String initialStr) {
        Pattern MY_PATTERN = Pattern.compile("<a(.*?)a>");
        Matcher m = MY_PATTERN.matcher(initialStr);
        while (m.find()) {
            String link = "<a" + m.group(1) + "a>";
            ArrayList<HTMLLinkElement> result = new HtmlLinkExtraction().extractHTMLLinks(link);
            if (CollectionUtils.isNotEmpty(result)) {
                initialStr = initialStr.replace(link, result.get(0).getLinkElement()
                        + " (" + result.get(0).getLinkAddress() + ")");
            }
        }
        return initialStr;
    }

    /**
     * Cette fonction permet de récupérer les Id Ark d'une liste d'Identifiants
     * de Groups et les rajouter dans le tableau de NodeUri
     */
    private ArrayList<NodeUri> getListIdArkOfGroup(HikariDataSource ds, ArrayList<String> nodeListIdOfGroup,
            String idThesaurus) {

        ArrayList<NodeUri> nodeListIdOfGroup_idArk = new ArrayList<>();

        String idArk;
        for (String nodeListIdOfGroup1 : nodeListIdOfGroup) {
            idArk = new GroupHelper().getIdArkOfGroup(ds, nodeListIdOfGroup1, idThesaurus);
            NodeUri nodeUri = new NodeUri();
            if (idArk == null || idArk.trim().isEmpty()) {
                nodeUri.setIdArk("");
            } else {
                nodeUri.setIdArk(idArk);
            }
            nodeUri.setIdConcept(nodeListIdOfGroup1);
            nodeListIdOfGroup_idArk.add(nodeUri);
        }
        return nodeListIdOfGroup_idArk;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * ou plusieurs Concept par une chaîne de caractère, le thésaurus et la
     * langue
     */
    public ArrayList<NodeConceptExport> getMultiConceptForExport(HikariDataSource ds,
            String value, String idThesaurus, String idLang, boolean isArkActif) {

        ArrayList<NodeConceptExport> listNce = new ArrayList<>();

        //Récupération des concept
        ArrayList<NodeSearch> listRes = new SearchHelper().searchTermNew(ds, value, idLang, idThesaurus, "", 1, false);
        for (NodeSearch ns : listRes) {
            Concept concept = getThisConcept(ds, ns.getIdConcept(), idThesaurus);
            NodeConceptExport nce = new NodeConceptExport();
            nce.setConcept(concept);
            listNce.add(nce);
        }

        for (NodeConceptExport nce : listNce) {
            String idConcept = nce.getConcept().getIdConcept();
            RelationsHelper relationsHelper = new RelationsHelper();

            // récupération des BT
            ArrayList<NodeHieraRelation> nodeListIdOfBT_Ark
                    = relationsHelper.getListBT(ds, idConcept, idThesaurus);
            nce.setNodeListOfBT(nodeListIdOfBT_Ark);

            //récupération des termes spécifiques
            ArrayList<NodeHieraRelation> nodeListIdOfNT_Ark
                    = relationsHelper.getListNT(ds, idConcept, idThesaurus);
            nce.setNodeListOfNT(nodeListIdOfNT_Ark);

            //récupération des termes associés
            ArrayList<NodeHieraRelation> nodeListIdOfRT_Ark
                    = relationsHelper.getListRT(ds, idConcept, idThesaurus);
            nce.setNodeListIdsOfRT(nodeListIdOfRT_Ark);

            //récupération des Non Prefered Term
            nce.setNodeEM(new TermHelper().getAllNonPreferredTerms(ds, idConcept, idThesaurus));

            //récupération des traductions
            nce.setNodeTermTraductions(new TermHelper().getAllTraductionsOfConcept(ds, idConcept, idThesaurus));

            //récupération des Groupes
            ArrayList<NodeUri> nodeListIdsOfConceptGroup_Ark = getListIdArkOfGroup(ds,
                    new GroupHelper().getListIdGroupOfConcept(ds, idThesaurus, idConcept),
                    idThesaurus);
            nce.setNodeListIdsOfConceptGroup(nodeListIdsOfConceptGroup_Ark);

            //récupération des notes
            nce.setNodeNotes(new NoteHelper().getListNotesAllLang(ds, idConcept, idThesaurus));

            // récupération des Alignements
            nce.setNodeAlignmentsList(new AlignmentHelper().getAllAlignmentOfConceptNew(ds, idConcept, idThesaurus));
        }

        return listNce;
    }

    /**
     * Cette fonction permet de récupérer toutes les informations concernant un
     * ou plusieurs Concept par une chaîne de caractère, suivant le thésaurus,
     * la langue et le group
     */
    public ArrayList<NodeConceptExport> getMultiConceptForExport(HikariDataSource ds, String value,
            String idLang, String idGroup, String idThesaurus, boolean isArkActif) {

        ArrayList<NodeConceptExport> listNce = new ArrayList<>();

        //Récupération des concept
        ArrayList<NodeSearch> listRes = new SearchHelper().searchTermNew(ds, value, idLang, idThesaurus, idGroup, 1, false);
        for (NodeSearch ns : listRes) {
            Concept concept = getThisConcept(ds, ns.getIdConcept(), idThesaurus);
            NodeConceptExport nce = new NodeConceptExport();
            nce.setConcept(concept);
            listNce.add(nce);
        }

        for (NodeConceptExport nce : listNce) {
            String idConcept = nce.getConcept().getIdConcept();
            RelationsHelper relationsHelper = new RelationsHelper();

            // récupération des BT
            ArrayList<NodeHieraRelation> nodeListIdOfBT_Ark
                    = relationsHelper.getListBT(ds, idConcept, idThesaurus);
            nce.setNodeListOfBT(nodeListIdOfBT_Ark);

            //récupération des termes spécifiques
            ArrayList<NodeHieraRelation> nodeListIdOfNT_Ark
                    = relationsHelper.getListNT(ds, idConcept, idThesaurus);
            nce.setNodeListOfNT(nodeListIdOfNT_Ark);

            //récupération des termes associés
            ArrayList<NodeHieraRelation> nodeListIdOfRT_Ark
                    = relationsHelper.getListRT(ds, idConcept, idThesaurus);
            nce.setNodeListIdsOfRT(nodeListIdOfRT_Ark);

            //récupération des Non Prefered Term
            nce.setNodeEM(new TermHelper().getAllNonPreferredTerms(ds, idConcept, idThesaurus));

            //récupération des traductions
            nce.setNodeTermTraductions(new TermHelper().getAllTraductionsOfConcept(ds, idConcept, idThesaurus));

            //récupération des Groupes
            ArrayList<NodeUri> nodeListIdsOfConceptGroup_Ark = getListIdArkOfGroup(ds,
                    new GroupHelper().getListIdGroupOfConcept(ds, idThesaurus, idConcept),
                    idThesaurus);
            nce.setNodeListIdsOfConceptGroup(nodeListIdsOfConceptGroup_Ark);

            //récupération des notes
            nce.setNodeNotes(new NoteHelper().getListNotesAllLang(ds, idConcept, idThesaurus));

            // récupération des Alignements
            nce.setNodeAlignmentsList(new AlignmentHelper().getAllAlignmentOfConceptNew(ds, idConcept, idThesaurus));
        }

        return listNce;
    }
    
    /**
     *
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue ##MR ajout de limit NT
     * qui permet de définir la taille maxi des NT à récupérer, si = -1, pas de
     * limit offset 42 fetch next 21 rows only
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param step
     * @param offset
     * @return
     */
    public NodeFullConcept getConcept2(HikariDataSource ds, String idConcept, String idThesaurus, String idLang, int offset, int step) {
        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
        return daoResourceHelper.getFullConcept(ds, idThesaurus, idConcept, idLang, offset, step);
    }    

    /**
     *
     * Cette fonction permet de récupérer toutes les informations concernant un
     * Concept par son id et son thésaurus et la langue ##MR ajout de limit NT
     * qui permet de définir la taille maxi des NT à récupérer, si = -1, pas de
     * limit offset 42 fetch next 21 rows only
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param idLang
     * @param step
     * @param offset
     * @return
     */
    public NodeConcept getConcept(HikariDataSource ds, String idConcept, String idThesaurus, String idLang, int step, int offset) {
        NodeConcept nodeConcept = new NodeConcept();

        // récupération des BT
        ArrayList<NodeBT> nodeListBT = new RelationsHelper().getListBT(ds, idConcept, idThesaurus, idLang);
        nodeConcept.setNodeBT(nodeListBT);

        //récupération du Concept
        Concept concept = getThisConcept(ds, idConcept, idThesaurus);
        if (concept == null) {
            return null;
        }
        if ("dep".equalsIgnoreCase(concept.getStatus())) {
            concept.setDeprecated(true);
        }
        nodeConcept.setConcept(concept);

        //récupération du Terme
        Term term = new TermHelper().getThisTerm(ds, idConcept, idThesaurus, idLang);
        nodeConcept.setTerm(term);

        //récupération des termes spécifiques
        nodeConcept.setNodeNT(new RelationsHelper().getListNT(ds, idConcept, idThesaurus, idLang, step, offset));

        //récupération des termes associés
        nodeConcept.setNodeRT(new RelationsHelper().getListRT(ds, idConcept, idThesaurus, idLang));

        //récupération des Non Prefered Term
        nodeConcept.setNodeEM(new TermHelper().getNonPreferredTerms(ds, idConcept, idThesaurus, idLang));

        //récupération des traductions
        nodeConcept.setNodeTermTraductions(new TermHelper().getTraductionsOfConcept(ds, idConcept, idThesaurus, idLang));

        //récupération des notes du term
        nodeConcept.setNodeNotes(new NoteHelper().getListNotes(ds, idConcept, idThesaurus, idLang));

        nodeConcept.setNodeConceptGroup(new GroupHelper().getListGroupOfConcept(ds, idThesaurus, idConcept, idLang));

        nodeConcept.setNodeAlignments(new AlignmentHelper().getAllAlignmentOfConcept(ds, idConcept, idThesaurus));

        nodeConcept.setNodeimages(new ImagesHelper().getExternalImages(ds, idConcept, idThesaurus));

        //gestion des ressources externes
        nodeConcept.setNodeExternalResources(new ExternalResourcesHelper().getExternalResources(ds, idConcept, idThesaurus));

        // concepts qui remplacent un concept déprécié
        nodeConcept.setReplacedBy(new DeprecateHelper().getAllReplacedBy(ds, idThesaurus, idConcept, idLang));
        // les concepts dépécés que ce concept remplace
        nodeConcept.setReplaces(new DeprecateHelper().getAllReplaces(ds, idThesaurus, idConcept, idLang));

        /// récupération des Méta-données DC_terms
        nodeConcept.setDcElements(new DcElementHelper().getDcElementOfConcept(ds, idThesaurus, idConcept));

        return nodeConcept;
    }

    /**
     * Cette fonction permet de retourner l'id du Concept d'après un idTerm
     *
     * @param ds
     * @param idTerm
     * @param idThesaurus
     * @return
     */
    public String getIdConceptOfTerm(HikariDataSource ds, String idTerm, String idThesaurus) {

        String idConcept = null;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_concept FROM preferred_term WHERE id_thesaurus = '"
                        + idThesaurus + "' and id_term = '" + idTerm + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept");
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcept of idTerm : " + idConcept, sqle);
        }
        return idConcept;
    }

    public String getConceptIdFromPrefLabel(HikariDataSource ds, String prefLabel,
            String idThesaurus, String lang) {

        String idConcept = null;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                String str = prefLabel.replaceAll("\'", "%");
                stmt.executeQuery("SELECT DISTINCT(preferred_term.id_concept) FROM preferred_term, term "
                        + "WHERE term.id_thesaurus = '" + idThesaurus + "' AND term.id_term = preferred_term.id_term "
                        + "AND term.lexical_value like '%" + str + "%' AND lang = '" + lang + "'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        idConcept = resultSet.getString("id_concept");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting idConcept of idTerm : " + idConcept, sqle);
        }
        return idConcept;
    }

    /**
     * Cette fonction permet de savoir si un concept a des fils ou non suivant
     * l'id du Concept et l'id du thésaurus sous forme de classe Concept (sans
     * les relations)
     *
     * @param ds
     * @param idThesaurus
     * @param idConcept
     * @return
     */
    public boolean haveChildren(HikariDataSource ds, String idThesaurus, String idConcept) {

        boolean children = false;

        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select count(id_concept2)  from hierarchical_relationship, concept where "
                        + " hierarchical_relationship.id_concept2 = concept.id_concept and"
                        + " hierarchical_relationship.id_thesaurus = concept.id_thesaurus"
                        + " and hierarchical_relationship.id_thesaurus='" + idThesaurus + "'"
                        + " and id_concept1='" + idConcept + "' and role LIKE 'NT%' and concept.status != 'CA'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        if (resultSet.getInt(1) != 0) {
                            children = true;
                        }
                    }
                }
                // s'il n'a pas d'enfant, on regarde s'il n'a pas de facette également
                if (!children) {
                    stmt.executeQuery("SELECT count(id_facet) FROM thesaurus_array"
                            + " WHERE "
                            + " id_concept_parent = '" + idConcept + "'"
                            + " and "
                            + " id_thesaurus = '" + idThesaurus + "'");
                    try (ResultSet resultSet = stmt.getResultSet()) {
                        if (resultSet.next()) {
                            if (resultSet.getInt(1) != 0) {
                                children = true;
                            }
                        }
                    }

                }
            }
        } catch (SQLException sqle) {
            log.error("Error while testing if haveChildren of Concept : " + idConcept, sqle);
        }
        return children;
    }

    /**
     * Focntion récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête en incluant les Groupes on
     * peut rencontrer plusieurs têtes en remontant, alors on construit à chaque
     * fois un chemin complet.
     *
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<ArrayList<String>> getInvertPathOfConcept(HikariDataSource ds, String idConcept,
            String idThesaurus, ArrayList<String> firstPath, ArrayList<String> path,
            ArrayList<ArrayList<String>> tabId) {

        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> resultat = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if (resultat.size() > 1) {
            for (String path1 : path) {
                firstPath.add(path1);
            }
        }
        if (resultat.isEmpty()) {

            String group;

            do {
                group = getGroupIdOfConcept(ds, idConcept, idThesaurus);
                if (group == null) {
                    group = new GroupHelper().getIdFather(ds, idConcept, idThesaurus);
                }

                path.add(group);
                idConcept = group;
            } while (new GroupHelper().getIdFather(ds, group, idThesaurus) != null);

            ArrayList<String> pathTemp = new ArrayList<>();
            for (String path2 : firstPath) {
                pathTemp.add(path2);
            }
            for (String path1 : path) {
                if (pathTemp.indexOf(path1) == -1) {
                    pathTemp.add(path1);
                }
            }
            tabId.add(pathTemp);
            path.clear();
        }

        for (String resultat1 : resultat) {
            path.add(resultat1);
            getInvertPathOfConcept(ds, resultat1, idThesaurus, firstPath, path, tabId);
        }

        return tabId;

    }

    public ArrayList<ArrayList<String>> getPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus, ArrayList<String> path, ArrayList<ArrayList<String>> tabId) {

        ArrayList<String> firstPath = new ArrayList<>();
        ArrayList<ArrayList<String>> tabIdInvert = getInvertPathOfConcept(ds, idConcept,
                idThesaurus,
                firstPath,
                path, tabId);

        for (int i = 0; i < tabIdInvert.size(); i++) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (int j = tabIdInvert.get(i).size(); j > 0; j--) {
                pathTemp.add(tabIdInvert.get(i).get(j - 1));
            }
            tabIdInvert.remove(i);
            tabIdInvert.add(i, pathTemp);
        }
        return tabIdInvert;
    }

    /**
     * Focntion récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     */
    private ArrayList<ArrayList<String>> getInvertPathOfConceptWithoutGroup(HikariDataSource ds,
            String idConcept, String idThesaurus, ArrayList<String> firstPath, ArrayList<String> path,
            ArrayList<ArrayList<String>> tabId) {

        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> resultat = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if (resultat.size() > 1) {
            for (String path1 : path) {
                firstPath.add(path1);
            }
        }
        if (resultat.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String path2 : firstPath) {
                pathTemp.add(path2);
            }
            for (String path1 : path) {
                if (pathTemp.indexOf(path1) == -1) {
                    pathTemp.add(path1);
                }
            }
            tabId.add(pathTemp);
            path.clear();
        }

        for (String resultat1 : resultat) {
            path.add(resultat1);
            getInvertPathOfConceptWithoutGroup(ds, resultat1, idThesaurus, firstPath, path, tabId);
        }

        return tabId;

    }

    public ArrayList<ArrayList<String>> getPathOfConceptWithoutGroup(HikariDataSource ds,
            String idConcept, String idThesaurus, ArrayList<String> path, ArrayList<ArrayList<String>> tabId) {

        ArrayList<String> firstPath = new ArrayList<>();

        ArrayList<ArrayList<String>> tabIdInvert = getInvertPathOfConceptWithoutGroup(ds, idConcept,
                idThesaurus, firstPath, path, tabId);
        for (int i = 0; i < tabIdInvert.size(); i++) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (int j = tabIdInvert.get(i).size(); j > 0; j--) {
                pathTemp.add(tabIdInvert.get(i).get(j - 1));
            }
            tabIdInvert.remove(i);
            tabIdInvert.add(i, pathTemp);
        }
        return tabIdInvert;
    }

    public void updateGroupOfConcept(HikariDataSource ds, String idConcept, String idNewDomaine, String idOldDomaine, String idTheso) {
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set id_group='" + idNewDomaine
                        + "', modified = current_date WHERE id_concept ='" + idConcept
                        + "' AND id_thesaurus='" + idTheso + "' AND id_group ='" + idOldDomaine + "'");
            }
        } catch (SQLException sqle) {
            // Log exception
            log.error("Error while updating group of concept : " + idConcept, sqle);
        }
    }

    /**
     * Cette fonction permet de mettre à jour l'Id Ark dans la table concept ou
     * remplacer l'Id existant
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idArk
     * @return
     */
    public boolean updateArkIdOfConcept(HikariDataSource ds, String idConcept, String idTheso, String idArk) {
        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set id_ark='" + idArk + "' WHERE lower(id_concept) = lower('" + idConcept
                        + "') AND id_thesaurus='" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de modifier le status d'un concept
     */
    private boolean updateStatusConcept(HikariDataSource ds, String idConcept, String idTheso, String status) {
        boolean res = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set status='" + status + "' WHERE id_concept ='"
                        + idConcept + "' AND id_thesaurus='" + idTheso + "'");
                res = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating status of Concept : " + idConcept, sqle);
        }
        return res;
    }

    /**
     * Cette fonction permet d'ajouter un Ark Id au concept ou remplacer l'Id
     * existant
     */
    public boolean updateArkIdOfConcept(Connection conn, String idConcept, String idTheso, String idArk) {

        boolean status = false;

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE concept set id_ark='" + idArk + "' WHERE id_concept ='"
                    + idConcept + "' AND id_thesaurus='" + idTheso + "'");
            status = true;

        } catch (SQLException sqle) {
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet d'ajouter un Handle Id au concept ou remplacer l'Id
     * existant
     *
     * @param conn
     * @param idConcept
     * @param idTheso
     * @param idHandle
     * @return
     */
    public boolean updateHandleIdOfConcept(Connection conn, String idConcept, String idTheso, String idHandle) {

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE concept set id_handle='" + idHandle + "' WHERE id_concept ='"
                    + idConcept + "' AND id_thesaurus='" + idTheso + "'");
            return true;
        } catch (SQLException sqle) {
            log.error("Error while updating or adding HandleId of Concept : " + idConcept, sqle);
            return false;
        }
    }

    /**
     * Cette fonction permet d'ajouter un Handle Id au concept ou remplacer l'Id
     * existant
     *
     * @param ds
     * @param idConcept
     * @param idTheso
     * @param idHandle
     * @return
     */
    public boolean updateHandleIdOfConcept(HikariDataSource ds, String idConcept, String idTheso, String idHandle) {

        boolean status = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE concept set id_handle='" + idHandle
                        + "' WHERE id_concept ='" + idConcept + "' AND id_thesaurus='" + idTheso + "'");
                status = true;
            }
        } catch (SQLException sqle) {
            log.error("Error while updating HandleId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    /**
     * Cette fonction permet de mettre à jour la notation pour un concept
     *
     * @param conn
     * @param idConcept
     * @param idTheso
     * @param notation
     * @return
     */
    public boolean updateNotation(Connection conn, String idConcept, String idTheso, String notation) {
        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("UPDATE concept set notation ='" + notation + "' WHERE id_concept ='"
                    + idConcept + "' AND id_thesaurus='" + idTheso + "'");
            status = true;
        } catch (SQLException sqle) {
            log.error("Error while updating or adding ArkId of Concept : " + idConcept, sqle);
        }
        return status;
    }

    public boolean haveThisGroup(HikariDataSource ds, String idConcept, String idDomaine, String idTheso) {
        boolean group = false;
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT idconcept FROM concept_group_concept"
                        + " WHERE idthesaurus='" + idTheso + "'"
                        + " AND idconcept='" + idConcept + "'"
                        + " AND idgroup='" + idDomaine + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    resultSet.next();
                    group = (resultSet.getRow() != 0);
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while testing if haveChildren of Concept : " + idConcept, sqle);
        }
        return group;
    }

    public String getPereConcept(HikariDataSource ds, String id_theso, String id_concept) {
        String conceptPere = "";
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT id_concept2 FROM hierarchical_relationship WHERE id_thesaurus='"
                        + id_theso + "' AND id_concept1='" + id_concept + "'"
                        + " AND role LIKE 'BT%'");
                try (ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        conceptPere = resultSet.getString("id_concept2");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while get le pere du concept : " + id_concept, sqle);
        }
        return conceptPere;
    }

    /**
     * Change l'id d'un concept dans la table concept
     */
    public void setIdConcept(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE concept SET id_concept = '" + newIdConcept + "' WHERE id_concept = '"
                    + idConcept + "' AND id_thesaurus = '" + idTheso + "' ");
        }
    }

    /**
     * Change l'id d'un concept dans la table concept_group_concept
     */
    public void setIdConceptGroupConcept(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE concept_group_concept SET idconcept = '" + newIdConcept
                    + "' WHERE idconcept = '" + idConcept + "' AND idthesaurus = '" + idTheso + "'");
        }
    }

    /**
     * Change l'id d'un concept dans la table concept_historique
     */
    public void setIdConceptHistorique(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE concept_historique SET id_concept = '" + newIdConcept + "'"
                    + " WHERE id_concept = '" + idConcept + "' AND id_thesaurus = '" + idTheso + "'");
        }
    }

    /**
     * Change l'id d'un concept dans la table hierarchical_relationship
     */
    public void setIdConceptHieraRelation(Connection conn, String idTheso, String idConcept, String newIdConcept)
            throws SQLException {

        try (Statement stmt = conn.createStatement()) {
            String query = "UPDATE hierarchical_relationship"
                    + " SET id_concept1 = '" + newIdConcept + "'"
                    + " WHERE id_concept1 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            query += ";";
            query += "UPDATE hierarchical_relationship"
                    + " SET id_concept2 = '" + newIdConcept + "'"
                    + " WHERE id_concept2 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        }
    }

    /**
     * Change l'id d'un concept dans la table
     * hierarchical_relationship_historique
     */
    public void setIdConceptHieraRelationHisto(Connection conn, String idTheso, String idConcept,
            String newIdConcept) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String query = "UPDATE hierarchical_relationship_historique"
                    + " SET id_concept1 = '" + newIdConcept + "'"
                    + " WHERE id_concept1 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            query += ";";
            query += "UPDATE hierarchical_relationship_historique"
                    + " SET id_concept2 = '" + newIdConcept + "'"
                    + " WHERE id_concept2 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        }
    }

    /**
     * Change l'id d'un concept dans la table concept_replacedby
     */
    public void setIdConceptReplacedby(Connection conn, String idTheso, String idConcept, String newIdConcept) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            String query = "UPDATE concept_replacedby"
                    + " SET id_concept1 = '" + newIdConcept + "'"
                    + " WHERE id_concept1 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            query += ";";
            query += "UPDATE concept_replacedby"
                    + " SET id_concept2 = '" + newIdConcept + "'"
                    + " WHERE id_concept2 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
        }
    }

    /**
     * permet de supprimer l'appartenance du concept à des facettes
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @return
     */
    public boolean deleteConceptFromFacets(Connection conn, String idTheso, String idConcept) {
        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            String query = "delete from concept_facet WHERE id_concept = '" + idConcept
                    + "' AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
            status = true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de supprimer un concept dans la table concept_replacedby
     *
     * @param conn
     * @param idTheso
     * @param idConcept
     * @return
     */
    public boolean deleteConceptReplacedby(Connection conn, String idTheso, String idConcept) {
        boolean status = false;
        try (Statement stmt = conn.createStatement()) {
            String query = "delete from concept_replacedby WHERE id_concept1 = '" + idConcept
                    + "' AND id_thesaurus = '" + idTheso + "'";
            query += ";";
            query += "delete from concept_replacedby"
                    + " WHERE id_concept2 = '" + idConcept + "'"
                    + " AND id_thesaurus = '" + idTheso + "'";
            stmt.execute(query);
            status = true;

        } catch (SQLException ex) {
            Logger.getLogger(ConceptHelper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return status;
    }

    /**
     * permet de supprimer un concept dans la table concept_replacedby
     */
    public boolean deleteFacets(HikariDataSource ds, String idTheso, String idConcept) {
        FacetHelper facetHelper = new FacetHelper();
        List<String> listFacets = facetHelper.getAllIdFacetsOfConcept(ds, idConcept, idTheso);
        for (String idFacet : listFacets) {
            if (!facetHelper.deleteFacet(ds, idFacet, idTheso)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Change l'id d'un concept dans la table preferred_term
     */
    public void setIdConceptPreferedTerm(Connection conn, String idTheso, String idConcept,
            String newIdConcept) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("UPDATE preferred_term SET id_concept = '" + newIdConcept + "' WHERE id_concept = '"
                    + idConcept + "' AND id_thesaurus = '" + idTheso + "'");
        }
    }

    /**
     * Méthode pour récupérer une liste des identifiants BT à parti d'un
     * thesaurus et d'un concept
     */
    public ArrayList<String> getIdBtFromAConcept(Connection conn, String idTheso, String idConcept) {
        ArrayList<String> ret = new ArrayList();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("Select id_concept2 FROM hierarchical_relationship Where id_concept1=? and id_thesaurus=? and role=?");
            stmt.setString(1, idConcept);
            stmt.setString(2, idTheso);
            stmt.setString(3, "BT");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ret.add(rs.getString("id_concept2"));
                }
            }
            stmt.close();
        } catch (SQLException e) {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception ex) {
            }
            log.error("error while getting id BT from a concept Id", e);
        }
        return ret;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ConceptStatisticData> searchAllCondidats(HikariDataSource hikariDataSource, String idThesaurus, String lang,
            String dateDebut, String dateFin, String collectionId, String nbrResultat) throws SQLException {

        List<ConceptStatisticData> temps = new ArrayList<>();

        try (Statement stmt = hikariDataSource.getConnection().createStatement()) {
            StringBuffer request = new StringBuffer()
                    .append("SELECT con.id_concept, con.created, con.modified, users.username ")
                    .append("FROM concept con, users ");

            if (!StringUtils.isEmpty(collectionId)) {
                request.append(", concept_group_concept con_group ");
            }

            request.append("WHERE con.status = 'D' ")
                    .append("AND con.id_thesaurus = '").append(idThesaurus).append("' ");

            if (!StringUtils.isEmpty(collectionId)) {
                request.append("AND con.id_concept = con_group.idconcept ")
                        .append("AND con_group.idgroup = '").append(collectionId).append("' ");
            }

            if (!StringUtils.isEmpty(dateDebut) && !StringUtils.isEmpty(dateFin)) {
                request.append("AND con.modified BETWEEN '").append(dateDebut).append("' AND '").append(dateFin).append("' ");
            }

            request.append("ORDER BY con.id_concept ASC ");
            request.append("LIMIT " + nbrResultat);

            stmt.executeQuery(request.toString());

            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

            try (ResultSet resultSet = stmt.getResultSet()) {
                while (resultSet.next()) {
                    ConceptStatisticData concept = new ConceptStatisticData();
                    concept.setIdConcept(resultSet.getString("id_concept"));
                    concept.setDateCreation(formatter.format(resultSet.getDate("created")));
                    concept.setDateModification(formatter.format(resultSet.getDate("modified")));
                    concept.setUtilisateur(resultSet.getString("username"));
                    temps.add(concept);
                }
            }
        }

        return temps;
    }

}
