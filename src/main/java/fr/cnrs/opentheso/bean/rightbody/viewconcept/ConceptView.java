package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import com.jsf2leaf.model.Map;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.CorpusHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.dao.ConceptRelation;
import fr.cnrs.opentheso.bdd.helper.dao.DaoResourceHelper;
import fr.cnrs.opentheso.bdd.helper.dao.NodeFullConcept;
import fr.cnrs.opentheso.bdd.helper.dao.ResourceGPS;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeConceptType;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCustomRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePath;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.repositories.GpsRepository;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.annotation.PreDestroy;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.ResponsiveOption;


/**
 *
 * @author miledrousset
 */
@Data
@SessionScoped
@Named(value = "conceptView")
public class ConceptView implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private IndexSetting indexSetting;
    @Inject
    private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Inject
    private ViewEditorHomeBean viewEditorHomeBean;
    @Inject
    private Tree tree;
    @Inject
    private RoleOnThesoBean roleOnThesoBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private GpsRepository gpsRepository;
    @Inject
    private CurrentUser currentUser;

    private Map mapModel;
    private NodeConcept nodeConcept;
    
    /// nouvelle méthode de récupération du concept 
    private NodeFullConcept nodeFullConcept;    
    
    private String selectedLang;
    private GpsMode gpsModeSelected;
    private ArrayList<NodeCorpus> nodeCorpuses;
    private ArrayList<NodePath> pathLabel;
    
    private List<List<NodePath>> pathLabel2;    
    
    private ArrayList<NodeIdValue> nodeFacets;

    /// pagination
    private int offset;
    private int step;
    private boolean haveNext;

    // total de la branche
    private int countOfBranch;

    // pour savoir si le concept a des relations vers des corpus
    private boolean haveCorpus;
    private boolean searchedForCorpus;

    /// Notes du concept, un type de note par concept et par langue
    private NodeNote note;
    private NodeNote scopeNote;
    private NodeNote changeNote;
    private NodeNote definition;
    private NodeNote editorialNote;
    private NodeNote example;
    private NodeNote historyNote;

    /// Notes du concept pour l'affichage du multilingue
    private ArrayList<NodeNote> noteAllLang;
    private ArrayList<NodeNote> scopeNoteAllLang;
    private ArrayList<NodeNote> changeNoteAllLang;
    private ArrayList<NodeNote> definitionAllLang;
    private ArrayList<NodeNote> editorialNoteAllLang;
    private ArrayList<NodeNote> exampleAllLang;
    private ArrayList<NodeNote> historyNoteAllLang;    
    
    
    
    
    private ArrayList<NodeCustomRelation> nodeCustomRelationReciprocals;
    private ArrayList <NodeCustomRelation> nodeCustomRelations;
    
    
    private List<ResponsiveOption> responsiveOptions;

    private boolean toggleSwitchAltLabelLang;
    private boolean toggleSwitchNotesLang;

    private String creator;
    private String contributors;

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        nodeCorpuses = new ArrayList<>();
        pathLabel = new ArrayList<>();
        pathLabel2 = new ArrayList<>();
        note = null;
        scopeNote = null;
        changeNote = null;
        definition = null;
        editorialNote = null;
        example = null;
        historyNote = null;
        nodeConcept = new NodeConcept();
        nodeFullConcept = new NodeFullConcept();
        nodeFacets = new ArrayList<>();

        selectedLang = null;
        mapModel = null;
        nodeCustomRelationReciprocals = null;
    }

    /**
     * Creates a new instance of ConceptBean
     */
    public ConceptView() {
    }

    public void init() {
        toggleSwitchAltLabelLang = false;
        toggleSwitchNotesLang = false;
 /*       if (nodeConcept != null) {
            nodeConcept.clear();
        }*/
        nodeFullConcept = new NodeFullConcept();
        
        selectedLang = null;

        if (nodeFacets == null) {
            nodeFacets = new ArrayList<>();
        }
        clearNotes();

        offset = 0;
        step = 20;
        haveNext = false;

        nodeCorpuses = null;
        countOfBranch = 0;
        haveCorpus = false;
        searchedForCorpus = false;
        nodeCustomRelationReciprocals = null;

        responsiveOptions = new ArrayList<>();
        responsiveOptions.add(new ResponsiveOption("1024px", 5));
        responsiveOptions.add(new ResponsiveOption("768px", 3));
        responsiveOptions.add(new ResponsiveOption("560px", 1));
    }

    private void clearNotes() {
        note = null;
        scopeNote = null;
        changeNote = null;
        definition = null;
        editorialNote = null;
        example = null;
        historyNote = null;
    }
    private void clearNotesAllLang() {    
        noteAllLang = new ArrayList<>();
        scopeNoteAllLang = new ArrayList<>();
        changeNoteAllLang = new ArrayList<>();
        definitionAllLang = new ArrayList<>();
        editorialNoteAllLang = new ArrayList<>();
        exampleAllLang = new ArrayList<>();
        historyNoteAllLang = new ArrayList<>();   
    }
    
    public String getDrapeauImgLocal(String codePays) {
        if (StringUtils.isEmpty(codePays)) {
            return FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestContextPath() + "/resources/img/flag/noflag.png";
        }
        return FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath() + "/resources/img/flag/" + codePays + ".png";
    }
    
    public String getFlagFromCodeLang(String idLang){
        LanguageHelper languageHelper = new LanguageHelper();
        String flag = languageHelper.getFlagFromIdLang(connect.getPoolConnexion(), idLang);
        return FacesContext.getCurrentInstance().getExternalContext()
                .getRequestContextPath() + "/resources/img/flag/" + flag + ".png";        
    }

    /**
     * permet de retourner le label du type de concept en focntion de la langue de l'interface
     *
     * @param conceptType
     * @param idTheso
     * @return
     */
    public String getLabelOfConceptType(String conceptType, String idTheso) {
        String idLang;
        RelationsHelper relationsHelper = new RelationsHelper();
        idLang = getIdLangOfInterface();

        return relationsHelper.getLabelOfTypeConcept(connect.getPoolConnexion(),
                conceptType,
                idTheso,
                idLang);
    }

    /**
     * récuparation des informations pour le concept sélectionné c'est pour la
     * navigation entre les concepts dans la vue de droite avec deployement de
     * l'arbre
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConcept(String idTheso, String idConcept, String idLang) {
        offset = 0;
        gpsModeSelected = GpsMode.POINT;
        ConceptHelper conceptHelper = new ConceptHelper();
        
        if(currentUser != null) {
            nodeConcept = conceptHelper.getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang, offset, step + 1);
            if (nodeConcept == null) return;
        }        
        
        nodeFullConcept = null;
        nodeFullConcept = conceptHelper.getConcept2(connect.getPoolConnexion(), idConcept, idTheso, idLang, offset, step + 1);
        if(nodeFullConcept == null) return;
        
        
        searchedForCorpus = false;
        
        
        // permet de récupérer les qualificatifs
        if(roleOnThesoBean.getNodePreference() == null){
            roleOnThesoBean.initNodePref(idTheso);
        }
        if (roleOnThesoBean.getNodePreference().isUseCustomRelation()) {
            String interfaceLang = getIdLangOfInterface();

            nodeCustomRelations = new RelationsHelper().getAllNodeCustomRelation(
                    connect.getPoolConnexion(), idConcept, idTheso, idLang, interfaceLang);
            setNodeCustomRelationWithReciprocal(nodeCustomRelations);
        }

        setOffset();

        // récupération des informations sur les corpus liés
        nodeCorpuses = null;
        createMap(idConcept, idTheso, Boolean.TRUE);

        selectedLang = idLang;
        setNotes();

        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();

        // récupération des informations sur les corpus liés
        haveCorpus = false;
        setRoles();

        setFacetsOfConcept(idConcept, idTheso, idLang);

        // deployement de l'arbre si l'option est true
        if (roleOnThesoBean.getNodePreference() != null) {
            if (roleOnThesoBean.getNodePreference().isBreadcrumb())
                pathOfConcept2(idTheso, idConcept, idLang);

            if (roleOnThesoBean.getNodePreference().isAuto_expand_tree()) {
                tree.expandTreeToPath(
                        idConcept,
                        idTheso,
                        idLang);
                if (PrimeFaces.current().isAjaxRequest()) {
                    PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabTree:tree");
                    PrimeFaces.current().ajax().update("containerIndex:languageSelect");
                }
                selectedTheso.actionFromConceptToOn();
            }
        }

        countOfBranch = 0;
    }


    public void searchCorpus() {
        searchedForCorpus = true;
        SearchCorpus2 searchCorpus2 = new SearchCorpus2();
        nodeCorpuses = new CorpusHelper().getAllActiveCorpus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());     
        nodeCorpuses = searchCorpus2.SearchCorpus(nodeCorpuses, nodeFullConcept);
        haveCorpus = searchCorpus2.isHaveCorpus();
        if(!haveCorpus) {
            nodeCorpuses = null;
            nodeCorpuses = null;
        }
    }

    public void createMap(String idConcept, String idTheso, Boolean isFirstTime) {
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getGps())) {
            List<Gps> gpses = getGpsFromResource(nodeFullConcept.getGps());
             
            gpsModeSelected = getGpsMode(gpses);
            gpsList = formatCoordonnees(gpses);
            if (mapModel == null) {
                mapModel = new MapUtils().createMap(gpses, gpsModeSelected,
                        ObjectUtils.isEmpty(nodeFullConcept.getPrefLabel()) ? null : nodeFullConcept.getPrefLabel().getLabel());
            } else {
                mapModel = new MapUtils().updateMap(gpses, mapModel, gpsModeSelected,
                        ObjectUtils.isEmpty(nodeFullConcept.getPrefLabel()) ? null : nodeFullConcept.getPrefLabel().getLabel());
            }
        } else {
            gpsList = "";
        }        
    }
    private List<Gps> getGpsFromResource(List<ResourceGPS> resourceGps){
        List<Gps> gpses = new ArrayList<>();
        for (ResourceGPS resourceGp : resourceGps) {
            Gps gps = new Gps();
            gps.setIdConcept(nodeFullConcept.getIdentifier());
            gps.setIdTheso(selectedTheso.getCurrentIdTheso());
            gps.setLatitude(resourceGp.getLatitude());
            gps.setLongitude(resourceGp.getLongitude());
            gps.setPosition(resourceGp.getPosition());
            gpses.add(gps);
        }
        return gpses;
    }    
    private List<ResourceGPS> getResourceGpsFromGps(List<Gps> gps){
        List<ResourceGPS> resourceGPSs = new ArrayList<>();
        for (Gps gp : gps) {
            ResourceGPS resourceGPS = new ResourceGPS();
            resourceGPS.setLatitude(gp.getLatitude());
            resourceGPS.setLongitude(gp.getLongitude());
            resourceGPS.setPosition(gp.getPosition());
            resourceGPSs.add(resourceGPS);
        }
        return resourceGPSs;
    }      
    

    public boolean isGpsDisable() {
        return currentUser.getNodeUser() == null || (currentUser.getNodeUser() != null &&
                !(roleOnThesoBean.isManagerOnThisTheso() || roleOnThesoBean.isAdminOnThisTheso() || roleOnThesoBean.isSuperAdmin()));
    }

    private String formatCoordonnees(List<Gps> listeCoordonnees) {
        if (CollectionUtils.isEmpty(listeCoordonnees)) {
            return "";
        } else {
            StringBuilder resultat = new StringBuilder();
            for (Gps gps : listeCoordonnees) {
                resultat.append(gps.toString()).append(", ");
            }
            return "(" + resultat.substring(0, resultat.length() - 2) + ")";
        }
    }

    /**
     * récuparation des informations pour le concept sélectionné après une
     * sélection dans l'arbre
     *
     * @param idTheso
     * @param idConcept
     * @param idLang
     */
    public void getConceptForTree(String idTheso, String idConcept, String idLang) {
//                System.out.println("entree : " + LocalTime.now());
        selectedLang = idLang;
        offset = 0;
        ConceptHelper conceptHelper = new ConceptHelper();
        
        if(currentUser != null) {
            nodeConcept = conceptHelper.getConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang, offset, step + 1);
            if (nodeConcept == null) return;
        }
        
        nodeFullConcept = null;
        
        nodeFullConcept = conceptHelper.getConcept2(connect.getPoolConnexion(), idConcept, idTheso, idLang, offset, step+1);
        if(nodeFullConcept == null) return;
        
        // permet de récupérer les qualificatifs
        if (roleOnThesoBean.getNodePreference().isUseCustomRelation()) {
            nodeCustomRelations = new RelationsHelper().getAllNodeCustomRelation(
                    connect.getPoolConnexion(), idConcept, idTheso, idLang, getIdLangOfInterface());
            setNodeCustomRelationWithReciprocal(nodeCustomRelations);
        }

        if (roleOnThesoBean.getNodePreference().isBreadcrumb())
            pathOfConcept2(idTheso, idConcept, idLang);

        setNotes();
        setOffset();
        

        // récupération des informations sur les corpus liés
        nodeCorpuses = null;
        haveCorpus = false;
        searchedForCorpus = false;

        setRoles();

        createMap(idConcept, idTheso, Boolean.TRUE);
        
        setFacetsOfConcept(idConcept, idTheso, idLang);

        indexSetting.setIsValueSelected(true);
        viewEditorHomeBean.reset();
        viewEditorThesoHomeBean.reset();
        countOfBranch = 0;
