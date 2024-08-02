/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
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
@Named(value = "notationBean")
@jakarta.enterprise.context.SessionScoped

public class NotationBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private Tree tree;
    @Inject private SelectedTheso selectedTheso;
    
    private String notation;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        notation = null;
    }     
    
    public NotationBean() {
    }

    public void reset() {
        notation = conceptBean.getNodeConcept().getConcept().getNotation();
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour modifier Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter ou modifier la Notation
     * @param idTheso
     * @param idUser 
     */
    public void updateNotation(
            String idTheso,
            int idUser) {

        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        ConceptHelper conceptHelper = new ConceptHelper();
        
        if(!notation.isEmpty()) {
            if(conceptHelper.isNotationExist(connect.getPoolConnexion(),
                    idTheso,
                    notation)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La notation existe déjà dans le thésaurus !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }            
                return;
            }
        }
        
        if(!conceptHelper.updateNotation(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                idTheso,
                notation.trim())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }            
            return;
        } 

        conceptBean.getConcept(idTheso,
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "La notation a bien été modifiée");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        // mettre à jour le label dans l'arbre
        if(selectedTheso.isSortByNotation()) {
            if (tree.getSelectedNode() != null) {
                // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
                if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                        conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                    tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(), idTheso, conceptBean.getSelectedLang());
                }
                ((TreeNodeData) tree.getSelectedNode().getData()).setNotation(notation);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
                }
            }  
        }        
        
        
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
        reset();
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }
    
    
}
