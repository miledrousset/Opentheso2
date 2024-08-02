package fr.cnrs.opentheso.bean.toolbox.atelier;

import java.io.ByteArrayInputStream;
import jakarta.faces.view.ViewScoped;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.inject.Inject;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.fusion.FusionService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


@Named("atelierThesBean")
@ViewScoped
public class AtelierThesBean implements Serializable {

    @Inject
    private AtelierThesService atelierThesService;

    @Inject
    private FusionService fusionService;

    private List<String> titles;
    private List<List<String>> values = new ArrayList<>();
    private NodeIdValue thesoSelected;
    private ArrayList<NodeIdValue> nodeListTheso;
    private ArrayList<ConceptResultNode> result;
    private int spanTable;
    private int choiceDelimiter = 0;
    private char delimiterCsv = ',';
    private String selectedColumn;
    private String actionSelected;

    @PreDestroy
    public void destroy(){
        clear();
    }
    public void clear(){

        if(titles!= null){
            titles.clear();
            titles = null;
        }
        if(values!= null){
            values.clear();
            values = null;
        }
        if(nodeListTheso!= null){
            nodeListTheso.clear();
            nodeListTheso = null;
        }
        if(result!= null){
            result.clear();
            result = null;
        }
        thesoSelected = null;
        selectedColumn = null;
        actionSelected = null;
    }

    @PostConstruct
    public void init() {

        fusionService.setConceptsAjoutes(new ArrayList<>());
        fusionService.setConceptsExists(new ArrayList<>());
        fusionService.setConceptsModifies(new ArrayList<>());

        thesoSelected = null;
        actionSelected = null;
        fusionService.setFusionBtnEnable(false);
        fusionService.setFusionDone(false);
        fusionService.setLoadDone(false);

        choiceDelimiter = 0;
        delimiterCsv = ',';
        titles = new ArrayList<>();
        values = new ArrayList<>();
        result = new ArrayList<>();
        nodeListTheso = atelierThesService.searchAllThesaurus();
    }

    public void clearAll() {
        if(titles == null)
            titles = new ArrayList<>();
        else
            titles.clear();
        if(values == null)
            values = new ArrayList<>();
        else
            values.clear();
        if(result == null)
            result = new ArrayList<>();
        else
            result.clear();
        nodeListTheso = atelierThesService.searchAllThesaurus();
    }

    public void actionChoice() {
        if(choiceDelimiter == 0)
            delimiterCsv = ',';
        if(choiceDelimiter == 1)
            delimiterCsv = ';';
        if(choiceDelimiter == 2)
            delimiterCsv = '\t';
    }

    public void fusionner() {
        fusionService.lancerFussion(thesoSelected);
    }

    public void comparer() {
        int position = titles.indexOf(selectedColumn);
        result = atelierThesService.comparer(values, position, thesoSelected);

        showMessage(FacesMessage.SEVERITY_INFO, result.size() + " résultat(s) trouvées !");

        PrimeFaces.current().executeScript("PF('bui').hide();");
    }

    public void loadFileCsv(FileUploadEvent event) {
        clearAll();
        List<List<String>> temp = atelierThesService.loadCsvFile(event, delimiterCsv);
        if (!CollectionUtils.isEmpty(temp)) {
            titles = temp.get(0);
            for (int i = 1; i < temp.size(); i++) {
                values.add(temp.get(i));
            }
            spanTable = 12 / titles.size();
            showMessage(FacesMessage.SEVERITY_INFO, values.size() + " donnée(s) trouvées !");
        } else {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucune donnée trouvées !");
        }

        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
    }

    public StreamedContent exportResultat() {

        ExportResultatCsv exportResultatCsv = new ExportResultatCsv();
        exportResultatCsv.createResultatFileRapport(result);

        return DefaultStreamedContent.builder()
                .contentType("text/csv")
                .name("Résultat.csv")
                .stream(() -> new ByteArrayInputStream(exportResultatCsv.getOutput().toByteArray()))
                .build();

    }

    public List<List<String>> getValues() {
        return values;
    }

    public ArrayList<NodeIdValue> getNodeListTheso() {
        return nodeListTheso;
    }

    public void setNodeListTheso(ArrayList<NodeIdValue> nodeListTheso) {
        this.nodeListTheso = nodeListTheso;
    }

    public NodeIdValue getThesoSelected() {
        return thesoSelected;
    }

    public void setThesoSelected(NodeIdValue thesoSelected) {
        this.thesoSelected = thesoSelected;
    }

    public ArrayList<ConceptResultNode> getResult() {
        return result;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }

    public int getSpanTable() {
        return spanTable;
    }

    public String getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(String selectedColumn) {
        this.selectedColumn = selectedColumn;
    }

    public String onFlowProcess(FlowEvent event) {
        if ("actions".equals(event.getNewStep())){
            return event.getNewStep();
        }
        if ("actions".equals(event.getOldStep())) {
            if (StringUtils.isEmpty(actionSelected)) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez selectionnez une action !");
                return event.getOldStep();
            } else if ("opt2".equals(actionSelected)) {
                showMessage(FacesMessage.SEVERITY_INFO, "Cette action n'est pas disponible pour le moment ..");
                return event.getOldStep();
            } else {
                return event.getNewStep();
            }
        } else if ("entre".equals(event.getOldStep())) {
            if ("opt1".equals(actionSelected)) {
                if ("actions".equals(event.getNewStep())) {
                    return event.getNewStep();
                } else if (CollectionUtils.isEmpty(values)) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez ajouter des données d'entrées !");
                    return event.getOldStep();
                } else {
                    return event.getNewStep();
                }
            } else {
                if (!fusionService.isLoadDone()) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez importer un thesaurus !");
                    return event.getOldStep();
                }
                return event.getNewStep();
            }
        } else if ("thesaurus".equals(event.getOldStep())) {
            if ("entre".equals(event.getNewStep())) {
                return event.getNewStep();
            } else if (thesoSelected == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Vous devez selectionner un thesaurus !");
                return event.getOldStep();
            } else {
                return event.getNewStep();
            }
        } else {
            fusionService.initFusionResult();
            return event.getNewStep();
        }
    }

    public int getChoiceDelimiter() {
        return choiceDelimiter;
    }

    public void setChoiceDelimiter(int choiceDelimiter) {
        this.choiceDelimiter = choiceDelimiter;
    }

    private void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

    public String getActionSelected() {
        return actionSelected;
    }

    public boolean isRubriqueSelected(String nomAction) {
        return nomAction.equals(actionSelected);
    }

    public void setActionSelected(String actionSelected) {
        this.actionSelected = actionSelected;
    }



}