package fr.cnrs.opentheso.bean.toolbox.edition;

import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.importexport.ExportFileBean;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.primefaces.event.ToggleSelectEvent;

/**
 *
 * @author miledrousset
 */
@Named(value = "viewExportBean")
@SessionScoped
public class ViewExportBean implements Serializable {
    @Inject private SelectedTheso selectedTheso;
    @Inject private Connect connect;
    @Inject private ExportFileBean downloadBean;
    @Inject private LanguageBean languageBean;

    private ArrayList<NodeLangTheso> languagesOfTheso;
    private ArrayList<NodeGroup> groupList;
    private NodePreference nodePreference;

    private List<NodeLangTheso> selectedLanguages;
    private List<NodeGroup> selectedGroups;

    private boolean exportUriArk = false;
    private boolean exportUriHandle = false;
    private NodeIdValue nodeIdValueOfTheso;

    private List<String> exportFormat;
    private List<String> types;
    private String selectedExportFormat, typeSelected, formatFile, csvDelimiter;
    
    // ajouté par Miled
    private boolean isAllGroupsSelected; 
    private boolean isAllLangsSelected;
    private boolean toogleFilterByGroup;
    private boolean toogleExportByGroup;
    
    private boolean toogleClearHtmlCharacter;    
    
    private String selectedGroup;
    private String selectedIdLangTheso;
    private List<String> selectedIdGroups;

    // pour le format PDF
    private String selectedLang1_PDF; // pour comparer entre 2 langues maxi pour le PDF
    private String selectedLang2_PDF;
     
    private boolean exportDone;
    
    @PreDestroy
    public void destroy(){
        clear();
    }    
    
    public void clear() {
        nodePreference = null;    
        nodeIdValueOfTheso = null;            
        csvDelimiter = ";";  
        formatFile = null;  
        typeSelected = null;  
        selectedExportFormat = null;  
        selectedLang1_PDF = null;  
        selectedLang2_PDF = null;  
        selectedIdLangTheso = null;
        
        if(languagesOfTheso != null){
            languagesOfTheso.clear();
            languagesOfTheso = null;
        }
        if(groupList != null){
            groupList.clear();
            groupList = null;
        }
        if(selectedLanguages != null){
            selectedLanguages.clear();
            selectedLanguages = null;
        }
        if(selectedGroups != null){
            selectedGroups.clear();
            selectedGroups = null;
        }
        if(selectedIdGroups != null){
            selectedIdGroups.clear();
            selectedIdGroups = null;
        }

        exportFormat = null;
        types = null;
    }    
    
    
    public void init(NodeIdValue nodeIdValueOfTheso, String format) {
        nodePreference = new PreferencesHelper().getThesaurusPreferences(connect.getPoolConnexion(), nodeIdValueOfTheso.getId());

        selectedLang1_PDF = nodePreference.getSourceLang();
        selectedLang2_PDF = null;
        
        isAllGroupsSelected = true;
        isAllLangsSelected = true;
        exportDone = false;
        
        this.formatFile = format;
        this.nodeIdValueOfTheso = nodeIdValueOfTheso;

        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();


        String idLang = selectedTheso.getCurrentLang();
        if (idLang == null || idLang.isEmpty()) {
            idLang = connect.getWorkLanguage();
        }
        languagesOfTheso = thesaurusHelper.getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(), nodeIdValueOfTheso.getId(), idLang);        

        types = Arrays.asList(languageBean.getMsg("export.hierarchical"), languageBean.getMsg("export.alphabetical"));//"Hiérarchique", "Alphabétique");
        typeSelected = types.get(0);

        groupList = new GroupHelper().getListConceptGroup(connect.getPoolConnexion(), nodeIdValueOfTheso.getId(), idLang);

        toogleFilterByGroup = false;
        toogleExportByGroup = false;
        toogleClearHtmlCharacter = false;
        
        if(selectedIdGroups == null){
            selectedIdGroups = new ArrayList<>();
        } else
            selectedIdGroups.clear();

        if(groupList != null) {
            for (NodeGroup nodeGroup : groupList) {
                selectedIdGroups.add(nodeGroup.getConceptGroup().getIdgroup());
            }
        }

        if(selectedLanguages == null)
            selectedLanguages = new ArrayList<>();
        else
            selectedLanguages.clear();
        for (NodeLangTheso nodeLang : languagesOfTheso) {
            selectedLanguages.add(nodeLang);
        }

        selectedGroups = null;
        selectedGroups = new ArrayList<>();

        exportUriArk = false;
        exportUriHandle = false;

        exportFormat = Arrays.asList("rdf", "json", "jsonLd", "turtle");
        selectedExportFormat = "rdf";

        downloadBean.setProgressBar(0);
        downloadBean.setProgressStep(0);

