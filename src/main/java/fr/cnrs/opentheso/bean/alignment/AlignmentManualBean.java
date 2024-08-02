/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.alignment;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentType;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "alignmentManualBean")
@SessionScoped
public class AlignmentManualBean implements Serializable {

    @Inject private Connect connect;
    @Inject private AlignmentBean alignmentBean;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;
    @Inject private CandidatBean candidatBean;
    
    private ArrayList<NodeAlignmentType> nodeAlignmentTypes;
    
    // pour l'aligenement manuel
    private String manualAlignmentSource;
    private String manualAlignmentUri;
    private int manualAlignmentType;
    
    @PreDestroy
    public void destroy(){
        clear();
        nodeAlignmentTypes = new AlignmentHelper().getAlignmentsType(connect.getPoolConnexion());
    }  
    public void clear(){
        if(nodeAlignmentTypes!= null){
            nodeAlignmentTypes.clear();
            nodeAlignmentTypes = null;
        }        
        manualAlignmentSource = null;
        manualAlignmentUri = null;
    }
    
    public AlignmentManualBean() {
    }

    public void reset() {
        nodeAlignmentTypes = new AlignmentHelper().getAlignmentsType(connect.getPoolConnexion());
        manualAlignmentSource = "";
        manualAlignmentUri = "";
        manualAlignmentType = -1;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    public void deleteAlignment(NodeAlignment nodeAlignment) {
        
        if(nodeAlignment == null) return;

        if(!new AlignmentHelper().deleteAlignment(connect.getPoolConnexion(),
                nodeAlignment.getId_alignement(),
                selectedTheso.getCurrentIdTheso())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());

        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "alignement supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void updateAlignement(){
        if(alignmentBean.getAlignementElementSelected() == null) return;
        
        AlignmentHelper alignmentHelper = new AlignmentHelper();

        FacesMessage msg;

        if(!alignmentHelper.updateAlignment(connect.getPoolConnexion(),
                alignmentBean.getAlignementElementSelected().getIdAlignment(),
                alignmentBean.getAlignementElementSelected().getConceptTarget(),
                alignmentBean.getAlignementElementSelected().getThesaurus_target(),
                alignmentBean.getAlignementElementSelected().getTargetUri(),
                alignmentBean.getAlignementElementSelected().getAlignement_id_type(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de mofication !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }
        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "alignement modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        alignmentBean.getIdsAndValues(selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void updateAlignementFromConceptInterface(){
        if(!new AlignmentHelper().updateAlignment(connect.getPoolConnexion(),
                alignmentBean.getAlignementElementSelected().getIdAlignment(),
                alignmentBean.getAlignementElementSelected().getConceptTarget(),
                alignmentBean.getAlignementElementSelected().getThesaurus_target(),
                alignmentBean.getAlignementElementSelected().getTargetUri(),
                alignmentBean.getAlignementElementSelected().getAlignement_id_type(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());

        alignmentBean.getIdsAndValues2(conceptView.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }
    
    public void addManualAlignement(String idConcept, boolean isFromConceptView){

    /*    if(manualAlignmentSource == null || manualAlignmentSource.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Veuillez saisir une valeur !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;             
        }*/
        if(manualAlignmentUri == null || manualAlignmentUri.isEmpty()){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Veuillez saisir une valeur  !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        } 

        if(!new StringPlus().urlValidator(manualAlignmentUri)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'URL n'est pas valide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        if(!new AlignmentHelper().addNewAlignment(
                connect.getPoolConnexion(),
                currentUser.getNodeUser().getIdUser(),
                "",
                manualAlignmentSource,
                manualAlignmentUri,
                manualAlignmentType,
                idConcept,
                selectedTheso.getCurrentIdTheso(),
                0)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de mofication !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }

        updateDateOfConcept(selectedTheso.getCurrentIdTheso(), idConcept, currentUser.getNodeUser().getIdUser());

        if (isFromConceptView) {
            conceptView.getConcept(selectedTheso.getCurrentIdTheso(), conceptView.getNodeConcept().getConcept().getIdConcept(),
                    conceptView.getSelectedLang());
        } else {
            candidatBean.getCandidatSelected().setAlignments(new AlignmentHelper()
                    .getAllAlignmentOfConcept(connect.getPoolConnexion(), idConcept, selectedTheso.getCurrentIdTheso()));
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        alignmentBean.getIdsAndValues(selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }    
    
    /**
     * 
     * @param idTheso
     * @param idConcept
     * @param idUser 
     */
    private void updateDateOfConcept(String idTheso, String idConcept, int idUser) {
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                idTheso,
                idConcept, idUser);      
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                idConcept, idTheso);
        ///////////////        
    }
    

    public ArrayList<NodeAlignmentType> getNodeAlignmentTypes() {
        return nodeAlignmentTypes;
    }

    public void setNodeAlignmentTypes(ArrayList<NodeAlignmentType> nodeAlignmentTypes) {
        this.nodeAlignmentTypes = nodeAlignmentTypes;
    }

    public String getManualAlignmentSource() {
        return manualAlignmentSource;
    }

    public void setManualAlignmentSource(String manualAlignmentSource) {
        this.manualAlignmentSource = manualAlignmentSource;
    }

    public String getManualAlignmentUri() {
        return manualAlignmentUri;
    }

    public void setManualAlignmentUri(String manualAlignmentUri) {
        this.manualAlignmentUri = manualAlignmentUri;
    }

    public int getManualAlignmentType() {
        return manualAlignmentType;
    }

    public void setManualAlignmentType(int manualAlignmentType) {
        this.manualAlignmentType = manualAlignmentType;
    }

}
