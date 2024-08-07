package fr.cnrs.opentheso.bean.proposition.service;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.mail.MailBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.bean.proposition.TraductionPropBean;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDao;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDetailDao;
import fr.cnrs.opentheso.bean.proposition.helper.PropositionDetailHelper;
import fr.cnrs.opentheso.bean.proposition.helper.PropositionHelper;
import fr.cnrs.opentheso.bean.proposition.model.Proposition;
import fr.cnrs.opentheso.bean.proposition.model.PropositionActionEnum;
import fr.cnrs.opentheso.bean.proposition.model.PropositionCategoryEnum;
import fr.cnrs.opentheso.bean.proposition.model.PropositionStatusEnum;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

@SessionScoped
public class PropositionService implements Serializable {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    @Inject
    private Tree tree;

    @Inject
    private Connect connect;

    @Inject
    private ConceptView conceptView;

    @Inject
    private CurrentUser currentUser;

    @Inject
    private IndexSetting indexSetting;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private RoleOnThesoBean roleOnThesoBean;

    @Inject
    private MailBean mailBean;

    public boolean envoyerProposition(Proposition proposition, String nom, String email, String commentaire) {

        if (new PropositionHelper().searchPropositionByEmailAndConceptAndLang(connect.getPoolConnexion(), email,
                proposition.getConceptID(), selectedTheso.getCurrentLang()) != null) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous avez déjà une proposition pour le même concept en cours de etraitement !");
            return false;
        }

