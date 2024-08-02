package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bean.candidat.dao.TermeDao;
import fr.cnrs.opentheso.bean.candidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import java.io.IOException;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;

@Named(value = "traductionService")
@SessionScoped
public class TraductionService implements Serializable {

    @Inject
    private CandidatBean candidatBean;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private SelectedTheso selectedTheso;

    private String langage;
    private String traduction;

    private String langageOld;
    private String traductionOld;

    private String newLangage;
    private String newTraduction;

    private ArrayList<NodeLangTheso> nodeLangs;
    private ArrayList<NodeLangTheso> nodeLangsFiltered; // uniquement les langues non traduits    

    private StringBuilder messages;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeLangs != null) {
            nodeLangs.clear();
            nodeLangs = null;
        }
        if (nodeLangsFiltered != null) {
            nodeLangsFiltered.clear();
            nodeLangsFiltered = null;
        }
        langage = null;
        traduction = null;
        langageOld = null;
        traductionOld = null;
        newLangage = null;
        newTraduction = null;
        messages = null;
    }

    public TraductionService() {
        langage = null;
        traduction = null;
        messages = new StringBuilder();
    }

    /**
     * set pour la modification d'une tradcution
     *
     * @param traductionDto
     */
    public void init(TraductionDto traductionDto) {
        langage = traductionDto.getLangue();
        traduction = traductionDto.getTraduction();

        langageOld = langage;
        traductionOld = traduction;
    }

    /**
     * set pour une nouvelle traduction
     */
    public void init() {
        newLangage = "";
        newTraduction = "";
        nodeLangsFiltered = new ArrayList<>();
        initLanguages();
    }

    private void initLanguages() {
        nodeLangs = selectedTheso.getNodeLangs();

        nodeLangs.forEach((nodeLang) -> {
            nodeLangsFiltered.add(nodeLang);
        });

        // les langues à ignorer
        ArrayList<String> langsToRemove = new ArrayList<>();
        langsToRemove.add(candidatBean.getCandidatSelected().getLang());
        for (TraductionDto traductionDto : candidatBean.getCandidatSelected().getTraductions()) {
            langsToRemove.add(traductionDto.getLangue());
        }
        for (NodeLangTheso nodeLang : nodeLangs) {
            if (langsToRemove.contains(nodeLang.getCode())) {
                nodeLangsFiltered.remove(nodeLang);
            }
        }

    }

    public void addTraductionCandidat() {
        TermHelper termHelper = new TermHelper();

        if (termHelper.isTermExistIgnoreCase(candidatBean.getConnect().getPoolConnexion(), newTraduction,
                candidatBean.getCandidatSelected().getIdThesaurus(), newLangage)) {
            messages = new StringBuilder();
            messages.append("Un label existe dans le thésaurus pour : ").append(candidatBean.getCandidatSelected().getIdConcepte())
                    .append("#").append(newTraduction).append(" (").append(langage).append(")");
            candidatBean.showMessage(FacesMessage.SEVERITY_ERROR, messages.toString());
            return;
        }
        TermeDao termeDao = new TermeDao();
        Term term = new Term();
        term.setStatus("D");
        term.setSource("Candidat");
        term.setLang(newLangage);
        term.setLexical_value(newTraduction);
        term.setId_thesaurus(candidatBean.getCandidatSelected().getIdThesaurus());
        term.setContributor(candidatBean.getCandidatSelected().getUserId());
        term.setCreator(candidatBean.getCandidatSelected().getUserId());
        term.setId_term(candidatBean.getCandidatSelected().getIdTerm());

        try {
            termeDao.addNewTerme(candidatBean.getConnect().getPoolConnexion(), term);
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg1"));
        } catch (SQLException | IOException ex) {
            Logger.getLogger(TraductionService.class.getName()).log(Level.SEVERE, null, ex);
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, ex.getMessage());

            messages.append("erreur pour :").append(newTraduction).append(" (").append(langage).append(")");
            messages.append('\n');
        }

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex");

    }

    /**
     * permet de supprimer une tradcution #MR
     */
    public void deleteTraduction() {
        TermeDao termeDao = new TermeDao();
        try {
            termeDao.deleteTermByIdTermAndLang(candidatBean.getConnect().getPoolConnexion(),
                    candidatBean.getCandidatSelected().getIdTerm(),
                    langage,
                    candidatBean.getCandidatSelected().getIdThesaurus());
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg2"));
        } catch (SQLException | IOException ex) {
            Logger.getLogger(TraductionService.class.getName()).log(Level.SEVERE, null, ex);
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, ex.getMessage());
        }

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex");
    }

    /**
     * permet de modifier une traduction #MR
     */
    public void updateTraduction() {
        TermeDao termeDao = new TermeDao();
        try {
            termeDao.updateIntitule(candidatBean.getConnect().getPoolConnexion(),
                    traduction,
                    candidatBean.getCandidatSelected().getIdTerm(),
                    candidatBean.getCandidatSelected().getIdThesaurus(),
                    langage);
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("candidat.traduction.msg3"));
        } catch (SQLException | IOException ex) {
            Logger.getLogger(TraductionService.class.getName()).log(Level.SEVERE, null, ex);
            candidatBean.showMessage(FacesMessage.SEVERITY_INFO, ex.getMessage());
        }

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex");
    }

    public String getLangage() {
        return langage;
    }

    public void setLangage(String langage) {
        this.langage = langage;
    }

    public String getTraduction() {
        return traduction;
    }

    public void setTraduction(String traduction) {
        this.traduction = traduction;
    }

    public String getLangageOld() {
        return langageOld;
    }

    public void setLangageOld(String langageOld) {
        this.langageOld = langageOld;
    }

    public String getTraductionOld() {
        return traductionOld;
    }

    public void setTraductionOld(String traductionOld) {
        this.traductionOld = traductionOld;
    }

    public String getNewLangage() {
        return newLangage;
    }

    public void setNewLangage(String newLangage) {
        this.newLangage = newLangage;
    }

    public String getNewTraduction() {
        return newTraduction;
    }

    public void setNewTraduction(String newTraduction) {
        this.newTraduction = newTraduction;
    }

    public ArrayList<NodeLangTheso> getNodeLangsFiltered() {
        return nodeLangsFiltered;
    }

    public void setNodeLangsFiltered(ArrayList<NodeLangTheso> nodeLangsFiltered) {
        this.nodeLangsFiltered = nodeLangsFiltered;
    }

    public String getMessages() {
        return messages.toString();
    }
}
