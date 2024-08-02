package fr.cnrs.opentheso.bean.fusion;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.*;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.core.imports.rdf4j.helper.AddConceptsStruct;
import fr.cnrs.opentheso.core.imports.rdf4j.helper.ImportRdf4jHelper;
import fr.cnrs.opentheso.core.imports.rdf4j.nouvelle.ReadRDF4JNewGen;
import fr.cnrs.opentheso.skosapi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;

import org.eclipse.rdf4j.rio.RDFFormat;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Named
@ViewScoped
public class FusionService implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CurrentUser currentUser;

    private SKOSXmlDocument sourceSkos;
    private boolean loadDone, fusionDone, fusionBtnEnable;
    private String uri;
    private double total;
    private List<String> conceptsAjoutes, conceptsModifies, conceptsExists;

    public void lancerFussion(NodeIdValue thesoSelected) {

        fusionDone = false;
        fusionBtnEnable = true;
        if (conceptsModifies == null) {
            conceptsModifies = new ArrayList<>();
        } else {
            conceptsModifies.clear();
        }
        if (conceptsAjoutes == null) {
            conceptsAjoutes = new ArrayList<>();
        } else {
            conceptsAjoutes.clear();
        }
        if (conceptsExists == null) {
            conceptsExists = new ArrayList<>();
        } else {
            conceptsExists.clear();
        }

        ArrayList<NodeNote> nodeNotesLocal;
        ArrayList<NodeEM> nodeEMsLocal;

        ConceptHelper conceptHelper = new ConceptHelper();
        ImportRdf4jHelper importRdf4jHelper = new ImportRdf4jHelper();
        importRdf4jHelper.setDs(connect.getPoolConnexion());
        TermHelper termHelper = new TermHelper();
        AlignmentHelper alignmentHelper = new AlignmentHelper();
        NoteHelper noteHelper = new NoteHelper();
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        String workLang = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), thesoSelected.getId());

        for (SKOSResource conceptSource : sourceSkos.getConceptList()) {
            if (!StringUtils.isEmpty(conceptSource.getIdentifier())) {

                /// prépration d'un objet au format Concept  
                importRdf4jHelper.setRdf4jThesaurus(sourceSkos);
                AddConceptsStruct acs = new AddConceptsStruct();
                importRdf4jHelper.initAddConceptsStruct(acs, conceptSource, thesoSelected.getId(), false);
                importRdf4jHelper.addRelation(acs, thesoSelected.getId());

                // récupération du concept Local
                NodeConcept conceptFound = conceptHelper.getConcept(connect.getPoolConnexion(),
                        conceptSource.getIdentifier(),
                        thesoSelected.getId(),
                        workLang, -1, -1);

                if (conceptFound == null && !conceptSource.getLabelsList().isEmpty()) {
                    importRdf4jHelper.addConceptToBdd(acs, thesoSelected.getId(), false);
                    conceptsAjoutes.add(acs.concept.getIdConcept() + " - " + conceptSource.getLabelsList().get(0).getLabel());
                } else {
                    if (conceptFound == null) {
                        return;
                    }
                    boolean isUpdated = false;

                    // Traduction OK validée #MR
                    //alignment
                    if (!CollectionUtils.isEmpty(acs.nodeAlignments)) {
                        for (NodeAlignment nodeAlignment : acs.nodeAlignments) {
                            if (!isAlignementExist(nodeAlignment, conceptFound.getNodeAlignments())) {
                                alignmentHelper.addNewAlignment(connect.getPoolConnexion(),
                                        nodeAlignment.getId_author(),
                                        nodeAlignment.getConcept_target(),
                                        nodeAlignment.getThesaurus_target(),
                                        nodeAlignment.getUri_target(),// URI
                                        nodeAlignment.getAlignement_id_type(),
                                        conceptSource.getIdentifier(),
                                        conceptFound.getConcept().getIdThesaurus(),
                                        nodeAlignment.getId_alignement());
                                isUpdated = true;
                            }
                        }
                    }

                    // Traduction OK validée #MR
                    // Synonymes : NonPreferredTerms
                    if (!CollectionUtils.isEmpty(acs.nodeEMList)) {
                        nodeEMsLocal = termHelper.getAllNonPreferredTerms(connect.getPoolConnexion(), conceptSource.getIdentifier(), conceptFound.getConcept().getIdThesaurus());
                        for (NodeEM nodeEM : acs.nodeEMList) {
                            if (!isSynonymeExist(nodeEM, nodeEMsLocal)) {
                                Term term = new Term();
                                term.setId_term(conceptFound.getTerm().getId_term());
                                term.setId_concept(conceptFound.getConcept().getIdConcept());
                                term.setLexical_value(nodeEM.getLexical_value());
                                term.setLang(nodeEM.getLang());
                                term.setId_thesaurus(conceptFound.getConcept().getIdThesaurus());
                                term.setSource(nodeEM.getSource());
                                term.setStatus(nodeEM.getStatus());
                                term.setHidden(nodeEM.isHiden());
                                termHelper.addNonPreferredTerm(connect.getPoolConnexion(), term,
                                        currentUser.getNodeUser().getIdUser());
                                isUpdated = true;
                            }
                        }
                    }

                    // Traduction OK validée #MR
                    if (!CollectionUtils.isEmpty(acs.nodeTermTraductionList)) {
                        for (NodeTermTraduction nodeTermTraduction : acs.nodeTermTraductionList) {
                            if (!isTraductionExist(nodeTermTraduction, conceptFound)) {
                                Term term = new Term();
                                term.setId_term(conceptFound.getTerm().getId_term());
                                term.setId_concept(conceptFound.getConcept().getIdConcept());
                                term.setLexical_value(nodeTermTraduction.getLexicalValue());
                                term.setLang(nodeTermTraduction.getLang());
                                term.setId_thesaurus(conceptFound.getConcept().getIdThesaurus());
                                if (termHelper.isTermExistInThisLang(connect.getPoolConnexion(),
                                                term.getId_term(), nodeTermTraduction.getLang(),
                                                term.getId_thesaurus())) {
                                    termHelper.updateTermTraduction(connect.getPoolConnexion(),
                                            term,
                                            currentUser.getNodeUser().getIdUser());
                                } else {
                                    termHelper.addTraduction(connect.getPoolConnexion(),
                                            nodeTermTraduction.getLexicalValue(),
                                            acs.nodeTerm.getIdTerm(),
                                            nodeTermTraduction.getLang(),
                                            "fusion",
                                            "",
                                            conceptFound.getConcept().getIdThesaurus(),
                                            currentUser.getNodeUser().getIdUser());
                                }
                                isUpdated = true;
                            }
                        }
                    }

                    // OK validé par #MR
                    //Definition
                    if (!CollectionUtils.isEmpty(acs.nodeNotes)) {
                        nodeNotesLocal = noteHelper.getListNotesAllLang(connect.getPoolConnexion(), acs.concept.getIdConcept(), conceptFound.getConcept().getIdThesaurus());
                        for (NodeNote nodeNote : acs.nodeNotes) {
                            /// détecter le type de note avant 
                            if (nodeNote.getNotetypecode().equalsIgnoreCase("definition")) {
                                if (!noteHelper.isNoteExist(connect.getPoolConnexion(),
                                        acs.concept.getIdConcept(), 
                                        conceptFound.getConcept().getIdThesaurus(),
                                        nodeNote.getLang(), nodeNote.getLexicalvalue(), "definition")) {
                                    noteHelper.addNote(connect.getPoolConnexion(),
                                            acs.concept.getIdConcept(),
                                            nodeNote.getLang(),
                                            conceptFound.getConcept().getIdThesaurus(),
                                            nodeNote.getLexicalvalue(),
                                            nodeNote.getNotetypecode(),
                                            "",
                                            nodeNote.getIdUser());
                                    isUpdated = true;
                                }
                            }
                        }
                    }

                    if (isUpdated) {
                        conceptsModifies.add(acs.concept.getIdConcept() + " - " + conceptSource.getLabelsList().get(0).getLabel());
                    } else {
                        conceptsExists.add(acs.concept.getIdConcept());
                    }
                }

                fusionDone = true;
                PrimeFaces.current().executeScript("PF('bui').hide();");
                PrimeFaces.current().ajax().update("statistiques");
            }
        }

        importRdf4jHelper.addGroups(sourceSkos.getGroupList(), thesoSelected.getId());
        importRdf4jHelper.addLangsToThesaurus(connect.getPoolConnexion(), thesoSelected.getId());

    }

    
    /**
     * pour comparer la traduction locale à la traduction importée
     * @param nodeTermTraductionImport
     * @return 
     * #MR
     */
    private boolean isTraductionExist(NodeTermTraduction nodeTermTraductionImport, NodeConcept nodeConceptLocal) {
        if (nodeConceptLocal == null)
            return false;
        
        // comparaison avec le prefLabel 
        if(nodeConceptLocal.getTerm() != null) {
            if (nodeTermTraductionImport.getLexicalValue().equalsIgnoreCase(nodeConceptLocal.getTerm().getLexical_value())
                && (nodeTermTraductionImport.getLang().equalsIgnoreCase(nodeConceptLocal.getTerm().getLang()))) {
                return true;
            }
        }
        
        // comparaison aux traductions
        for(NodeTermTraduction existingTranslation  : nodeConceptLocal.getNodeTermTraductions()){
            if (nodeTermTraductionImport.getLexicalValue().equalsIgnoreCase(existingTranslation.getLexicalValue())
                && (nodeTermTraductionImport.getLang().equalsIgnoreCase(existingTranslation.getLang()))) {
                return true;
            }
        }
        return false;
    }
    /**
     * pour comparer savoir si la langue existe ou non pour appliquer un ipdate ou instert
     * @param langToFind
     * @param nodeTermTraductionLocal
     * @return 
     * #MR
     */    
    private boolean isExistLang(String langToFind, ArrayList<NodeTermTraduction> nodeTermTraductionLocal) {
        if (nodeTermTraductionLocal == null || nodeTermTraductionLocal.isEmpty())
            return false;
        
        for(NodeTermTraduction existingTranslation  : nodeTermTraductionLocal){
            if (langToFind.equalsIgnoreCase(existingTranslation.getLang())) {
                return true;
            }
        }
        return false;
    }    
    
    private boolean isDefinitionExist(NodeNote nodeNoteImport, ArrayList<NodeNote> nodeNoteTermLocal) {
        if (nodeNoteTermLocal == null || nodeNoteTermLocal.isEmpty())
            return false;
        
        for(NodeNote nodeNoteTermLocal1  : nodeNoteTermLocal){
            if(nodeNoteTermLocal1.getNotetypecode().equalsIgnoreCase("definition")) {
                if (nodeNoteImport.getLexicalvalue().equalsIgnoreCase(nodeNoteTermLocal1.getLexicalvalue())
                    && (nodeNoteImport.getLang().equalsIgnoreCase(nodeNoteTermLocal1.getLang())) ) {
                    return true;    
                }
            }
        }
        return false;
    }    

    /**
     * permet de vérifier si l'alignement existe ou non  
     * @param nodeAlignementImport
     * @param nodeAlignmentsLocal
     * @return 
     */
    private boolean isAlignementExist(NodeAlignment nodeAlignementImport, ArrayList<NodeAlignment> nodeAlignmentsLocal) {
        if (nodeAlignmentsLocal == null || nodeAlignmentsLocal.isEmpty())
            return false;        

        for(NodeAlignment nodeAlignmentsLocal1  : nodeAlignmentsLocal){
            if (nodeAlignementImport.getAlignement_id_type() == nodeAlignmentsLocal1.getAlignement_id_type()
                && (nodeAlignementImport.getUri_target().equalsIgnoreCase(nodeAlignmentsLocal1.getUri_target())) ) {
                return true;
            }
        }         
        return false;
    }

    private boolean isDefinitionExist(NodeNote nodeNote, NodeConcept concept) {
        if (CollectionUtils.isEmpty(concept.getNodeEM()))
            return false;

        for(NodeNote node : concept.getNodeNotesTerm()){
            if ("definition".equalsIgnoreCase(node.getNotetypecode())
                    && node.getLexicalvalue().equals(nodeNote.getLexicalvalue())) {
                return true;
            }
        }
        return false;
    }

    private boolean isSynonymeExist(NodeEM nodeEMimport, ArrayList<NodeEM> nodeEMLocal) {
        if (nodeEMLocal == null || nodeEMLocal.isEmpty())
            return false;

        for(NodeEM existingNodeEM  : nodeEMLocal){
            if (nodeEMimport.getLexical_value().equalsIgnoreCase(existingNodeEM.getLexical_value())
                && (nodeEMimport.getLang().equalsIgnoreCase(existingNodeEM.getLang()))) {
                return true;
            }
        }
        return false;
    }

    public void importTheso(FileUploadEvent event) {
        try (InputStream is = event.getFile().getInputStream()) {
            sourceSkos = new ReadRDF4JNewGen().readRdfFlux(is, RDFFormat.RDFXML, connect.getWorkLanguage());
            total = sourceSkos.getConceptList().size();
            uri = sourceSkos.getTitle();
            loadDone = true;
        } catch (Exception e) {}
        
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
        PrimeFaces.current().ajax().update("statistiques");
    }

    public boolean isLoadDone() {
        return loadDone;
    }

    public void setLoadDone(boolean loadDone) {
        this.loadDone = loadDone;
    }

    public String getUri() {
        return uri;
    }

    public double getTotal() {
        return total;
    }

    public List<String> getConceptsAjoutes() {
        return conceptsAjoutes;
    }

    public List<String> getConceptsModifies() {
        return conceptsModifies;
    }

    public boolean isFusionDone() {
        return fusionDone;
    }

    public void setFusionDone(boolean fusionDone) {
        this.fusionDone = fusionDone;
    }

    public List<String> getConceptsExists() {
        return conceptsExists;
    }

    public boolean isFusionBtnEnable() {
        return fusionBtnEnable;
    }

    public void initFusionResult() {
        fusionBtnEnable = false;
        fusionDone = false;
    }

    public void setConceptsAjoutes(List<String> conceptsAjoutes) {
        this.conceptsAjoutes = conceptsAjoutes;
    }

    public void setConceptsModifies(List<String> conceptsModifies) {
        this.conceptsModifies = conceptsModifies;
    }

    public void setConceptsExists(List<String> conceptsExists) {
        this.conceptsExists = conceptsExists;
    }

    public void setFusionBtnEnable(boolean fusionBtnEnable) {
        this.fusionBtnEnable = fusionBtnEnable;
    }
}
