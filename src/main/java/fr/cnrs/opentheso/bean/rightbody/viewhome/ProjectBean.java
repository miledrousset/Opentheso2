package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.bdd.datas.Languages_iso639;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.StatisticHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.ProjectDescription;
import fr.cnrs.opentheso.repositories.GpsRepository;
import fr.cnrs.opentheso.repositories.ProjectDescriptionRepository;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;


@Data
@SessionScoped
@Named(value = "projectBean")
public class ProjectBean implements Serializable {

    @Inject private Connect connect;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private GpsRepository gpsRepository;
    @Inject private LanguageBean languageBean;
    @Inject private ProjectDescriptionRepository projectDescriptionRepository;
    @Inject private CurrentUser currentUser;

    private String description, langCode, langCodeSelected, projectIdSelected;
    private boolean projectDescription, editingHomePage, isButtonEnable;
    private ProjectDescription projectDescriptionSelected;
    private List<NodeIdValue> listeThesoOfProject;
    private List<Languages_iso639> allLangs, selectedLangs;


    public void initProject(String projectIdSelected, boolean isPrivate) {
        this.projectIdSelected = projectIdSelected;
        selectedLangs = new LanguageHelper().getLanguagesByProject(connect.getPoolConnexion(), projectIdSelected);
        projectDescription = CollectionUtils.isNotEmpty(selectedLangs);

        projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(projectIdSelected, getLang());

        if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
            if (CollectionUtils.isNotEmpty(selectedLangs)) {
                projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(projectIdSelected,
                        selectedLangs.get(0).getId_iso639_1());

                if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
                    projectDescriptionSelected = new ProjectDescription();
                    projectDescriptionSelected.setLang(getLang());
                    projectDescriptionSelected.setIdGroup(projectIdSelected);
                    projectDescriptionSelected.setLang(getLang());
                }
            } else {
                projectDescriptionSelected = new ProjectDescription();
                projectDescriptionSelected.setLang(getLang());
                projectDescriptionSelected.setIdGroup(projectIdSelected);
                projectDescriptionSelected.setLang(getLang());
            }
        }

        if (ObjectUtils.isNotEmpty(projectDescriptionSelected)) {
            description = projectDescriptionSelected.getDescription();
            langCodeSelected = projectDescriptionSelected.getLang();
        }
        listeThesoOfProject = currentUser.getUserPermissions().getListThesos();
                //new UserHelper().getThesaurusOfProject(connect.getPoolConnexion(),
                //Integer.parseInt(projectIdSelected), connect.getWorkLanguage(), isPrivate);

        for (NodeIdValue element : listeThesoOfProject) {
            try {
                element.setNbrConcepts(new StatisticHelper().getNbCpt(connect.getPoolConnexion(),
                        element.getId()));
            } catch(Exception ex) {
                element.setNbrConcepts(0);
            }
        }
    }

    /**
     * pour effacer toutes les données des variables
     */
    public void reset(){
        projectDescriptionSelected = null;
    }

    public void init() {
        editingHomePage = false;
        isButtonEnable = true;
    }

    public void back() {
        projectDescription = true;
        editingHomePage = false;
        isButtonEnable = true;
    }

    public void setEditPage() {
        if (CollectionUtils.isEmpty(allLangs)) {
            allLangs = new LanguageHelper().getAllLanguages(connect.getPoolConnexion());
        }

        projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(projectIdSelected, langCodeSelected);
        if (!ObjectUtils.isEmpty(projectDescriptionSelected)) {
            description = projectDescriptionSelected.getDescription();
            langCode = projectDescriptionSelected.getLang();
        } else {
            projectDescriptionSelected = new ProjectDescription();
            projectDescriptionSelected.setLang(getLang());
            projectDescriptionSelected.setIdGroup(projectIdSelected);
            langCode = getLang();
        }

        projectDescription = false;
        editingHomePage = true;
        isButtonEnable = false;
    }

    public void updateHomePage() {

        if (StringUtils.isEmpty(description)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Veuillez saisir une description !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        if (description.equalsIgnoreCase(projectDescriptionSelected.getDescription())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Veuillez proposer une présentation différente de l'ancienne !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            PrimeFaces.current().ajax().update("messageIndex");
            return;
        }

        projectDescriptionSelected.setDescription(description);
        projectDescriptionSelected.setLang(langCode);

        if (projectDescriptionSelected.getId() != null) {
            projectDescriptionRepository.updateProjectDescription(projectDescriptionSelected);
        } else {
            projectDescriptionRepository.saveProjectDescription(projectDescriptionSelected);
        }

        langCodeSelected = projectDescriptionSelected.getLang();

        selectedLangs = new LanguageHelper().getLanguagesByProject(connect.getPoolConnexion(),
                projectDescriptionSelected.getIdGroup());
        projectDescription = CollectionUtils.isNotEmpty(selectedLangs);
        editingHomePage = false;
        isButtonEnable = true;

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Description ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex");
    }

    public void deleteDescription() {

        projectDescriptionRepository.removeProjectDescription(projectDescriptionSelected);

        selectedLangs = new LanguageHelper().getLanguagesByProject(connect.getPoolConnexion(),
                projectDescriptionSelected.getIdGroup());

        if (CollectionUtils.isNotEmpty(selectedLangs)) {
            projectDescription = true;
            projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(
                    projectDescriptionSelected.getIdGroup(), selectedLangs.get(0).getId_iso639_1());

            projectDescription = true;
            langCodeSelected = selectedLangs.get(0).getId_iso639_1();
            description = projectDescriptionSelected.getDescription();
        } else {
            projectDescription = false;
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Description supprimée avec succès !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public Boolean isListThesoVisible() {
        return CollectionUtils.isNotEmpty(listeThesoOfProject);
    }

    private String getLang() {
        String lang = languageBean.getIdLangue().toLowerCase();
        if (StringUtils.isEmpty(lang)) {
            lang = connect.getWorkLanguage();
        }
        return lang;
    }

    public String getLabel(NodeIdValue nodeIdValue) {
        return nodeIdValue.getValue () + " (" + nodeIdValue.getNbrConcepts() + " concepts)";
    }

    public void changeLangListener() {
        projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(selectedTheso.getProjectIdSelected(), langCode);
        if (ObjectUtils.isEmpty(projectDescriptionSelected)) {
            projectDescriptionSelected = new ProjectDescription();
            projectDescriptionSelected.setLang(langCode);
            projectDescriptionSelected.setIdGroup(selectedTheso.getProjectIdSelected());
        }
        description = projectDescriptionSelected.getDescription();
    }

    public void changeProjectDescription() {
        projectDescriptionSelected = projectDescriptionRepository.getProjectDescription(selectedTheso.getProjectIdSelected(), langCodeSelected);
    }

    public String getDrapeauImgLocal(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }

        var pays = allLangs.stream().filter(element -> codePays.equalsIgnoreCase(element.getId_iso639_1())).findFirst();
        if (pays.isPresent()) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/" + pays.get().getCodePays() + ".png";
        } else {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }
    }

    public boolean isDescriptionVisible() {
        return ObjectUtils.isNotEmpty(projectDescriptionSelected)
                && StringUtils.isEmpty(projectDescriptionSelected.getDescription());
    }
}
