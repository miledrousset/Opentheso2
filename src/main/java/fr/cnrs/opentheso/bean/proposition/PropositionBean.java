package fr.cnrs.opentheso.bean.proposition;

import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.proposition.model.PropositionStatusEnum;
import fr.cnrs.opentheso.bean.proposition.model.Proposition;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDao;
import fr.cnrs.opentheso.bean.proposition.service.PropositionService;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.search.SearchBean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.PrimeFaces;

@Data
@SessionScoped
@Named(value = "propositionBean")
public class PropositionBean implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private CurrentUser currentUser;

    @Inject
    private ConceptView conceptView;

    @Inject
    private IndexSetting indexSetting;

    @Inject
    private RoleOnThesoBean roleOnThesoBean;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private RightBodySetting rightBodySetting;

    @Inject
    private PropositionService propositionService;

    @Inject
    private SearchBean searchBean;

    @Inject
    private LanguageBean languageBean;

    private boolean isRubriqueVisible, isNewProposition, isConsultation;
    private Proposition proposition;
    private String nom, email, commentaire, commentaireAdmin;
    private String message;
    private String actionNom;
    private String showAllPropositions = "1";
    private int nbrNewPropositions, filter2 = 1;

    private PropositionDao propositionSelected;
    private List<PropositionDao> propositions;

    private boolean propositionVisibleControle;
    private boolean prefTermeAccepted, varianteAccepted, traductionAccepted,
            noteAccepted, definitionAccepted, changeNoteAccepted, scopeAccepted,
            editorialNotesAccepted, examplesAccepted, historyAccepted;

    public void init() {
        nom = "";
        email = "";
        commentaire = "";
        commentaireAdmin = "";
        message = "";
        propositionSelected = null;
        propositions = null;
    }

    public void onSelectConcept(PropositionDao propositionDao) throws IOException {

        this.propositionSelected = propositionDao;

        NodePreference preference = new PreferencesHelper().getThesaurusPreferences(
                connect.getPoolConnexion(), propositionDao.getIdTheso());
        if (!preference.isSuggestion()) {
            showMessage(FacesMessage.SEVERITY_WARN,
                    languageBean.getMsg("rightbody.proposal.avertissement"));
            return;
        }

        roleOnThesoBean.initNodePref(propositionDao.getIdTheso());
        selectedTheso.setSelectedIdTheso(propositionDao.getIdTheso());
        selectedTheso.setSelectedLang(propositionDao.getLang());
        selectedTheso.setSelectedThesoForSearch();
        rightBodySetting.setIndex("3");
        indexSetting.setIsSelectedTheso(true);
        isRubriqueVisible = true;

        conceptView.getConcept(propositionDao.getIdTheso(), propositionDao.getIdConcept(), propositionDao.getLang());

        proposition = new Proposition();
        propositionService.preparerPropositionSelect(proposition, propositionDao);

        nom = propositionDao.getNom();
        email = propositionDao.getEmail();
        commentaire = propositionDao.getCommentaire();
        commentaireAdmin = propositionDao.getAdminComment();

        chercherProposition();
        nbrNewPropositions = propositionService.searchNbrNewProposition();

        varianteAccepted = false;
        traductionAccepted = false;
        noteAccepted = false;
        definitionAccepted = false;
        changeNoteAccepted = false;
        scopeAccepted = false;
        editorialNotesAccepted = false;
        examplesAccepted = false;
        historyAccepted = false;

        isConsultation = true;

        prefTermeAccepted = proposition.isUpdateNomConcept();
        checkSynonymPropositionStatus();
        checkTraductionPropositionStatus();
        checkNotePropositionStatus();

        PrimeFaces.current().executeScript("afficheSearchBar()");
    }

    public void checkSynonymPropositionStatus() {
        varianteAccepted = proposition.getSynonymsProp().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
    }

    public void checkTraductionPropositionStatus() {
        traductionAccepted = proposition.getTraductionsProp().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
    }

    public void checkNotePropositionStatus() {
        if (CollectionUtils.isNotEmpty(proposition.getNote())) {
            noteAccepted = proposition.getNote().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }

        if (CollectionUtils.isNotEmpty(proposition.getDefinitions())) {
            definitionAccepted = proposition.getDefinitions().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }

        if (CollectionUtils.isNotEmpty(proposition.getChangeNotes())) {
            changeNoteAccepted = proposition.getChangeNotes().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }

        if (CollectionUtils.isNotEmpty(proposition.getScopeNotes())) {
            scopeAccepted = proposition.getScopeNotes().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }

        if (CollectionUtils.isNotEmpty(proposition.getEditorialNotes())) {
            editorialNotesAccepted = proposition.getEditorialNotes().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }

        if (CollectionUtils.isNotEmpty(proposition.getExamples())) {
            examplesAccepted = proposition.getExamples().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }

        if (CollectionUtils.isNotEmpty(proposition.getHistoryNotes())) {
            historyAccepted = proposition.getHistoryNotes().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }

    }

    public void annuler() {
        rightBodySetting.setIndex("0");
    }

    public void afficherPropositionsNotification() {
        afficherListPropositions();
        if (searchBean.isSearchVisibleControle()) {
            PrimeFaces.current().executeScript("disparaitre();");
            PrimeFaces.current().executeScript("afficher();");
            searchBean.setBarVisisble(true);
            searchBean.setSearchResultVisible(false);
            searchBean.setSearchVisibleControle(false);
            propositionVisibleControle = true;
        } else {
            if (searchBean.isBarVisisble()) {
                PrimeFaces.current().executeScript("disparaitre();");
                searchBean.setBarVisisble(false);
                propositionVisibleControle = false;
            } else {
                PrimeFaces.current().executeScript("afficher();");
                searchBean.setBarVisisble(true);
                propositionVisibleControle = true;
            }
        }

        PrimeFaces.current().ajax().update("containerIndex:notificationPanel");
    }

    public void afficherListPropositions() {
        chercherProposition();
        searchBean.setSearchResultVisible(false);
    }

    public void chercherProposition() {
        propositions = new ArrayList<>();
        String idTheso = filter2 == 2 ? selectedTheso.getSelectedIdTheso() : "%";
        switch (showAllPropositions) {
            case "1":
                propositions = propositionService.searchPropositionsNonTraitter(idTheso);
                break;
            case "2":
                propositions = propositionService.searchOldPropositions(idTheso);
                break;
            default:
                propositions = propositionService.searchAllPropositions(idTheso);
        }
    }

    public void searchNewPropositions() {
        nbrNewPropositions = propositionService.searchNbrNewProposition();
    }

    public void preparerConfirmationDialog(String action) {
        actionNom = action;
        switch (actionNom) {
            case "envoyerProposition":
                message = languageBean.getMsg("rightbody.proposal.confirmSendProposal");
                break;
            case "approuverProposition":
                message = languageBean.getMsg("rightbody.proposal.confirmValidateProposal");
                break;
            case "refuserProposition":
                message = languageBean.getMsg("rightbody.proposal.confirmRejectProposal");
                break;
            case "supprimerProposition":
                message = languageBean.getMsg("rightbody.proposal.confirmDeleteProposal");
                break;
            default:
                message = languageBean.getMsg("rightbody.proposal.confirmCancelProposal");
        }
        PrimeFaces.current().executeScript("PF('confirmDialog').show();");
    }

    public void executionAction() throws IOException {
        if (null != actionNom) {
            if (propositionSelected != null) {
                propositionSelected.setAdminComment(commentaireAdmin);
                propositionSelected.setThesoName(selectedTheso.getThesoName());
            }
            switch (actionNom) {
                case "envoyerProposition":
                    envoyerProposition();
                    break;
                case "approuverProposition":
                    propositionService.insertProposition(proposition, propositionSelected, commentaireAdmin,
                            prefTermeAccepted, varianteAccepted, traductionAccepted,
                            noteAccepted, definitionAccepted, changeNoteAccepted, scopeAccepted,
                            editorialNotesAccepted, examplesAccepted, historyAccepted);
                    switchToConceptInglet();
                    showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("rightbody.proposal.confirmIntegratedProposal") + " '"
                            + propositionSelected.getNomConcept() + "' (" + propositionSelected.getIdTheso() + ") !");
                    break;
                case "refuserProposition":
                    propositionService.refuserProposition(propositionSelected, commentaireAdmin);
                    switchToConceptInglet();
                    showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("rightbody.proposal.confirmProposalForConcept") + " '"
                            + propositionSelected.getNomConcept() + "' (" + propositionSelected.getIdTheso() + ") a été refusée avec succès !");
                    break;
                case "supprimerProposition":
                    propositionService.supprimerPropostion(propositionSelected);
                    switchToConceptInglet();
                    showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("rightbody.proposal.confirmProposalForConcept") + " '" + propositionSelected.getNomConcept()
                            + "' (" + propositionSelected.getIdTheso() + ") " + languageBean.getMsg("rightbody.proposal.confirmProposalWasDeleted") + "!");
                    break;
                case "annulerProposition":
                    annulerPropostion();
                    break;
            }
        }

        chercherProposition();
        nbrNewPropositions = propositionService.searchNbrNewProposition();

        PrimeFaces.current().executeScript("PF('confirmDialog').hide();");
    }

    public void switchToNouvelleProposition(NodeConcept nodeConcept) {
        init();

        isNewProposition = true;
        isRubriqueVisible = true;
        if (currentUser.getNodeUser() == null) {
            rightBodySetting.setIndex("2");
        } else {
            if (ObjectUtils.isNotEmpty(roleOnThesoBean.getNodeUserRoleGroup()) && roleOnThesoBean.getNodeUserRoleGroup().isIsContributor()) {
                rightBodySetting.setIndex("2");
            } else {
                rightBodySetting.setIndex("3");
            }
        }

        proposition = propositionService.selectProposition(nodeConcept);
        isConsultation = false;

        if (!ObjectUtils.isEmpty(currentUser.getNodeUser())) {
            nom = currentUser.getNodeUser().getName();
            email = currentUser.getNodeUser().getMail();
        } else {
            nom = "";
            email = "";
        }
    }

    public void updateNomConcept() {

        if (StringUtils.isEmpty(proposition.getNomConceptProp())) {
            proposition.setNomConceptProp("");
            proposition.setUpdateNomConcept(false);

            showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("rightbody.proposal.alertEmptyLabel"));
        } else {
            if (propositionService.updateNomConcept(proposition.getNomConceptProp())) {
                proposition.setUpdateNomConcept(true);
            } else {
                proposition.setNomConceptProp("");
                proposition.setUpdateNomConcept(false);
            }
        }

        prefTermeAccepted = proposition.isUpdateNomConcept();
        PrimeFaces.current().executeScript("PF('nouveauNomConcept').hiden();");
    }

    private void switchToConceptInglet() {
        rightBodySetting.setIndex("0");
        isRubriqueVisible = false;
    }

    public void annulerPropostion() {
        switchToConceptInglet();
        proposition = null;
        nom = "";
        email = "";
    }

    private void envoyerProposition() {

        if (StringUtils.isEmpty(nom)) {
            showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("rightbody.proposal.alertEmptyName"));
            return;
        }

        if (StringUtils.isEmpty(email)) {
            showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("rightbody.proposal.alertEmptyMail"));
            return;
        }
        if (StringUtils.isEmpty(commentaire)) {
            showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("candidat.send_message.msg1"));
            return;
        }

        if (StringUtils.isEmpty(proposition.getNomConceptProp()) && !isSynchroProPresent() && !isTraductionProPresent()
                && !isNoteProPresent() && !isChangeNoteProPresent() && !isDefinitionProPresent()
                && !isEditorialNoteProPresent() && !isExempleNoteProPresent()
                && !isHistoryNoteProPresent() && !isScopeNoteProPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("rightbody.proposal.alertEmptyProposal"));
            return;
        }

        if (propositionService.envoyerProposition(proposition, nom, email, commentaire)) {
            showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("rightbody.proposal.confirmProposalSent"));
        }

        switchToConceptInglet();
    }

    private boolean isSynchroProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getSynonymsProp())) {
            for (SynonymPropBean synonymProp : proposition.getSynonymsProp()) {
                if (synonymProp.isToAdd() || synonymProp.isToRemove() || synonymProp.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isHistoryNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getHistoryNotes())) {
            return proposition.getHistoryNotes().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }
        return false;
    }

    private boolean isScopeNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getScopeNotes())) {
            return proposition.getScopeNotes().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }
        return false;
    }

    private boolean isExempleNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getExamples())) {
            return proposition.getExamples().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }
        return false;
    }

    private boolean isEditorialNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getEditorialNotes())) {
            return proposition.getEditorialNotes().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }
        return false;
    }

    private boolean isNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getNote())) {
            return proposition.getNote().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }
        return false;
    }

    private boolean isChangeNoteProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getChangeNotes())) {
            return proposition.getChangeNotes().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }
        return false;
    }

    private boolean isDefinitionProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getDefinitions())) {
            return proposition.getDefinitions().stream().anyMatch(element -> element.isToAdd() || element.isToRemove() || element.isToUpdate());
        }
        return false;
    }

    private boolean isTraductionProPresent() {
        if (CollectionUtils.isNotEmpty(proposition.getTraductionsProp())) {
            for (TraductionPropBean traductionProp : proposition.getTraductionsProp()) {
                if (traductionProp.isToAdd() || traductionProp.isToRemove() || traductionProp.isToUpdate()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean showButtonDecision() {
        return propositionSelected != null && (PropositionStatusEnum.LU.name().equals(propositionSelected.getStatus())
                || PropositionStatusEnum.ENVOYER.name().equals(propositionSelected.getStatus()));
    }

    public boolean showButtonAction() {
        return (proposition != null && propositionSelected == null) || showButtonDecision();
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        FacesMessage msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public Boolean isCanMakeAction() {
        return (currentUser.getNodeUser() != null && !currentUser.getNodeUser().getMail().equalsIgnoreCase(email))
                && (currentUser.getNodeUser().isSuperAdmin() || roleOnThesoBean.isAdminOnThisTheso());
    }

    public boolean isSameUser() {
        if (currentUser.getNodeUser() != null) {
            return currentUser.getNodeUser().getMail().equalsIgnoreCase(email);
        }
        return false;
    }

    public boolean isIsConsultation() {
        return isConsultation;
    }

}
