/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "conceptType")
@jakarta.enterprise.context.SessionScoped

public class ConceptType implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;

    private String type;
    private ArrayList<String> allTypes;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        type = null;
    }     
    
    public ConceptType() {
    }

    public void reset() {
        ConceptHelper conceptHelper = new ConceptHelper();
        type = conceptBean.getNodeConcept().getConcept().getConceptType();
        allTypes = conceptHelper.getAllTypeConcept(connect.getPoolConnexion());
        
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour modifier Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter ou modifier la Notation
     * @param idTheso
     * @param idConcept
     * @param idUser 
     */
    public void updateTypeOfConcept(
            String idTheso,
            String idConcept,
            int idUser) {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        ConceptHelper conceptHelper = new ConceptHelper();
        
        if(!conceptHelper.updateTypeOfConcept(connect.getPoolConnexion(), idConcept, idTheso, type.trim())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de BDD !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }            
            return;
        } 

        conceptBean.getConcept(idTheso,
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le type a bien été modifié");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
        reset();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    
    
}