//                System.out.println("sortie : " + LocalTime.now());
    }

    /**
     * permet de récupérer toutes les notes dans toutes les langues
     */
    public void setNotes() {
        if (toggleSwitchNotesLang) {
            setNotesForAllLang();//getNotesWithAllLanguages();
        } else 
            setNotesCurrentLang();    
        
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }
    
    public boolean isHaveAlignment(){
        return (CollectionUtils.isNotEmpty(nodeFullConcept.getExactMatchs()) || CollectionUtils.isNotEmpty(nodeFullConcept.getCloseMatchs())
                || CollectionUtils.isNotEmpty(nodeFullConcept.getBroadMatchs()) || CollectionUtils.isNotEmpty(nodeFullConcept.getRelatedMatchs()) );
    }
    
    public boolean isHaveDefinition(){
        return !(definition == null && (definitionAllLang == null || definitionAllLang.isEmpty()));
    }
    public boolean isHaveNote(){
        return !(note == null && (noteAllLang == null || noteAllLang.isEmpty()));
    }    
    public boolean isHaveChangeNote(){
        return !(changeNote == null && (changeNoteAllLang == null || changeNoteAllLang.isEmpty()));
    }  
    public boolean isHaveEditorialNote(){
        return !(editorialNote == null && (editorialNoteAllLang == null || editorialNoteAllLang.isEmpty()));
    }       
    public boolean isHaveExampleNote(){
        return !(example == null && (exampleAllLang == null || exampleAllLang.isEmpty()));
    }      
    public boolean isHaveHistoryNote(){
        return !(historyNote == null && (historyNoteAllLang == null || historyNoteAllLang.isEmpty()));
    }    
    public boolean isHaveScopeNote(){
        return !(scopeNote == null && (scopeNoteAllLang == null || scopeNoteAllLang.isEmpty()));
    }      

    private void setFacetsOfConcept(String idConcept, String idTheso, String idLang) {
        FacetHelper facetHelper = new FacetHelper();
        List<String> facetIds = facetHelper.getAllIdFacetsConceptIsPartOf(connect.getPoolConnexion(), idConcept, idTheso);
        if (nodeFacets == null)
            nodeFacets = new ArrayList<>();
        else
            nodeFacets.clear();
        for (String facetId : facetIds) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(facetId);
            nodeIdValue.setValue(facetHelper.getLabelOfFacet(connect.getPoolConnexion(), facetId, idTheso, idLang));
            nodeFacets.add(nodeIdValue);
        }
    }

    public void setOffset() {
        if(CollectionUtils.isNotEmpty(nodeFullConcept.getNarrowers())){
            if (nodeFullConcept.getNarrowers().size() < step) {
                offset = 0;
                haveNext = false;
            } else {
                offset = offset + step + 1;
                haveNext = true;
            }
        }

    }

    public void countTheTotalOfBranch() {
        ConceptHelper conceptHelper = new ConceptHelper();
        List<String> listIdsOfBranch = conceptHelper.getIdsOfBranch2(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeFullConcept.getIdentifier()
                );
        this.countOfBranch = listIdsOfBranch.size();
    }

    public void setNodeCustomRelationWithReciprocal(ArrayList<NodeCustomRelation> nodeCustomRelations) {
        nodeCustomRelationReciprocals = new ArrayList<>();
        for (NodeCustomRelation nodeCustomRelation : nodeCustomRelations) {
            if (nodeCustomRelation.isReciprocal())
                nodeCustomRelationReciprocals.add(nodeCustomRelation);
        }
        if (nodeCustomRelationReciprocals.isEmpty())
            nodeCustomRelationReciprocals = null;
    }

    public void getNextNT(String idTheso, String idConcept, String idLang) {
        if (tree != null && tree.getSelectedNode() != null && tree.getSelectedNode().getData() != null) {
            List<ConceptRelation> conceptRelations = new DaoResourceHelper().getListNT(connect.getPoolConnexion(),
                    idTheso,
                    ((TreeNodeData) tree.getSelectedNode().getData()).getNodeId(),
                    idLang, offset, step + 1);
            if (conceptRelations != null && !conceptRelations.isEmpty()) {
                nodeFullConcept.getNarrowers().addAll(conceptRelations);
                setOffset();
                return;
            }
            haveNext = false;
        }
    }

