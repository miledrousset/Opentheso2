package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeFacet;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.facet.EditFacet;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import org.apache.commons.text.StringEscapeUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "noteBean")
@SessionScoped

public class NoteBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private PropositionBean propositionBean;
    @Inject
    private ConceptView conceptBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;    

    @Inject private EditFacet editFacet;
    @Inject private GroupView groupView;
    
    private String selectedLang;
    
    private ArrayList<NoteHelper.NoteType> noteTypes;
    private ArrayList<NodeLangTheso> nodeLangs;
    private String selectedTypeNote;
    private String noteValue;
    private NodeNote selectedNodeNote;
    
    boolean isFacetNote;
    boolean isConceptNote;
    boolean isGroupNote;
    
    private NodeFacet nodeFacet;
    private NodeGroup nodeGroup;
    

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (noteTypes != null) {
            noteTypes.clear();
            noteTypes = null;
        }
        if (nodeLangs != null) {
            nodeLangs.clear();
            nodeLangs = null;
        }
        selectedLang = null;
        selectedTypeNote = null;
        noteValue = null;
        selectedNodeNote = null;
        nodeGroup = null;
    }
    
    /**
     * permet d'initialiser l'édition des notes pour les facettes
     * @param nodeFacet 
     */
    public void resetForFacet(NodeFacet nodeFacet){
        resetGroup();
        setIsFacetNote();
        this.nodeFacet = nodeFacet;
    }
    
    /**
     * permet d'initialiser l'édition des notes pour les facettes
     * @param nodeGroup 
     */
    public void resetForGroup(NodeGroup nodeGroup){
        resetGroup();
        setIsGroupNote();
        this.nodeGroup = nodeGroup;
    }    
    private void resetGroup() {
        noteTypes = new NoteHelper().getNotesType(connect.getPoolConnexion());
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = selectedTheso.getSelectedLang();
        noteValue = "";
        selectedTypeNote = null;
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = false;    
    }    
    
    
    public void reset() {
        ArrayList<NoteHelper.NoteType> noteTypes1 = findNoteTypes();
        filterNotesByUsage(noteTypes1);
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = selectedTheso.getSelectedLang();
        noteValue = "";
        selectedTypeNote = null;
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = false;    
    }
    
    private ArrayList<NoteHelper.NoteType> findNoteTypes(){
        ArrayList<NoteHelper.NoteType> noteTypes1 = new NoteHelper().getNotesType(connect.getPoolConnexion());
        return noteTypes1;
    }
    
    private void filterNotesByUsage(ArrayList<NoteHelper.NoteType> noteTypes1){
        noteTypes = new ArrayList<>();
        for (NoteHelper.NoteType noteType : noteTypes1) {
            switch (noteType.getCodeNote()) {
                case "note":
                    if(conceptBean.getNote() == null || conceptBean.getNote().getLexicalvalue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;
                case "definition":
                    if(conceptBean.getDefinition() == null || conceptBean.getDefinition().getLexicalvalue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;     
                case "scopeNote":
                    if(conceptBean.getScopeNote()== null || conceptBean.getScopeNote().getLexicalvalue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;  
                case "example":          
                    if(conceptBean.getExample() == null || conceptBean.getExample().getLexicalvalue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;                     
                case "historyNote":
                    if(conceptBean.getHistoryNote()== null || conceptBean.getHistoryNote().getLexicalvalue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break; 
                case "editorialNote":
                    if(conceptBean.getEditorialNote()== null || conceptBean.getEditorialNote().getLexicalvalue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;                      
                    
                case "changeNote":
                    if(conceptBean.getChangeNote() == null || conceptBean.getChangeNote().getLexicalvalue().isEmpty()) {
                        noteTypes.add(noteType);
                    }                     
                    break;                      
                default:
                    break;
            }
        }
    }
    
    private void setIsFacetNote(){
        isFacetNote = true;
        isConceptNote = false;
        isGroupNote = false;           
    }
    private void setIsConceptNote(){
        isFacetNote = false;
        isConceptNote = true;
        isGroupNote = false;           
    }    
    private void setIsGroupNote(){
        isFacetNote = false;
        isConceptNote = false;
        isGroupNote = true;           
    }    
    
    public void initNoteProp(String noteType) {
        reset();
        setSelectedTypeNote(noteType);
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter une note 
     *
     * @param idUser
     */
    public void addNewNote(int idUser) {

        if (noteValue == null || noteValue.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }


        noteValue = new StringPlus().clearValue(noteValue);
        noteValue = StringEscapeUtils.unescapeXml(noteValue);

        if(isFacetNote){
            addFacetNote(idUser);
            return;
        }
        if(isGroupNote){
            addGroupNote(idUser);
            return;
        }        
            
            
        ///// notes pour les concepts    
        if (!addNote(conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser)) {
            printErreur();
            return;
        }
        new ConceptHelper().updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        noteValue = "";
        ArrayList<NoteHelper.NoteType> noteTypes1 = findNoteTypes();
        filterNotesByUsage(noteTypes1);
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    
    
    //// notes pour les facettes 
    private void addFacetNote(int idUser){
        if (!addNote(nodeFacet.getIdFacet(), idUser)) {
            printErreur();
            return;
        }
        new FacetHelper().updateDateOfFacet(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeFacet.getIdFacet(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }
    
    /// notes pour les collections
    private void addGroupNote(int idUser){
        if (!addNote(nodeGroup.getConceptGroup().getIdgroup(), idUser)) {
            printErreur();
            return;
        }
        new GroupHelper().updateModifiedDate(connect.getPoolConnexion(),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());
        noteValue = "";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);           
    }
    
    
    
    
    private boolean addNote(String identifier, int idUser) {
        if (new NoteHelper().isNoteExist(
                connect.getPoolConnexion(),
                identifier,
                selectedTheso.getCurrentIdTheso(),
                selectedLang,
                noteValue,
                selectedTypeNote)) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }

        return new NoteHelper().addNote(
                connect.getPoolConnexion(),
                identifier,
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                noteValue,
                selectedTypeNote,
                "",
                idUser);
    }    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public void addNewNoteProp() {

        if (noteValue == null || noteValue.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        noteValue = new StringPlus().clearValue(noteValue);
        noteValue = StringEscapeUtils.unescapeXml(noteValue);

        if (new NoteHelper().isNoteExist(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                selectedLang,
                noteValue,
                selectedTypeNote)) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        switch (selectedTypeNote) {
            case "note":
                if (!checkExisting(propositionBean.getProposition().getNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "definition":
                if (!checkExisting(propositionBean.getProposition().getDefinition())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "scopeNote":
                if (!checkExisting(propositionBean.getProposition().getScopeNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "example":
                if (!checkExisting(propositionBean.getProposition().getExample())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "historyNote":
                if (!checkExisting(propositionBean.getProposition().getHistoryNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "editorialNote":
                if (!checkExisting(propositionBean.getProposition().getEditorialNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "changeNote":
                if (!checkExisting(propositionBean.getProposition().getChangeNote())) {
                    showErrorMessageNote();
                    return;
                }
                break;
        }

        NotePropBean notePropBean = new NotePropBean();
        notePropBean.setToAdd(true);
        notePropBean.setLang(selectedLang);
        notePropBean.setLexicalvalue(noteValue);
        notePropBean.setId_concept(conceptBean.getNodeConcept().getConcept().getIdConcept());
        notePropBean.setNotetypecode(selectedTypeNote);

        switch (selectedTypeNote) {
            case "note":
                propositionBean.getProposition().setNote(notePropBean);
                break;
            case "definition":
                propositionBean.getProposition().setDefinition(notePropBean);
                break;
            case "scopeNote":
                propositionBean.getProposition().setScopeNote(notePropBean);
                break;
            case "example":
                propositionBean.getProposition().setExample(notePropBean);
                break;
            case "historyNote":
                propositionBean.getProposition().setHistoryNote(notePropBean);
                break;
            case "editorialNote":
                propositionBean.getProposition().setEditorialNote(notePropBean);
                break;
            case "changeNote":
                propositionBean.getProposition().setChangeNote(notePropBean);
                break;
        }
        
        propositionBean.checkNotePropositionStatus();
    }

    private void showErrorMessageNote() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note "
                + noteValue + " ( " + selectedLang + ") note existe déjà !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private boolean checkExisting(NotePropBean note) {
        if(note == null) return true;
        if (selectedLang.equalsIgnoreCase(note.getLang())
                && noteValue.equalsIgnoreCase(note.getLexicalvalue())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
    }

    
    
    
    
    public void updateNote(NodeNote nodeNote, int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        FacesMessage msg;
        
        if(nodeNote.getLexicalvalue().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne peut pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            conceptBean.getConcept(
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());            
            return;            
        }
        nodeNote.setLexicalvalue(new StringPlus().clearValue(nodeNote.getLexicalvalue()));
        nodeNote.setLexicalvalue(StringEscapeUtils.unescapeXml(nodeNote.getLexicalvalue()));        
        
        if(isFacetNote){
            updateFacetNote(nodeNote, idUser);
            return;
        }
        if(isGroupNote){
            updateGroupNote(nodeNote, idUser);
            return;
        }            
        
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalvalue(),
                nodeNote.getNotetypecode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    // mise à jour des notes pour les facettes
    private void updateFacetNote(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        NoteHelper noteHelper = new NoteHelper();
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeFacet.getIdFacet(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalvalue(),
                nodeNote.getNotetypecode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        new FacetHelper().updateDateOfFacet(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeFacet.getIdFacet(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);    
        
    }
    
    // mise à jour des notes pour les facettes
    private void updateGroupNote(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        NoteHelper noteHelper = new NoteHelper();
        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeGroup.getConceptGroup().getIdgroup(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getLexicalvalue(),
                nodeNote.getNotetypecode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        new GroupHelper().updateModifiedDate(connect.getPoolConnexion(),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentIdTheso());
        ///////////////  
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);           
    }    
    
    public void updateNoteProp(NotePropBean notePropBean) {

        switch (selectedTypeNote) {
            case "note":
                    if (propositionBean.getProposition().getNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getNote().setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getNote().getLexicalvalue())) {
                            propositionBean.getProposition().getNote().setToRemove(false);
                            propositionBean.getProposition().getNote().setToUpdate(false);
                            propositionBean.getProposition().getNote().setToAdd(false);
                            propositionBean.getProposition().getNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getNote().isToRemove()) {
                            propositionBean.getProposition().getNote().setToUpdate(true);
                            propositionBean.getProposition().getNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getNote().isToRemove()) {
                            propositionBean.getProposition().getNote().setToRemove(false);
                            propositionBean.getProposition().getNote().setToUpdate(true);
                            propositionBean.getProposition().getNote().setToAdd(false);
                            propositionBean.getProposition().getNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else {
                            propositionBean.getProposition().getNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                break;
            case "definition":
                    if (propositionBean.getProposition().getDefinition().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getDefinition().setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getDefinition().getLexicalvalue())) {
                            propositionBean.getProposition().getDefinition().setToRemove(false);
                            propositionBean.getProposition().getDefinition().setToUpdate(false);
                            propositionBean.getProposition().getDefinition().setToAdd(false);
                            propositionBean.getProposition().getDefinition().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getDefinition().isToAdd()) {
                            propositionBean.getProposition().getDefinition().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getDefinition().isToRemove()) {
                            propositionBean.getProposition().getDefinition().setToUpdate(true);
                            propositionBean.getProposition().getDefinition().setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                break;
            case "scopeNote":
                    if (propositionBean.getProposition().getScopeNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getScopeNote().setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getScopeNote().getLexicalvalue())) {
                            propositionBean.getProposition().getScopeNote().setToRemove(false);
                            propositionBean.getProposition().getScopeNote().setToUpdate(false);
                            propositionBean.getProposition().getScopeNote().setToAdd(false);
                            propositionBean.getProposition().getScopeNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getScopeNote().isToAdd()) {
                            propositionBean.getProposition().getScopeNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getScopeNote().isToRemove()) {
                            propositionBean.getProposition().getScopeNote().setToUpdate(true);
                            propositionBean.getProposition().getScopeNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                break;
            case "example":
                    if (propositionBean.getProposition().getExample().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getExample().setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getExample().getLexicalvalue())) {
                            propositionBean.getProposition().getExample().setToRemove(false);
                            propositionBean.getProposition().getExample().setToUpdate(false);
                            propositionBean.getProposition().getExample().setToAdd(false);
                            propositionBean.getProposition().getExample().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getExample().isToAdd()) {
                            propositionBean.getProposition().getExample().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getExample().isToRemove()) {
                            propositionBean.getProposition().getExample().setToUpdate(true);
                            propositionBean.getProposition().getExample().setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                break;
            case "historyNote":
                    if (propositionBean.getProposition().getHistoryNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getHistoryNote().setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getHistoryNote().getLexicalvalue())) {
                            propositionBean.getProposition().getHistoryNote().setToRemove(false);
                            propositionBean.getProposition().getHistoryNote().setToUpdate(false);
                            propositionBean.getProposition().getHistoryNote().setToAdd(false);
                            propositionBean.getProposition().getHistoryNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getHistoryNote().isToAdd()) {
                            propositionBean.getProposition().getHistoryNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getHistoryNote().isToRemove()) {
                            propositionBean.getProposition().getHistoryNote().setToUpdate(true);
                            propositionBean.getProposition().getHistoryNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                break;
            case "editorialNote":
                    if (propositionBean.getProposition().getEditorialNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getEditorialNote().setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getEditorialNote().getLexicalvalue())) {
                            propositionBean.getProposition().getEditorialNote().setToRemove(false);
                            propositionBean.getProposition().getEditorialNote().setToUpdate(false);
                            propositionBean.getProposition().getEditorialNote().setToAdd(false);
                            propositionBean.getProposition().getEditorialNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getEditorialNote().isToAdd()) {
                            propositionBean.getProposition().getEditorialNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getEditorialNote().isToRemove()) {
                            propositionBean.getProposition().getEditorialNote().setToUpdate(true);
                            propositionBean.getProposition().getEditorialNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                break;
            case "changeNote":
                    if (propositionBean.getProposition().getChangeNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        
                        if (notePropBean.isToAdd()) {
                            propositionBean.getProposition().getChangeNote().setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }
                        
                        if (notePropBean.getOldValue().equalsIgnoreCase(propositionBean.getProposition()
                                .getChangeNote().getLexicalvalue())) {
                            propositionBean.getProposition().getChangeNote().setToRemove(false);
                            propositionBean.getProposition().getChangeNote().setToUpdate(false);
                            propositionBean.getProposition().getChangeNote().setToAdd(false);
                            propositionBean.getProposition().getChangeNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (propositionBean.getProposition().getChangeNote().isToAdd()) {
                            propositionBean.getProposition().getChangeNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!propositionBean.getProposition().getChangeNote().isToRemove()) {
                            propositionBean.getProposition().getChangeNote().setToUpdate(true);
                            propositionBean.getProposition().getChangeNote().setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                break;
        }
        
        propositionBean.checkNotePropositionStatus();
    }

    public NodeNote nodeToEdit() {
        if(isFacetNote) {
            return nodeToEditFacet();
        }
        if(isGroupNote) {
            return nodeToEditGroup();
        }        
        return nodeToEditConcept();
    }
    
    private NodeNote nodeToEditConcept() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return conceptBean.getNote();
            case "definition":
                return conceptBean.getDefinition();
            case "scopeNote":
                return conceptBean.getScopeNote();
            case "example":
                return conceptBean.getExample();
            case "historyNote":
                return conceptBean.getHistoryNote();
            case "editorialNote":
                return conceptBean.getEditorialNote();
            case "changeNote":
                return conceptBean.getChangeNote();
            default:
                return null;
        }
    }
    
    private NodeNote nodeToEditFacet() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return editFacet.getNote();
            case "definition":
                return editFacet.getDefinition();
            case "scopeNote":
                return editFacet.getScopeNote();
            case "example":
                return editFacet.getExample();
            case "historyNote":
                return editFacet.getHistoryNote();
            case "editorialNote":
                return editFacet.getEditorialNote();
            case "changeNote":
                return editFacet.getChangeNote();
            default:
                return null;
        }
    }    
    
    private NodeNote nodeToEditGroup() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return groupView.getNote();
            case "definition":
                return groupView.getDefinition();
            case "scopeNote":
                return groupView.getScopeNote();
            case "example":
                return groupView.getExample();
            case "historyNote":
                return groupView.getHistoryNote();
            case "editorialNote":
                return groupView.getEditorialNote();
            case "changeNote":
                return groupView.getChangeNote();
            default:
                return null;
        }
    }       

    public NotePropBean nodePropToEdit() {
        if (selectedTypeNote == null) {
            return new NotePropBean();
        }
        if(propositionBean.getProposition() == null) return new NotePropBean();
        switch (selectedTypeNote) {
            case "note":
                return propositionBean.getProposition().getNote();
            case "definition":
                return propositionBean.getProposition().getDefinition();
            case "scopeNote":
                return propositionBean.getProposition().getScopeNote();
            case "example":
                return propositionBean.getProposition().getExample();
            case "historyNote":
                return propositionBean.getProposition().getHistoryNote();
            case "editorialNote":
                return propositionBean.getProposition().getEditorialNote();
            default:
                return propositionBean.getProposition().getChangeNote();
        }
    }

    public void deleteNote(NodeNote nodeNote, int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        FacesMessage msg;

        if(isFacetNote){
            deleteThisNoteFacet(nodeNote, idUser);
            return;
        }
        if(isGroupNote){
            deleteThisNoteGroup(nodeNote, idUser);
            return;
        }          
        
        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeNote.getId_concept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNotetypecode(),
                nodeNote.getLexicalvalue(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

    }
    
    private void deleteThisNoteFacet(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        NoteHelper noteHelper = new NoteHelper();        
        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeNote.getId_concept(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNotetypecode(),
                nodeNote.getLexicalvalue(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        editFacet.initEditFacet(nodeFacet.getIdFacet(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }
    private void deleteThisNoteGroup(NodeNote nodeNote, int idUser){
        FacesMessage msg;
        NoteHelper noteHelper = new NoteHelper();        
        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                nodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                nodeGroup.getConceptGroup().getIdgroup(),
                nodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeNote.getNotetypecode(),
                nodeNote.getLexicalvalue(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        groupView.getGroup(selectedTheso.getCurrentIdTheso(),  nodeGroup.getConceptGroup().getIdgroup(), selectedTheso.getCurrentLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);             
    }    
    
    public NodeNote nodeToDelete() {
        if(isFacetNote) {
            return nodeToDeleteFacet();
        }
        if(isGroupNote) {
            return nodeToDeleteGroup();
        }        
        return nodeToDeleteConcept();
    }    

    private NodeNote nodeToDeleteConcept() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return conceptBean.getNote();
            case "definition":
                return conceptBean.getDefinition();
            case "scopeNote":
                return conceptBean.getScopeNote();
            case "example":
                return conceptBean.getExample();
            case "historyNote":
                return conceptBean.getHistoryNote();
            case "editorialNote":
                return conceptBean.getEditorialNote();
            case "changeNote":
                return conceptBean.getChangeNote();
            default:
                return null;
        }
    }
    
    private NodeNote nodeToDeleteFacet() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return editFacet.getNote();
            case "definition":
                return editFacet.getDefinition();
            case "scopeNote":
                return editFacet.getScopeNote();
            case "example":
                return editFacet.getExample();
            case "historyNote":
                return editFacet.getHistoryNote();
            case "editorialNote":
                return editFacet.getEditorialNote();
            case "changeNote":
                return editFacet.getChangeNote();
            default:
                return null;
        }
    }    
    
    private NodeNote nodeToDeleteGroup() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return groupView.getNote();
            case "definition":
                return groupView.getDefinition();
            case "scopeNote":
                return groupView.getScopeNote();
            case "example":
                return groupView.getExample();
            case "historyNote":
                return groupView.getHistoryNote();
            case "editorialNote":
                return groupView.getEditorialNote();
            case "changeNote":
                return groupView.getChangeNote();
            default:
                return null;
        }
    }      

    public void deleteNoteProp(NotePropBean notePropBean) {

        switch (selectedTypeNote) {
            case "note":
                    if (propositionBean.getProposition().getNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getNote().isToAdd()) {
                            propositionBean.getProposition().setNote(null);
                        } else if (propositionBean.getProposition().getNote().isToUpdate()) {
                            propositionBean.getProposition().getNote().setToUpdate(false);
                            propositionBean.getProposition().getNote().setToRemove(true);
                            propositionBean.getProposition().getNote()
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getNote().setToRemove(
                                    !propositionBean.getProposition().getNote().isToRemove());
                        }
                    }
                break;
            case "definition":
                    if (propositionBean.getProposition().getDefinition().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getDefinition().isToAdd()) {
                            propositionBean.getProposition().setDefinition(null);
                        } else if (propositionBean.getProposition().getDefinition().isToUpdate()) {
                            propositionBean.getProposition().getDefinition().setToUpdate(false);
                            propositionBean.getProposition().getDefinition().setToRemove(true);
                            propositionBean.getProposition().getDefinition()
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getDefinition().setToRemove(
                                    !propositionBean.getProposition().getDefinition().isToRemove());
                        }
                    }
                
                break;
            case "scopeNote":
                    if (propositionBean.getProposition().getScopeNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getScopeNote().isToAdd()) {
                            propositionBean.getProposition().setScopeNote(null);
                        } else if (propositionBean.getProposition().getScopeNote().isToUpdate()) {
                            propositionBean.getProposition().getScopeNote().setToUpdate(false);
                            propositionBean.getProposition().getScopeNote().setToRemove(true);
                            propositionBean.getProposition().getScopeNote()
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getScopeNote().setToRemove(
                                    !propositionBean.getProposition().getScopeNote().isToRemove());
                        }
                    }
                
                break;
            case "example":
                    if (propositionBean.getProposition().getExample().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getExample().isToAdd()) {
                            propositionBean.getProposition().setExample(null);
                        } else if (propositionBean.getProposition().getExample().isToUpdate()) {
                            propositionBean.getProposition().getExample().setToUpdate(false);
                            propositionBean.getProposition().getExample().setToRemove(true);
                            propositionBean.getProposition().getExample()
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getExample().setToRemove(
                                    !propositionBean.getProposition().getExample().isToRemove());
                        }
                    }
                break;
            case "historyNote":
                    if (propositionBean.getProposition().getHistoryNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getHistoryNote().isToAdd()) {
                            propositionBean.getProposition().setHistoryNote(null);
                        } else if (propositionBean.getProposition().getHistoryNote().isToUpdate()) {
                            propositionBean.getProposition().getHistoryNote().setToUpdate(false);
                            propositionBean.getProposition().getHistoryNote().setToRemove(true);
                            propositionBean.getProposition().getHistoryNote()
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getHistoryNote().setToRemove(
                                    !propositionBean.getProposition().getHistoryNote().isToRemove());
                        }
                    }
                break;
            case "editorialNote":
                    if (propositionBean.getProposition().getEditorialNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getEditorialNote().isToAdd()) {
                            propositionBean.getProposition().setEditorialNote(null);
                        } else if (propositionBean.getProposition().getEditorialNote().isToUpdate()) {
                            propositionBean.getProposition().getEditorialNote().setToUpdate(false);
                            propositionBean.getProposition().getEditorialNote().setToRemove(true);
                            propositionBean.getProposition().getEditorialNote()
                                    .setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getEditorialNote().setToRemove(
                                    !propositionBean.getProposition().getEditorialNote().isToRemove());
                        }
                    }
                
                break;
            case "changeNote":
                    if (propositionBean.getProposition().getChangeNote().getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {
                        if (propositionBean.getProposition().getChangeNote().isToAdd()) {
                            propositionBean.getProposition().setChangeNote(null);
                        } else if (propositionBean.getProposition().getChangeNote().isToUpdate()) {
                            propositionBean.getProposition().getChangeNote().setToUpdate(false);
                            propositionBean.getProposition().getChangeNote().setToRemove(true);
                            propositionBean.getProposition().getChangeNote().setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            propositionBean.getProposition().getChangeNote().setToRemove(
                                    !propositionBean.getProposition().getChangeNote().isToRemove());
                        }
                    }
                
                break;
        }
        
        propositionBean.checkNotePropositionStatus();
    }

    private void printErreur() {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de création de note !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public ArrayList<NoteHelper.NoteType> getNoteTypes() {
        return noteTypes;
    }

    public void setNoteTypes(ArrayList<NoteHelper.NoteType> noteTypes) {
        this.noteTypes = noteTypes;
    }

    public String getSelectedTypeNote() {
        return selectedTypeNote;
    }

    public void setSelectedTypeNote(String selectedTypeNote) {
        this.selectedTypeNote = selectedTypeNote;
    }

    public String getNoteValue() {
        return noteValue;
    }

    public void setNoteValue(String noteValue) {
        this.noteValue = noteValue;
    }

    public ArrayList<NodeLangTheso> getNodeLangs() {
        return nodeLangs;
    }

    public void setNodeLangs(ArrayList<NodeLangTheso> nodeLangs) {
        this.nodeLangs = nodeLangs;
    }

    public NodeNote getSelectedNodeNote() {
        return selectedNodeNote;
    }

    public void setSelectedNodeNote(NodeNote selectedNodeNote) {
        this.selectedNodeNote = selectedNodeNote;
    }

    public boolean isIsConceptNote() {
        return isConceptNote;
    }

    public void setIsConceptNote(boolean isConceptNote) {
        this.isConceptNote = isConceptNote;
    }

    public boolean isIsGroupNote() {
        return isGroupNote;
    }

    public void setIsGroupNote(boolean isGroupNote) {
        this.isGroupNote = isGroupNote;
    }

    public boolean isIsFacetNote() {
        return isFacetNote;
    }

    public void setIsFacetNote(boolean isFacetNote) {
        this.isFacetNote = isFacetNote;
    }

}
