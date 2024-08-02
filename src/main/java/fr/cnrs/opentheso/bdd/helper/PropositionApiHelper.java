package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDao;
import fr.cnrs.opentheso.bean.proposition.dao.PropositionDetailDao;
import fr.cnrs.opentheso.bean.proposition.helper.PropositionDetailHelper;
import fr.cnrs.opentheso.bean.proposition.helper.PropositionHelper;
import fr.cnrs.opentheso.bean.proposition.model.PropositionActionEnum;
import fr.cnrs.opentheso.bean.proposition.model.PropositionCategoryEnum;
import fr.cnrs.opentheso.bean.proposition.model.PropositionFromApi;
import fr.cnrs.opentheso.bean.proposition.model.PropositionStatusEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import jakarta.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;

import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;


public class PropositionApiHelper {

    public Response createProposition(PropositionFromApi proposition, int userId) {

        try (var ds = connect()) {

            var user = new UserHelper().getUser(ds, userId);
            var thesaurusLang = new PreferencesHelper().getWorkLanguageOfTheso(ds, proposition.getIdTheso());

            var propositionId = saveProposition(ds, proposition, user, thesaurusLang);

            saveTerme(proposition, ds, propositionId, thesaurusLang);

            if (CollectionUtils.isNotEmpty(proposition.getTraductionsProp())) {
                saveTraductions(proposition, propositionId, ds, thesaurusLang);
            }

            if (!CollectionUtils.isEmpty(proposition.getSynonymsProp())) {
                saveSynonymes(proposition, propositionId, ds);
            }

            if (CollectionUtils.isNotEmpty(proposition.getNotes())) {
                for (NotePropBean note : proposition.getDefinitions()) {
                    noteManagement(ds, propositionId, note, PropositionCategoryEnum.NOTE.name());
                }
            }

            if (CollectionUtils.isNotEmpty(proposition.getDefinitions())) {
                for (NotePropBean definition : proposition.getDefinitions()) {
                    noteManagement(ds, propositionId, definition, PropositionCategoryEnum.DEFINITION.name());
                }
            }
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Integer saveProposition(HikariDataSource ds, PropositionFromApi proposition, NodeUser user, String thesaurusLang) {

        var thesaurusName = new ThesaurusHelper().getTitleOfThesaurus(ds, proposition.getIdTheso(), thesaurusLang);

        var propositionDao = PropositionDao.builder()
                .nom(user.getName())
                .email(user.getMail())
                .commentaire(proposition.getCommentaire())
                .datePublication(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()))
                .idConcept(proposition.getConceptID())
                .idTheso(proposition.getIdTheso())
                .lang(thesaurusLang)
                .status(PropositionStatusEnum.ENVOYER.name())
                .thesoName(thesaurusName)
                .build();
        return new PropositionHelper().createNewProposition(ds, propositionDao);
    }

    private void saveTerme(PropositionFromApi proposition, HikariDataSource ds, Integer propositionId, String thesaurusLang) {
        var prefLabel = proposition.getTraductionsProp().stream()
                .filter(element -> element.getLang().equals(thesaurusLang))
                .findFirst();
        if (prefLabel.isPresent()) {
            var propositionDetail = PropositionDetailDao.builder()
                    .idProposition(propositionId)
                    .action(PropositionActionEnum.UPDATE.name())
                    .categorie(PropositionCategoryEnum.NOM.name())
                    .lang(prefLabel.get().getLang())
                    .value(prefLabel.get().getLexicalValue())
                    .oldValue(prefLabel.get().getOldValue())
                    .build();
            new PropositionDetailHelper().createNewPropositionDetail(ds, propositionDetail);
        }
    }

    private void saveTraductions(PropositionFromApi proposition, Integer propositionId, HikariDataSource ds, String thesaurusLang) {

        var traduction = proposition.getTraductionsProp().stream()
                .filter(element -> !element.getLang().equals(thesaurusLang))
                .findFirst();
        if (traduction.isPresent()) {
            if (traduction.get().isToAdd() || traduction.get().isToUpdate() || traduction.get().isToRemove()) {
                var propositionDetail = new PropositionDetailDao();
                PropositionActionEnum action;
                if (traduction.get().isToAdd()) {
                    action = PropositionActionEnum.ADD;
                    propositionDetail.setOldValue(traduction.get().getLexicalValue());
                } else if (traduction.get().isToRemove()) {
                    action = PropositionActionEnum.DELETE;
                    propositionDetail.setOldValue(traduction.get().getOldValue());
                } else {
                    action = PropositionActionEnum.UPDATE;
                    propositionDetail.setOldValue(traduction.get().getOldValue());
                }

                var idTerm = new TermHelper().getIdTermOfConcept(ds, proposition.getConceptID(), proposition.getIdTheso());
                if (StringUtils.isNotEmpty(idTerm)) {
                    propositionDetail.setIdProposition(propositionId);
                    propositionDetail.setAction(action.name());
                    propositionDetail.setCategorie(PropositionCategoryEnum.TRADUCTION.name());
                    propositionDetail.setLang(traduction.get().getLang());
                    propositionDetail.setValue(traduction.get().getLexicalValue());
                    propositionDetail.setIdTerm(idTerm);
                    new PropositionDetailHelper().createNewPropositionDetail(ds, propositionDetail);
                }
            }
        }
    }

    private void saveSynonymes(PropositionFromApi proposition, Integer propositionId, HikariDataSource ds) {
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

                var propositionDetail = PropositionDetailDao.builder()
                        .idProposition(propositionId)
                        .action(action.name())
                        .categorie(PropositionCategoryEnum.SYNONYME.name())
                        .lang(synonymProp.getLang())
                        .value(synonymProp.getLexical_value())
                        .oldValue(synonymProp.getOldValue())
                        .status(synonymProp.getStatus())
                        .hiden(synonymProp.isHiden())
                        .idTerm(synonymProp.getIdTerm())
                        .build();
                new PropositionDetailHelper().createNewPropositionDetail(ds, propositionDetail);
            }
        }
    }

    private void noteManagement(HikariDataSource connexion, int propositionID, NotePropBean note, String category) {
        if (note.isToAdd() || note.isToUpdate() || note.isToRemove()) {
            var propositionDetail = new PropositionDetailDao();
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
            propositionDetail.setCategorie(category);
            propositionDetail.setLang(note.getLang());
            propositionDetail.setValue(note.getLexicalvalue());
            propositionDetail.setIdTerm(note.getId_term());
            new PropositionDetailHelper().createNewPropositionDetail(connexion, propositionDetail);
        }
    }

}