/*    private void pathOfConcept(String idTheso, String idConcept, String idLang) {
        PathHelper pathHelper = new PathHelper();
        List<Path> paths = pathHelper.getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);
        if (pathHelper.getMessage() != null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", pathHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        if (paths == null) {
            System.out.println("Erreur de path pour le concept :" + idConcept);
            if (pathLabel != null) {
                pathLabel.clear();
            }
            return;
        }
        pathLabel = pathHelper.getPathWithLabel(connect.getPoolConnexion(), paths, idTheso, idLang, idConcept);
    }*/

    /**
     * méthode pour construire le graphe pour représenter tous les chemins vers la racine
     * @param idTheso
     * @param idConcept
     * @param idLang 
     * #MR
     */
    private void pathOfConcept2(String idTheso, String idConcept, String idLang) {
        PathHelper pathHelper = new PathHelper();
        List<String> graphPaths = pathHelper.getGraphOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);
        /*if (pathHelper.getMessage() != null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", pathHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }*/
        /*if (paths == null) {
            System.out.println("Erreur de path pour le concept :" + idConcept);
            if (pathLabel != null) {
                pathLabel.clear();
            }
            return;
        }*/
        List<List<String>> paths = pathHelper.getPathFromGraph(graphPaths);        
        pathLabel2 = pathHelper.getPathWithLabel2(connect.getPoolConnexion(), paths, idTheso, idLang);
    }    
    
    private void setRoles() {
        contributors = null;
        creator = null;
        boolean firstElement = true;
        
        if(StringUtils.isNotEmpty(nodeFullConcept.getCreatorName())){
            creator = nodeFullConcept.getCreatorName();
        }
        if(CollectionUtils.isNotEmpty(nodeFullConcept.getContributorName())){
            for (String contributor : nodeFullConcept.getContributorName()) {
                if (firstElement) {
                    contributors = contributor;
                    firstElement = false;
                } else {
                    contributors = contributors + "; " + contributor;
                }   
            }
        }
    }

    public String getNoteSource(String noteSource) {
        if (StringUtils.isEmpty(noteSource))
            return "";
        else
            return " (" + noteSource + ")";
    }

    
    /////////////////////////////////
    /////////////////////////////////
    // fonctions pour les notes /////    
    /////////////////////////////////
    /////////////////////////////////
    private void setNotesCurrentLang() {
        clearNotes();
        
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getNotes())) {
            nodeFullConcept.getNotes().stream()
                .filter(note1 -> selectedLang.equalsIgnoreCase(note1.getIdLang()))
                .findFirst()
                .ifPresent(note1 -> {
                    if (note == null) {
                        note = new NodeNote();
                    }
                    note.setId_note(note1.getIdNote());
                    note.setLexicalvalue(note1.getLabel());
                    note.setLang(note1.getIdLang());
                    note.setNoteSource(note1.getNoteSource());
                });
        } 
        
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getScopeNotes())) {
            nodeFullConcept.getScopeNotes().stream()
                .filter(note1 -> selectedLang.equalsIgnoreCase(note1.getIdLang()))
                .findFirst()
                .ifPresent(note1 -> {
                    if (scopeNote == null) {
                        scopeNote = new NodeNote();
                    }
                    scopeNote.setId_note(note1.getIdNote());
                    scopeNote.setLexicalvalue(note1.getLabel());
                    scopeNote.setLang(note1.getIdLang());
                    scopeNote.setNoteSource(note1.getNoteSource());
                });
        }        
        
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getChangeNotes())) {
            nodeFullConcept.getChangeNotes().stream()
                .filter(note1 -> selectedLang.equalsIgnoreCase(note1.getIdLang()))
                .findFirst()
                .ifPresent(note1 -> {
                    if (changeNote == null) {
                        changeNote = new NodeNote();
                    }
                    changeNote.setId_note(note1.getIdNote());
                    changeNote.setLexicalvalue(note1.getLabel());
                    changeNote.setLang(note1.getIdLang());
                    changeNote.setNoteSource(note1.getNoteSource());
                });
        }         
  
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getDefinitions())) {
            nodeFullConcept.getDefinitions().stream()
                .filter(note1 -> selectedLang.equalsIgnoreCase(note1.getIdLang()))
                .findFirst()
                .ifPresent(note1 -> {
                    if (definition == null) {
                        definition = new NodeNote();
                    }
                    definition.setId_note(note1.getIdNote());
                    definition.setLexicalvalue(note1.getLabel());
                    definition.setLang(note1.getIdLang());
                    definition.setNoteSource(note1.getNoteSource());
                });
        }          

        if (CollectionUtils.isNotEmpty(nodeFullConcept.getEditorialNotes())) {
            nodeFullConcept.getEditorialNotes().stream()
                .filter(note1 -> selectedLang.equalsIgnoreCase(note1.getIdLang()))
                .findFirst()
                .ifPresent(note1 -> {
                    if (editorialNote == null) {
                        editorialNote = new NodeNote();
                    }
                    editorialNote.setId_note(note1.getIdNote());
                    editorialNote.setLexicalvalue(note1.getLabel());
                    editorialNote.setLang(note1.getIdLang());
                    editorialNote.setNoteSource(note1.getNoteSource());
                });
        }        
   
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getExamples())) {
            nodeFullConcept.getExamples().stream()
                .filter(note1 -> selectedLang.equalsIgnoreCase(note1.getIdLang()))
                .findFirst()
                .ifPresent(note1 -> {
                    if (example == null) {
                        example = new NodeNote();
                    }
                    example.setId_note(note1.getIdNote());
                    example.setLexicalvalue(note1.getLabel());
                    example.setLang(note1.getIdLang());
                    example.setNoteSource(note1.getNoteSource());
                });
        }         
        
        if(CollectionUtils.isNotEmpty(nodeFullConcept.getHistoryNotes())){
            
        }   
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getHistoryNotes())) {
            nodeFullConcept.getHistoryNotes().stream()
                .filter(note1 -> selectedLang.equalsIgnoreCase(note1.getIdLang()))
                .findFirst()
                .ifPresent(note1 -> {
                    if (historyNote == null) {
                        historyNote = new NodeNote();
                    }
                    historyNote.setId_note(note1.getIdNote());
                    historyNote.setLexicalvalue(note1.getLabel());
                    historyNote.setLang(note1.getIdLang());
                    historyNote.setNoteSource(note1.getNoteSource());
                });
        }         
    }
    
    private void setNotesForAllLang() {
        clearNotesAllLang();

        if (CollectionUtils.isNotEmpty(nodeFullConcept.getNotes())) {
            nodeFullConcept.getNotes().stream()
                .map(note1 -> {
                    NodeNote nodeNote = new NodeNote();
                    nodeNote.setId_note(note1.getIdNote());
                    nodeNote.setLexicalvalue(note1.getLabel());
                    nodeNote.setLang(note1.getIdLang());
                    nodeNote.setNoteSource(note1.getNoteSource());
                    return nodeNote;
                })
                .forEach(noteAllLang::add);
        }        
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getScopeNotes())) {
            nodeFullConcept.getScopeNotes().stream()
                .map(note1 -> {
                    NodeNote nodeNote = new NodeNote();
                    nodeNote.setId_note(note1.getIdNote());
                    nodeNote.setLexicalvalue(note1.getLabel());
                    nodeNote.setLang(note1.getIdLang());
                    nodeNote.setNoteSource(note1.getNoteSource());
                    return nodeNote;
                })
                .forEach(scopeNoteAllLang::add);
        }     
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getChangeNotes())) {
            nodeFullConcept.getChangeNotes().stream()
                .map(note1 -> {
                    NodeNote nodeNote = new NodeNote();
                    nodeNote.setId_note(note1.getIdNote());
                    nodeNote.setLexicalvalue(note1.getLabel());
                    nodeNote.setLang(note1.getIdLang());
                    nodeNote.setNoteSource(note1.getNoteSource());
                    return nodeNote;
                })
                .forEach(changeNoteAllLang::add);
        }     
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getDefinitions())) {
            nodeFullConcept.getDefinitions().stream()
                .map(note1 -> {
                    NodeNote nodeNote = new NodeNote();
                    nodeNote.setId_note(note1.getIdNote());
                    nodeNote.setLexicalvalue(note1.getLabel());
                    nodeNote.setLang(note1.getIdLang());
                    nodeNote.setNoteSource(note1.getNoteSource());
                    return nodeNote;
                })
                .forEach(definitionAllLang::add);
        }         
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getEditorialNotes())) {
            nodeFullConcept.getEditorialNotes().stream()
                .map(note1 -> {
                    NodeNote nodeNote = new NodeNote();
                    nodeNote.setId_note(note1.getIdNote());
                    nodeNote.setLexicalvalue(note1.getLabel());
                    nodeNote.setLang(note1.getIdLang());
                    nodeNote.setNoteSource(note1.getNoteSource());
                    return nodeNote;
                })
                .forEach(editorialNoteAllLang::add);
        }     
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getExamples())) {
            nodeFullConcept.getExamples().stream()
                .map(note1 -> {
                    NodeNote nodeNote = new NodeNote();
                    nodeNote.setId_note(note1.getIdNote());
                    nodeNote.setLexicalvalue(note1.getLabel());
                    nodeNote.setLang(note1.getIdLang());
                    nodeNote.setNoteSource(note1.getNoteSource());
                    return nodeNote;
                })
                .forEach(exampleAllLang::add);
        }   
        if (CollectionUtils.isNotEmpty(nodeFullConcept.getHistoryNotes())) {
            nodeFullConcept.getHistoryNotes().stream()
                .map(note1 -> {
                    NodeNote nodeNote = new NodeNote();
                    nodeNote.setId_note(note1.getIdNote());
                    nodeNote.setLexicalvalue(note1.getLabel());
                    nodeNote.setLang(note1.getIdLang());
                    nodeNote.setNoteSource(note1.getNoteSource());
                    return nodeNote;
                })
                .forEach(historyNoteAllLang::add);
        }        
    }    

    public String getNoteValue(NodeNote nodeNote){
        if(nodeNote != null)
            return nodeNote.getLexicalvalue();
        return "";
    }
    
    public String getColorOfTypeConcept() {
        if ("concept".equalsIgnoreCase(nodeFullConcept.getConceptType()))
            return "";
        else
            return "#fcd8bf";
    }

    public String geLabelReciprocal(NodeConceptType nodeConceptType) {
        if ("concept".equalsIgnoreCase(nodeConceptType.getCode())) {
            return "";
        }
        String idLang = languageBean.getIdLangue();
        if (nodeConceptType.isReciprocal()) {
            if ("fr".equalsIgnoreCase(idLang)) {
                return " - Relation réciproque";
            }
            if ("en".equalsIgnoreCase(idLang)) {
                return " - Reciprocal relation";
            }
        } else {
            if ("fr".equalsIgnoreCase(idLang)) {
                return " - Relation à sens unique";
            }
            if ("en".equalsIgnoreCase(idLang)) {
                return " - One-way relationship";
            }
        }
        return "";
    }

    /**
     * permet de retouver la langue de l'interface et se limiter au fr et en
     *
     * @return
     */
    private String getIdLangOfInterface() {
        String idLang;
        if ("en".equalsIgnoreCase(languageBean.getIdLangue()) || "fr".equalsIgnoreCase(languageBean.getIdLangue())) {
            idLang = languageBean.getIdLangue();
        } else
            idLang = "en";
        return idLang;
    }

    public Boolean isMapVisible() {
        return ObjectUtils.isNotEmpty(nodeFullConcept) && CollectionUtils.isNotEmpty(nodeFullConcept.getGps());
    }

    public void onRowEdit(RowEditEvent<Gps> event) {
        gpsRepository.updateGps(event.getObject());
        createMap(nodeFullConcept.getIdentifier(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage("Coordonnée modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowCancel(RowEditEvent<Gps> event) {
        gpsRepository.removeGps(event.getObject());

        createMap(nodeFullConcept.getIdentifier(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage("Coordonnée supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void addNewGps() {
        Gps gps = new Gps();
        gps.setIdTheso(selectedTheso.getCurrentIdTheso());
        gps.setIdConcept(nodeFullConcept.getIdentifier());
        gps.setPosition(nodeFullConcept.getGps().size() + 1);

        gpsRepository.saveNewGps(gps);
        createMap(nodeFullConcept.getIdentifier(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage("Nouvelle coordonnée ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

//    public void onRowReorder(ReorderEvent event) {
//        Integer fromId = 0, toId = 0;
//
//        for (Gps gps : nodeFullConcept.getGps()) {
//            if (gps.getPosition() == (event.getFromIndex() + 1)) {
//                fromId = gps.getId();
//            }
//            if (gps.getPosition() == (event.getToIndex() + 1)) {
//                toId = gps.getId();
//            }
//        }
//
//        gpsRepository.updateGpsPosition(fromId, (event.getToIndex() + 1));
//        gpsRepository.updateGpsPosition(toId, (event.getFromIndex() + 1));
//
//        createMap(nodeConcept.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);
//
//        FacesMessage msg = new FacesMessage("Réorganisation des coordonnées effectuée");
//        FacesContext.getCurrentInstance().addMessage(null, msg);
//    }

    private String gpsList;

    public void formatGpsList() {

        if (StringUtils.isEmpty(gpsList)) {
            nodeFullConcept.setGps(null);
            gpsRepository.removeGpsByConcept(nodeFullConcept.getIdentifier(), selectedTheso.getCurrentIdTheso());
        } else {
            List<Gps> gpsListTmps = readGps(gpsList, selectedTheso.getCurrentIdTheso(), nodeFullConcept.getIdentifier());

            if (ObjectUtils.isNotEmpty(gpsListTmps)) {
                nodeFullConcept.setGps(getResourceGpsFromGps(gpsListTmps));
                gpsModeSelected = getGpsMode(gpsListTmps);
                gpsRepository.removeGpsByConcept(nodeFullConcept.getIdentifier(), selectedTheso.getCurrentIdTheso());
                for (Gps gps : gpsListTmps) {
                    gpsRepository.saveNewGps(gps);
                }
            } else {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Aucune coordonnée GPS trouvée !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                PrimeFaces.current().ajax().update("messageIndex");
                return;
            }
        }

        createMap(nodeFullConcept.getIdentifier(), selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Coordonnée GPS modifiés !");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:rightTab");
    }

    private GpsMode getGpsMode(List<Gps> nodeGps) {
        if (CollectionUtils.isNotEmpty(nodeGps)) {
            var lastIndex = nodeGps.size() - 1;
            if (nodeGps.size() == 1) {
                return GpsMode.POINT;
            } else if (nodeGps.get(0).getLongitude().equals(nodeGps.get(lastIndex).getLongitude())
                    && nodeGps.get(0).getLatitude().equals(nodeGps.get(lastIndex).getLatitude())) {
                return GpsMode.POLYGONE;
            } else {
                return GpsMode.POLYLINE;
            }
        }
        return null;
    }

    public static List<Gps> readGps(String gpsValue, String idTheso, String idConcept) {

        List<Gps> gpsList = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\(([^)]+)\\)").matcher(gpsValue);
        while (matcher.find()) {

           // Matcher matcher2 = Pattern.compile("([0-9]+[.,][0-9]+) ([0-9]+[.,][0-9]+)").matcher(matcher.group(1));
           // Matcher matcher2 = Pattern.compile("\\((-?[0-9]+[.,][0-9]+)\\s+(-?[0-9]+[.,][0-9]+)\\)").matcher(gpsValue);
            Matcher matcher2 = Pattern.compile("(-?[0-9]+[.,][0-9]+)\\s+(-?[0-9]+[.,][0-9]+)").matcher(gpsValue);
            
        
            while (matcher2.find()) {
                Gps gpsTmp = new Gps();
                gpsTmp.setIdTheso(idTheso);
                gpsTmp.setIdConcept(idConcept);
                gpsTmp.setPosition(gpsList.size() + 1);
                gpsTmp.setLatitude(Double.parseDouble(matcher2.group(1).replace(",", ".")));
                gpsTmp.setLongitude(Double.parseDouble(matcher2.group(2).replace(",", ".")));
                gpsList.add(gpsTmp);
            }
        }

        return gpsList;
    }
}
