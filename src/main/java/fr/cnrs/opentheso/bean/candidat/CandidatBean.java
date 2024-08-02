package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.CandidateHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.ImagesHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignment;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.alignment.AlignmentBean;
import fr.cnrs.opentheso.bean.alignment.AlignmentManualBean;
import fr.cnrs.opentheso.bean.candidat.dao.DomaineDao;
import fr.cnrs.opentheso.bean.candidat.dao.NoteDao;
import fr.cnrs.opentheso.bean.candidat.dao.RelationDao;
import fr.cnrs.opentheso.bean.candidat.dao.TermeDao;
import fr.cnrs.opentheso.bean.candidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.candidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.candidat.enumeration.VoteType;
import fr.cnrs.opentheso.bean.concept.ImageBean;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PreDestroy;
import jakarta.faces.context.ExternalContext;
import jakarta.servlet.http.HttpServletRequest;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

@Data
@SessionScoped
@Named(value = "candidatBean")
public class CandidatBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private CurrentUser currentUser;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private ConceptView conceptView;
    @Inject
    private ImageBean imageBean;
    @Inject
    private AlignmentBean alignmentBean;
    @Inject
    private AlignmentManualBean alignmentManualBean;

    private final CandidatService candidatService = new CandidatService();

    private boolean isListCandidatsActivate, isNewCandidatActivate, isShowCandidatActivate;
    private boolean isRejectCandidatsActivate, isAcceptedCandidatsActivate, isExportViewActivate, isImportViewActivate;
    private boolean myCandidatsSelected1, myCandidatsSelected2, myCandidatsSelected3;

    private int tabViewIndexSelected, progressBarStep, progressBarValue;

    private NodeAlignment alignementSelected;

    private String employePour;
    private String message, definition, selectedExportFormat;
    private String searchValue1, searchValue2, searchValue3;

    private CandidatDto candidatSelected, initialCandidat;
    private List<String> exportFormat;
    private List<CandidatDto> candidatList, rejetCadidat, acceptedCadidat, allTermes;
    private List<DomaineDto> domaines;
    private List<NodeLangTheso> selectedLanguages;
    private List<NodeIdValue> allCollections, allTermesGenerique, AllTermesAssocies;
    private ArrayList<NodeLangTheso> languagesOfTheso;
    private List<CandidatDto> selectedCandidates;
    private boolean listSelected;
    private boolean traductionVisible;
    private boolean isModifiedLabel;

    private List<NodeIdValue> collectionTemps, termesGeneriqueTmp, termesAssociesTmp;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        candidatSelected = null;
        initialCandidat = null;
        exportFormat = null;
        candidatList = null;
        rejetCadidat = null;
        acceptedCadidat = null;
        allTermes = null;
        domaines = null;
        selectedLanguages = null;
        languagesOfTheso = null;
        definition = null;
        isModifiedLabel = false;
    }

    public void setStateForSelectedCandidate() {
        if (selectedCandidates != null) {
            listSelected = !selectedCandidates.isEmpty();
        }
    }

    public void initCandidatModule() {
        isListCandidatsActivate = true;
        isRejectCandidatsActivate = true;
        isAcceptedCandidatsActivate = true;
        isNewCandidatActivate = false;
        isShowCandidatActivate = false;
        isImportViewActivate = false;
        isExportViewActivate = false;
        listSelected = false;
        isModifiedLabel = false;

        collectionTemps = new ArrayList<>();

        candidatList = new ArrayList<>();
        allTermes = new ArrayList<>();
        domaines = new ArrayList<>();
        selectedLanguages = new ArrayList<>();
        rejetCadidat = new ArrayList<>();
        acceptedCadidat = new ArrayList<>();
        selectedCandidates = new ArrayList<>();

        getAllCandidatsByThesoAndLangue();
        getRejectCandidatByThesoAndLangue();
        getAcceptedCandidatByThesoAndLangue();
        tabViewIndexSelected = 0;

        alignementSelected = new NodeAlignment();

        exportFormat = Arrays.asList("skos", "json", "jsonLd", "turtle");
        selectedExportFormat = "skos";

        try {
            languagesOfTheso = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(
                    connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
            languagesOfTheso.forEach((nodeLang) -> {
                selectedLanguages.add(nodeLang);
            });
        } catch (Exception e) {
        }
    }

    public void getAllCandidatsByThesoAndLangue() {
        isModifiedLabel = false;
        tabViewIndexSelected = 0;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            candidatList = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 1, "CA");
        } else {
            candidatList.clear();
        }
    }

    public void getRejectCandidatByThesoAndLangue() {
        tabViewIndexSelected = 2;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            rejetCadidat = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 3, "CA");
        } else {
            rejetCadidat.clear();
        }
    }

    public void getAcceptedCandidatByThesoAndLangue() {
        tabViewIndexSelected = 1;
        if (!StringUtils.isEmpty(selectedTheso.getSelectedIdTheso())) {
            acceptedCadidat = candidatService.getCandidatsByStatus(connect, selectedTheso.getSelectedIdTheso(),
                    getIdLang(), 2, null);
        } else {
            acceptedCadidat.clear();
        }
        isAcceptedCandidatsActivate = true;
    }

    /**
     * permet de supprimer les candidats sélectionnés
     *
     * @param idUser
     */
    public void deleteSelectedCandidate(int idUser) {
        if (selectedCandidates == null) {
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        for (CandidatDto selectedCandidate : selectedCandidates) {
            if (!conceptHelper.deleteConcept(connect.getPoolConnexion(), selectedCandidate.getIdConcepte(),
                    selectedCandidate.getIdThesaurus(), idUser)) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de suppression");
                return;
            }
        }
        initCandidatModule();
        getAllCandidatsByThesoAndLangue();
        showMessage(FacesMessage.SEVERITY_INFO, "Candidats supprimés");
    }

    /**
     * permet de savoir si l'identifiant actuel est propriétaire du candidat
     *
     * @return
     */
    public boolean isMyCandidate() {
        return candidatSelected.getCreatedById() == candidatSelected.getUserId();
    }

    /**
     * permet de déctercter la langue préférée d'un thésaurus
     *
     * @return
     */
    private String getIdLang() {
        String idLang = connect.getWorkLanguage();
        if (roleOnThesoBean.getNodePreference() != null) {
            idLang = roleOnThesoBean.getNodePreference().getSourceLang();
        }
        return idLang;
    }

    public void selectMyCandidats() {
        if (myCandidatsSelected1) {
            candidatList = candidatList.stream()
                    .filter(candidat -> candidat.getCreatedById() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            getAllCandidatsByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public String getCountOfCandidats() {
        return "" + candidatList.size();
    }

    public String getCountOfAcceptedCandidats() {
        return "" + acceptedCadidat.size();
    }

    public String getCountOfRejectedCandidats() {
        return "" + rejetCadidat.size();
    }

    public void selectMyRejectCandidats() {
        if (myCandidatsSelected3) {
            rejetCadidat = rejetCadidat.stream()
                    .filter(candidat -> candidat.getUserId() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            getRejectCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(rejetCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void searchRejectCandByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue3)) {
            rejetCadidat = rejetCadidat.stream()
                    .filter(candidat -> candidat.getNomPref().contains(searchValue3) || candidat.getUser().contains(searchValue3))
                    .collect(Collectors.toList());
        } else {
            getRejectCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(rejetCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void selectMyAcceptedCandidats() {
        if (myCandidatsSelected2) {
            acceptedCadidat = acceptedCadidat.stream()
                    .filter(candidat -> candidat.getUserId() == currentUser.getNodeUser().getIdUser())
                    .collect(Collectors.toList());
        } else {
            getAcceptedCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(acceptedCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void searchAcceptedCandByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue2)) {
            acceptedCadidat = acceptedCadidat.stream()
                    .filter(candidat -> candidat.getNomPref().contains(searchValue2) || candidat.getUser().contains(searchValue2))
                    .collect(Collectors.toList());
        } else {
            getAcceptedCandidatByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(acceptedCadidat.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void searchByTermeAndAuteur() {
        if (!StringUtils.isEmpty(searchValue1)) {
            candidatList = candidatService.searchCandidats(connect, searchValue1,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(),
                    1, "CA");
        } else {
            getAllCandidatsByThesoAndLangue();
        }
        showMessage(FacesMessage.SEVERITY_INFO, new StringBuffer().append(candidatList.size()).append(" ")
                .append(languageBean.getMsg("candidat.result_found")).toString());
    }

    public void deleteAlignment(NodeAlignment nodeAlignment) {

        if (!new AlignmentHelper().deleteAlignment(connect.getPoolConnexion(),
                nodeAlignment.getId_alignement(),
                selectedTheso.getCurrentIdTheso())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        candidatSelected.setAlignments(new AlignmentHelper().getAllAlignmentOfConcept(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Alignement supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public void showRejectCandidatSelected(CandidatDto candidatDto) throws IOException {

        tabViewIndexSelected = 2;

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }

        isRejectCandidatsActivate = false;
        getCandidatInformations(candidatDto);

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());

    }

    public void showAcceptedCandidatSelected(CandidatDto candidatDto) throws IOException {

        tabViewIndexSelected = 1;

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }

        isAcceptedCandidatsActivate = false;
        getCandidatInformations(candidatDto);

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());

    }

    public void getCandidatInformations(CandidatDto candidatDto) {
        candidatSelected = candidatDto;
        candidatSelected.setLang(languageBean.getIdLangue());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(connect, candidatSelected, selectedTheso.getCurrentIdTheso());
    }

    public void showCandidatSelected(CandidatDto candidatDto) throws IOException {

        tabViewIndexSelected = 0;

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }

        candidatSelected = candidatDto;
        candidatSelected.setLang(getIdLang());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(connect, candidatSelected, selectedTheso.getCurrentIdTheso());
        initialCandidat = new CandidatDto(candidatSelected);

        allTermes = candidatList.stream().filter(candidat -> !candidat.getNomPref().equals(candidatDto.getNomPref()))
                .collect(Collectors.toList());

        isShowCandidatActivate = true;
        isNewCandidatActivate = false;
        isListCandidatsActivate = false;

        alignmentManualBean.reset();
    }

    public CandidatDto getAllInfosOfCandidate(CandidatDto candidatDto) {
        candidatDto.setLang(getIdLang());
        candidatDto.setUserId(currentUser.getNodeUser().getIdUser());
        candidatDto.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatService.getCandidatDetails(connect, candidatDto, selectedTheso.getCurrentIdTheso());
        return candidatDto;
    }

    public void setIsListCandidatsActivate(boolean isListCandidatsActivate) throws IOException {

        tabViewIndexSelected = 0;

        this.isListCandidatsActivate = true;
        isRejectCandidatsActivate = true;
        isAcceptedCandidatsActivate = true;

        isNewCandidatActivate = false;
        isShowCandidatActivate = false;

        isExportViewActivate = false;
        isImportViewActivate = false;

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public boolean isNewCandidatActivate() {
        return isNewCandidatActivate;
    }

    public void setIsNewCandidatActivate(boolean isNewCandidatActivate) {
        this.isNewCandidatActivate = isNewCandidatActivate;
        isListCandidatsActivate = false;
        isImportViewActivate = false;
        isExportViewActivate = false;
    }

    public void setIsNewCandidatRejected(boolean isCandidatRejected) {
        isRejectCandidatsActivate = true;
        isImportViewActivate = false;
        isExportViewActivate = false;
    }

    public boolean isShowCandidatActivate() {
        return isShowCandidatActivate;
    }

    public void setShowCandidatActivate(boolean isShowCandidatActivate) {
        this.isShowCandidatActivate = isShowCandidatActivate;
        isListCandidatsActivate = false;
        isNewCandidatActivate = false;
        isImportViewActivate = false;
        isExportViewActivate = false;
    }

    public void saveConcept() throws SQLException, IOException {

        if (StringUtils.isEmpty(candidatSelected.getNomPref())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg1"));
            return;
        }

        if (isNewCandidatActivate) {
            if (StringUtils.isEmpty(definition)) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.def"));
                return;
            }
        }

        if (roleOnThesoBean.getNodePreference() == null) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg2"));
            return;
        }

        var termHelper = new TermHelper();

        if (initialCandidat == null) {

            // en cas d'un nouveau candidat, verification dans les prefLabels
            if (termHelper.isPrefLabelExist(connect.getPoolConnexion(), candidatSelected.getNomPref().trim(),
                    selectedTheso.getCurrentIdTheso(), getIdLang())) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg3"));
                return;
            }
            // verification dans les altLabels
            if (termHelper.isAltLabelExist(connect.getPoolConnexion(), candidatSelected.getNomPref().trim(),
                    selectedTheso.getCurrentIdTheso(), getIdLang())) {
                showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg4"));
                return;
            }

            var concept = Concept.builder()
                    .idConcept(candidatSelected.getIdConcepte())
                    .idThesaurus(selectedTheso.getCurrentIdTheso())
                    .topConcept(false)
                    .lang(getIdLang())
                    .idUser(currentUser.getNodeUser().getIdUser())
                    .userName(currentUser.getUsername())
                    .status("CA")
                    .build();

            ConceptHelper conceptHelper = new ConceptHelper();
            conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

            var idNewConcept = candidatService.saveNewCondidat(connect, concept, conceptHelper);
            if (idNewConcept == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("candidat.save.msg5"));
                return;
            }
            candidatSelected.setIdConcepte(idNewConcept);

            var terme = Term.builder()
                    .lang(getIdLang())
                    .id_thesaurus(selectedTheso.getCurrentIdTheso())
                    .contributor(currentUser.getNodeUser().getIdUser())
                    .lexical_value(candidatSelected.getNomPref().trim())
                    .source("candidat")
                    .status("D")
                    .build();

            candidatSelected.setIdTerm(candidatService.saveNewTerm(connect, terme, candidatSelected.getIdConcepte(),
                    candidatSelected.getUserId()));

            new NoteHelper().addNote(connect.getPoolConnexion(), candidatSelected.getIdConcepte(),
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso(),
                    definition, "definition", "", currentUser.getNodeUser().getIdUser());

            setIsListCandidatsActivate(true);

        } else {
            if (!initialCandidat.getNomPref().equals(candidatSelected.getNomPref())) {
                if (termHelper.isTermExistInThisLang(connect.getPoolConnexion(), candidatSelected.getIdTerm(), getIdLang(), candidatSelected.getIdThesaurus())) {
                    candidatService.updateIntitule(connect, candidatSelected.getNomPref(), candidatSelected.getIdThesaurus(),
                            getIdLang(), candidatSelected.getIdTerm());
                } else {
                    Term term = new Term();
                    term.setId_thesaurus(selectedTheso.getCurrentIdTheso());
                    term.setLang(getIdLang());
                    term.setContributor(currentUser.getNodeUser().getIdUser());
                    term.setLexical_value(candidatSelected.getNomPref().trim());
                    term.setSource("candidat");
                    term.setStatus("D");
                    term.setId_term(candidatSelected.getIdTerm());
                    TermeDao termeDao = new TermeDao();
                    termeDao.addNewTerme(connect.getPoolConnexion(), term);
                }

            }
        }
        /////////////////////////
        ///// insert DcTermsData
        var dcElement = DcElement.builder()
                .name(DCMIResource.CREATOR)
                .value(currentUser.getNodeUser().getName())
                .build();
        new DcElementHelper().addDcElementConcept(connect.getPoolConnexion(), dcElement, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus());
        ///////////////        

        candidatService.updateDetailsCondidat(connect, candidatSelected, currentUser.getNodeUser().getIdUser());

        //getAllCandidatsByThesoAndLangue();
        candidatSelected.setNodeNotes(new NoteDao().getNotesCandidat(connect.getPoolConnexion(), candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus()));
        definition = "";
        isNewCandidatActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = true;

        showMessage(FacesMessage.SEVERITY_INFO, "Candidat enregistré avec succès");
    }

    public List<NodeIdValue> searchCollection(String enteredValue) {

        if (StringUtils.isNotEmpty(enteredValue)) {
            allCollections = new GroupHelper().searchGroup(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(), enteredValue);
            return createCollectionsFiltred(allCollections, candidatSelected.getCollections());
        } else {
            return Collections.emptyList();
        }
    }

    private List<NodeIdValue> createCollectionsFiltred(List<NodeIdValue> collections, List<NodeIdValue> collectionsSelected) {
        if (CollectionUtils.isNotEmpty(collections)) {
            return collections.stream()
                    .filter(element -> !isExist(collectionsSelected, element))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private boolean isExist(List<NodeIdValue> collections, NodeIdValue nodeIdValue) {
        return collections.stream()
                .filter(element -> element.getValue().equals(nodeIdValue.getValue()))
                .findFirst()
                .isPresent();
    }

    //// ajouté par Miled
    /**
     * permet d'ajouter un vote pour ce candidat, c'est 1 seul vote par
     * utilisateur
     */
    public void addVote() {
        try {
            // cas où il y a un vote, on le supprime
            if (candidatService.getVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser(), null, VoteType.CANDIDAT)) {
                candidatService.removeVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), null, VoteType.CANDIDAT);
                candidatSelected.setVoted(false);
            } else {
                // cas ou il n'y a pas de vote, alors on vote
                candidatService.addVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), null, VoteType.CANDIDAT);
                candidatSelected.setVoted(true);
            }

        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, sqle.toString());
                showMessage(FacesMessage.SEVERITY_ERROR, "Le vote a échoué");
                return;
            }
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Vote enregistré");

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    /**
     * permet d'ajouter un vote pour un note de candidat, c'est 1 seul vote par
     * utilisateur
     *
     * @param nodeNote
     */
    public void addNoteVote(NodeNote nodeNote) {
        try {
            // cas où il y a un vote, on le supprime
            if (candidatService.getVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                    currentUser.getNodeUser().getIdUser(), nodeNote.getId_note() + "", VoteType.NOTE)) {

                candidatService.removeVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), nodeNote.getId_note() + "", VoteType.NOTE);
                nodeNote.setVoted(false);
            } else {
                // cas ou il n'y a pas de vote, alors on vote
                candidatService.addVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        currentUser.getNodeUser().getIdUser(), nodeNote.getId_note() + "", VoteType.NOTE);
                nodeNote.setVoted(true);
            }
        } catch (SQLException sqle) {
            if (!sqle.getSQLState().equalsIgnoreCase("23505")) {
                Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, sqle.toString());
                showMessage(FacesMessage.SEVERITY_ERROR, "Le vote a échoué");
                return;
            }
        }

        showMessage(FacesMessage.SEVERITY_INFO, "Vote du note enregistré");

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("candidatForm:vote");
    }

    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     *
     * @param value
     * @return
     */
    public List<NodeIdValue> searchTermeGenerique(String value) {

        if (StringUtils.isNotEmpty(value)) {
            allTermesGenerique = new SearchHelper().searchAutoCompletionForRelationIdValue(connect.getPoolConnexion(), value,
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
            return createCollectionsFiltred(allTermesGenerique, candidatSelected.getTermesGenerique());
        } else {
            return Collections.emptyList();
        }
    }

    public List<NodeIdValue> searchTermeAssocie(String value) {

        if (StringUtils.isNotEmpty(value)) {
            AllTermesAssocies = new SearchHelper().searchAutoCompletionForRelationIdValue(connect.getPoolConnexion(), value,
                    selectedTheso.getCurrentLang(), selectedTheso.getCurrentIdTheso());
            return createCollectionsFiltred(AllTermesAssocies, candidatSelected.getTermesAssocies());
        } else {
            return Collections.emptyList();
        }
    }

    public void initialNewCandidat() throws IOException {
        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, languageBean.getMsg("candidat.save.msg9"));
            return;
        }
        setIsNewCandidatActivate(true);

        candidatSelected = new CandidatDto();
        candidatSelected.setIdConcepte(null);//candidatService.getCandidatID(connect));
        candidatSelected.setLang(getIdLang());
        candidatSelected.setIdThesaurus(selectedTheso.getCurrentIdTheso());
        candidatSelected.setUserId(currentUser.getNodeUser().getIdUser());

        allTermes = candidatList;

        initialCandidat = null;
        definition = null;

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    /**
     * permet de récupérer le nom d'un utilisateur d'après son ID
     *
     * @param idUser
     * @return
     */
    public String getUserName(int idUser) {
        UserHelper userHelper = new UserHelper();
        return userHelper.getNameUser(connect.getPoolConnexion(), idUser);

    }

    public void reactivateRejectedCandidat() {
        if (candidatSelected == null || candidatSelected.getIdConcepte() == null || candidatSelected.getIdConcepte().isEmpty()) {
            return;
        }

        CandidateHelper candidateHelper = new CandidateHelper();
        if (!candidateHelper.reactivateRejectedCandidat(connect.getPoolConnexion(),
                candidatSelected.getIdThesaurus(),
                candidatSelected.getIdConcepte())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "l'action a échoué");
        } else {
            try {
                showMessage(FacesMessage.SEVERITY_INFO, "l'action a réussi");
                initCandidatModule();
                getAllCandidatsByThesoAndLangue();
                setIsListCandidatsActivate(true);
            } catch (IOException ex) {
                Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addCollection(SelectEvent<NodeIdValue> event) {
        Optional<NodeIdValue> elementAdded = allCollections.stream()
                .filter(element -> event.getObject().getId().equalsIgnoreCase(element.getId()))
                .findFirst();
        if (elementAdded.isPresent()) {
            try {
                new DomaineDao().addNewDomaine(connect, elementAdded.get().getId(),
                        candidatSelected.getIdThesaurus(),
                        candidatSelected.getIdConcepte());

                candidatSelected.getCollections().add(elementAdded.get());
                collectionTemps = Collections.emptyList();
                PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatCollection");

                showMessage(FacesMessage.SEVERITY_INFO, "Collection ajoutée avec succès !");
            } catch (Exception exception) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant l'enregistrement de la nouvelle collection !");
            }
        }
    }

    public void removeCollection(NodeIdValue collection) {
        if (CollectionUtils.isNotEmpty(candidatSelected.getCollections())) {
            try {
                new DomaineDao().deleteDomaine(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(), collection.getId());

                candidatSelected.getCollections().remove(collection);
                PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatCollection");

                showMessage(FacesMessage.SEVERITY_INFO, "Collection supprimée avec succès !");
            } catch (Exception exception) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la suppression de la collection !");
            }
        }
    }

    public void addSynonyme() {

        if (StringUtils.isNotEmpty(employePour)) {
            if (candidatSelected.getEmployePourList().contains(employePour)) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Le mot '" + employePour + "' existe déjà !");
            } else {
                try {
                    new TermeDao().addNewEmployePour(connect, employePour, candidatSelected.getIdThesaurus(),
                            candidatSelected.getLang(), candidatSelected.getIdTerm());

                    candidatSelected.getEmployePourList().add(employePour);
                    employePour = "";
                    PrimeFaces.current().ajax().update("tabViewCandidat");

                    showMessage(FacesMessage.SEVERITY_INFO, "Synonyme ajouté avec succès !");
                } catch (Exception ex) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant l'ajout du nouveau synonyme !");
                }
            }
        }
    }

    public void removeSynonyme(String synonyme) {
        if (CollectionUtils.isNotEmpty(candidatSelected.getEmployePourList())) {
            try {
                new TermeDao().deleteEMByIdTermAndLang(connect.getPoolConnexion(), candidatSelected.getIdTerm(),
                        candidatSelected.getIdThesaurus(), candidatSelected.getLang());

                candidatSelected.getEmployePourList().remove(synonyme);
                PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatSynonym");

                showMessage(FacesMessage.SEVERITY_INFO, "Synonyme supprimé avec succès !");
            } catch (Exception ex) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant la suppression du synonyme !");
            }
        }
    }

    public void addTraduction(SelectEvent<NodeIdValue> event) throws SQLException {
        var elementAdded = event.getObject();

        if (candidatSelected.getTermesGenerique().stream()
                .filter(element -> element.getId().equalsIgnoreCase(elementAdded.getId()))
                .findFirst().isPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Le terme existe déjà !");
        } else {

            var relationDao = new RelationDao();

            relationDao.addRelationBT(connect, candidatSelected.getIdConcepte(), elementAdded.getId(), selectedTheso.getCurrentIdTheso());

            candidatSelected.setTermesGenerique(relationDao.getCandidatRelationsBT(connect.getPoolConnexion(),
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            showMessage(FacesMessage.SEVERITY_INFO, "Term générique ajoutée avec succès !");

            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatBT");
        }
        termesGeneriqueTmp = Collections.emptyList();
    }

    public void removeGenericTerm(NodeIdValue genericTerm) throws SQLException {
        if (CollectionUtils.isNotEmpty(candidatSelected.getTermesGenerique())) {

            new RelationsHelper().deleteRelationBT(connect.getPoolConnexion(), candidatSelected.getIdConcepte(),
                    selectedTheso.getCurrentIdTheso(), genericTerm.getId(), currentUser.getNodeUser().getIdUser());

            candidatSelected.setTermesGenerique(new RelationDao().getCandidatRelationsBT(connect.getPoolConnexion(),
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatBT");

            showMessage(FacesMessage.SEVERITY_INFO, "Term générique supprimée avec succès !");
        }
    }

    public void addTraductionAssocieSelect(SelectEvent<NodeIdValue> event) throws SQLException {

        if (candidatSelected.getTermesAssocies().stream()
                .filter(element -> element.getId().equalsIgnoreCase(event.getObject().getId()))
                .findFirst().isPresent()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Le terme existe déjà !");
        } else {
            var relationDao = new RelationDao();

            relationDao.addRelationRT(connect, candidatSelected.getIdConcepte(), event.getObject().getId(), selectedTheso.getCurrentIdTheso());

            candidatSelected.setTermesAssocies(relationDao.getCandidatRelationsRT(connect.getPoolConnexion(),
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            showMessage(FacesMessage.SEVERITY_INFO, "Term associé ajouté avec succès !");
            PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatRT");
        }
        termesAssociesTmp = Collections.emptyList();
    }

    public void removeAssociesTerm(NodeIdValue associeTerm) throws SQLException {
        new RelationsHelper().deleteRelationRT(connect.getPoolConnexion(), candidatSelected.getIdConcepte(),
                selectedTheso.getCurrentIdTheso(), associeTerm.getId(), currentUser.getNodeUser().getIdUser());

        candidatSelected.setTermesAssocies(new RelationDao().getCandidatRelationsRT(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                candidatSelected.getLang()));

        showMessage(FacesMessage.SEVERITY_INFO, "Term associé supprimé avec succès !");
        PrimeFaces.current().ajax().update("tabViewCandidat:containerIndexCandidat:candidatRT");
    }

    public void onRelationBTAdded(SelectEvent<NodeIdValue> event) {
        Optional<NodeIdValue> elementAdded = allCollections.stream()
                .filter(element -> event.getObject().getId().equalsIgnoreCase(element.getId()))
                .findFirst();
        if (elementAdded.isPresent()) {
            candidatSelected.getCollections().add(elementAdded.get());
            collectionTemps = new ArrayList<>();
            PrimeFaces.current().ajax().update("tabViewCandidat");
        }
    }

    /**
     * permet de récupérer les anciens candidats saisies dans l'ancien module
     * uniquement les candidats qui étatient en attente
     */
    public void getOldCandidates() {
        String messageInfo = new CandidatService().getOldCandidates(connect, selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser(), roleOnThesoBean.getNodePreference());
        showMessage(FacesMessage.SEVERITY_INFO, messageInfo);
        getAllCandidatsByThesoAndLangue();
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public void setListCandidatsActivate(boolean isListCandidatsActivate) {
        getAllCandidatsByThesoAndLangue();
        this.isListCandidatsActivate = isListCandidatsActivate;
        isImportViewActivate = false;
        isExportViewActivate = false;
    }

    public void setExportViewActivate(boolean isExportViewActivate) {
        this.isExportViewActivate = isExportViewActivate;
        isImportViewActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = false;

        progressBarStep = 0;
        progressBarValue = 0;
    }

    public void setImportViewActivate(boolean isImportViewActivate) {
        this.isImportViewActivate = isImportViewActivate;
        isExportViewActivate = false;
        isListCandidatsActivate = false;
        isShowCandidatActivate = false;

        progressBarStep = 0;
        progressBarValue = 0;
    }

    public List<String> getSelectedCandidatesAsId() {
        List<String> listIdOfConcept = new ArrayList<>();
        for (CandidatDto selectedCandidate : selectedCandidates) {
            listIdOfConcept.add(selectedCandidate.getIdConcepte());
        }
        return listIdOfConcept;
    }

    public void deleteAlignement() {
        new AlignmentHelper().deleteAlignment(connect.getPoolConnexion(),
                alignementSelected.getId_alignement(),
                selectedTheso.getCurrentIdTheso());

        candidatSelected.setAlignments(new AlignmentHelper().getAllAlignmentOfConcept(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement supprimé avec succès !");

        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public void updateAlignement() {
        new AlignmentHelper().updateAlignment(connect.getPoolConnexion(),
                alignementSelected.getId_alignement(),
                alignementSelected.getConcept_target(),
                alignementSelected.getThesaurus_target(),
                alignementSelected.getUri_target(),
                alignementSelected.getAlignement_id_type(),
                candidatSelected.getIdConcepte(),
                selectedTheso.getCurrentIdTheso());

        candidatSelected.setAlignments(new AlignmentHelper().getAllAlignmentOfConcept(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso()));

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement mise à jour avec succès !");

        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public String getCreatedByBtnTitle() {
        var createdBy = (candidatSelected != null && StringUtils.isNotEmpty(candidatSelected.getCreatedBy()))
                ? " " + languageBean.getMsg("rightbody.concept.createdBy") + " " + candidatSelected.getCreatedBy() : "";
        return languageBean.getMsg("candidat.file") + createdBy;
    }

    public void addNewImage(int idUser) {

        if (StringUtils.isEmpty(imageBean.getUri())) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucune URI insérée !");
            return;
        }

        if (!new ImagesHelper().addExternalImage(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(),
                selectedTheso.getCurrentIdTheso(),
                imageBean.getName(),
                imageBean.getCopyright(),
                imageBean.getUri(),
                imageBean.getCreator(),
                idUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant l'ajout de l'image !");
            return;
        }

        candidatSelected.setImages(new ImagesHelper().getExternalImages(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));

        showMessage(FacesMessage.SEVERITY_INFO, "Image ajoutée avec succès");
        initImageDialog();
        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public void openAddAlignementWindow() {
        alignmentBean.setConceptValueForAlignment(candidatSelected.getNomPref());
        alignmentBean.setExistingAlignment(candidatSelected.getIdConcepte(), selectedTheso.getCurrentIdTheso());
        alignmentBean.prepareValuesForIdRef();
        alignmentBean.setListAlignValues(null);
        alignmentBean.initAlignmentSources(selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        alignmentBean.setIdConceptSelectedForAlignment(candidatSelected.getIdConcepte());

        PrimeFaces.current().executeScript("PF('searchAlignement').show();");
    }

    public void searchAlignementAuto() {
        alignmentBean.addAlignment(selectedTheso.getSelectedIdTheso(),
                alignmentBean.getIdConceptSelectedForAlignment(),
                currentUser.getNodeUser().getIdUser(),
                false);

        showMessage(FacesMessage.SEVERITY_INFO, "Alignement ajouté avec sucée !");

        candidatService.getCandidatDetails(connect, candidatSelected, selectedTheso.getCurrentIdTheso());

        PrimeFaces.current().ajax().update("tabViewCandidat");
    }

    public String getNoteType(String typeCode) {
        switch (typeCode) {
            case "note":
                return "Note";
            case "historyNote":
                return "Note historique";
            case "scopeNote":
                return "Note d'application";
            case "example":
                return "Exemple";
            case "editorialNote":
                return "Note éditoriale";
            case "definition":
                return "Définition";
            default:
                return "Note de changement";
        }
    }

    public void initImageDialog() {
        imageBean.setUri(null);
        imageBean.setCopyright(null);
        imageBean.setName(null);
        imageBean.setCreator(null);
    }

    public void deleteImage(String imageUri) {

        new ImagesHelper().deleteExternalImage(connect.getPoolConnexion(), candidatSelected.getIdConcepte(),
                selectedTheso.getSelectedIdTheso(), imageUri);

        candidatSelected.setImages(new ImagesHelper().getExternalImages(connect.getPoolConnexion(),
                candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));

        showMessage(FacesMessage.SEVERITY_INFO, "Image supprimée avec succès");
    }

    public boolean isIsModifiedLabel() {
        return isModifiedLabel;
    }

    public void setIsModifiedLabel(boolean isModifiedLabel) {
        this.isModifiedLabel = isModifiedLabel;
    }

    public void changeStateOfLabel() {
        this.isModifiedLabel = true;
    }

    public void updateCandidateLabel() {
        try {
            candidatService.updateIntitule(connect, candidatSelected.getNomPref(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang(), candidatSelected.getIdTerm());
            isModifiedLabel = false;
        } catch (SQLException ex) {
            Logger.getLogger(CandidatBean.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
