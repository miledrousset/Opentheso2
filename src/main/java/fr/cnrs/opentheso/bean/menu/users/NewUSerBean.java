/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.menu.users;

import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.profile.MyProjectBean;
import fr.cnrs.opentheso.bean.profile.SuperAdminBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import liquibase.util.StringUtil;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "newUSerBean")
@SessionScoped
public class NewUSerBean implements Serializable {
    @Inject private Connect connect;
    @Inject private MyProjectBean myProjectBean;
    @Inject private SuperAdminBean superAdminBean;
    private NodeUser nodeUser;
    private String passWord1;
    private String passWord2; 
    private String selectedProject;
    private String selectedRole;
    private ArrayList<NodeUserGroup> nodeAllProjects;
    private ArrayList<NodeUserRoleGroup> nodeAllRoles; 
    
    private String name;
    private boolean limitOnTheso;
    private ArrayList<NodeIdValue> listThesoOfProject;
    private List<String> selectedThesos;
    
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        nodeUser = null;
        passWord1 = null;
        passWord2 = null;
        selectedProject = null;
        selectedRole = null;    
        limitOnTheso = false;    
        selectedThesos = null;
    }
    
    
    /**
     * permet d'initialiser les variables
     *
     * @param selectedProject
     */
    public void init(String selectedProject) {
        nodeUser = new NodeUser();
        passWord1 = null;
        passWord2 = null;
        this.selectedProject = selectedProject;
        selectedRole = null;
        limitOnTheso = false; 
        listThesoOfProject = null;
        selectedThesos = null;
    }   
    
    public void initForSuperAdmin() {
        nodeUser = new NodeUser();
        passWord1 = null;
        passWord2 = null;
        UserHelper userHelper = new UserHelper();
        nodeAllProjects = userHelper.getAllProject(connect.getPoolConnexion());
        nodeAllRoles = userHelper.getAllRole(connect.getPoolConnexion());
        
        if(nodeAllProjects != null && !nodeAllProjects.isEmpty())
            selectedProject = "" + nodeAllProjects.get(0).getIdGroup();
        else
            selectedProject = null;
        setSelectedRole(null);
        selectedRole = null; 
        limitOnTheso = false; 
        listThesoOfProject = null;
        selectedThesos = null;
    }       
    
    public void addNewUserBySuperAdmin(){
        FacesMessage msg;
        
        if(nodeUser== null ) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur à ajouter !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }

        if(nodeUser.getName().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Le pseudo est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }        
        
        if(passWord1 == null || passWord1.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un mot de passe est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        if(passWord2 == null || passWord2.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un mot de passe est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   
        if(!passWord1.equals(passWord2)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Mot de passe non identique !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        } 
        
        
        UserHelper userHelper = new UserHelper();
        if(userHelper.isMailExist(connect.getPoolConnexion(), nodeUser.getMail())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Email existe déjà !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
        nodeUser.setName(nodeUser.getName().trim());
        if(userHelper.isPseudoExist(connect.getPoolConnexion(), nodeUser.getName())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pseudo existe déjà !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }        
        
        if(StringUtil.isEmpty(selectedRole)) {
            nodeUser.setSuperAdmin(false);
            selectedProject = null;
        } else {
            try {
                int role = Integer.parseInt(selectedRole);
                if(role == 1)
                    nodeUser.setSuperAdmin(true);
            } catch (Exception e) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Role non reconnu !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }


        if(!userHelper.addUser(
                connect.getPoolConnexion(),
                nodeUser.getName(),
                nodeUser.getMail(),
                MD5Password.getEncodedPassword(passWord1),
                nodeUser.isSuperAdmin(),
                nodeUser.isAlertMail())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création de l'utilisateur !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
        int idUser = userHelper.getIdUser(connect.getPoolConnexion(), nodeUser.getName(), MD5Password.getEncodedPassword(passWord1));
        if(idUser == -1) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création de l'utilisateur !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;                
        }

        if( (selectedProject != null) && (!selectedProject.isEmpty()) ){
            if((selectedRole != null) && (!selectedRole.isEmpty()))
                
                // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
                if(limitOnTheso) {
                    if(!userHelper.addUserRoleOnTheso(connect.getPoolConnexion(), idUser, Integer.parseInt(selectedRole), Integer.parseInt(selectedProject), selectedThesos)){
                        return;
                    }
                } else {
                    if(!userHelper.addUserRoleOnGroup(
                            connect.getPoolConnexion(),
                            idUser,
                            Integer.parseInt(selectedRole),
                            Integer.parseInt(selectedProject))) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant l'ajout des droits !!!");
                        FacesContext.getCurrentInstance().addMessage(null, msg);
                        return;                       
                    }
                }
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Utilisateur créé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
        
        superAdminBean.init();
        
        PrimeFaces.current().executeScript("PF('addNewUser2').hide();");
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
            
        }

    }     
    
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
    
    public void addUser(){
        FacesMessage msg;
        
        if(nodeUser== null ) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas d'utilisateur à ajouter !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        
        if(nodeUser.getName().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Le pseudo est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }        
        
        if(passWord1 == null || passWord1.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un mot de passe est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        if(passWord2 == null || passWord2.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Un mot de passe est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }   
        if(!passWord1.equals(passWord2)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Mot de passe non identique !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        } 
        
        
        UserHelper userHelper = new UserHelper();
        if(userHelper.isMailExist(connect.getPoolConnexion(), nodeUser.getMail())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Email existe déjà !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
        nodeUser.setName(nodeUser.getName().trim());
        if(userHelper.isPseudoExist(connect.getPoolConnexion(), nodeUser.getName())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pseudo existe déjà !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }        
        
        if(StringUtil.isEmpty(selectedRole)) {
            nodeUser.setSuperAdmin(false);
            selectedProject = null;
        } else {
            try {
                int role = Integer.parseInt(selectedRole);
                if(role == 1)
                    nodeUser.setSuperAdmin(true);
            } catch (Exception e) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Role non reconnu !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }        
        
        if(!userHelper.addUser(
                connect.getPoolConnexion(),
                nodeUser.getName(),
                nodeUser.getMail(),
                MD5Password.getEncodedPassword(passWord1),
                 nodeUser.isSuperAdmin(),
                nodeUser.isAlertMail())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création de l'utilisateur !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }
        int idUser = userHelper.getIdUser(connect.getPoolConnexion(), nodeUser.getName(), MD5Password.getEncodedPassword(passWord1));
        if(idUser == -1) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création de l'utilisateur !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;                
        }

        if( (selectedProject != null) && (!selectedProject.isEmpty()) ){
            if((selectedRole != null) && (!selectedRole.isEmpty()))
                
                // contrôle si le role est uniquement sur une liste des thésaurus ou le projet entier 
                if(limitOnTheso) {
                    if(!userHelper.addUserRoleOnTheso(connect.getPoolConnexion(), idUser, Integer.parseInt(selectedRole), Integer.parseInt(selectedProject), selectedThesos)){
                        return;
                    }
                } else {                
                    if(!userHelper.addUserRoleOnGroup(
                            connect.getPoolConnexion(),
                            idUser,
                            Integer.parseInt(selectedRole),
                            Integer.parseInt(selectedProject))) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant l'ajout des droits !!!");
                        FacesContext.getCurrentInstance().addMessage(null, msg);
                        return;                       
                    }
                }
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Utilisateur créé avec succès !!!");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
        
        myProjectBean.setLists();
        
        PrimeFaces.current().executeScript("PF('newUserForProject').hide();");
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }
    }      

    public NodeUser getNodeUser() {
        return nodeUser;
    }

    public void setNodeUser(NodeUser nodeUser) {
        this.nodeUser = nodeUser;
    }

    public String getPassWord1() {
        return passWord1;
    }

    public void setPassWord1(String passWord1) {
        this.passWord1 = passWord1;
    }

    public String getPassWord2() {
        return passWord2;
    }

    public void setPassWord2(String passWord2) {
        this.passWord2 = passWord2;
    }

    public String getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProject = selectedProject;
    }

    public String getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(String selectedRole) {
        this.selectedRole = selectedRole;
    }
    
    
    ////// For superAdmin

    public ArrayList<NodeUserGroup> getNodeAllProjects() {
        return nodeAllProjects;
    }

    public void setNodeAllProjects(ArrayList<NodeUserGroup> nodeAllProjects) {
        this.nodeAllProjects = nodeAllProjects;
    }

    public ArrayList<NodeUserRoleGroup> getNodeAllRoles() {
        return nodeAllRoles;
    }

    public void setNodeAllRoles(ArrayList<NodeUserRoleGroup> nodeAllRoles) {
        this.nodeAllRoles = nodeAllRoles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    
    
    
    
    
}