        PropositionDao propositionDao = new PropositionDao();
        propositionDao.setNom(nom);
        propositionDao.setEmail(email);
        propositionDao.setCommentaire(commentaire);
        propositionDao.setDatePublication(DATE_FORMAT.format(new Date()));
        propositionDao.setIdConcept(proposition.getConceptID());
        propositionDao.setIdTheso(selectedTheso.getCurrentIdTheso());
        propositionDao.setLang(selectedTheso.getCurrentLang());
        propositionDao.setStatus(PropositionStatusEnum.ENVOYER.name());
        propositionDao.setThesoName(new ThesaurusHelper().getTitleOfThesaurus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang()));

        try {
            String subject = "[Opentheso] Confirmation de l'envoi de votre proposition";
            String contentFile = "<html><body>"
                    + "Cher(e) " + propositionDao.getNom() + ", <br/> "
                    + "<p> Votre proposition a bien été reçue par nos administrateurs, elle sera étudiée dans les plus brefs délais.<br/>"
                    + "Vous recevrez un mail dès que votre proposition sera traitée.<br/></p> "
                    + "Nous vous remercions de votre contribution à l'enrichissement du thésaurus <b>" + propositionDao.getThesoName() + "(" + propositionDao.getIdTheso() + ")" + "</b> "
                    + "(concept : <a href=\"" + getPath() + "/?idc=" + propositionDao.getIdConcept()
                    + "&idt=" + propositionDao.getIdTheso() + "\">" + proposition.getNomConcept().getLexical_value() + "</a>). <br/><br/> Cordialement,<br/>"
                    + "L'équipe " + propositionDao.getThesoName() + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            if (!sendEmail(propositionDao.getEmail(), subject, contentFile)) {
                showMessage(FacesMessage.SEVERITY_ERROR, "!! votre propostion n'a pas été envoyée !!");
                return false;
            }
        } catch (Exception ex) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur detectée pendant l'envoie du mail de notification! \n votre propostion n'a pas été envoyée !");
            return false;
        }

        int propositionID = new PropositionHelper().createNewProposition(connect.getPoolConnexion(), propositionDao);

        if (propositionID == -1) {
            showMessage(FacesMessage.SEVERITY_WARN, "Erreur pendant l'enregistrement de la proposition !");
            return false;
        }

        if (StringUtils.isNotEmpty(proposition.getNomConceptProp())) {
            PropositionDetailDao propositionDetail = new PropositionDetailDao();
            propositionDetail.setIdProposition(propositionID);
            propositionDetail.setAction(PropositionActionEnum.UPDATE.name());
            propositionDetail.setCategorie(PropositionCategoryEnum.NOM.name());
            propositionDetail.setLang(selectedTheso.getCurrentLang());
            propositionDetail.setValue(proposition.getNomConceptProp());
            propositionDetail.setOldValue(proposition.getNomConcept().getLexical_value());
            new PropositionDetailHelper().createNewPropositionDetail(connect.getPoolConnexion(), propositionDetail);
        }

        if (!CollectionUtils.isEmpty(proposition.getSynonymsProp())) {
            for (SynonymPropBean synonymProp : proposition.getSynonymsProp()) {
                if (synonymProp.isToAdd() || synonymProp.isToUpdate() || synonymProp.isToRemove()) {
                    PropositionActionEnum action;
                    if (synonymProp.isToAdd()) {
                        action = PropositionActionEnum.ADD;
                    } else if (synonymProp.isToRemove()) {
                        action = PropositionActionEnum.DELETE;
                    } else {
                        action = PropositionActionEnum.UPDATE;
                    }

                    PropositionDetailDao propositionDetail = new PropositionDetailDao();
                    propositionDetail.setIdProposition(propositionID);
                    propositionDetail.setAction(action.name());
                    propositionDetail.setCategorie(PropositionCategoryEnum.SYNONYME.name());
                    propositionDetail.setLang(synonymProp.getLang());
                    propositionDetail.setValue(synonymProp.getLexical_value());
                    propositionDetail.setOldValue(synonymProp.getOldValue());
                    propositionDetail.setStatus(synonymProp.getStatus());
                    propositionDetail.setHiden(synonymProp.isHiden());
                    propositionDetail.setIdTerm(synonymProp.getIdTerm());
                    new PropositionDetailHelper().createNewPropositionDetail(connect.getPoolConnexion(), propositionDetail);
                }
            }
        }

        if (!CollectionUtils.isEmpty(proposition.getTraductionsProp())) {
            for (TraductionPropBean traductionProp : proposition.getTraductionsProp()) {
                if (traductionProp.isToAdd() || traductionProp.isToUpdate() || traductionProp.isToRemove()) {
                    PropositionDetailDao propositionDetail = new PropositionDetailDao();
                    PropositionActionEnum action;
                    if (traductionProp.isToAdd()) {
                        action = PropositionActionEnum.ADD;
                        propositionDetail.setOldValue(traductionProp.getLexicalValue());
                    } else if (traductionProp.isToRemove()) {
                        action = PropositionActionEnum.DELETE;
                        propositionDetail.setOldValue(traductionProp.getOldValue());
                    } else {
                        action = PropositionActionEnum.UPDATE;
                        propositionDetail.setOldValue(traductionProp.getOldValue());
                    }

                    propositionDetail.setIdProposition(propositionID);
                    propositionDetail.setAction(action.name());
                    propositionDetail.setCategorie(PropositionCategoryEnum.TRADUCTION.name());
                    propositionDetail.setLang(traductionProp.getLang());
                    propositionDetail.setValue(traductionProp.getLexicalValue());
                    propositionDetail.setIdTerm(traductionProp.getIdTerm());
                    new PropositionDetailHelper().createNewPropositionDetail(connect.getPoolConnexion(), propositionDetail);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(proposition.getNote())) {
            sendNote(proposition.getNote(), PropositionCategoryEnum.NOTE.name(), propositionID);
        }

        if (CollectionUtils.isNotEmpty(proposition.getChangeNotes())) {
            sendNote(proposition.getChangeNotes(), PropositionCategoryEnum.CHANGE_NOTE.name(), propositionID);
        }

        if (CollectionUtils.isNotEmpty(proposition.getDefinitions())) {
            sendNote(proposition.getDefinitions(), PropositionCategoryEnum.DEFINITION.name(), propositionID);
        }

        if (CollectionUtils.isNotEmpty(proposition.getEditorialNotes())) {
            sendNote(proposition.getEditorialNotes(), PropositionCategoryEnum.EDITORIAL_NOTE.name(), propositionID);
        }

        if (CollectionUtils.isNotEmpty(proposition.getExamples())) {
            sendNote(proposition.getExamples(), PropositionCategoryEnum.EXAMPLE.name(), propositionID);
        }

        if (CollectionUtils.isNotEmpty(proposition.getHistoryNotes())) {
            sendNote(proposition.getHistoryNotes(), PropositionCategoryEnum.HISTORY.name(), propositionID);
        }

        if (CollectionUtils.isNotEmpty(proposition.getScopeNotes())) {
            sendNote(proposition.getScopeNotes(), PropositionCategoryEnum.SCOPE.name(), propositionID);
        }

        return true;
    }

    private void sendNote(List<NotePropBean> notes, String noteType, Integer propositionID) {
        for (NotePropBean note : notes) {
            if (note.isToAdd() || note.isToUpdate() || note.isToRemove()) {
                PropositionDetailDao propositionDetail = new PropositionDetailDao();
                PropositionActionEnum action;
                if (note.isToAdd()) {
                    action = PropositionActionEnum.ADD;
                    propositionDetail.setOldValue(note.getLexicalvalue());
                } else if (note.isToRemove()) {
                    action = PropositionActionEnum.DELETE;
                    propositionDetail.setOldValue(note.getOldValue());
                } else {
                    action = PropositionActionEnum.UPDATE;
                    propositionDetail.setOldValue(note.getOldValue());
                }

                propositionDetail.setIdProposition(propositionID);
                propositionDetail.setAction(action.name());
                propositionDetail.setCategorie(noteType);
                propositionDetail.setLang(note.getLang());
                propositionDetail.setValue(note.getLexicalvalue());
                propositionDetail.setIdTerm(note.getId_term());
                new PropositionDetailHelper().createNewPropositionDetail(connect.getPoolConnexion(), propositionDetail);
            }
        }
    }

    private String getPath() {
        if (FacesContext.getCurrentInstance() == null) {
            return "";
        }
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        path = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        return path;
    }

    public int searchNbrNewProposition() {
        return new PropositionHelper().searchNbrPorpositoinByStatus(connect.getPoolConnexion(),
                PropositionStatusEnum.ENVOYER.name());
    }

    public boolean sendEmail(String emailDestination, String subject, String contentFile) throws IOException {
        //return true;
        if (currentUser.getNodeUser() != null) {
            if (currentUser.getNodeUser().isAlertMail()) {
                return mailBean.sendMail(emailDestination, subject, contentFile);
            } else {
                return true;
            }
        }
        return mailBean.sendMail(emailDestination, subject, contentFile);
    }

    public void refuserProposition(PropositionDao propositionSelected, String commentaireAdmin) {

        new PropositionHelper().updateStatusProposition(connect.getPoolConnexion(),
                PropositionStatusEnum.REFUSER.name(),
                currentUser.getNodeUser().getName(),
                DATE_FORMAT.format(new Date()),
                propositionSelected.getId(), commentaireAdmin);

        try {
            String subject = "[Opentheso] Résultat de votre proposition";
            String contentFile = "<html><body>"
                    + "Cher(e) " + propositionSelected.getNom() + ", <br/> "
                    + "<p>Votre proposition d’enrichissement sur le concept " + propositionSelected.getNomConcept() + " du thésaurus " + propositionSelected.getThesoName()
                    + " a été refusée par les administrateurs.<br/>"
                    + "Voici leur message: <br/>"
                    + propositionSelected.getAdminComment() + "<br/>"
                    + "N’hésitez pas à faire de nouvelles propositions, nous les étudierons avec attention. <b>"
                    + "<br/><br/> Cordialement,<br/>"
                    + "L'équipe " + propositionSelected.getThesoName()
                    + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            sendEmail(propositionSelected.getEmail(), subject, contentFile);
        } catch (IOException ex) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur detectée pendant l'envoie du mail de notification!");
        }
    }

    public void supprimerPropostion(PropositionDao propositionSelected) {

        new PropositionDetailHelper().supprimerPropositionDetails(connect.getPoolConnexion(), propositionSelected.getId());
        new PropositionHelper().supprimerProposition(connect.getPoolConnexion(), propositionSelected.getId());
    }

    public void insertProposition(Proposition proposition, PropositionDao propositionSelected,
            String commentaireAdmin, boolean prefTermeAccepted, boolean varianteAccepted, boolean traductionAccepted,
            boolean noteAccepted, boolean definitionAccepted, boolean changeNoteAccepted, boolean scopeAccepted,
            boolean editorialNotesAccepted, boolean examplesAccepted, boolean historyAccepted) throws IOException {

        TermHelper termHelper = new TermHelper();

        if (prefTermeAccepted && proposition.isUpdateNomConcept()) {
            Term term = termHelper.getThisTerm(connect.getPoolConnexion(),
                    propositionSelected.getIdConcept(),
                    propositionSelected.getIdTheso(),
                    propositionSelected.getLang());
            term.setLexical_value(proposition.getNomConceptProp());
            termHelper.updateTermTraduction(connect.getPoolConnexion(), term, currentUser.getNodeUser().getIdUser());

            tree.reset();
            tree.initialise(propositionSelected.getIdTheso(), propositionSelected.getLang());
            roleOnThesoBean.initNodePref(propositionSelected.getIdTheso());
            selectedTheso.setSelectedIdTheso(propositionSelected.getIdTheso());
            selectedTheso.setSelectedLang(propositionSelected.getLang());
            selectedTheso.setSelectedThesoForSearch();
            indexSetting.setIsSelectedTheso(true);
        }

        if (varianteAccepted && CollectionUtils.isNotEmpty(proposition.getSynonymsProp())) {
            for (SynonymPropBean synonymPropBean : proposition.getSynonymsProp()) {
                if (synonymPropBean.isToAdd()) {

                    String idTerm = new TermHelper().getIdTermOfConcept(
                            connect.getPoolConnexion(),
                            propositionSelected.getIdConcept(),
                            propositionSelected.getIdTheso());

                    if (!termHelper.addNonPreferredTerm(connect.getPoolConnexion(),
                            idTerm,
                            synonymPropBean.getLexical_value(),
                            synonymPropBean.getLang().toLowerCase(),
                            selectedTheso.getCurrentIdTheso(),
                            "",
                            synonymPropBean.isHiden() ? "Hidden" : "USE",
                            synonymPropBean.isHiden(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La création du nouveau synonyme a échoué !");
                        return;
                    }
                } else if (synonymPropBean.isToRemove()) {

                    if (!termHelper.deleteNonPreferedTerm(connect.getPoolConnexion(),
                            synonymPropBean.getIdTerm(),
                            synonymPropBean.getLang().toLowerCase(),
                            synonymPropBean.getLexical_value(),
                            selectedTheso.getCurrentIdTheso(),
                            synonymPropBean.getStatus(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La suppression du synonyme a échoué !");
                        return;
                    }
                } else if (synonymPropBean.isToUpdate()) {

                    if (!termHelper.updateTermSynonyme(connect.getPoolConnexion(),
                            synonymPropBean.getOldValue(),
                            synonymPropBean.getLexical_value(),
                            synonymPropBean.getIdTerm(),
                            synonymPropBean.getLang().toLowerCase(),
                            selectedTheso.getCurrentIdTheso(),
                            synonymPropBean.isHiden(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La modification du synonyme a échoué !");
                        return;
                    }
                }
            }
        }

        if (traductionAccepted && CollectionUtils.isNotEmpty(proposition.getTraductionsProp())) {
            for (TraductionPropBean traductionProp : proposition.getTraductionsProp()) {
                if (traductionProp.isToAdd()) {
                    if (!termHelper.addTraduction(connect.getPoolConnexion(),
                            traductionProp.getLexicalValue(),
                            traductionProp.getIdTerm(),
                            traductionProp.getLang(),
                            "", "",
                            selectedTheso.getCurrentIdTheso(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "L'ajout d'une traduction a échouée !");
                        return;
                    }
                } else if (traductionProp.isToRemove()) {
                    if (!new TermHelper().deleteTraductionOfTerm(connect.getPoolConnexion(),
                            traductionProp.getIdTerm(),
                            traductionProp.getLexicalValue(),
                            traductionProp.getLang(),
                            selectedTheso.getCurrentIdTheso(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La suppression d'une traduction a échouée !");
                        return;
                    }
                } else if (traductionProp.isToUpdate()) {
                    if (!termHelper.updateTraduction(connect.getPoolConnexion(),
                            traductionProp.getLexicalValue(),
                            traductionProp.getIdTerm(),
                            traductionProp.getLang(),
                            selectedTheso.getCurrentIdTheso(),
                            currentUser.getNodeUser().getIdUser())) {

                        showMessage(FacesMessage.SEVERITY_ERROR, "La modification de la traduction "
                                + traductionProp.getLexicalValue() + " (" + traductionProp.getLang()
                                + ") a échouée !");
                        return;
                    }
                }
            }
        }

        if (noteAccepted && CollectionUtils.isNotEmpty(proposition.getNote())) {
            for (NotePropBean note : proposition.getNote()) {
                if (note.isToAdd()) {
                    addNewNote(note, "note");
                } else if (note.isToUpdate()) {
                    updateNote(note, "note");
                } else if (note.isToRemove()) {
                    deleteNote(note);
                }
            }
        }

        if (changeNoteAccepted && CollectionUtils.isNotEmpty(proposition.getChangeNotes())) {
            for (NotePropBean changeNote : proposition.getChangeNotes()) {
                if (changeNote.isToAdd()) {
                    addNewNote(changeNote, "changeNote");
                } else if (changeNote.isToUpdate()) {
                    updateNote(changeNote, "changeNote");
                } else if (changeNote.isToRemove()) {
                    deleteNote(changeNote);
                }
            }
        }

        if (definitionAccepted && CollectionUtils.isNotEmpty(proposition.getDefinitions())) {
            for (NotePropBean definition : proposition.getDefinitions()) {
                if (definition.isToAdd()) {
                    addNewNote(definition, "definition");
                } else if (definition.isToUpdate()) {
                    updateNote(definition, "definition");
                } else if (definition.isToRemove()) {
                    deleteNote(definition);
                }
            }
        }

        if (editorialNotesAccepted && CollectionUtils.isNotEmpty(proposition.getEditorialNotes())) {
            for (NotePropBean editorialNote : proposition.getEditorialNotes()) {
                if (editorialNote.isToAdd()) {
                    addNewNote(editorialNote, "editorialNote");
                } else if (editorialNote.isToUpdate()) {
                    updateNote(editorialNote, "editorialNote");
                } else if (editorialNote.isToRemove()) {
                    deleteNote(editorialNote);
                }
            }
        }

        if (examplesAccepted && CollectionUtils.isNotEmpty(proposition.getExamples())) {
            for (NotePropBean example : proposition.getExamples()) {
                if (example.isToAdd()) {
                    addNewNote(example, "example");
                } else if (example.isToUpdate()) {
                    updateNote(example, "example");
                } else if (example.isToRemove()) {
                    deleteNote(example);
                }
            }
        }

        if (scopeAccepted && CollectionUtils.isNotEmpty(proposition.getScopeNotes())) {
            for (NotePropBean scopeNote : proposition.getScopeNotes()) {
                if (scopeNote.isToAdd()) {
                    addNewNote(scopeNote, "scopeNote");
                } else if (scopeNote.isToUpdate()) {
                    updateNote(scopeNote, "scopeNote");
                } else if (scopeNote.isToRemove()) {
                    deleteNote(scopeNote);
                }
            }
        }

        if (historyAccepted && CollectionUtils.isNotEmpty(proposition.getHistoryNotes())) {
            for (NotePropBean history : proposition.getHistoryNotes()) {
                if (history.isToAdd()) {
                    addNewNote(history, "historyNote");
                } else if (history.isToUpdate()) {
                    updateNote(history, "historyNote");
                } else if (history.isToRemove()) {
                    deleteNote(history);
                }
            }
        }

        new ConceptHelper().updateDateOfConcept(connect.getPoolConnexion(),
                propositionSelected.getIdTheso(),
                propositionSelected.getLang(),
                currentUser.getNodeUser().getIdUser());
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                propositionSelected.getIdConcept(), propositionSelected.getIdTheso());
        ///////////////  

        new PropositionHelper().updateStatusProposition(connect.getPoolConnexion(),
                PropositionStatusEnum.APPROUVER.name(),
                currentUser.getNodeUser().getName(),
                DATE_FORMAT.format(new Date()),
                propositionSelected.getId(), commentaireAdmin);

        conceptView.getConcept(propositionSelected.getIdTheso(), propositionSelected.getIdConcept(),
                propositionSelected.getLang());

        try {
            String subject = "[Opentheso] Résultat de votre proposition";
            String contentFile = "<html><body>"
                    + "Cher(e) " + propositionSelected.getNom() + ", <br/> "
                    + "<p>Votre proposition d'enrichissement sur le concept " + propositionSelected.getNomConcept()
                    + " du thésaurus " + propositionSelected.getThesoName()
                    + " a été acceptée par les administrateurs.<br/>"
                    + " voici leur message : "
                    + propositionSelected.getAdminComment()
                    + "<br/>"
                    + "N’hésitez pas à faire de nouvelles propositions, nous les étudierons avec attention."
                    + "<br/><br/> Cordialement,<br/>"
                    + "L'équipe " + propositionSelected.getThesoName() + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            sendEmail(propositionSelected.getEmail(), subject, contentFile);
        } catch (IOException ex) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur detectée pendant l'envoie du mail de notification!");
        }
    }

    private void deleteNote(NotePropBean notePropBean) {
        if (!new NoteHelper().deleteThisNote(connect.getPoolConnexion(),
                notePropBean.getId_note(),
                notePropBean.getId_concept(),
                notePropBean.getLang(),
                selectedTheso.getCurrentIdTheso(),
                notePropBean.getNotetypecode(),
                notePropBean.getLexicalvalue(),
                currentUser.getNodeUser().getIdUser())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    private void updateNote(NotePropBean notePropBean, String typeNote) {
        if (!new NoteHelper().updateNote(connect.getPoolConnexion(),
                notePropBean.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                notePropBean.getId_concept(),
                notePropBean.getLang(),
                selectedTheso.getCurrentIdTheso(),
                notePropBean.getLexicalvalue(),
                notePropBean.getNotetypecode(),
                currentUser.getNodeUser().getIdUser())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    private void addNewNote(NotePropBean notePropBean, String typeNote) {
        if (!new NoteHelper().addNote(
                connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                notePropBean.getLang(),
                selectedTheso.getCurrentIdTheso(),
                notePropBean.getLexicalvalue(),
                typeNote, "",
                currentUser.getNodeUser().getIdUser())) {

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", "Erreur pendant l'ajout d'une nouvelle note !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public boolean updateNomConcept(String newConceptName) {
        TermHelper termHelper = new TermHelper();

        // vérification si le term à ajouter existe déjà, s oui, on a l'Id, sinon, on a Null
        String idTerm = termHelper.isTermEqualTo(connect.getPoolConnexion(),
                newConceptName, selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());

        if (idTerm != null) {
            String label = termHelper.getLexicalValue(connect.getPoolConnexion(), idTerm,
                    selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", label + " : existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }

        if (termHelper.isAltLabelExist(connect.getPoolConnexion(), idTerm,
                selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " un synonyme existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
    }

    private void showMessage(FacesMessage.Severity type, String message) {
        FacesMessage msg = new FacesMessage(type, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public Proposition selectProposition(NodeConcept nodeConcept) {
        Proposition proposition = new Proposition();
        proposition.setConceptID(nodeConcept.getConcept().getIdConcept());
        proposition.setUpdateNomConcept(false);
        proposition.setNomConceptProp(null);
        proposition.setNomConcept(nodeConcept.getTerm());
        proposition.setSynonymsProp(toSynonymPropBean(nodeConcept.getNodeEM(),
                conceptView.getNodeConcept().getTerm().getId_term()));
        proposition.setTraductionsProp(toTraductionPropBeans(nodeConcept.getNodeTermTraductions(),
                nodeConcept.getTerm().getId_term()));

        for (NodeNote nodeNote : nodeConcept.getNodeNotes()) {
            switch (nodeNote.getNotetypecode()) {
                case "note":
                    proposition.getNote().add(toNotePropBean(nodeNote));
                    break;
                case "scopeNote":
                    proposition.getScopeNotes().add(toNotePropBean(nodeNote));
                    break;
                case "changeNote":
                    proposition.getChangeNotes().add(toNotePropBean(nodeNote));
                    break;
                case "definition":
                    proposition.getDefinitions().add(toNotePropBean(nodeNote));
                    break;
                case "editorialNote":
                    proposition.getEditorialNotes().add(toNotePropBean(nodeNote));
                    break;
                case "example":
                    proposition.getExamples().add(toNotePropBean(nodeNote));
                    break;
                case "historyNote":
                    proposition.getHistoryNotes().add(toNotePropBean(nodeNote));
                    break;
            }
        }

        return proposition;
    }

    private NotePropBean toNotePropBean(NodeNote nodeNote) {
        NotePropBean notePropBean = new NotePropBean();
        notePropBean.setModified(nodeNote.getModified());
        notePropBean.setCreated(nodeNote.getCreated());
        notePropBean.setIdUser(nodeNote.getIdUser());
        notePropBean.setId_concept(nodeNote.getId_concept());
        notePropBean.setId_note(nodeNote.getId_note());
        notePropBean.setId_term(nodeNote.getId_term());
        notePropBean.setLang(nodeNote.getLang());
        notePropBean.setLexicalvalue(nodeNote.getLexicalvalue());
        notePropBean.setOldValue(nodeNote.getLexicalvalue());
        notePropBean.setNotetypecode(nodeNote.getNotetypecode());
        notePropBean.setUser(nodeNote.getUser());
        return notePropBean;
    }

    public void  preparerPropositionSelect(Proposition proposition, PropositionDao propositionDao) {
        List<PropositionDetailDao> propositionDetails = new PropositionDetailHelper()
                .getPropositionDetail(connect.getPoolConnexion(), propositionDao.getId());

        proposition.setConceptID(conceptView.getNodeConcept().getConcept().getIdConcept());
        proposition.setNomConceptProp(null);
        proposition.setNomConcept(conceptView.getNodeConcept().getTerm());

        proposition.setSynonymsProp(toSynonymPropBean(conceptView.getNodeConcept().getNodeEM(),
                conceptView.getNodeConcept().getTerm().getId_term()));

        proposition.setTraductionsProp(toTraductionPropBeans(conceptView.getNodeConcept().getNodeTermTraductions(),
                conceptView.getNodeConcept().getTerm().getId_term()));

        for (NodeNote nodeNote : conceptView.getNodeConcept().getNodeNotes()) {
            switch (nodeNote.getNotetypecode()) {
                case "note":
                    proposition.getNote().add(toNotePropBean(nodeNote));
                    break;
                case "scopeNote":
                    proposition.getScopeNotes().add(toNotePropBean(nodeNote));
                    break;
                case "changeNote":
                    proposition.getChangeNotes().add(toNotePropBean(nodeNote));
                    break;
                case "definition":
                    proposition.getDefinitions().add(toNotePropBean(nodeNote));
                    break;
                case "editorialNote":
                    proposition.getEditorialNotes().add(toNotePropBean(nodeNote));
                    break;
                case "example":
                    proposition.getExamples().add(toNotePropBean(nodeNote));
                    break;
                case "historyNote":
                    proposition.getHistoryNotes().add(toNotePropBean(nodeNote));
                    break;                    
            }
        }

        for (PropositionDetailDao propositionDetailDao : propositionDetails) {
            if (PropositionCategoryEnum.NOM.name().equals(propositionDetailDao.getCategorie())) {
                proposition.setUpdateNomConcept(true);
                proposition.setNomConceptProp(propositionDetailDao.getValue());
            } else if (PropositionCategoryEnum.SYNONYME.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    SynonymPropBean propAddSyn = new SynonymPropBean();
                    propAddSyn.setLexical_value(propositionDetailDao.getValue());
                    propAddSyn.setToAdd(true);
                    propAddSyn.setIdUser(currentUser.getNodeUser().getIdUser() + "");
                    propAddSyn.setLang(propositionDetailDao.getLang());
                    propAddSyn.setStatus(propositionDetailDao.getStatus());
                    propAddSyn.setIdTerm(propositionDetailDao.getIdTerm());
                    propAddSyn.setHiden(propositionDetailDao.isHiden());
                    proposition.getSynonymsProp().add(propAddSyn);
                }

                for (int i = 0; i < proposition.getSynonymsProp().size(); i++) {
                    if (proposition.getSynonymsProp().get(i).getLexical_value().equals(propositionDetailDao.getValue())) {
                        if (PropositionActionEnum.DELETE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getSynonymsProp().get(i).setToRemove(true);
                        } else if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                            proposition.getSynonymsProp().get(i).setToAdd(true);
                        } else {
                            proposition.getSynonymsProp().get(i).setToUpdate(false);
                            proposition.getSynonymsProp().get(i).setToRemove(false);
                            proposition.getSynonymsProp().get(i).setToAdd(false);
                        }
                    } else if (proposition.getSynonymsProp().get(i).getLexical_value().equals(propositionDetailDao.getOldValue())
                            && proposition.getSynonymsProp().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        proposition.getSynonymsProp().get(i).setToUpdate(true);
                        proposition.getSynonymsProp().get(i).setLexical_value(propositionDetailDao.getValue());
                    }
                }
            } else if (PropositionCategoryEnum.TRADUCTION.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    TraductionPropBean traductionPropBean = new TraductionPropBean();
                    traductionPropBean.setLexicalValue(propositionDetailDao.getValue());
                    traductionPropBean.setToAdd(true);
                    traductionPropBean.setLang(propositionDetailDao.getLang());
                    traductionPropBean.setIdTerm(propositionDetailDao.getIdTerm());
                    proposition.getTraductionsProp().add(traductionPropBean);
                }

                for (int i = 0; i < proposition.getTraductionsProp().size(); i++) {
                    if (proposition.getTraductionsProp().get(i).getOldValue() != null
                            && proposition.getTraductionsProp().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getTraductionsProp().get(i).setToUpdate(true);
                            proposition.getTraductionsProp().get(i).setLexicalValue(propositionDetailDao.getValue());
                        } else {
                            proposition.getTraductionsProp().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.NOTE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalvalue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setId_term(propositionDetailDao.getIdTerm());
                    proposition.getNote().add(notePropBean);
                }

                for (int i = 0; i < proposition.getNote().size(); i++) {
                    if (proposition.getNote().get(i).getOldValue() != null
                            && proposition.getNote().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getNote().get(i).setToUpdate(true);
                            proposition.getNote().get(i).setLexicalvalue(propositionDetailDao.getValue());
                        } else {
                            proposition.getNote().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.CHANGE_NOTE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalvalue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setId_term(propositionDetailDao.getIdTerm());
                    proposition.getChangeNotes().add(notePropBean);
                }

                for (int i = 0; i < proposition.getChangeNotes().size(); i++) {
                    if (proposition.getChangeNotes().get(i).getOldValue() != null
                            && proposition.getChangeNotes().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getChangeNotes().get(i).setToUpdate(true);
                            proposition.getChangeNotes().get(i).setLexicalvalue(propositionDetailDao.getValue());
                        } else {
                            proposition.getChangeNotes().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.DEFINITION.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalvalue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setId_term(propositionDetailDao.getIdTerm());
                    proposition.getDefinitions().add(notePropBean);
                }

                for (int i = 0; i < proposition.getDefinitions().size(); i++) {
                    if (proposition.getDefinitions().get(i).getOldValue() != null
                            && proposition.getDefinitions().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getDefinitions().get(i).setToUpdate(true);
                            proposition.getDefinitions().get(i).setLexicalvalue(propositionDetailDao.getValue());
                        } else {
                            proposition.getDefinitions().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.EDITORIAL_NOTE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalvalue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setId_term(propositionDetailDao.getIdTerm());
                    proposition.getEditorialNotes().add(notePropBean);
                }

                for (int i = 0; i < proposition.getEditorialNotes().size(); i++) {
                    if (proposition.getEditorialNotes().get(i).getOldValue() != null
                            && proposition.getEditorialNotes().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getEditorialNotes().get(i).setToUpdate(true);
                            proposition.getEditorialNotes().get(i).setLexicalvalue(propositionDetailDao.getValue());
                        } else {
                            proposition.getEditorialNotes().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.HISTORY.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalvalue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setId_term(propositionDetailDao.getIdTerm());
                    proposition.getHistoryNotes().add(notePropBean);
                }

                for (int i = 0; i < proposition.getHistoryNotes().size(); i++) {
                    if (proposition.getHistoryNotes().get(i).getOldValue() != null
                            && proposition.getHistoryNotes().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getHistoryNotes().get(i).setToUpdate(true);
                            proposition.getHistoryNotes().get(i).setLexicalvalue(propositionDetailDao.getValue());
                        } else {
                            proposition.getHistoryNotes().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.SCOPE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalvalue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setId_term(propositionDetailDao.getIdTerm());
                    proposition.getScopeNotes().add(notePropBean);
                }

                for (int i = 0; i < proposition.getScopeNotes().size(); i++) {
                    if (proposition.getScopeNotes().get(i).getOldValue() != null
                            && proposition.getScopeNotes().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getScopeNotes().get(i).setToUpdate(true);
                            proposition.getScopeNotes().get(i).setLexicalvalue(propositionDetailDao.getValue());
                        } else {
                            proposition.getScopeNotes().get(i).setToRemove(true);
                        }
                    }
                }
            } else if (PropositionCategoryEnum.EXAMPLE.name().equals(propositionDetailDao.getCategorie())) {

                if (PropositionActionEnum.ADD.name().equals(propositionDetailDao.getAction())) {
                    NotePropBean notePropBean = new NotePropBean();
                    notePropBean.setLexicalvalue(propositionDetailDao.getValue());
                    notePropBean.setToAdd(true);
                    notePropBean.setLang(propositionDetailDao.getLang());
                    notePropBean.setId_term(propositionDetailDao.getIdTerm());
                    proposition.getExamples().add(notePropBean);
                }

                for (int i = 0; i < proposition.getExamples().size(); i++) {
                    if (proposition.getExamples().get(i).getOldValue() != null
                            && proposition.getExamples().get(i).getLang().equals(propositionDetailDao.getLang())) {
                        if (PropositionActionEnum.UPDATE.name().equals(propositionDetailDao.getAction())) {
                            proposition.getExamples().get(i).setToUpdate(true);
                            proposition.getExamples().get(i).setLexicalvalue(propositionDetailDao.getValue());
                        } else {
                            proposition.getExamples().get(i).setToRemove(true);
                        }
                    }
                }
            }
        }

        if (propositionDao.getStatus().equals(PropositionStatusEnum.ENVOYER.name())) {
            new PropositionHelper().setLuStatusProposition(connect.getPoolConnexion(), propositionDao.getId());
        }
    }

    private List<TraductionPropBean> toTraductionPropBeans(List<NodeTermTraduction> nodesTermTraductions, String idTerm) {
        List<TraductionPropBean> traduction = new ArrayList<>();
        for (NodeTermTraduction nodeTermTraduction : nodesTermTraductions) {
            TraductionPropBean traductionPropBean = new TraductionPropBean();
            traductionPropBean.setLang(nodeTermTraduction.getLang());
            traductionPropBean.setLexicalValue(nodeTermTraduction.getLexicalValue());
            traductionPropBean.setOldValue(nodeTermTraduction.getLexicalValue());
            traductionPropBean.setIdTerm(idTerm);
            traduction.add(traductionPropBean);
        }
        return traduction;
    }

    public List<SynonymPropBean> toSynonymPropBean(List<NodeEM> nodesEm, String idTerm) {
        List<SynonymPropBean> synonyms = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(nodesEm)) {
            for (NodeEM nodeEM : nodesEm) {
                SynonymPropBean synonymPropBean = new SynonymPropBean();
                synonymPropBean.setAction(nodeEM.getAction());
                synonymPropBean.setCreated(nodeEM.getCreated());
                synonymPropBean.setModified(nodeEM.getModified());
                synonymPropBean.setHiden(nodeEM.isHiden());
                synonymPropBean.setOldHiden(nodeEM.isHiden());
                synonymPropBean.setLang(nodeEM.getLang());
                synonymPropBean.setLexical_value(nodeEM.getLexical_value());
                synonymPropBean.setOldValue(nodeEM.getLexical_value());
                synonymPropBean.setSource(nodeEM.getSource());
                synonymPropBean.setStatus(nodeEM.getStatus());
                synonymPropBean.setIdTerm(idTerm);
                synonyms.add(synonymPropBean);
            }
        }

        return synonyms;
    }

    public List<PropositionDao> searchAllPropositions(String idTheso) {
        List<PropositionDao> propositions = new ArrayList<>();
        PropositionHelper propositionHelper = new PropositionHelper();
        propositions.addAll(searchPropositionsNonTraitter(idTheso));
        propositions.addAll(propositionHelper.getAllPropositionByStatus(connect.getPoolConnexion(),
                PropositionStatusEnum.APPROUVER.name(), idTheso));
        propositions.addAll(propositionHelper.getAllPropositionByStatus(connect.getPoolConnexion(),
                PropositionStatusEnum.REFUSER.name(), idTheso));
        return propositions;
    }

    public List<PropositionDao> searchPropositionsNonTraitter(String idTheso) {
        List<PropositionDao> propositions = new ArrayList<>();
        propositions.addAll(new PropositionHelper().getAllPropositionByStatus(connect.getPoolConnexion(),
                PropositionStatusEnum.ENVOYER.name(), idTheso));
        propositions.addAll(new PropositionHelper().getAllPropositionByStatus(connect.getPoolConnexion(),
                PropositionStatusEnum.LU.name(), idTheso));
        return propositions;
    }

    public List<PropositionDao> searchOldPropositions(String idTheso) {
        return new PropositionHelper().getOldPropositionByStatus(connect.getPoolConnexion(), idTheso);
    }

}
