package fr.cnrs.opentheso.bean.toolbox.statistique;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.StatisticHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.candidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.core.exports.csv.StatistiquesRapportCSV;
import fr.cnrs.opentheso.core.exports.rdf4j.ExportRdf4jHelperNew;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.donut.DonutChartDataSet;
import org.primefaces.model.charts.donut.DonutChartModel;



@Named(value = "statistiqueBean")
@SessionScoped
public class StatistiqueBean implements Serializable {
    @Inject private Connect connect;
    @Inject private SelectedTheso selectedTheso;
    @Inject private LanguageBean languageBean;

    private boolean genericTypeVisible, conceptTypeVisible;
    private String selectedStatistiqueTypeCode, selectedCollection, nbrResultat;
    private int nbrCanceptByThes, nbrCandidateByThes, nbrDeprecatedByThes;
    private Date dateDebut, dateFin, derniereModification;
    private String selectedLanguage;
    private ConceptStatisticData canceptStatistiqueSelected;

    private List<GenericStatistiqueData> genericStatistiques;
    private List<ConceptStatisticData> conceptStatistic;
    private ArrayList<NodeLangTheso> languagesOfTheso;
    private ArrayList<DomaineDto> groupList;

    private List<String> colors = new ArrayList<>(List.of("rgb(255, 99, 132)","rgb(54, 162, 235)",
            "rgb(75, 192, 192)","rgb(158, 14, 64)",
            "rgb(136, 66, 29)", "rgb(240, 195, 0)", "rgb(63, 34, 4)", "rgb(29, 96, 198)",
            "rgb(121, 248, 248)", "rgb(0, 204, 203)", "rgb(23, 101, 125)", "rgb(102, 0, 255)",
            "rgb(0, 255, 0)", "rgb(135, 233, 144)", "rgb(9, 106, 9)", "rgb(112, 141, 35)",
            "rgb(255, 205, 86)"));

    @PreDestroy
    public void destroy(){
        clear();
    }

    public void clear(){
        if(genericStatistiques!= null){
            genericStatistiques.clear();
            genericStatistiques = null;
        }
        if(conceptStatistic!= null){
            conceptStatistic.clear();
            conceptStatistic = null;
        }
        if(languagesOfTheso!= null){
            languagesOfTheso.clear();
            languagesOfTheso = null;
        }
        if(groupList!= null){
            groupList.clear();
            groupList = null;
        }        
        selectedStatistiqueTypeCode = null;
        selectedCollection = null;        
        nbrResultat = null;
        dateDebut = null;
        dateFin = null;        
        derniereModification = null;
        selectedLanguage = null;
        canceptStatistiqueSelected = null;        
    }    
    
