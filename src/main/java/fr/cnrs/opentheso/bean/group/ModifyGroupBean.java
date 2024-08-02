/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAutoCompletion;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
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
@Named(value = "modifyGroupBean")
@jakarta.enterprise.context.SessionScoped

public class ModifyGroupBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private TreeGroups treeGroups;
    @Inject
    private GroupView groupView;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private CurrentUser currentUser;

    private String selectedGroupType;
    private String idGroup;
    private String titleGroup;
    private String notation;
    private List<SelectItem> listGroupType;
    
    private NodeAutoCompletion selectedNodeAutoCompletionGroup;    
    private boolean moveToRoot = false;
    
    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        selectedGroupType = null;
        titleGroup = null;
        titleGroup = null;
        notation = null;
        if (listGroupType != null) {
            listGroupType.clear();
            listGroupType = null;
        }
        moveToRoot = false;
    }

    public ModifyGroupBean() {
    }

    public void init() {
        moveToRoot = false;        
        idGroup = groupView.getNodeGroup().getConceptGroup().getIdgroup();
        titleGroup = groupView.getNodeGroup().getLexicalValue();
        notation = groupView.getNodeGroup().getConceptGroup().getNotation();
        selectedGroupType = groupView.getNodeGroup().getConceptGroup().getIdtypecode();

        listGroupType = new GroupHelper().getAllGroupType(connect.getPoolConnexion());
     /*   if (!listGroupType.isEmpty()) {
            selectedGroupType = listGroupType.get(0).getLabel();
        }*/
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Group !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    
    public void moveGroupTo(){
        if(StringUtils.isEmpty(idGroup)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);     
            return;
        }     
        
        GroupHelper groupHelper = new GroupHelper();
        
        String idParent = groupHelper.getIdFather(connect.getPoolConnexion(), idGroup, selectedTheso.getCurrentIdTheso());
        
        if(isMoveToRoot()) {
            if(!StringUtils.isEmpty(idParent)) {
                if(!groupHelper.removeGroupFromGroup(connect.getPoolConnexion(), idGroup, idParent, selectedTheso.getCurrentIdTheso())){
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);               
                    return;
                }                
            } else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Déplacement à la même place !");
                FacesContext.getCurrentInstance().addMessage(null, msg);     
                return;                
            }
        } else {
            
            if(selectedNodeAutoCompletionGroup == null || StringUtils.isEmpty(selectedNodeAutoCompletionGroup.getIdGroup())){
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Pas de sélection !");
                FacesContext.getCurrentInstance().addMessage(null, msg);     
                return;
            }

            if(selectedNodeAutoCompletionGroup.getIdGroup().equalsIgnoreCase(idGroup)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Déplacement impossible !");
                FacesContext.getCurrentInstance().addMessage(null, msg);     
                return;
            }               
            
            /// contrôle si le groupe est à déplacer dans la même hiérarchie, c'est interdit
            if(groupHelper.isMoveToDescending(connect.getPoolConnexion(),
                    idGroup, selectedNodeAutoCompletionGroup.getIdGroup(), selectedTheso.getCurrentIdTheso())){
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Déplacement impossible !");
                FacesContext.getCurrentInstance().addMessage(null, msg);     
                return;           
            }        


            if(!StringUtils.isEmpty(idParent)) {
                if(selectedNodeAutoCompletionGroup.getIdGroup().equalsIgnoreCase(idParent)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Déplacement à la même place !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);     
                    return;
                }              

                if(!groupHelper.removeGroupFromGroup(connect.getPoolConnexion(), idGroup, idParent, selectedTheso.getCurrentIdTheso())){
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);               
                    return;
                }
            }
            groupHelper.addSubGroup(connect.getPoolConnexion(),
                    selectedNodeAutoCompletionGroup.getIdGroup(), idGroup, selectedTheso.getCurrentIdTheso());
        }

        selectedTheso.reloadGroups();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", " Déplacement réussi !");
        FacesContext.getCurrentInstance().addMessage(null, msg);                    
    }
    
    /**
     * permet de retourner la liste des groupes / collections contenus dans le
     * thésaurus
     *
     * @param value
     * @return
     */
    public List<NodeAutoCompletion> getAutoCompletCollection(String value) {
        selectedNodeAutoCompletionGroup = new NodeAutoCompletion();
        List<NodeAutoCompletion> liste = new ArrayList<>();
        if (selectedTheso.getCurrentIdTheso() != null && selectedTheso.getCurrentLang() != null) {
            liste = new GroupHelper().getAutoCompletionGroup(
                    connect.getPoolConnexion(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(),
                    value);
        }
        return liste;
    }    
    
    /**
     * Modification du label du gourpe
     *
     */
    public void renameGroup() {
        FacesMessage msg;

        PrimeFaces pf = PrimeFaces.current();

        if (titleGroup.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, " ", " Le label ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        GroupHelper groupHelper = new GroupHelper();
        if (groupHelper.isDomainExist(connect.getPoolConnexion(),
                titleGroup,
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, " ", " un group existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        if(groupHelper.isHaveTraduction(connect.getPoolConnexion(), idGroup, selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang())){
            if (!groupHelper.renameGroup(
                    connect.getPoolConnexion(),
                    titleGroup,
                    selectedTheso.getCurrentLang(),
                    idGroup,
                    selectedTheso.getCurrentIdTheso(),
                    currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur lors de la modification du label !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }            
        } else {
            if (!groupHelper.addGroupTraduction(
                    connect.getPoolConnexion(),
                    idGroup,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(),
                    titleGroup)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur lors de la modification du label !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " Libellé modifié avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (treeGroups.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre

            // sinon, on modifie le label
            if (((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    groupView.getNodeGroup().getConceptGroup().getIdgroup())) {
                ((TreeNodeData) treeGroups.getSelectedNode().getData()).setName(titleGroup);
            }
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formLeftTab:tabTree:treeGroups");
            }
        }

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void updateNotation() {

        PrimeFaces pf = PrimeFaces.current();
        FacesMessage msg;

        if (notation == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Notation ne doit pas être null !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        GroupHelper groupHelper = new GroupHelper();

        if (groupHelper.isNotationExist(
                connect.getPoolConnexion(),
                notation,
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " La notation existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (!groupHelper.setNotationOfGroup(connect.getPoolConnexion(),
                notation,
                idGroup,
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur pendant la modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);

            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " Notation modifiée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (treeGroups.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre

            // sinon, on modifie le label
            if (((TreeNodeData) treeGroups.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    groupView.getNodeGroup().getConceptGroup().getIdgroup())) {
                ((TreeNodeData) treeGroups.getSelectedNode().getData()).setNotation(notation);
            }
        }

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("containerIndex:formLeftTab:treeGroups");
        }
    }

    public void updateGroupType() {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        if (selectedGroupType == null || selectedGroupType.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Le Type de groupe ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        GroupHelper groupHelper = new GroupHelper();
        if (!groupHelper.updateTypeGroup(
                connect.getPoolConnexion(),
                selectedGroupType,
                selectedTheso.getCurrentIdTheso(),
                idGroup)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, " ", " Erreur pendant la modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        groupView.getGroup(selectedTheso.getCurrentIdTheso(), idGroup, groupView.getNodeGroup().getIdLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", " groupe modifié avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("containerIndex:formLeftTab:treeGroups");
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

    public NodeAutoCompletion getSelectedNodeAutoCompletionGroup() {
        return selectedNodeAutoCompletionGroup;
    }

    public void setSelectedNodeAutoCompletionGroup(NodeAutoCompletion selectedNodeAutoCompletionGroup) {
        this.selectedNodeAutoCompletionGroup = selectedNodeAutoCompletionGroup;
    }

    public boolean isMoveToRoot() {
        return moveToRoot;
    }

    public void setMoveToRoot(boolean moveToRoot) {
        this.moveToRoot = moveToRoot;
    }

}
