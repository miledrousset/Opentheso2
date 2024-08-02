/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRole;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "myProjectBean")
@SessionScoped
public class MyProjectBean implements Serializable {

    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private CurrentUser currentUser;
    
    private ArrayList<NodeIdValue> listeThesoOfProject;
    private Map<String, String> listeGroupsOfUser;
    private ArrayList<NodeUserRole> listeUser; // la liste des utilisateur du groupe    
    
    private ArrayList<NodeUserRole> listeUserLimitedRole; // la liste des utilisateur du groupe avec des droits limités       
    
    private NodeUserRoleGroup nodeUserRoleOnThisGroup;
    private NodeUserRoleGroup nodeUserRoleSuperAdmin;   
    
    // liste des roles que l'utilisateur en cours peut attribuer
    private ArrayList<NodeIdValue> myAuthorizedRoles;   
    
    // Mon Role sur ce groupe  
    private NodeUserRoleGroup myRoleOnThisProject;     
    
    private String selectedProject;
    private String selectedProjectName;    
    private String selectedIndex;
    

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(listeThesoOfProject!= null){
            listeThesoOfProject.clear();
            listeThesoOfProject = null;
        }
        if(listeGroupsOfUser!= null){
            listeGroupsOfUser.clear();
            listeGroupsOfUser = null;
        }
        if(listeUser!= null){
            listeUser.clear();
            listeUser = null;
        }
        
        if(listeUserLimitedRole != null){
            listeUserLimitedRole.clear();
            listeUserLimitedRole = null;
        }
        