    public void init() {

        genericTypeVisible = false;
        conceptTypeVisible = false;

        genericStatistiques = new ArrayList<>();
        conceptStatistic = new ArrayList<>();

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez choisir un Thesorus avant !");
            return;
        }
        selectedLanguage = selectedTheso.getCurrentLang();
        initChamps();
    }
    public void clearFilter(){
        dateDebut = null;
        dateFin = null;
        selectedCollection = "";
        conceptStatistic = new ArrayList<>();
        genericStatistiques = new ArrayList<>();
    }

    public DonutChartModel createChartModel(int model) {

        DonutChartModel donutModel = new DonutChartModel();
        ChartData data = new ChartData();

        DonutChartDataSet dataSet = new DonutChartDataSet();

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<String> bgColors = new ArrayList<>();

        int pos = 0;
        for (GenericStatistiqueData genericStatistiqueData : genericStatistiques) {
            switch(model) {
                case 1:
                    values.add(genericStatistiqueData.getConceptsNbr());
                    break;
                case 2:
                    values.add(genericStatistiqueData.getSynonymesNbr());
                    break;
                case 3:
                    values.add(genericStatistiqueData.getTermesNonTraduitsNbr());
                    break;
                case 4:
                    values.add(genericStatistiqueData.getNotesNbr());
                    break;
            }
            labels.add(genericStatistiqueData.getCollection());
            bgColors.add(colors.get(pos));
            pos ++;
            if (pos == colors.size()) pos = 0;
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(bgColors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        donutModel.setData(data);

        return donutModel;
    }
    
    private void initChamps() {
        languagesOfTheso = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), selectedTheso.getSelectedIdTheso(), languageBean.getIdLangue());

        groupList = new GroupHelper().getAllGroupsByThesaurusAndLang(connect, selectedTheso.getSelectedIdTheso(),
                languageBean.getIdLangue());
    }

    public void onSelectStatType() {
        
        genericStatistiques = new ArrayList<>();
        conceptStatistic = new ArrayList<>();

        if (StringUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            showMessage(FacesMessage.SEVERITY_WARN, "Vous devez choisir un Thesorus avant !");
            return;
        }

        if (CollectionUtils.isEmpty(languagesOfTheso)) {
            initChamps();
        }

        genericTypeVisible = false;
        conceptTypeVisible = false;
    }

    public List<String> searchDomaineName(String enteredValue) {

        if ("%".equals(enteredValue)) {
            return groupList.stream().map(group -> group.getName()).collect(Collectors.toList());
        }

        List<String> matches = new ArrayList<>();

        groupList.stream().filter((s) -> (s.getName() != null && s.getName().toLowerCase().startsWith(enteredValue.toLowerCase()))).forEachOrdered((s) -> {
            matches.add(s.getName());
        });

        return matches;
    }

    private String searchGroupIdFromLabel(String label) {
        String groupId = "";
        for (DomaineDto group : groupList) {
            if (group.getName().equals(label)) {
                groupId = group.getId();
                break;
            }
        }
        return groupId;
    }

    public String formatLanguage(String langLabel) {
        return langLabel.substring(0, 1).toUpperCase() + langLabel.substring(1, langLabel.length());
    }

    public void onSelectLanguageType() throws SQLException {
        onSelectStatType();
        clearFilter();

        if ("0".equals(selectedStatistiqueTypeCode)) {

            ConceptHelper conceptHelper = new ConceptHelper();
            StatisticHelper statisticHelper = new StatisticHelper();

            genericStatistiques = new StatistiqueService().searchAllCollectionsByThesaurus(connect, selectedTheso.getCurrentIdTheso(), selectedLanguage);

            nbrCanceptByThes = statisticHelper.getNbCpt(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
            
            nbrCandidateByThes = statisticHelper.getNbCandidate(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
            nbrDeprecatedByThes = statisticHelper.getNbOfDeprecatedConcepts(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
            

            derniereModification = conceptHelper.getLastModification(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

            genericTypeVisible = true;
            conceptTypeVisible = false;

        } else {

            genericTypeVisible = false;
            conceptTypeVisible = true;
        }
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
        }
    }
    
    public boolean isExportVisible() {
        return !CollectionUtils.isEmpty(genericStatistiques) || !CollectionUtils.isEmpty(conceptStatistic);
    }

    public void getStatisticByConcept() {
        conceptStatistic = new StatistiqueService().searchAllConceptsByThesaurus(this, 
                connect, selectedTheso.getCurrentIdTheso(),
                selectedLanguage, dateDebut, dateFin,
                searchGroupIdFromLabel(selectedCollection), nbrResultat);

        //PrimeFaces.current().executeScript("PF('bui').hide();");
    }

    public StreamedContent exportStatiqituque() {

        StatistiquesRapportCSV statistiquesRapportCSV = new StatistiquesRapportCSV();
        if (genericTypeVisible) {
            statistiquesRapportCSV.createGenericStatitistiquesRapport(genericStatistiques);
        } else {
            statistiquesRapportCSV.createConceptsStatitistiquesRapport(conceptStatistic);
        }

        return DefaultStreamedContent.builder()
                .contentType("text/csv")
                .name(selectedTheso.getThesoName() + ".csv")
                .stream(() -> new ByteArrayInputStream(statistiquesRapportCSV.getOutput().toByteArray()))
                .build();

    }
    
    private ExportRdf4jHelperNew getThesorusDatas(String idTheso) {

        NodePreference nodePreference = new PreferencesHelper().getThesaurusPreferences(connect.getPoolConnexion(), idTheso);

        if (nodePreference == null) {
            return null;
        }

        ExportRdf4jHelperNew resources = new ExportRdf4jHelperNew();
        resources.setInfos(nodePreference);
        resources.exportTheso(connect.getPoolConnexion(), idTheso, nodePreference);
        ArrayList<String> allConcepts = new ConceptHelper().getAllIdConceptOfThesaurus(connect.getPoolConnexion(), idTheso);
        for (String idConcept : allConcepts) {
            resources.exportConcept(connect.getPoolConnexion(), idTheso, idConcept, false);
        }

        return resources;
    }
    
    public void setConceptSelected(ConceptStatisticData canceptStatistiqueSelected) {
        this.canceptStatistiqueSelected = canceptStatistiqueSelected;
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

    public boolean showExportButton() {
        return genericTypeVisible || conceptTypeVisible;
    }

    public Date getDerniereModification() {
        return derniereModification;
    }

    public int getNbrCanceptByThes() {
        return nbrCanceptByThes;
    }

    public String getSelectedStatistiqueTypeCode() {
        return selectedStatistiqueTypeCode;
    }

    public void setSelectedStatistiqueTypeCode(String selectedStatistiqueTypeCode) {
        this.selectedStatistiqueTypeCode = selectedStatistiqueTypeCode;
    }

    public ArrayList<NodeLangTheso> getLanguagesOfTheso() {
        return languagesOfTheso;
    }

    public void setLanguagesOfTheso(ArrayList<NodeLangTheso> languagesOfTheso) {
        this.languagesOfTheso = languagesOfTheso;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public boolean isGenericTypeVisible() {
        return genericTypeVisible;
    }

    public boolean isConceptTypeVisible() {
        return conceptTypeVisible;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public List<GenericStatistiqueData> getGenericStatistiques() {
        return genericStatistiques;
    }

    public void setGenericStatistiques(List<GenericStatistiqueData> genericStatistiques) {
        this.genericStatistiques = genericStatistiques;
    }

    public List<ConceptStatisticData> getCanceptStatistiques() {
        return conceptStatistic;
    }

    public void setCanceptStatistiques(List<ConceptStatisticData> canceptStatistiques) {
        this.conceptStatistic = canceptStatistiques;
    }

    public String getSelectedCollection() {
        return selectedCollection;
    }

    public void setSelectedCollection(String selectedCollection) {
        this.selectedCollection = selectedCollection;
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    public String getNbrResultat() {
        return nbrResultat;
    }

    public void setNbrResultat(String nbrResultat) {
        this.nbrResultat = nbrResultat;
    }

    public ConceptStatisticData getCanceptStatistiqueSelected() {
        return canceptStatistiqueSelected;
    }

    public int getNbrCandidateByThes() {
        return nbrCandidateByThes;
    }

    public void setNbrCandidateByThes(int nbrCandidateByThes) {
        this.nbrCandidateByThes = nbrCandidateByThes;
    }

    public int getNbrDeprecatedByThes() {
        return nbrDeprecatedByThes;
    }

    public void setNbrDeprecatedByThes(int nbrDeprecatedByThes) {
        this.nbrDeprecatedByThes = nbrDeprecatedByThes;
    }
    
    
}
