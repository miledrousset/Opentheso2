/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bdd.datas.Languages_iso639;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "newThesoBean")
@SessionScoped
public class NewThesoBean implements Serializable {

    @Inject private Connect connect;
    @Inject private CurrentUser currentUser;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private ViewEditionBean viewEditionBean;

    private String title;
    private ArrayList<Languages_iso639> allLangs;
    private String selectedLang;

    private ArrayList<NodeUserGroup> nodeProjects;
    private String selectedProject;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(allLangs!= null){
            allLangs.clear();
            allLangs = null;
        }
        if(nodeProjects!= null){
            nodeProjects.clear();
            nodeProjects = null;
        }
        title = null;
        selectedLang = null;        
        selectedProject = null;
    }      
    
    /**
     * Creates a new instance of DeleteThesoBean
     */
    public NewThesoBean() {
    }

    public void init() {
        LanguageHelper languageHelper = new LanguageHelper();
        allLangs = languageHelper.getAllLanguages(connect.getPoolConnexion());
        selectedLang = null;
        selectedProject = "";
        title = "";
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeProjects = userHelper.getAllProject(connect.getPoolConnexion());
        } else {
            nodeProjects = userHelper.getProjectsOfUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
            for (NodeUserGroup nodeUserProject : nodeProjects) {
                selectedProject = "" + nodeUserProject.getIdGroup();
            }
        }
    }

    /**
     * Permet de supprimer un thésaurus
     */
    public void addNewTheso() {
        String idNewTheso = null;
        FacesMessage msg;

        if (title == null || title.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "le label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "la langue est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        int idProject = -1;
        try {
            if (!StringUtils.isEmpty(selectedProject)) {
                idProject = Integer.parseInt(selectedProject);
            }
        } catch (NumberFormatException e) {
        }
        Connection conn;
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        try {
            conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            // création du thésaurus
            idNewTheso = thesaurusHelper.addThesaurusRollBack(conn, "", false);
            if(idNewTheso == null) {
                conn.rollback();
                conn.close();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            
            Thesaurus thesaurus = new Thesaurus();
            thesaurus.setCreator(currentUser.getNodeUser().getName());
            thesaurus.setContributor(currentUser.getNodeUser().getName());            
            thesaurus.setId_thesaurus(idNewTheso);
            thesaurus.setTitle(title);
            thesaurus.setLanguage(selectedLang);
            if (!thesaurusHelper.addThesaurusTraductionRollBack(conn, thesaurus)) {
                conn.rollback();
                conn.close();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            // ajouter le thésaurus dans le group de l'utilisateur
            if (idProject != -1) { // si le groupeUser = - 1, c'est le cas d'un SuperAdmin, alors on n'intègre pas le thésaurus dans un groupUser
                UserHelper userHelper = new UserHelper();
                if (!userHelper.addThesoToGroup(conn, idNewTheso, idProject)) {
                    conn.rollback();
                    conn.close();
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la création !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    return;
                }
            }            
            conn.commit();
            conn.close();            
        } catch (SQLException ex) {
            Logger.getLogger(NewThesoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        

        // écriture des préférences en utilisant le thésaurus en cours pour duppliquer les infos
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = roleOnThesoBean.getNodePreference();
        if (nodePreference == null) {
            preferencesHelper.initPreferences(
                    connect.getPoolConnexion(),
                    idNewTheso,
                    selectedLang);
        } else {
            nodePreference.setPreferredName(title);
            nodePreference.setSourceLang(selectedLang);
            preferencesHelper.addPreference(//updateAllPreferenceUser(
                    connect.getPoolConnexion(),
                    nodePreference, idNewTheso);
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "thesaurus ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init();
        roleOnThesoBean.showListTheso();
        viewEditionBean.init();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formMenu:idListTheso");
            pf.ajax().update("toolBoxForm");
            pf.ajax().update("toolBoxForm:listThesoForm");
            pf.ajax().update("messageIndex");           
        }  
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<Languages_iso639> getAllLangs() {
        return allLangs;
    }

    public void setAllLangs(ArrayList<Languages_iso639> allLangs) {
        this.allLangs = allLangs;
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public ArrayList<NodeUserGroup> getNodeProjects() {
        return nodeProjects;
    }

    public void setNodeProjects(ArrayList<NodeUserGroup> nodeProjects) {
        this.nodeProjects = nodeProjects;
    }

    public String getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(String selectedProject) {
        this.selectedProject = selectedProject;
    }

}