        if(myAuthorizedRoles!= null){
            myAuthorizedRoles.clear();
            myAuthorizedRoles = null;
        }        
        nodeUserRoleOnThisGroup = null;
        nodeUserRoleSuperAdmin = null;
        myRoleOnThisProject = null;  
        selectedProject = null;  
        selectedProjectName = null;  
        selectedIndex = "0";
    }  
    
    public MyProjectBean() {
    }

    public void init() {
        listeThesoOfProject = null;
        listeGroupsOfUser = null;        
        selectedProject = null;
        selectedProjectName = null;
        myRoleOnThisProject = null;
        myAuthorizedRoles = null;
        selectedIndex = "0";
      
        getGroupsOfUser();
        getListThesoByGroup();
    }
    
    private void initMyAuthorizedRoleOnThisGroup(){
        if(selectedProject.isEmpty()) return;
        UserHelper userHelper = new UserHelper();        
        myRoleOnThisProject = userHelper.getUserRoleOnThisGroup(
                connect.getPoolConnexion(),
                currentUser.getNodeUser().getIdUser(),
                Integer.parseInt(selectedProject));
    }    
    
    /**
     * permet de récupérer la liste des roles autorisés pour un utilisateur
     * c'est la liste des roles qu'il aura le droit d'attribuer aux nouveaux utilisateurs
     */
    private void initAuthorizedRoles() {
        int idRoleFrom = 4;
        if (currentUser.getNodeUser().isSuperAdmin()) {
            idRoleFrom = 1; // l'utilisateur est SuperAdmin
        } else {
            if (myRoleOnThisProject == null) {
                return;
            }
            if (myRoleOnThisProject.isIsAdmin()) {
                idRoleFrom = 2; // l'utilisateur est Admin            
            }
            if (myRoleOnThisProject.isIsManager()) {
                idRoleFrom = 3; // l'utilisateur est Manager            
            }
            if (myRoleOnThisProject.isIsContributor()) {
                idRoleFrom = 4; // l'utilisateur est Contributeur / user       
            }
        }
 //       roleAdded = idRoleFrom;
        UserHelper userHelper = new UserHelper();
        myAuthorizedRoles = userHelper.getMyAuthorizedRoles(connect.getPoolConnexion(),
                idRoleFrom);
    }     

    /**
     * permet de récupérer la liste des groupes/projets d'un utilisateur #MR
     */
    private void getGroupsOfUser() {
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            listeGroupsOfUser = userHelper.getAllGroups(connect.getPoolConnexion());
            return;
        }
        listeGroupsOfUser = userHelper.getGroupsOfUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
    }    
    
    public void setLists() {
        listeUser = null;
        listeUserLimitedRole = null;
        listeThesoOfProject = null;
        nodeUserRoleOnThisGroup = null;
        nodeUserRoleSuperAdmin = null;
        getListThesoByGroup();
        listUsersByGroup();
        listUsersLimitedRoleByGroup();
        initMyAuthorizedRoleOnThisGroup();
        initAuthorizedRoles();        
    }    

    /**
     * appel après la modifcation d'un rôle pour un utilisateur
     */
    public void resetListUsers(){
        listUsersByGroup();
    }
    
    /**
     * appel après la modifcation d'un rôle limité pour un utilisateur
     */
    public void resetListLimitedRoleUsers(){
        listUsersLimitedRoleByGroup(); 
    }    
    
   
    
    /**
     * retourne la liste des thésaurus par groupe
     */
   
    private void getListThesoByGroup(){
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }
        
        int idGroup = Integer.parseInt(selectedProject);
        
        listeThesoOfProject = new UserHelper().getThesaurusOfProject(connect.getPoolConnexion(), idGroup,
                connect.getWorkLanguage());
    } 
    
    /**
     * permet de récupérer la liste des utilisateurs suivants les options choisies
     */
    private void listUsersByGroup(){
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }
        UserHelper userHelper = new UserHelper();
        int idGroup = Integer.parseInt(selectedProject);
        setUserRoleOnThisGroup();
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            listeUser = userHelper.getUsersRolesByGroup(connect.getPoolConnexion(),
                    idGroup, nodeUserRoleSuperAdmin.getIdRole());
        } else {
            if (nodeUserRoleOnThisGroup != null) {
                listeUser = userHelper.getUsersRolesByGroup(connect.getPoolConnexion(),
                        idGroup, nodeUserRoleOnThisGroup.getIdRole());
            } else {
                if (listeUser != null) {
                    listeUser.clear(); //cas où on supprime l'utilisateur en cours
                }
            }
        }
        listUsersLimitedRoleByGroup();
    }
    
     
    
    /**
     * permet de récupérer la liste des utilisateurs et les rôles sur les thésaurus du projet
     */
    private void listUsersLimitedRoleByGroup(){
        if (selectedProject == null || selectedProject.isEmpty()) {
            return;
        }
        UserHelper userHelper = new UserHelper();
        int idGroup = Integer.parseInt(selectedProject);
//        setUserRoleOnThisGroup();
        listeUserLimitedRole = userHelper.getAllUsersRolesLimitedByTheso(connect.getPoolConnexion(),
                idGroup);
    /*    if (listeUserLimitedRole != null) {
            listeUserLimitedRole.clear(); //cas où on supprime l'utilisateur en cours
        }*/
    }    
    
    /**
     * setting du role de l'utilisateur sur le group séléctionné
     *
     * #MR
     *
     * @return
     */
    private void setUserRoleOnThisGroup() {
        if (selectedProject == null) {
            return;
        }
        if (selectedProject.isEmpty()) {
            return;
        }
        int idGroup = Integer.parseInt(selectedProject);        
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            nodeUserRoleSuperAdmin = userHelper.getUserRoleForSuperAdmin(
                    connect.getPoolConnexion());
            return;
        }
        nodeUserRoleOnThisGroup = userHelper.getUserRoleOnThisGroup(
                connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser(), idGroup);
    } 
    
    
    /**
     * permet de savoir si l'utilisateur est Admin sur ce Groupe / SuperAdmin
     *
     * @return
     */
    public boolean isAdminOnThisGroup() {
        if (currentUser.getNodeUser().isSuperAdmin()) {
            return true;
        }
        if (selectedProject == null) {
            return false;
        }
        if (selectedProject.isEmpty()) {
            return false;
        }
        int idGroup = Integer.parseInt(selectedProject);
        UserHelper userHelper = new UserHelper();
        return userHelper.isAdminOnThisGroup(connect.getPoolConnexion(),
                currentUser.getNodeUser().getIdUser(), idGroup);
    }
    
    public String getSelectedProjectName() {
        UserHelper userHelper = new UserHelper();
        if(selectedProject != null) 
            if(!selectedProject.isEmpty())
                selectedProjectName = userHelper.getGroupName(connect.getPoolConnexion(), Integer.parseInt(selectedProject));
            else
                selectedProjectName = selectedProject;
        return selectedProjectName;
    }    

    public ArrayList<NodeIdValue> getListeThesoOfProject() {
        return listeThesoOfProject;
    }

    public void setListeThesoOfProject(ArrayList<NodeIdValue> listeThesoOfProject) {
        this.listeThesoOfProject = listeThesoOfProject;
    }



    public String getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProject = selectedProject;
    }



    public Map<String, String> getListeGroupsOfUser() {
        return listeGroupsOfUser;
    }

    public void setListeGroupsOfUser(Map<String, String> listeGroupsOfUser) {
        this.listeGroupsOfUser = listeGroupsOfUser;
    }

    public ArrayList<NodeUserRole> getListeUser() {
        return listeUser;
    }

    public void setListeUser(ArrayList<NodeUserRole> listeUser) {
        this.listeUser = listeUser;
    }

    public ArrayList<NodeIdValue> getMyAuthorizedRoles() {
        return myAuthorizedRoles;
    }

    public void setMyAuthorizedRoles(ArrayList<NodeIdValue> myAuthorizedRoles) {
        this.myAuthorizedRoles = myAuthorizedRoles;
    }

    public NodeUserRoleGroup getMyRoleOnThisProject() {
        return myRoleOnThisProject;
    }

    public void setMyRoleOnThisProject(NodeUserRoleGroup myRoleOnThisProject) {
        this.myRoleOnThisProject = myRoleOnThisProject;
    }

    public ArrayList<NodeUserRole> getListeUserLimitedRole() {
        return listeUserLimitedRole;
    }

    public void setListeUserLimitedRole(ArrayList<NodeUserRole> listeUserLimitedRole) {
        this.listeUserLimitedRole = listeUserLimitedRole;
    }

    public String getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(String selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
 
    
    
}
