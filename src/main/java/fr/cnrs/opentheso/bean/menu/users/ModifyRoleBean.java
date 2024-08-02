/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRole;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "modifyRoleBean")
@SessionScoped
public class ModifyRoleBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    
    private NodeUser nodeSelectedUser;
    private String selectedProject;
    private String roleOfSelectedUser;

    // pour l'ajout d'un utilisateur existant au projet
    private NodeUser selectedUser;

    // liste des (rôle -> projet) qui existent déjà pour l'utilisateur     
    ArrayList<NodeUserRoleGroup> allMyRoleProject;
    
    // pour gérer les droits limités sur un ou plusieurs thésaurus
    private boolean limitOnTheso;    
    private ArrayList<NodeIdValue> listThesoOfProject;
    private List<String> selectedThesos; 
    private NodeUserRole selectedNodeUserRole;
    private ArrayList<NodeThesoRole> listeLimitedThesoRoleForUser; // la liste des roles / thesos de l'utilisateur et du groupe avec des droits limités       
    private ArrayList<NodeIdValue> myAuthorizedRolesLimited;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(allMyRoleProject!= null){
            allMyRoleProject.clear();
            allMyRoleProject = null;
        }
        nodeSelectedUser = null;
        selectedProject = null;
        roleOfSelectedUser = null;
        selectedUser = null;      
        limitOnTheso = false;
        listThesoOfProject = null;
        selectedThesos = null;    
        myAuthorizedRolesLimited = null;
    }   
    
    public ModifyRoleBean() {
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProject = selectedProject;
    }
    
    
    /**
     * permet de selectionner l'utilisateur dans la liste avec toutes les
     * informations nécessaires pour sa modification
     *
     * @param idUser
     * @param roleOfSelectedUser
     * @param selectedProject
     */
    public void selectUser(int idUser, int roleOfSelectedUser, String selectedProject) {
        UserHelper userHelper = new UserHelper();
        nodeSelectedUser = userHelper.getUser(connect.getPoolConnexion(), idUser);
        this.selectedProject = selectedProject;

        this.roleOfSelectedUser = "" + roleOfSelectedUser;
        initAllMyRoleProject();
        
        limitOnTheso = false;
        listThesoOfProject = null;
        selectedThesos = null;    
        myAuthorizedRolesLimited = null;
    }
    
    /**
     * permet de selectionner l'utilisateur qui a des droits limités 
     * informations nécessaires pour sa modification
     *
     * @param selectedNodeUserRole
     * @param selectedProject
     */
    public void selectUserWithLimitedRole(NodeUserRole selectedNodeUserRole, String selectedProject) {
        this.selectedNodeUserRole = selectedNodeUserRole;
        this.selectedProject = selectedProject;
        limitOnTheso = true;
        myAuthorizedRolesLimited = null;
        UserHelper userHelper = new UserHelper();
        selectedThesos = new ArrayList<>();
        ArrayList<NodeUserRole> nodeUserRoles = userHelper.getListRoleByThesoLimited(connect.getPoolConnexion(), Integer.parseInt(selectedProject), selectedNodeUserRole.getIdUser());
        for (NodeUserRole nodeUserRole1 : nodeUserRoles) {
            selectedThesos.add(nodeUserRole1.getIdTheso());
        }
        toogleLimitTheso();
        setLimitedRoleForThisUserByGroup();
    }    
    
    /**
     * permet de récupérer la liste des rôles pour l'utilisateur sur les thésaurus du projet
     */
    private void setLimitedRoleForThisUserByGroup(){
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }
        UserHelper userHelper = new UserHelper();
        int idGroup = Integer.parseInt(selectedProject);
        if (selectedNodeUserRole != null) {
            listeLimitedThesoRoleForUser = userHelper.getAllRolesThesosByUserGroupLimited(connect.getPoolConnexion(),
                    idGroup, selectedNodeUserRole.getIdUser());
        } else {
            if (listeLimitedThesoRoleForUser != null) {
                listeLimitedThesoRoleForUser.clear(); //cas où on supprime l'utilisateur en cours
            }
        }
        ArrayList<String> idThesosTemp = new ArrayList<>();
        for (NodeThesoRole nodeThesoRole : listeLimitedThesoRoleForUser) {
            idThesosTemp.add(nodeThesoRole.getIdTheso());
        }

        ArrayList<NodeIdValue> allThesoOfProject = userHelper.getThesaurusOfProject(connect.getPoolConnexion(), idGroup, connect.getWorkLanguage());
        for (NodeIdValue nodeIdValue : allThesoOfProject) {
            if(!idThesosTemp.contains(nodeIdValue.getId())){
                NodeThesoRole nodeThesoRole = new NodeThesoRole();
                nodeThesoRole.setIdTheso(nodeIdValue.getId());
                nodeThesoRole.setThesoName(nodeIdValue.getValue());
                nodeThesoRole.setIdRole(-1);
                nodeThesoRole.setRoleName("");
                
                listeLimitedThesoRoleForUser.add(nodeThesoRole);
            }    
        }
        
        myAuthorizedRolesLimited = myProjectBean.getMyAuthorizedRoles();
        NodeIdValue nodeIdValue = new NodeIdValue();
        nodeIdValue.setId("-1");
        nodeIdValue.setValue("");
        myAuthorizedRolesLimited.add(0,nodeIdValue);
    }      
    
    public void setSelectedRoleLimitedForTheso(){
    }

    /**
     * met à jour les rôles de l'utilisateur sur les thésaurus du projet ou 
     * si on redonne les droits sur le projet entier
     */
    public void updateLimitedRoleOnThesosForUser () {
        FacesMessage msg;
        
        if(listeLimitedThesoRoleForUser == null || listeLimitedThesoRoleForUser.isEmpty())  {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        UserHelper userHelper = new UserHelper();
        
        
        // suppression de tous les rôles
        if(!userHelper.deleteAllUserRoleOnTheso(connect.getPoolConnexion(), selectedNodeUserRole.getIdUser(), Integer.parseInt(selectedProject))){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;                   
        }
        
        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            // ajout des rôles pour l'utilisateur sur les thésaurus
            for (NodeThesoRole nodeThesoRole : listeLimitedThesoRoleForUser) {
                if(nodeThesoRole.getIdRole() != -1) {
                    if(!userHelper.addUserRoleOnThisTheso(connect.getPoolConnexion(), 
                            selectedNodeUserRole.getIdUser(), nodeThesoRole.getIdRole(),
                            Integer.parseInt(selectedProject), nodeThesoRole.getIdTheso())){
                        return;
                    }
                }
            }
            myProjectBean.setSelectedIndex("2");
        } else {
            if(!userHelper.addUserRoleOnGroup(
                    connect.getPoolConnexion(),
                    nodeSelectedUser.getIdUser(),
                    Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject))) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;             
            }
            myProjectBean.setSelectedIndex("1");
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }
    
    /**
     * met à jour les rôles de l'utilisateur sur les thésaurus du projet
     */
    public void updateUserRoleLimitedForSelectedUser () {
        FacesMessage msg;
        
        if(selectedNodeUserRole == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        UserHelper userHelper = new UserHelper();

        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            if(!userHelper.deleteAllUserRoleOnTheso(connect.getPoolConnexion(), selectedNodeUserRole.getIdUser(), Integer.parseInt(selectedProject))){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la modification des rôles !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                    
            }
            if(!userHelper.addUserRoleOnTheso(connect.getPoolConnexion(), 
                    selectedNodeUserRole.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject), selectedThesos)){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la modification des rôles !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                
            }
        } else {
            if(!userHelper.updateUserRoleOnGroup(
                    connect.getPoolConnexion(),
                    nodeSelectedUser.getIdUser(),
                    Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject))) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;             
            }
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }       
    
    /**
     * permet de supprimer le rôle de l'utilisateur sur ce thésaurus du projet
     */
    public void removeUserRoleOnTheso () {
        FacesMessage msg;
        
        if(selectedNodeUserRole == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de rôle sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        UserHelper userHelper = new UserHelper();
        if(!userHelper.deleteUserRoleOnTheso(connect.getPoolConnexion(),
                selectedNodeUserRole.getIdUser(),
                selectedNodeUserRole.getIdRole(),
                Integer.parseInt(selectedProject),
                selectedNodeUserRole.getIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de suppression du rôle de l'utilisateur pour ce thésaurus !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été supprimé !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListLimitedRoleUsers();
    }      
    
    private void initAllMyRoleProject(){
        UserHelper userHelper = new UserHelper();
        allMyRoleProject = userHelper.getUserRoleGroup(connect.getPoolConnexion(), nodeSelectedUser.getIdUser());
    }
    
 /*   private void initAuthorizedProject(){
        UserHelper userHelper = new UserHelper();
        myAuthorizedProjects = userHelper.getMyAuthorizedProjects(connect.getPoolConnexion(), nodeUserSelected.getIdUser());
    }
   */ 
   
    public void toogleLimitTheso(){
        if(!limitOnTheso) return;
        /// récupérer la liste des thésaurus d'un projet
        int idProject = -1;
        try {
            idProject = Integer.parseInt(selectedProject);
        } catch (Exception e) {
            return;
        }
        if(idProject == -1) return;
        UserHelper userHelper = new UserHelper();
        listThesoOfProject = userHelper.getThesaurusOfProject(connect.getPoolConnexion(), idProject, connect.getWorkLanguage());
    }  
    
    /**
     * met à jour le nouveau rôle de l'utilisateur sur le projet
     */
    public void updateRoleForSelectedUser () {
        FacesMessage msg;
        
        if(nodeSelectedUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        UserHelper userHelper = new UserHelper();

        // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
        if(limitOnTheso) {
            if(!userHelper.addUserRoleOnTheso(connect.getPoolConnexion(), 
                    nodeSelectedUser.getIdUser(), Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject), selectedThesos)){
                return;
            }
            myProjectBean.setSelectedIndex("2");
        } else {
            if(!userHelper.updateUserRoleOnGroup(
                    connect.getPoolConnexion(),
                    nodeSelectedUser.getIdUser(),
                    Integer.parseInt(roleOfSelectedUser),
                    Integer.parseInt(selectedProject))) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;             
            }
            myProjectBean.setSelectedIndex("1");
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Le rôle a été changé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }
    
      
   
    /**
     * permet de supprimer l'utilisateur du projet
     */
    public void removeUserFromProject () {
        FacesMessage msg;
        
        if(nodeSelectedUser== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        UserHelper userHelper = new UserHelper();
        if(!userHelper.deleteRoleOnGroup(
                connect.getPoolConnexion(),
                nodeSelectedUser.getIdUser(),
                Integer.parseInt(selectedProject))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de suppression de l'utilisateur du projet !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'utilisateur a été supprimé du projet !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
    }    
    
    /**
     * permet d'ajouter un utilisateur existant au projet
     */
    public void addUserToProject () {
        FacesMessage msg;
        
        if(selectedUser == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        UserHelper userHelper = new UserHelper();
        if(!userHelper.addUserRoleOnGroup(
                connect.getPoolConnexion(),
                selectedUser.getIdUser(),
                Integer.parseInt(roleOfSelectedUser),
                Integer.parseInt(selectedProject))) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'utilisateur a été ajouté avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.resetListUsers();
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
            
        }
    }      
    
    
    public ArrayList<NodeUser> autoCompleteUser(String userName) {
        UserHelper userHelper = new UserHelper();
        ArrayList <NodeUser> nodeUsers = userHelper.searchUser(connect.getPoolConnexion(), userName);
        return nodeUsers;
    }    
    

    public NodeUser getNodeUser() {
        return nodeSelectedUser;
    }

    public void setNodeUser(NodeUser nodeUser) {
        this.nodeSelectedUser = nodeUser;
    }

    public String getRoleOfSelectedUser() {
        return roleOfSelectedUser;
    }

    public void setRoleOfSelectedUser(String roleOfSelectedUser) {
        this.roleOfSelectedUser = roleOfSelectedUser;
    }

    public ArrayList<NodeUserRoleGroup> getAllMyRoleProject() {
        return allMyRoleProject;
    }

    public void setAllMyRoleProject(ArrayList<NodeUserRoleGroup> allMyRoleProject) {
        this.allMyRoleProject = allMyRoleProject;
    }

    public NodeUser getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(NodeUser selectedUser) {
        this.selectedUser = selectedUser;
    }

    public boolean isLimitOnTheso() {
        return limitOnTheso;
    }

    public void setLimitOnTheso(boolean limitOnTheso) {
        this.limitOnTheso = limitOnTheso;
    }

    public ArrayList<NodeIdValue> getListThesoOfProject() {
        return listThesoOfProject;
    }

    public void setListThesoOfProject(ArrayList<NodeIdValue> listThesoOfProject) {
        this.listThesoOfProject = listThesoOfProject;
    }

    public List<String> getSelectedThesos() {
        return selectedThesos;
    }

    public void setSelectedThesos(List<String> selectedThesos) {
        this.selectedThesos = selectedThesos;
    }

    public NodeUserRole getSelectedNodeUserRole() {
        return selectedNodeUserRole;
    }

    public void setSelectedNodeUserRole(NodeUserRole selectedNodeUserRole) {
        this.selectedNodeUserRole = selectedNodeUserRole;
    }

    public ArrayList<NodeThesoRole> getListeLimitedThesoRoleForUser() {
        return listeLimitedThesoRoleForUser;
    }

    public void setListeLimitedThesoRoleForUser(ArrayList<NodeThesoRole> listeLimitedThesoRoleForUser) {
        this.listeLimitedThesoRoleForUser = listeLimitedThesoRoleForUser;
    }

    public ArrayList<NodeIdValue> getMyAuthorizedRolesLimited() {
        return myAuthorizedRolesLimited;
    }

    public void setMyAuthorizedRolesLimited(ArrayList<NodeIdValue> myAuthorizedRolesLimited) {
        this.myAuthorizedRolesLimited = myAuthorizedRolesLimited;
    }

    
}
