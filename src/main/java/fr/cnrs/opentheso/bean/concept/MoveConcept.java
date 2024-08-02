/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "moveConcept")
@jakarta.enterprise.context.SessionScoped

public class MoveConcept implements Serializable {
    @Inject
    private Connect connect;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private CurrentUser currentUser;    
    @Inject
    private CandidatBean candidatBean;   
    
    private String idThesoFrom, idThesoTo;
    
    private String idConceptFrom;
    
    private NodeSearchMini nodeSearchSelected;
    
    private List<String> idConceptsToMove;

    public void initForCandidate(List idConcepts1, String idThesoFrom) {
        idConceptsToMove = new ArrayList<>();
        this.idConceptsToMove = idConcepts1;
        this.idThesoFrom = idThesoFrom;
    }
    
    public void initForConcept(String idConcept, String idThesoFrom) {
        idConceptsToMove = new ArrayList<>();        
        this.idConceptFrom = idConcept;
        ConceptHelper conceptHelper = new ConceptHelper();
        idConceptsToMove = conceptHelper.getIdsOfBranch(connect.getPoolConnexion(),
                idConcept, selectedTheso.getCurrentIdTheso());
        this.idThesoFrom = idThesoFrom;
        idThesoTo = null;
    }    
    
    public void action(){
        
    }

