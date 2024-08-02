/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
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
@Named(value = "moveThesoToProjectBean")
@SessionScoped
public class MoveThesoToProjectBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    @Inject private CurrentUser currentUser;
    @Inject private SuperAdminBean superAdminBean;
    
    private NodeIdValue selectedThesoToMove;
    private String currentProject;
    private NodeUserGroup newProject;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        selectedThesoToMove = null;
        currentProject = null;
        newProject = null;      
    }      
            
    public MoveThesoToProjectBean() {
    }
    
    
    /**
     * permet d'initialiser les variables 
     *
     * @param selectedThesoToMove
     * @param currentProject
     */
    public void setTheso(NodeIdValue selectedThesoToMove, String currentProject) {
        this.selectedThesoToMove = selectedThesoToMove;
        this.currentProject = currentProject;
        newProject = null; 
    }   
    
    public void setThesoSuperAdmin(String idTheso, String thesoName, String currentProject) {
        selectedThesoToMove = new NodeIdValue();
        selectedThesoToMove.setId(idTheso);
        selectedThesoToMove.setValue(thesoName);

        this.currentProject = currentProject;
        newProject = null; 
    }       
    
    public ArrayList<NodeUserGroup> autoCompleteProject(String projectName) {
        UserHelper userHelper = new UserHelper();
        ArrayList<NodeUserGroup> nodeProjects = null;
        if(currentUser.getNodeUser().isSuperAdmin()) {
            nodeProjects = userHelper.searchAllProject(
                    connect.getPoolConnexion(),
                    projectName);            
        } else {
            nodeProjects = userHelper.searchMyProject(
                    connect.getPoolConnexion(),
                    currentUser.getNodeUser().getIdUser(),
                    projectName);
        }
        return nodeProjects;
    }        

    public void moveThesoToProject(){
        FacesMessage msg;
        
        if(newProject== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de projet sélectioné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   

        UserHelper userHelper = new UserHelper();
        if(!userHelper.moveThesoToGroup(
                connect.getPoolConnexion(),
                selectedThesoToMove.getId(),
                Integer.parseInt(currentProject),
                newProject.getIdGroup() )){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de déplacement !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet déplacé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        myProjectBean.init();

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:contenu");
        }
    }
    
    public void moveThesoToProjectSA(){
        FacesMessage msg;
        
        if(newProject== null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de projet sélectioné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   

        UserHelper userHelper = new UserHelper();
        if(!userHelper.moveThesoToGroup(
                connect.getPoolConnexion(),
                selectedThesoToMove.getId(),
                Integer.parseInt(currentProject),
                newProject.getIdGroup() )){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur de déplacement !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
               
             

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Projet déplacé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        superAdminBean.init();
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }      
    

    public NodeUserGroup getNewProject() {
        return newProject;
    }

    public void setNewProject(NodeUserGroup newProject) {
        this.newProject = newProject;
    }



    public NodeIdValue getSelectedThesoToMove() {
        return selectedThesoToMove;
    }

    public void setSelectedThesoToMove(NodeIdValue selectedThesoToMove) {
        this.selectedThesoToMove = selectedThesoToMove;
    }
    

    
    
}
