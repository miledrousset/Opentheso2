/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@Named(value = "noteBeanCandidat")
@SessionScoped
public class NoteBeanCandidat implements Serializable {

    @Inject private Connect connect;
    @Inject private NoteBeanCandidat noteBeanCandidat;
    @Inject private SelectedTheso selectedTheso;
    @Inject private CandidatBean candidatBean;

    private String selectedLang;
    private ArrayList<NoteHelper.NoteType> noteTypes;
    private ArrayList<NodeLangTheso> nodeLangs;

    private String selectedTypeNote;
    private String noteValue;

    private NodeNote selectedNodeNote;
    private String noteValueToChange;
    private boolean visible;
    private boolean isEditMode;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(noteTypes!= null){
            noteTypes.clear();
            noteTypes = null;
        } 
        if(nodeLangs!= null){
            nodeLangs.clear();
            nodeLangs = null;
        }         
        selectedLang = null;        
        selectedTypeNote = null;
        noteValue = null;
        selectedNodeNote = null;
        noteValueToChange = null;
    }
    
    public NoteBeanCandidat() {
    }

    public void reset() {
        visible = true;
        noteTypes = new NoteHelper().getNotesType(connect.getPoolConnexion());
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = candidatBean.getCandidatSelected().getLang();
        noteValue = "";
        selectedTypeNote = null;
        isEditMode = false;
    }
   
    public void resetEditNode(NodeNote selectedNodeNote) {
        reset();
        noteValue = selectedNodeNote.getLexicalvalue();
        selectedTypeNote = selectedNodeNote.getLang();
        this.selectedNodeNote = selectedNodeNote;
        isEditMode = true;
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private String removeParagraphTags(String rawNote){
        rawNote = rawNote.replaceAll("<p>", "");
        rawNote = rawNote.replaceAll("</p>", "\n");
        return rawNote;
    }    
    /**
     * permet d'ajouter un nouveau concept si le groupe = null, on ajoute un
     * concept sans groupe si l'id du concept est fourni, il faut controler s'il
     * est unique
     *
     * @param idUser
     */
    public void addNewNote(int idUser) {
        if(isEditMode) {
            updateNote(idUser);
            return;
        }
            
        FacesMessage msg;
        if (noteValue == null || noteValue.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        noteValue = removeParagraphTags(noteValue);
        noteValue = StringEscapeUtils.unescapeXml(noteValue);

        if (!addNote(idUser)) {
            printErreur();
            return;
        }        
        reset();

        try {          
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        } catch (IOException ex) {
            Logger.getLogger(NoteBeanCandidat.class.getName()).log(Level.SEVERE, null, ex);
        }

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("candidatForm:listTraductionForm");
            pf.ajax().update("candidatForm");
        }
        visible = false;
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }

    
    public void updateNote(int idUser){
        NoteHelper noteHelper = new NoteHelper();
     
        FacesMessage msg;        

        if (!noteHelper.updateNote(connect.getPoolConnexion(),
                selectedNodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                selectedNodeNote.getId_concept(),
                selectedNodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                selectedNodeNote.getLexicalvalue(),
                selectedNodeNote.getNotetypecode(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        reset();
        setVisible(false);
        try {          
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        } catch (IOException ex) {
            Logger.getLogger(NoteBeanCandidat.class.getName()).log(Level.SEVERE, null, ex);
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
      
    }
    
    public void deleteNote(int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        FacesMessage msg;

        if (!noteHelper.deleteThisNote(connect.getPoolConnexion(),
                selectedNodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                selectedNodeNote.getId_concept(),
                selectedNodeNote.getLang(),
                selectedTheso.getCurrentIdTheso(),
                selectedNodeNote.getNotetypecode(),
                noteValueToChange, idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        noteHelper.deleteVoteByNoteId(connect.getPoolConnexion(), selectedNodeNote.getId_note(), selectedTheso.getCurrentIdTheso(),
                selectedNodeNote.getId_concept());

        reset();
        try {          
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        } catch (IOException ex) {
            Logger.getLogger(NoteBeanCandidat.class.getName()).log(Level.SEVERE, null, ex);
        }

        noteBeanCandidat.setVisible(false);

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("candidatForm");
        }
    }

    private boolean addNote(int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        return noteHelper.addNote(
                connect.getPoolConnexion(),
                candidatBean.getCandidatSelected().getIdConcepte(),
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                noteValue,
                selectedTypeNote,
                "",
                idUser);
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

    public String getNoteValueToChange() {
        return noteValueToChange;
    }

    public void setNoteValueToChange(String noteValueToChange) {
        this.noteValueToChange = noteValueToChange;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isIsEditMode() {
        return isEditMode;
    }

    public void setIsEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
    }
    
}
