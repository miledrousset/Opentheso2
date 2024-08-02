/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "newProjectBean")
@SessionScoped
public class NewProjectBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    @Inject private CurrentUser currentUser;
 
    private String projectName;
    private ArrayList<NodeUserGroup> listeProjectOfUser;
            
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(listeProjectOfUser!= null){
            listeProjectOfUser.clear();
            listeProjectOfUser = null;
        }
        projectName = null;
    }    
    
    public NewProjectBean() {
    }
    
    
    /**
     * permet d'initialiser les variables 
     *
     */
    public void init() {
        projectName = null;
        
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            listeProjectOfUser = userHelper.getAllProject(connect.getPoolConnexion());
            return;
        }
        listeProjectOfUser = userHelper.getProjectsOfUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());        
    }   
    
    /**
     * permet de créer un nouveau projet de regroupement de thésaurus, 
     * si l'utilisateur est SuperAdmin, le projet n'aura pas d'utilisateur pas défaut,
     * si l'utilisateur est Admin, il aura les droits admin dessus par défaut
     */
    public void addNewProject(){
        FacesMessage msg;
        
        if(projectName== null || projectName.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        
        UserHelper userHelper = new UserHelper();
        
        if(userHelper.isUserGroupExist(
                connect.getPoolConnexion(),
                projectName)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Ce nom de projet existe déjà !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }        
        if(!userHelper.addNewProject(
                connect.getPoolConnexion(),
                projectName)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
        currentUser.initUserPermissions();
        // on vérifie si l'utilisateur en cours est un Admin 
        if(!currentUser.getNodeUser().isSuperAdmin()){
            if(currentUser.getAllAuthorizedProjectAsAdmin() != null && !currentUser.getAllAuthorizedProjectAsAdmin().isEmpty()) {
                // on donne le droit admin pour l'utilisateur courant sur ce groupe
                int projectId = userHelper.getThisProjectId(connect.getPoolConnexion(), projectName);
                if(projectId == -1) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur interne BDD !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    return;   
                }
                if(!userHelper.addUserRoleOnGroup(
                        connect.getPoolConnexion(),
                        currentUser.getNodeUser().getIdUser(),
                        2,
                        projectId)) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de création de rôle !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    return;             
                }
            }
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet créé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.init();
    }      

    public void updateProject(NodeUserGroup nodeUserGroup){
        FacesMessage msg;
        
        if(nodeUserGroup== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de projet sélectioné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   
        if(nodeUserGroup.getGroupName().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        UserHelper userHelper = new UserHelper();
        
        if(userHelper.isUserGroupExist(
                connect.getPoolConnexion(),
                nodeUserGroup.getGroupName())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Ce nom de projet existe déjà !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            init();
            return;             
        }          
        if(!userHelper.updateProject(
                connect.getPoolConnexion(),
                nodeUserGroup.getGroupName(),
                nodeUserGroup.getIdGroup())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de modification !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet modifié avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.init();
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }     
    }      
    
    /**
     * permet de supprimer un Groupe/projet
     *
     * @param nodeUserGroup
     */
    public void deleteProject(NodeUserGroup nodeUserGroup) {
        FacesMessage msg;
        UserHelper userHelper = new UserHelper();
        if (!userHelper.deleteProjectGroup(connect.getPoolConnexion(), nodeUserGroup.getIdGroup())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur :", nodeUserGroup.getGroupName()));
            return;
        }
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Suppression OK :", nodeUserGroup.getGroupName()));
        currentUser.initUserPermissions();
        myProjectBean.init();        
        init();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }        
    }    
    
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public ArrayList<NodeUserGroup> getListeProjectOfUser() {
        return listeProjectOfUser;
    }

    public void setListeProjectOfUser(ArrayList<NodeUserGroup> listeProjectOfUser) {
        this.listeProjectOfUser = listeProjectOfUser;
    }    
}
