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
import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
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
                if (!checkExisting(propositionBean.getProposition().getDefinitions())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "scopeNote":
                if (!checkExisting(propositionBean.getProposition().getScopeNotes())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "example":
                if (!checkExisting(propositionBean.getProposition().getExamples())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "historyNote":
                if (!checkExisting(propositionBean.getProposition().getHistoryNotes())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "editorialNote":
                if (!checkExisting(propositionBean.getProposition().getEditorialNotes())) {
                    showErrorMessageNote();
                    return;
                }
                break;
            case "changeNote":
                if (!checkExisting(propositionBean.getProposition().getChangeNotes())) {
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
                propositionBean.getProposition().getNote().add(notePropBean);
                break;
            case "definition":
                propositionBean.getProposition().getDefinitions().add(notePropBean);
                break;
            case "scopeNote":
                propositionBean.getProposition().getScopeNotes().add(notePropBean);
                break;
            case "example":
                propositionBean.getProposition().getExamples().add(notePropBean);
                break;
            case "historyNote":
                propositionBean.getProposition().getHistoryNotes().add(notePropBean);
                break;
            case "editorialNote":
                propositionBean.getProposition().getEditorialNotes().add(notePropBean);
                break;
            case "changeNote":
                propositionBean.getProposition().getChangeNotes().add(notePropBean);
                break;
        }
        
        propositionBean.checkNotePropositionStatus();
    }

    private void showErrorMessageNote() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note "
                + noteValue + " ( " + selectedLang + ") note existe déjà !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private boolean checkExisting(List<NotePropBean> notes) {
        if(CollectionUtils.isEmpty(notes)) return true;

        for (NotePropBean note : notes) {
            if (selectedLang.equalsIgnoreCase(note.getLang()) && noteValue.equalsIgnoreCase(note.getLexicalvalue())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Cette note existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
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
                for (NotePropBean note : propositionBean.getProposition().getNote()) {
                    if (note.getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {

                        if (notePropBean.isToAdd()) {
                            note.setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }

                        if (notePropBean.getOldValue().equalsIgnoreCase(note.getLexicalvalue())) {
                            note.setToRemove(false);
                            note.setToUpdate(false);
                            note.setToAdd(false);
                            note.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!note.isToRemove()) {
                            note.setToUpdate(true);
                            note.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (note.isToRemove()) {
                            note.setToRemove(false);
                            note.setToUpdate(true);
                            note.setToAdd(false);
                            note.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else {
                            note.setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "definition":
                for (NotePropBean definition : propositionBean.getProposition().getDefinitions()) {
                    if (definition.getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {

                        if (notePropBean.isToAdd()) {
                            definition.setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }

                        if (notePropBean.getOldValue().equalsIgnoreCase(definition.getLexicalvalue())) {
                            definition.setToRemove(false);
                            definition.setToUpdate(false);
                            definition.setToAdd(false);
                            definition.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (definition.isToAdd()) {
                            definition.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!definition.isToRemove()) {
                            definition.setToUpdate(true);
                            definition.setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "scopeNote":
                for (NotePropBean scopteNote : propositionBean.getProposition().getScopeNotes()) {
                    if (scopteNote.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {

                        if (notePropBean.isToAdd()) {
                            scopteNote.setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }

                        if (notePropBean.getOldValue().equalsIgnoreCase(scopteNote.getLexicalvalue())) {
                            scopteNote.setToRemove(false);
                            scopteNote.setToUpdate(false);
                            scopteNote.setToAdd(false);
                            scopteNote.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (scopteNote.isToAdd()) {
                            scopteNote.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!scopteNote.isToRemove()) {
                            scopteNote.setToUpdate(true);
                            scopteNote.setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "example":
                for (NotePropBean example : propositionBean.getProposition().getExamples()) {
                    if (example.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {

                        if (notePropBean.isToAdd()) {
                            example.setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }

                        if (notePropBean.getOldValue().equalsIgnoreCase(example.getLexicalvalue())) {
                            example.setToRemove(false);
                            example.setToUpdate(false);
                            example.setToAdd(false);
                            example.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (example.isToAdd()) {
                            example.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!example.isToRemove()) {
                            example.setToUpdate(true);
                            example.setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "historyNote":
                for (NotePropBean history : propositionBean.getProposition().getHistoryNotes()) {
                    if (history.getLexicalvalue()
                            .equals(notePropBean.getLexicalvalue())) {

                        if (notePropBean.isToAdd()) {
                            history.setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }

                        if (notePropBean.getOldValue().equalsIgnoreCase(history.getLexicalvalue())) {
                            history.setToRemove(false);
                            history.setToUpdate(false);
                            history.setToAdd(false);
                            history.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (history.isToAdd()) {
                            history.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!history.isToRemove()) {
                            history.setToUpdate(true);
                            history.setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "editorialNote":
                for (NotePropBean editorial : propositionBean.getProposition().getEditorialNotes()) {
                    if (editorial.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {

                        if (notePropBean.isToAdd()) {
                            editorial.setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }

                        if (notePropBean.getOldValue().equalsIgnoreCase(editorial.getLexicalvalue())) {
                            editorial.setToRemove(false);
                            editorial.setToUpdate(false);
                            editorial.setToAdd(false);
                            editorial.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (editorial.isToAdd()) {
                            editorial.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!editorial.isToRemove()) {
                            editorial.setToUpdate(true);
                            editorial.setLexicalvalue(notePropBean.getLexicalvalue());
                        }
                    }
                }
                break;
            case "changeNote":
                for (NotePropBean changeNote : propositionBean.getProposition().getChangeNotes()) {
                    if (changeNote.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {

                        if (notePropBean.isToAdd()) {
                            changeNote.setLexicalvalue(notePropBean.getLexicalvalue());
                            break;
                        }

                        if (notePropBean.getOldValue().equalsIgnoreCase(changeNote.getLexicalvalue())) {
                            changeNote.setToRemove(false);
                            changeNote.setToUpdate(false);
                            changeNote.setToAdd(false);
                            changeNote.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (changeNote.isToAdd()) {
                            changeNote.setLexicalvalue(notePropBean.getLexicalvalue());
                        } else if (!changeNote.isToRemove()) {
                            changeNote.setToUpdate(true);
                            changeNote.setLexicalvalue(notePropBean.getLexicalvalue());
                        }
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

    public List<NotePropBean> nodePropToEdit() {
        if (selectedTypeNote == null) {
            return List.of(new NotePropBean());
        }

        if(propositionBean.getProposition() == null) return List.of(new NotePropBean());

        switch (selectedTypeNote) {
            case "note":
                return propositionBean.getProposition().getNote();
            case "definition":
                return propositionBean.getProposition().getDefinitions();
            case "scopeNote":
                return propositionBean.getProposition().getScopeNotes();
            case "example":
                return propositionBean.getProposition().getExamples();
            case "historyNote":
                return propositionBean.getProposition().getHistoryNotes();
            case "editorialNote":
                return propositionBean.getProposition().getEditorialNotes();
            default:
                return propositionBean.getProposition().getChangeNotes();
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
                for (int i = 0; i < propositionBean.getProposition().getNote().size(); i++) {
                    var note = propositionBean.getProposition().getNote().get(i);
                    if (note.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {
                        if (note.isToAdd()) {
                            propositionBean.getProposition().getNote().remove(note);
                        } else if (note.isToUpdate()) {
                            note.setToUpdate(false);
                            note.setToRemove(true);
                            note.setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            note.setToRemove(!note.isToRemove());
                        }
                    }
                }
                break;
            case "definition":
                for (int i = 0; i < propositionBean.getProposition().getDefinitions().size(); i++) {
                    var definition = propositionBean.getProposition().getDefinitions().get(i);
                    if (definition.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {
                        if (definition.isToAdd()) {
                            propositionBean.getProposition().getDefinitions().remove(definition);
                        } else if (definition.isToUpdate()) {
                            definition.setToUpdate(false);
                            definition.setToRemove(true);
                            definition.setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            definition.setToRemove(!definition.isToRemove());
                        }
                    }
                }
                break;
            case "scopeNote":
                for (int i = 0; i < propositionBean.getProposition().getScopeNotes().size(); i++) {
                    var scopeNote = propositionBean.getProposition().getScopeNotes().get(i);
                    if (scopeNote.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {
                        if (scopeNote.isToAdd()) {
                            propositionBean.getProposition().getScopeNotes().remove(scopeNote);
                        } else if (scopeNote.isToUpdate()) {
                            scopeNote.setToUpdate(false);
                            scopeNote.setToRemove(true);
                            scopeNote.setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            scopeNote.setToRemove(!scopeNote.isToRemove());
                        }
                    }
                }
                break;
            case "example":
                for (int i = 0; i < propositionBean.getProposition().getExamples().size(); i++) {
                    var example = propositionBean.getProposition().getExamples().get(i);
                    if (example.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {
                        if (example.isToAdd()) {
                            propositionBean.getProposition().getExamples().remove(example);
                        } else if (example.isToUpdate()) {
                            example.setToUpdate(false);
                            example.setToRemove(true);
                            example.setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            example.setToRemove(!example.isToRemove());
                        }
                    }
                }
                break;
            case "historyNote":
                for (int i = 0; i < propositionBean.getProposition().getHistoryNotes().size(); i++) {
                    var history = propositionBean.getProposition().getHistoryNotes().get(i);
                    if (history.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {
                        if (history.isToAdd()) {
                            propositionBean.getProposition().getHistoryNotes().remove(history);
                        } else if (history.isToUpdate()) {
                            history.setToUpdate(false);
                            history.setToRemove(true);
                            history.setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            history.setToRemove(!history.isToRemove());
                        }
                    }
                }
                break;
            case "editorialNote":
                for (int i = 0; i < propositionBean.getProposition().getEditorialNotes().size(); i++) {
                    var editorial = propositionBean.getProposition().getEditorialNotes().get(i);
                    if (editorial.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {
                        if (editorial.isToAdd()) {
                            propositionBean.getProposition().getEditorialNotes().remove(editorial);
                        } else if (editorial.isToUpdate()) {
                            editorial.setToUpdate(false);
                            editorial.setToRemove(true);
                            editorial.setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            editorial.setToRemove(!editorial.isToRemove());
                        }
                    }
                }
                break;
            case "changeNote":
                for (int i = 0; i < propositionBean.getProposition().getChangeNotes().size(); i++) {
                    var changeNote = propositionBean.getProposition().getChangeNotes().get(i);
                    if (changeNote.getLexicalvalue().equals(notePropBean.getLexicalvalue())) {
                        if (changeNote.isToAdd()) {
                            propositionBean.getProposition().getChangeNotes().remove(changeNote);
                        } else if (changeNote.isToUpdate()) {
                            changeNote.setToUpdate(false);
                            changeNote.setToRemove(true);
                            changeNote.setLexicalvalue(notePropBean.getOldValue());
                        } else {
                            changeNote.setToRemove(!changeNote.isToRemove());
                        }
                    }
                }
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