        selectedGroup = "all";
    }
    
    /**
     * permet capter si toutes les options sont sélectionnées 
     * @param event 
     */
    public void onAllGroupsSelect(ToggleSelectEvent event) {
        isAllGroupsSelected =event.isSelected();
    }    
    
    /**
     * permet capter si toutes les options sont sélectionnées 
     * @param event 
     */    
    public void onAllLangsSelect(ToggleSelectEvent event) {
        isAllLangsSelected = event.isSelected();
    }     
    
    public String getExportButtonLabel() {
        if (isPdfExport()) {
            return languageBean.getMsg("edit.exportpdf");
        } else if (isRdfExport())  {
            return "Export RDF";
        } else {
            return languageBean.getMsg("edit.exportcsv");
        }
    }

    
    public void listenerForToogleFilterByGroup() {
        this.toogleFilterByGroup = !this.toogleFilterByGroup;
        this.toogleExportByGroup = false;
    }    
    
    public void listenerForToogleExportByGroup() {
        this.toogleExportByGroup = !this.toogleExportByGroup;
        this.toogleFilterByGroup = false;
    }      
    
    public void listenerForToogleClearHtmlCharacter() {

    }      

    public boolean isToogleClearHtmlCharacter() {
        return toogleClearHtmlCharacter;
    }

    public void setToogleClearHtmlCharacter(boolean toogleClearHtmlCharacter) {
        this.toogleClearHtmlCharacter = toogleClearHtmlCharacter;
    }
    
    
    public boolean isPdfExport() {
        return "PDF".equals(formatFile);
    }

    public boolean isRdfExport() {
        return "RDF".equals(formatFile);
    }

    public boolean isCsvExport() {
        return "CSV".equals(formatFile);
    }
    
    public boolean isCsvIdExport() {
        return "CSV_id".equals(formatFile);
    }
    
    public boolean isDeprecatedExport() {
        return "deprecated".equals(formatFile);
    }
    
    public boolean isCsvStrucExport() {
        return "CSV_STRUC".equals(formatFile);
    }

    public String getFormat() {
        return formatFile;
    }

    public ArrayList<NodeLangTheso> getLanguagesOfTheso() {
        return languagesOfTheso;
    }

    public void setLanguagesOfTheso(ArrayList<NodeLangTheso> languagesOfTheso) {
        this.languagesOfTheso = languagesOfTheso;
    }

    public ArrayList<NodeGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(ArrayList<NodeGroup> groupList) {
        this.groupList = groupList;
    }

    public boolean isExportUriHandle() {
        return exportUriHandle;
    }

    public void setExportUriHandle(boolean exportUriHandle) {
        this.exportUriHandle = exportUriHandle;
    }

    public NodeIdValue getNodeIdValueOfTheso() {
        return nodeIdValueOfTheso;
    }

    public void setNodeIdValueOfTheso(NodeIdValue nodeIdValueOfTheso) {
        this.nodeIdValueOfTheso = nodeIdValueOfTheso;
    }

    public List<NodeLangTheso> getSelectedLanguages() {
        return selectedLanguages;
    }

    public void setSelectedLanguages(List<NodeLangTheso> selectedLanguages) {
        this.selectedLanguages = selectedLanguages;
    }

    public List<NodeGroup> getSelectedGroups() {
        return selectedGroups;
    }

    public void setSelectedGroups(List<NodeGroup> selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

    public boolean isExportUriArk() {
        return exportUriArk;
    }

    public void setExportUriArk(boolean exportUriArk) {
        this.exportUriArk = exportUriArk;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public List<String> getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ArrayList<String> exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getSelectedExportFormat() {
        return selectedExportFormat;
    }

    public void setSelectedExportFormat(String selectedExportFormat) {
        this.selectedExportFormat = selectedExportFormat;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getTypeSelected() {
        return typeSelected;
    }

    public void setTypeSelected(String typeSelected) {
        this.typeSelected = typeSelected;
    }

    public String getCsvDelimiter() {
        return csvDelimiter;
    }

    public void setCsvDelimiter(String csvDelimiter) {
        this.csvDelimiter = csvDelimiter;
    }
    
    public char getCsvDelimiterChar() {
        if (csvDelimiter.equalsIgnoreCase(";")) {
            return ';';
        } 
        if (csvDelimiter.equalsIgnoreCase("\\t")) {
            return '\t';
        }
        return ',';
    }  

    public boolean isIsAllGroupsSelected() {
        return isAllGroupsSelected;
    }

    public void setIsAllGroupsSelected(boolean isAllGroupsSelected) {
        this.isAllGroupsSelected = isAllGroupsSelected;
    }

    public boolean isIsAllLangsSelected() {
        return isAllLangsSelected;
    }

    public void setIsAllLangsSelected(boolean isAllLangsSelected) {
        this.isAllLangsSelected = isAllLangsSelected;
    }

    public String getSelectedLang1_PDF() {
        return selectedLang1_PDF;
    }

    public void setSelectedLang1_PDF(String selectedLang1_PDF) {
        this.selectedLang1_PDF = selectedLang1_PDF;
    }

    public String getSelectedLang2_PDF() {
        return selectedLang2_PDF;
    }

    public void setSelectedLang2_PDF(String selectedLang2_PDF) {
        this.selectedLang2_PDF = selectedLang2_PDF;
    }

    public boolean isExportDone() {
        return exportDone;
    }

    public void setExportDone(boolean exportDone) {
        this.exportDone = exportDone;
    }

    public String getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(String selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public String getSelectedIdLangTheso() {
        return selectedIdLangTheso;
    }

    public void setSelectedIdLangTheso(String selectedIdLangTheso) {
        this.selectedIdLangTheso = selectedIdLangTheso;
    }

    public List<String> getSelectedIdGroups() {
        return selectedIdGroups;
    }

    public void setSelectedIdGroups(List<String> selectedIdGroups) {
        this.selectedIdGroups = selectedIdGroups;
    }

    public boolean isToogleFilterByGroup() {
        return toogleFilterByGroup;
    }

    public void setToogleFilterByGroup(boolean toogleFilterByGroup) {
    }

    public boolean isToogleExportByGroup() {
        return toogleExportByGroup;
    }

    public void setToogleExportByGroup(boolean toogleExportByGroup) {
    }
    
    
}
