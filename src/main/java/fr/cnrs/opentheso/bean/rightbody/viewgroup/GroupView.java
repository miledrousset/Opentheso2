/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.rightbody.viewgroup;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGroupType;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "groupView")
@SessionScoped
public class GroupView implements Serializable {
    @Inject private Connect connect;
    @Inject private IndexSetting indexSetting;     
    @Inject private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Inject private ViewEditorHomeBean viewEditorHomeBean;       

    private NodeGroup nodeGroup;
    private ArrayList<NodeGroupTraductions> nodeGroupTraductions;
    private NodeGroupType nodeGroupType;
    
    private NodeNote note;
    private NodeNote scopeNote;
    private NodeNote changeNote;
    private NodeNote definition;
    private NodeNote editorialNote;
    private NodeNote example;
    private NodeNote historyNote;        
    
    private int count;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeGroupTraductions!= null){
            nodeGroupTraductions.clear();
            nodeGroupTraductions = null;
        }
        nodeGroup = null;
        nodeGroupType = null;
    }      
    
    /**
     * Creates a new instance of ConceptBean
     */
    public GroupView() {
    }

    public void init() {
        /*  if(isUriRequest) {
            isUriRequest = false;
            return;
        }*/
        count = 0;
        nodeGroup = null;
        nodeGroupType = null;
        nodeGroupTraductions = null;
    }

    /**
     * récuparation des informations pour le concept sélectionné
     *
     * @param idTheso
     * @param idGroup
     * @param idLang
     */
    public void getGroup(String idTheso, String idGroup, String idLang) {
        GroupHelper groupHelper = new GroupHelper();
        nodeGroup = groupHelper.getThisConceptGroup(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        
        nodeGroupTraductions = groupHelper.getGroupTraduction(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        nodeGroupType = groupHelper.getGroupType(
                connect.getPoolConnexion(), nodeGroup.getConceptGroup().getIdtypecode());
        
        
        NoteHelper noteHelper = new NoteHelper();
        ArrayList<NodeNote> nodeNotes = noteHelper.getListNotes(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        setAllNotes(nodeNotes);        
        
        ConceptHelper conceptHelper = new ConceptHelper();
        count = conceptHelper.getCountOfConceptsOfGroup(connect.getPoolConnexion(), idTheso, idGroup);
        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
    }
    
    /////////////////////////////////
    /////////////////////////////////
    // fonctions pour les notes /////    
    /////////////////////////////////
    /////////////////////////////////
    private void setAllNotes(ArrayList<NodeNote> nodeNotes) {
        clearNotes();
        for (NodeNote nodeNote : nodeNotes) {
            switch (nodeNote.getNotetypecode()) {
                case "note":
                    note  = nodeNote;
                    break;
                case "scopeNote":
                    scopeNote = nodeNote;
                    break;
                case "changeNote":
                    changeNote = nodeNote;
                    break;
                case "definition":
                    definition = nodeNote;
                    break;
                case "editorialNote":
                    editorialNote = nodeNote;
                    break;
                case "example":
                    example = nodeNote;
                    break;
                case "historyNote":
                    historyNote = nodeNote;
                    break;
            }
        }
    }
    private void clearNotes() {
        note = null;
        scopeNote = null;
        changeNote = null;
        definition = null;
        editorialNote = null;
        example = null;
        historyNote = null;
    }        

    public NodeGroup getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(NodeGroup nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<NodeGroupTraductions> getNodeGroupTraductions() {
        return nodeGroupTraductions;
    }

    public void setNodeGroupTraductions(ArrayList<NodeGroupTraductions> nodeGroupTraductions) {
        this.nodeGroupTraductions = nodeGroupTraductions;
    }

    public NodeGroupType getNodeGroupType() {
        return nodeGroupType;
    }

    public void setNodeGroupType(NodeGroupType nodeGroupType) {
        this.nodeGroupType = nodeGroupType;
    }

    public NodeNote getNote() {
        return note;
    }

    public void setNote(NodeNote note) {
        this.note = note;
    }

    public NodeNote getScopeNote() {
        return scopeNote;
    }

    public void setScopeNote(NodeNote scopeNote) {
        this.scopeNote = scopeNote;
    }

    public NodeNote getChangeNote() {
        return changeNote;
    }

    public void setChangeNote(NodeNote changeNote) {
        this.changeNote = changeNote;
    }

    public NodeNote getDefinition() {
        return definition;
    }

    public void setDefinition(NodeNote definition) {
        this.definition = definition;
    }

    public NodeNote getEditorialNote() {
        return editorialNote;
    }

    public void setEditorialNote(NodeNote editorialNote) {
        this.editorialNote = editorialNote;
    }

    public NodeNote getExample() {
        return example;
    }

    public void setExample(NodeNote example) {
        this.example = example;
    }

    public NodeNote getHistoryNote() {
        return historyNote;
    }

    public void setHistoryNote(NodeNote historyNote) {
        this.historyNote = historyNote;
    }

 
   
}
