/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import java.io.IOException;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "deleteThesoBean")
@SessionScoped
public class DeleteThesoBean implements Serializable {
    
    @Inject 
    private Connect connect;
    
    @Inject 
    private SelectedTheso selectedTheso;
    
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    
    private String idThesoToDelete;
    private String valueOfThesoToDelelete;
    private boolean isDeleteOn;
    
    private boolean deletePerennialIdentifiers;
            
    private String currentIdTheso;
    /**
     * Creates a new instance of DeleteThesoBean
     */
    public DeleteThesoBean() {
        idThesoToDelete = null;
        valueOfThesoToDelelete = null;
        isDeleteOn = false;
        currentIdTheso = null;
        deletePerennialIdentifiers = false;
    }
    
    public void init() {
        idThesoToDelete = null;
        valueOfThesoToDelelete = null;
        isDeleteOn = false;     
        currentIdTheso = null;
        deletePerennialIdentifiers = false;
    }
    
    public void confirmDelete(NodeIdValue nodeTheso, String cucurrentIdTheso) throws IOException {
        this.idThesoToDelete = nodeTheso.getId();
        this.valueOfThesoToDelelete = nodeTheso.getValue();
        isDeleteOn = true;
        deletePerennialIdentifiers = false;
        // récupération de l'idTheso en cours
        this.currentIdTheso = cucurrentIdTheso;
    }
    
    /**
     * Permet de supprimer un thésaurus 
     */
    public void deleteTheso() throws IOException {
        if(idThesoToDelete == null) return;
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), idThesoToDelete);
        if(nodePreference != null) {
            // suppression des Identifiants Handle
            ConceptHelper conceptHelper = new ConceptHelper();
            conceptHelper.setNodePreference(nodePreference);
            if(deletePerennialIdentifiers) {
                conceptHelper.deleteAllIdHandle(connect.getPoolConnexion(), idThesoToDelete);
            }
        }
        FacesMessage msg;
        
        // supression des droits
        UserHelper userHelper = new UserHelper();
        try {
            try (Connection conn = connect.getPoolConnexion().getConnection()) {
                conn.setAutoCommit(false);
                if(!userHelper.deleteThesoFromGroup(conn, idThesoToDelete)) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur pendant la suppression !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    conn.rollback();
                    conn.commit();
                    return;
                }
                conn.commit();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DeleteThesoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // suppression complète du thésaurus        
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        if(!thesaurusHelper.deleteThesaurus(connect.getPoolConnexion(), idThesoToDelete)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur pendant la suppression !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        // vérification si le thésaurus supprimé est en cours de consultation, alors il faut nettoyer l'écran
        if(idThesoToDelete.equalsIgnoreCase(currentIdTheso)) {
            selectedTheso.setSelectedIdTheso("");
            selectedTheso.setSelectedLang(null);
            selectedTheso.setSelectedTheso();
            selectedTheso.setProjectIdSelected("-1");
            selectedTheso.setSelectedProject();
        }
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Thesaurus supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init();
        //viewEditionBean.init();
        roleOnThesoBean.showListTheso();
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }    
    }
    
    public void setThesaurusBeforRemove(String idThesoToDelete, String valueOfThesoToDelelete) {
        this.idThesoToDelete = idThesoToDelete;
        this.valueOfThesoToDelelete = valueOfThesoToDelelete;
    }

    public boolean isIsDeleteOn() {
        return isDeleteOn;
    }

    public void setIsDeleteOn(boolean isDeleteOn) {
        this.isDeleteOn = isDeleteOn;
    }

    public String getIdThesoToDelete() {
        return idThesoToDelete;
    }

    public void setIdThesoToDelete(String idThesoToDelete) {
        this.idThesoToDelete = idThesoToDelete;
    }

    public String getValueOfThesoToDelelete() {
        return valueOfThesoToDelelete;
    }

    public void setValueOfThesoToDelelete(String valueOfThesoToDelelete) {
        this.valueOfThesoToDelelete = valueOfThesoToDelelete;
    }

    public boolean isDeletePerennialIdentifiers() {
        return deletePerennialIdentifiers;
    }

    public void setDeletePerennialIdentifiers(boolean deletePerennialIdentifiers) {
        this.deletePerennialIdentifiers = deletePerennialIdentifiers;
    }


    
    
    
}
