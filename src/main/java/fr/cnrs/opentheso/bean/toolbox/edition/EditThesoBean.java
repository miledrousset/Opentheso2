/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bdd.datas.Languages_iso639;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.AccessThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.connect.MenuBean;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import java.io.IOException;
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
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "editThesoBean")
@SessionScoped
public class EditThesoBean implements Serializable {
    @Inject private Connect connect;
    @Inject private CurrentUser currentUser;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private MenuBean menuBean;
    @Inject private ThesaurusMetadataAdd thesaurusMetadataAdd;

    private NodeLangTheso langSelected;
    private ArrayList<Languages_iso639> allLangs;
    private ArrayList<NodeLangTheso> languagesOfTheso;
    private boolean isPrivateTheso;
    
    private String title;
    private String selectedLang;
    private NodeIdValue nodeIdValueOfTheso;
    private String preferredLang;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(allLangs!= null){
            allLangs.clear();
            allLangs = null;
        }
        if(languagesOfTheso!= null){
            languagesOfTheso.clear();
            languagesOfTheso = null;
        }
        langSelected = null;
        title = null;
        selectedLang = null;        
        nodeIdValueOfTheso = null;
        preferredLang = null;        
    }      
    
    /**
     * Creates a new instance of DeleteThesoBean
     */
    public EditThesoBean() {
    }

    public void init(String idTheso) {
        nodeIdValueOfTheso = null;
        nodeIdValueOfTheso = new NodeIdValue();

        this.nodeIdValueOfTheso.setId(idTheso);

        LanguageHelper languageHelper = new LanguageHelper();
        // toutes les langues Iso
        allLangs = languageHelper.getAllLanguages(connect.getPoolConnexion());

        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        // les langues du thésaurus


        isPrivateTheso = thesaurusHelper.isThesoPrivate(
                connect.getPoolConnexion(),
                nodeIdValueOfTheso.getId());
        // langue par defaut
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(
                connect.getPoolConnexion(),
                nodeIdValueOfTheso.getId());
        preferredLang = nodePreference.getSourceLang();
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), nodeIdValueOfTheso.getId(), preferredLang);        
        selectedLang = null;
        langSelected = null;
        langSelected = new NodeLangTheso();
        title = "";
        try {
            menuBean.redirectToEditionPage();
        } catch (IOException ex) {
            Logger.getLogger(EditThesoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        /// initialisation des métadonnées pour le thésaurus 
        thesaurusMetadataAdd.init(nodeIdValueOfTheso.getId());        
    }    
    
    public void init(NodeIdValue nodeIdValueOfTheso) {
        this.nodeIdValueOfTheso = nodeIdValueOfTheso;

        LanguageHelper languageHelper = new LanguageHelper();
        // toutes les langues Iso
        allLangs = languageHelper.getAllLanguages(connect.getPoolConnexion());

        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        // les langues du thésaurus


        isPrivateTheso = thesaurusHelper.isThesoPrivate(
                connect.getPoolConnexion(),
                nodeIdValueOfTheso.getId());
        // langue par defaut
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(
                connect.getPoolConnexion(),
                nodeIdValueOfTheso.getId());
        preferredLang = nodePreference.getSourceLang();
        
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), nodeIdValueOfTheso.getId(), preferredLang);        
        selectedLang = null;
        langSelected = null;
        langSelected = new NodeLangTheso();
        title = "";
        /// initialisation des métadonnées pour le thésaurus 
        thesaurusMetadataAdd.init(nodeIdValueOfTheso.getId());        
    }

    private void reset() {
        selectedLang = null;
        title = "";
        preferredLang = null;
    }

    public void changeSourceLang(){
       FacesMessage msg;
        if (nodeIdValueOfTheso == null || nodeIdValueOfTheso.getId() == null || nodeIdValueOfTheso.getId().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Pas de thésaurus sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (preferredLang == null || preferredLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "la langue source est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        PreferencesHelper preferencesHelper = new PreferencesHelper();
        
        if (!preferencesHelper.setWorkLanguageOfTheso(
                connect.getPoolConnexion(), preferredLang, nodeIdValueOfTheso.getId())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la modification de la langue source !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Langue source modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init(nodeIdValueOfTheso);
    }
    
    /**
     * permet de changer le status du thésaurus entre public et privé
     */
    public void changeStatus() {
        AccessThesaurusHelper accessThesaurusHelper = new AccessThesaurusHelper();
        FacesMessage msg;
        if (!accessThesaurusHelper.updateVisibility(
                connect.getPoolConnexion(), nodeIdValueOfTheso.getId(), isPrivateTheso)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "La modification a échoué !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if(isPrivateTheso)
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le thésaurus est maintenant privé");
        else
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le thésaurus est maintenant public");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init(nodeIdValueOfTheso);
        roleOnThesoBean.showListTheso();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("toolBoxForm:idLangToModify");
        }
    }

    /**
     * Permet de supprimer un thésaurus
     */
    public void addNewLang() {
        FacesMessage msg;
        if (nodeIdValueOfTheso == null || nodeIdValueOfTheso.getId() == null || nodeIdValueOfTheso.getId().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Pas de thésaurus sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

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

        Connection conn;
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        try {
            conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);

            Thesaurus thesaurus = new Thesaurus();
            thesaurus.setCreator(currentUser.getNodeUser().getName());
            thesaurus.setContributor(currentUser.getNodeUser().getName());
            thesaurus.setId_thesaurus(nodeIdValueOfTheso.getId());
            thesaurus.setTitle(title);
            thesaurus.setLanguage(selectedLang);
            if (!thesaurusHelper.addThesaurusTraductionRollBack(conn, thesaurus)) {
                conn.rollback();
                conn.close();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant l'ajout de la langue !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(NewThesoBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Langue ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init(nodeIdValueOfTheso);
    }
    
    public void updateLang(NodeLangTheso NodeLangThesoSelected){
        FacesMessage msg;
        if (nodeIdValueOfTheso == null || nodeIdValueOfTheso.getId() == null || nodeIdValueOfTheso.getId().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Pas de thésaurus sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        if (NodeLangThesoSelected == null || NodeLangThesoSelected.getValue() == null || NodeLangThesoSelected.getValue().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Le label est obligatoire !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        Thesaurus thesaurus = new Thesaurus();
        thesaurus.setCreator(currentUser.getNodeUser().getName());
        thesaurus.setContributor(currentUser.getNodeUser().getName());
        thesaurus.setId_thesaurus(nodeIdValueOfTheso.getId());
        thesaurus.setTitle(langSelected.getLabelTheso());
        thesaurus.setLanguage(NodeLangThesoSelected.getCode());
        if (!new ThesaurusHelper().UpdateThesaurus(connect.getPoolConnexion(), thesaurus)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la modification !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        roleOnThesoBean.showListTheso();
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Langue modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        String sourceLang = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), nodeIdValueOfTheso.getId());
        
        languagesOfTheso = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), nodeIdValueOfTheso.getId(), sourceLang);
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:listLangThes");
        }
    }
    
    public void deleteLangFromTheso(String idLang){
        FacesMessage msg;
        if (nodeIdValueOfTheso == null || nodeIdValueOfTheso.getId() == null || nodeIdValueOfTheso.getId().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Pas de thésaurus sélectionné !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }        
        if (idLang == null || idLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Pas de langue sélectionnée !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        if (!thesaurusHelper.deleteThesaurusTraduction(
                connect.getPoolConnexion(),
                nodeIdValueOfTheso.getId(),
                idLang)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression de la langue !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Langue supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init(nodeIdValueOfTheso);
       
    }

    public NodeIdValue getNodeIdValueOfTheso() {
        return nodeIdValueOfTheso;
    }

    public void setNodeIdValueOfTheso(NodeIdValue nodeIdValueOfTheso) {
        this.nodeIdValueOfTheso = nodeIdValueOfTheso;
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

    public ArrayList<NodeLangTheso> getLanguagesOfTheso() {
        return languagesOfTheso;
    }

    public void setLanguagesOfTheso(ArrayList<NodeLangTheso> languagesOfTheso) {
        this.languagesOfTheso = languagesOfTheso;
    }

    public boolean isIsPrivateTheso() {
        return isPrivateTheso;
    }

    public void setIsPrivateTheso(boolean isPrivateTheso) {
        this.isPrivateTheso = isPrivateTheso;
    }

    public String getPreferredLang() {
        return preferredLang;
    }

    public void setPreferredLang(String preferredLang) {
        this.preferredLang = preferredLang;
    }

    public NodeLangTheso getLangSelected() {
        return langSelected;
    }

    public void setLangSelected(NodeLangTheso langSelected) {
        if(langSelected == null) 
            langSelected = new NodeLangTheso();
        this.langSelected = langSelected;
    }

}