    /**
     * permet de déplacer des candidats d'un thésaurus vers un autre
     */
    public void moveConceptCA() {
        FacesMessage msg;
        if (StringUtils.isEmpty(idThesoTo) || StringUtils.isEmpty(idThesoFrom) || idConceptsToMove == null || idConceptsToMove.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        for (String idConcept : idConceptsToMove) {
            if(!conceptHelper.moveConceptToAnotherTheso(connect.getPoolConnexion(),
                    idConcept, idThesoFrom, idThesoTo, currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le déplacement a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);  
                return;
            }
            conceptHelper.updateDateOfConcept(connect.getPoolConnexion(), idThesoTo, idConcept,
                    currentUser.getNodeUser().getIdUser());  
            
            ///// insert DcTermsData to add contributor
            DcElementHelper dcElmentHelper = new DcElementHelper();
            dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                    new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                    idConcept, idThesoTo);
            /////////////// 
        }
        PrimeFaces pf = PrimeFaces.current();
        candidatBean.initCandidatModule();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("tabViewCandidat:panelCandidateList");
        }            
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le déplacement a réussi");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    /**
     * permet de déplacer des concepts d'un thésaurus vers un autre
     */
    public void moveConcept() {
        FacesMessage msg;
        if (StringUtils.isEmpty(idThesoTo) || StringUtils.isEmpty(idConceptFrom) || StringUtils.isEmpty(idThesoFrom) || idConceptsToMove == null || idConceptsToMove.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        GroupHelper groupHelper = new GroupHelper();
        ArrayList<String> lisIdGroup;
        String idArk;
        ArrayList<String> idConcepts = new ArrayList<>();
        
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), idThesoTo);
        for (String idConcept : idConceptsToMove) {
            if(!conceptHelper.moveConceptToAnotherTheso(connect.getPoolConnexion(),
                    idConcept, idThesoFrom, idThesoTo, currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Le déplacement a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);  
                return;
            }
            conceptHelper.updateDateOfConcept(connect.getPoolConnexion(), idThesoTo, idConcept,
                    currentUser.getNodeUser().getIdUser());
            
            ///// insert DcTermsData to add contributor
            DcElementHelper dcElmentHelper = new DcElementHelper();
            dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                    new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                    idConcept, idThesoTo);
            ///////////////            
            
            
            lisIdGroup = groupHelper.getListIdGroupOfConcept(connect.getPoolConnexion(), idThesoTo, idConcept);
            for (String idGroup : lisIdGroup) {
                groupHelper.deleteRelationConceptGroupConcept(connect.getPoolConnexion(), idGroup, idConcept, idThesoTo, currentUser.getNodeUser().getIdUser());                
            }
            idArk = conceptHelper.getIdArkOfConcept(connect.getPoolConnexion(), idConcept, idThesoTo);
            if(!StringUtils.isEmpty(idArk)) {
                idConcepts.clear();
                idConcepts.add(idConcept);
                conceptHelper.setNodePreference(nodePreference);
                conceptHelper.generateArkId(connect.getPoolConnexion(), idThesoTo, idConcepts, selectedTheso.getCurrentLang());
            }
        }
        
        RelationsHelper relationsHelper = new RelationsHelper();  
        
        // suppression des BT du concept de tête à déplacer 
        ArrayList<String> listBt = relationsHelper.getListIdBT(connect.getPoolConnexion(), idConceptFrom, idThesoTo);
        for (String idBt : listBt) {
            relationsHelper.deleteRelationBT(connect.getPoolConnexion(), idConceptFrom, idThesoTo, idBt, currentUser.getNodeUser().getIdUser());
        }
        
        if(nodeSearchSelected != null && !StringUtils.isEmpty(nodeSearchSelected.getIdConcept())) {
            //cas où le déplacement est vers un concept, on attache ce nouveau concept au concept cible
            relationsHelper.addRelationBT(connect.getPoolConnexion(), idConceptFrom, idThesoTo, nodeSearchSelected.getIdConcept(), currentUser.getNodeUser().getIdUser());
            conceptHelper.setNotTopConcept(connect.getPoolConnexion(), idConceptFrom, idThesoTo);
        } else {
            conceptHelper.setTopConcept(connect.getPoolConnexion(), idConceptFrom, idThesoTo);
        }
        
        try {
            selectedTheso.reloadSelectedTheso();
        } catch (IOException e) {
        }
      
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le déplacement a réussi");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    public List<NodeIdValue> getListThesoAsAdmin(){
        if(currentUser.getNodeUser() == null) return null;
        UserHelper userHelper = new UserHelper();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        List<String> authorizedThesoAsAdmin;
        if(currentUser.getNodeUser().isSuperAdmin()) {
            authorizedThesoAsAdmin = thesaurusHelper.getAllIdOfThesaurus(connect.getPoolConnexion(), true);
        } else {
            authorizedThesoAsAdmin = userHelper.getThesaurusOfUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser()); 
        }
        List<NodeIdValue> nodeIdValues = new ArrayList<>();
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        
        authorizedThesoAsAdmin.remove(selectedTheso.getCurrentIdTheso());
        for (String idTheso : authorizedThesoAsAdmin) {
            var nodeIdValue = NodeIdValue.builder()
                    .id(idTheso)
                    .value(idTheso)
                    .build();
            String idLang = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso);
            String title = thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(), idTheso, idLang);
            if(StringUtils.isEmpty(title))
                nodeIdValue.setValue("");
            else 
                nodeIdValue.setValue(title);
            nodeIdValues.add(nodeIdValue);
        }
        return nodeIdValues;
    }

    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        if (!StringUtils.isEmpty(idThesoTo)) {
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.getPoolConnexion(),
                    value,
                    selectedTheso.getCurrentLang(),
                    idThesoTo, true);
        }
        return liste;
    }    
    
    public String getIdThesoFrom() {
        return idThesoFrom;
    }

    public void setIdThesoFrom(String idThesoFrom) {
        this.idThesoFrom = idThesoFrom;
    }

    public String getIdThesoTo() {
        return idThesoTo;
    }

    public void setIdThesoTo(String idThesoTo) {
        this.idThesoTo = idThesoTo;
    }

    public List<String> getIdConceptsToMove() {
        return idConceptsToMove;
    }

    public void setIdConceptsToMove(List<String> idConceptsToMove) {
        this.idConceptsToMove = idConceptsToMove;
    }

    public String getIdConceptFrom() {
        return idConceptFrom;
    }

    public void setIdConceptFrom(String idConceptFrom) {
        this.idConceptFrom = idConceptFrom;
    }

    public NodeSearchMini getNodeSearchSelected() {
        return nodeSearchSelected;
    }

    public void setNodeSearchSelected(NodeSearchMini nodeSearchSelected) {
        this.nodeSearchSelected = nodeSearchSelected;
    }
    
}
