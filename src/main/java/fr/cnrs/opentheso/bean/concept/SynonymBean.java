/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "synonymBean")
@SessionScoped
public class SynonymBean implements Serializable {
    @Inject private Connect connect;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private PropositionBean propositionBean;
    @Inject private CurrentUser currentUser;     

    private String selectedLang;
    private String selectedValue;
    private ArrayList<NodeLangTheso> nodeLangs;
    private ArrayList<NodeEM> nodeEMs;
    private ArrayList<NodeEM> nodeEMsForEdit;
    private boolean isHidden;
    private boolean duplicate;
    private String value;
    // pour garder la valeur sélectionnée pour forcer une modfication en doublon (
    private NodeEM nodeEM;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeLangs != null) {
            nodeLangs.clear();
            nodeLangs = null;
        }
        if (nodeEMs != null) {
            nodeEMs.clear();
            nodeEMs = null;
        }
        if (nodeEMsForEdit != null) {
            nodeEMsForEdit.clear();
            nodeEMsForEdit = null;
        }
        selectedLang = null;
        selectedValue = null;
        value = null;
        nodeEM = null;
    }

    public void reset() {
        isHidden = false;
        selectedLang = conceptBean.getSelectedLang();
        nodeLangs = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(connect.getPoolConnexion(), 
                selectedTheso.getCurrentIdTheso(), selectedLang);        

        nodeEMs = conceptBean.getNodeConcept().getNodeEM();
        
        prepareNodeEMForEdit();
        
        value = "";
        duplicate = false;
        this.nodeEM = null;
        
        System.out.println(">> " + nodeEMsForEdit.size());
    }

    private void init() {
        value = "";
        duplicate = false;
        this.nodeEM = null;
    }
   
    public void prepareNodeEMForEdit() {
        nodeEMsForEdit = new ArrayList<>();
        for (NodeEM nodeEM1 : conceptBean.getNodeConcept().getNodeEM()) {
            nodeEM1.setOldValue(nodeEM1.getLexical_value());
            nodeEM1.setOldHiden(nodeEM1.isHiden());
            nodeEMsForEdit.add(nodeEM1);
        }
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter une nouvelle traduction au concept
     *
     * @param idUser
     */
    public void addNewSynonym(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        duplicate = false;
        if (value == null || value.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Une valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Pas de langue choisie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        TermHelper termHelper = new TermHelper();
        if (termHelper.isTermExist(connect.getPoolConnexion(),
                value,
                selectedTheso.getCurrentIdTheso(),
                selectedLang)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            duplicate = true;
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                value,
                selectedTheso.getCurrentIdTheso(),
                selectedLang)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            duplicate = true;
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        addForced(idUser);
    }

    /**
     * permet d'ajouter une nouvelle traduction au concept
     */
    public void addPropSynonym() {
        FacesMessage msg;
        if (value == null || value.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Une valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (selectedLang == null || selectedLang.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Pas de langue choisie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        TermHelper termHelper = new TermHelper();
        if (termHelper.isTermExist(connect.getPoolConnexion(), value,
                selectedTheso.getCurrentIdTheso(), conceptBean.getSelectedLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (termHelper.isAltLabelExist(connect.getPoolConnexion(), value,
                selectedTheso.getCurrentIdTheso(), conceptBean.getSelectedLang())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (CollectionUtils.isEmpty(propositionBean.getProposition().getSynonymsProp())) {
            propositionBean.getProposition().setSynonymsProp(new ArrayList<>());
         }
        
        for (SynonymPropBean synonym : propositionBean.getProposition().getSynonymsProp()) {
            if (synonym.getLexical_value().equalsIgnoreCase(value)
                    && synonym.getLang().equalsIgnoreCase(selectedLang)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }
        }

        SynonymPropBean synonymPropBean = new SynonymPropBean();
        synonymPropBean.setToAdd(true);
        synonymPropBean.setHiden(isHidden);
        synonymPropBean.setLang(selectedLang);
        synonymPropBean.setLexical_value(value);
        synonymPropBean.setOldValue(value);
        propositionBean.getProposition().getSynonymsProp().add(synonymPropBean);
        propositionBean.setVarianteAccepted(true);
    }

    /**
     * permet d'ajouter un synonyme sans controler le nom en doublon
     *
     * @param idUser
     */
    public void addForced(int idUser) {
        // pour la compatibilité avec les anciennes versions
        String use = "USE";
        if (isHidden) {
            use = "Hidden";
        }

        FacesMessage msg;
        TermHelper termHelper = new TermHelper();
        PrimeFaces pf = PrimeFaces.current();
        String idTerm = new TermHelper().getIdTermOfConcept(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        if (!termHelper.addNonPreferredTerm(connect.getPoolConnexion(),
                idTerm,
                value,
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                "",
                use,
                isHidden,
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La création a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            init();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab:viewTabConcept:addSynonymForm");
            }
            return;
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Synonyme ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        init();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("containerIndex:formLeftTab");
        }
    }

    /**
     * permet de modifier un synonyme
     *
     * @param nodeEMLocal
     * @param idUser
     */
    public void updateSynonym(NodeEM nodeEMLocal, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        TermHelper termHelper = new TermHelper();

        if (nodeEMLocal == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        // save de la valeur pour une modification forcée
        this.nodeEM = nodeEMLocal;

        if (!nodeEMLocal.getOldValue().equals(nodeEMLocal.getLexical_value())) {
            if (termHelper.isTermExist(connect.getPoolConnexion(),
                    nodeEMLocal.getLexical_value(),
                    selectedTheso.getCurrentIdTheso(),
                    nodeEMLocal.getLang())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                duplicate = true;
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
            if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                    nodeEMLocal.getLexical_value(),
                    selectedTheso.getCurrentIdTheso(),
                    nodeEMLocal.getLang())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                duplicate = true;
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
            updateSynonymForced(idUser);
        } else {
            if (nodeEMLocal.isOldHiden() != nodeEMLocal.isHiden()) {
                updateStatus(nodeEMLocal, idUser);
            }
        }
        reset();
        prepareNodeEMForEdit();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    public void resetRenameEMAfterCancel() {
        if (nodeEMsForEdit == null) {
            nodeEMsForEdit = new ArrayList<>();
        } else {
            nodeEMsForEdit.clear();
        }
        for (NodeEM nodeEM1 : nodeEMs) {
            nodeEM1.setLexical_value(nodeEM1.getOldValue());
            nodeEM1.setHiden(nodeEM1.isOldHiden());
            nodeEMsForEdit.add(nodeEM1);
        }
    }

    /**
     * permet de modifier un synonyme sans controle avec doublon
     *
     * @param idUser
     */
    public void updateSynonymForced(int idUser) {
        FacesMessage msg;
        TermHelper termHelper = new TermHelper();
        PrimeFaces pf = PrimeFaces.current();
        duplicate = false;
        if (!termHelper.updateTermSynonyme(connect.getPoolConnexion(),
                nodeEM.getOldValue(), nodeEM.getLexical_value(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                nodeEM.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeEM.isHiden(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La modification a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Synonyme modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        //    PrimeFaces.current().executeScript("PF('addNote').hide();");

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");;
        }
    }

    /**
     * permet de modifier tous les synonymes
     *
     * @param idUser
     */
    public void updateAllSynonyms(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        TermHelper termHelper = new TermHelper();

        if (nodeEMsForEdit == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        for (NodeEM nodeEM1 : nodeEMsForEdit) {
            // save de la valeur pour une modification forcée
            this.nodeEM = nodeEM1;

            if (!nodeEM1.getOldValue().equals(nodeEM1.getLexical_value())) {
                if (termHelper.isTermExist(connect.getPoolConnexion(),
                        nodeEM1.getLexical_value(),
                        selectedTheso.getCurrentIdTheso(),
                        nodeEM1.getLang())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    duplicate = true;
                    if (pf.isAjaxRequest()) {
                        pf.ajax().update("messageIndex");
                    }
                    return;
                }
                if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                        nodeEM1.getLexical_value(),
                        selectedTheso.getCurrentIdTheso(),
                        nodeEM1.getLang())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    duplicate = true;
                    if (pf.isAjaxRequest()) {
                        pf.ajax().update("messageIndex");
                    }
                    return;
                }
                updateSynonymForced(idUser);
            } else {
                if (nodeEM1.isOldHiden() != nodeEM1.isHiden()) {
                    updateStatus(nodeEM1, idUser);
                }
            }
        }
        reset();
        prepareNodeEMForEdit();
    }

    public void updateAllSynonymsProp() {

        if (CollectionUtils.isEmpty(propositionBean.getProposition().getSynonymsProp())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        for (int i = 0; i < propositionBean.getProposition().getSynonymsProp().size(); i++) {
            if (!propositionBean.getProposition().getSynonymsProp().get(i).getOldValue()
                    .equals(propositionBean.getProposition().getSynonymsProp().get(i).getLexical_value())) {
                if (new TermHelper().isTermExist(connect.getPoolConnexion(),
                        propositionBean.getProposition().getSynonymsProp().get(i).getLexical_value(),
                        selectedTheso.getCurrentIdTheso(),
                        propositionBean.getProposition().getSynonymsProp().get(i).getLang())) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    PrimeFaces.current().ajax().update("messageIndex");
                    return;
                }
                if (new TermHelper().isAltLabelExist(connect.getPoolConnexion(),
                        propositionBean.getProposition().getSynonymsProp().get(i).getLexical_value(),
                        selectedTheso.getCurrentIdTheso(),
                        propositionBean.getProposition().getSynonymsProp().get(i).getLang())) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " Un label identique existe déjà !");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    PrimeFaces.current().ajax().update("messageIndex");
                    return;
                }
                if (!propositionBean.getProposition().getSynonymsProp().get(i).isToAdd()) {
                    propositionBean.getProposition().getSynonymsProp().get(i).setToUpdate(true);
                }
            } else {
                if (propositionBean.getProposition().getSynonymsProp().get(i).isOldHiden()
                        != propositionBean.getProposition().getSynonymsProp().get(i).isHiden()) {
                    if (!propositionBean.getProposition().getSynonymsProp().get(i).isToAdd()) {
                        propositionBean.getProposition().getSynonymsProp().get(i).setToUpdate(true);
                    }
                }
            }
        }
    }

    public void updateSynonymProp(SynonymPropBean synonymPropBean) {
        
        if (synonymPropBean == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", "pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }
        
        if (StringUtils.isEmpty(synonymPropBean.getLexical_value())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", "Le champs valeur est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (synonymPropBean.isToUpdate()
                && synonymPropBean.getLexical_value().equals(synonymPropBean.getOldValue())) {
            synonymPropBean.setToUpdate(false);
            return;
        }

        if (!synonymPropBean.getOldValue().equals(synonymPropBean.getLexical_value())) {
            if (new TermHelper().isTermExist(connect.getPoolConnexion(),
                    synonymPropBean.getLexical_value(),
                    selectedTheso.getCurrentIdTheso(),
                    synonymPropBean.getLang())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }

            if (new TermHelper().isAltLabelExist(connect.getPoolConnexion(),
                    synonymPropBean.getLexical_value(),
                    selectedTheso.getCurrentIdTheso(),
                    synonymPropBean.getLang())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Un label identique existe déjà !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }

            if (synonymPropBean.isToRemove()) {
                synonymPropBean.setLexical_value(synonymPropBean.getOldValue());
            } else if (!synonymPropBean.isToAdd()) {
                synonymPropBean.setToUpdate(true);
            }
        } else {
            if (synonymPropBean.isOldHiden() != synonymPropBean.isHiden()) {
                if (synonymPropBean.isToRemove()) {
                    synonymPropBean.setLexical_value(synonymPropBean.getOldValue());
                } else if (!synonymPropBean.isToAdd()) {
                    synonymPropBean.setToUpdate(true);
                }
            }
        }
        
        propositionBean.checkSynonymPropositionStatus();
    }

    /**
     * permet de modifier un synonyme sans controle avec doublon
     *
     * @param nodeEM
     * @param idUser
     */
    public void updateStatus(NodeEM nodeEM, int idUser) {
        FacesMessage msg;
        TermHelper termHelper = new TermHelper();
        PrimeFaces pf = PrimeFaces.current();

        if (!termHelper.updateStatus(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                nodeEM.getLexical_value(),
                nodeEM.getLang(),
                selectedTheso.getCurrentIdTheso(),
                nodeEM.isHiden(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La modification a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Synonyme modifié avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }

    /**
     * permet de supprimer un synonyme
     *
     * @param nodeEM
     * @param idUser
     */
    public void deleteSynonym(NodeEM nodeEM, int idUser) {
        FacesMessage msg;
        TermHelper termHelper = new TermHelper();
        PrimeFaces pf = PrimeFaces.current();

        if (nodeEM == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }
        if (!termHelper.deleteNonPreferedTerm(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                nodeEM.getLang(), nodeEM.getLexical_value(),
                selectedTheso.getCurrentIdTheso(),
                nodeEM.getStatus(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Synonyme supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listSynonimesToDelete");
        }
    }

    public void deleteSynonymPropo(SynonymPropBean spb) {

        if (spb == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        for (SynonymPropBean synonym : propositionBean.getProposition().getSynonymsProp()) {
            if (spb.getLexical_value().equals(synonym.getLexical_value())) {
                if (synonym.isToAdd()) {
                    propositionBean.getProposition().getSynonymsProp().remove(synonym);
                } else if (synonym.isToUpdate()) {
                    synonym.setToRemove(true);
                    synonym.setToUpdate(false);
                    synonym.setLexical_value(synonym.getOldValue());
                } else {
                    synonym.setToRemove(!synonym.isToRemove());
                }
            }
        }
        
        propositionBean.checkSynonymPropositionStatus();
    }

    public void cancel() {
        duplicate = false;
        this.nodeEM = null;
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public ArrayList<NodeLangTheso> getNodeLangs() {
        return nodeLangs;
    }

    public void setNodeLangs(ArrayList<NodeLangTheso> nodeLangs) {
        this.nodeLangs = nodeLangs;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isIsHidden() {
        return isHidden;
    }

    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public ArrayList<NodeEM> getNodeEMs() {
        return nodeEMs;
    }

    public void setNodeEMs(ArrayList<NodeEM> nodeEMs) {
        this.nodeEMs = nodeEMs;
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        this.selectedValue = selectedValue;
    }

    public ArrayList<NodeEM> getNodeEMsForEdit() {
        return nodeEMsForEdit;
    }

    public void setNodeEMsForEdit(ArrayList<NodeEM> nodeEMsForEdit) {
        this.nodeEMsForEdit = nodeEMsForEdit;
    }

}
