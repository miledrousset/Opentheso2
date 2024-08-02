/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "addGroupBean")
@jakarta.enterprise.context.SessionScoped

public class AddGroupBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LeftBodySetting leftBodySetting;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private TreeGroups treeGroups;    
    
    private String selectedGroupType;
    private String titleGroup;    
    private String notation;    
    private List<SelectItem> listGroupType;    
    
    private String definition;
    
    private ArrayList<NodeNote> notes;
    private ArrayList<NodeNote> scopeNotes;
    private ArrayList<NodeNote> changeNotes;
    private ArrayList<NodeNote> definitions;
    private ArrayList<NodeNote> editorialNotes;
    private ArrayList<NodeNote> examples;
    private ArrayList<NodeNote> historyNotes;     
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        selectedGroupType = null;
        titleGroup = null;      
        titleGroup = null;        
        if(listGroupType!= null){
            listGroupType.clear();
            listGroupType = null;
        }    
    }
    
    public AddGroupBean() {
    }

    public void init() {
        titleGroup = "";
        notation = "";
        definition = "";
        selectedGroupType = null;
        listGroupType = new GroupHelper().getAllGroupType(connect.getPoolConnexion());
        if (!listGroupType.isEmpty()) {
            selectedGroupType = listGroupType.get(0).getLabel();
        }        
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Group !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    /**
     * Création d'un domaine/colection avec mise à jour dans l'arbre
     *
     * @param idTheso
     * @param idLang
     * @param idUser
     */
    public void addGroup(String idTheso, String idLang, int idUser) {

        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        NodeGroup nodeGroup = new NodeGroup();
        if (titleGroup.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Le label est obligatoire !"));
            return;
        }

        nodeGroup.setLexicalValue(titleGroup);
        nodeGroup.setIdLang(idLang);
        nodeGroup.getConceptGroup().setIdthesaurus(idTheso);
        nodeGroup.getConceptGroup().setNotation(notation);

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            selectedGroupType = "C";
        }
        nodeGroup.getConceptGroup().setIdtypecode(selectedGroupType);

        GroupHelper groupHelper = new GroupHelper();
        groupHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        if(notation == null || notation.isEmpty()){
        } else {
            if (groupHelper.isNotationExist(
                    connect.getPoolConnexion(),
                    notation,
                    idTheso)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " La notation existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }
        
        String idGroup = groupHelper.addGroup(connect.getPoolConnexion(),
                nodeGroup,
                idUser);
        if (idGroup == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "","Erreur interne"));
            return;
        }
        if(roleOnThesoBean.getNodePreference().isUseArkLocal()) {
            generateArkGroup(idGroup, titleGroup, idTheso);
        }
        
        // ajout de la définition s'il elle est renseignée
        if(StringUtils.isNotEmpty(definition)) {
            NoteHelper noteHelper = new NoteHelper();
            noteHelper.addNote(connect.getPoolConnexion(), idGroup, idLang, idTheso,
                    definition, "definition", "",  idUser);            
        }

        
        treeGroups.addNewGroupToTree(idGroup, idTheso, idLang);

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "",
                titleGroup + " a été ajouté avec succès"));

        PrimeFaces.current().executeScript("PF('addGroup').hide();");
        
        leftBodySetting.setIndex("2");
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }

    /**
     * permet de générer l'identifiant Ark, s'il n'existe pas, il sera créé,
     * sinon, il sera mis à jour.
     * @param idGroup
     * @param groupLabel
     * @param idTheso
     */
    public void generateArkGroup(String idGroup, String groupLabel, String idTheso) {
        FacesMessage msg;
        if(StringUtils.isEmpty(idGroup) || StringUtils.isEmpty(idTheso)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Pas de groupe séléctionné !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        GroupHelper groupHelper = new GroupHelper();
        groupHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        
        if(!groupHelper.addIdArkGroup(connect.getPoolConnexion(), idTheso, idGroup, groupLabel)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La génération de Ark a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", groupHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La génération de Ark a réussi !!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
        
    }    
    
    
    /**
     * permet d'ajouter un sous groupe avec un type défini, le groupe père doit
     * exister.Le sous-groupe prend le même type que le père On ajoute aussi la
     * notation
     *
     * @param idGroupFather
     * @param idTheso
     * @param idLang
     * @param idUser
     */
    public void addSubGroup(
            String idGroupFather,
            String idTheso,
            String idLang,
            int idUser) {
        // typeDom = "";
        //si on a bien selectioner un group
        //  String idGroup = tree.getSelectedTerme().getIdC();
        
        
        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }        
        if(idGroupFather == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                            "Id groupe Parent null "));
            return;
        } 
        if (titleGroup.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "", "Le label est obligatoire"));
            return;
        }
        NodeGroup nodeGroup = new NodeGroup();        
        nodeGroup.setLexicalValue(titleGroup);
        nodeGroup.setIdLang(idLang);
        nodeGroup.getConceptGroup().setIdthesaurus(idTheso);
        nodeGroup.getConceptGroup().setNotation(notation);

        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            selectedGroupType = "C";
        }
        nodeGroup.getConceptGroup().setIdtypecode(selectedGroupType);

        GroupHelper groupHelper = new GroupHelper();
        groupHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        String idSubGroup = groupHelper.addGroup(connect.getPoolConnexion(),
                nodeGroup,
                idUser);
        if (idSubGroup == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur interne"));
            return;
        }        

        if (!groupHelper.addSubGroup(connect.getPoolConnexion(), idGroupFather, idSubGroup, idTheso)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "",
                            titleGroup + " : Erreur de création"));
            return;
        }
        treeGroups.addNewSubGroupToTree(treeGroups.getSelectedNode(), idSubGroup, idTheso, idLang);        
        
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "",
                titleGroup + " a été ajouté avec succès"));

        PrimeFaces.current().executeScript("PF('addSubGroup').hide();");
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("formLeftTab:tabGroups:treeGroups");
        }        
    }

    public String getSelectedGroupType() {
        return selectedGroupType;
    }

    public void setSelectedGroupType(String selectedGroupType) {
        this.selectedGroupType = selectedGroupType;
    }

    public String getTitleGroup() {
        return titleGroup;
    }

    public void setTitleGroup(String titleGroup) {
        this.titleGroup = titleGroup;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public List<SelectItem> getListGroupType() {
        return listGroupType;
    }

    public void setListGroupType(List<SelectItem> listGroupType) {
        this.listGroupType = listGroupType;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public ArrayList<NodeNote> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<NodeNote> notes) {
        this.notes = notes;
    }

    public ArrayList<NodeNote> getScopeNotes() {
        return scopeNotes;
    }

    public void setScopeNotes(ArrayList<NodeNote> scopeNotes) {
        this.scopeNotes = scopeNotes;
    }

    public ArrayList<NodeNote> getChangeNotes() {
        return changeNotes;
    }

    public void setChangeNotes(ArrayList<NodeNote> changeNotes) {
        this.changeNotes = changeNotes;
    }

    public ArrayList<NodeNote> getDefinitions() {
        return definitions;
    }

    public void setDefinitions(ArrayList<NodeNote> definitions) {
        this.definitions = definitions;
    }

    public ArrayList<NodeNote> getEditorialNotes() {
        return editorialNotes;
    }

    public void setEditorialNotes(ArrayList<NodeNote> editorialNotes) {
        this.editorialNotes = editorialNotes;
    }

    public ArrayList<NodeNote> getExamples() {
        return examples;
    }

    public void setExamples(ArrayList<NodeNote> examples) {
        this.examples = examples;
    }

    public ArrayList<NodeNote> getHistoryNotes() {
        return historyNotes;
    }

    public void setHistoryNotes(ArrayList<NodeNote> historyNotes) {
        this.historyNotes = historyNotes;
    }


}
