package fr.cnrs.opentheso.bean.menu.theso;

import fr.cnrs.opentheso.bdd.helper.CorpusHelper;
import fr.cnrs.opentheso.bdd.helper.LanguageHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bean.alignment.ResultatAlignement;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.viewconcepts.TreeConcepts;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.leftbody.viewliste.ListIndex;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.MenuBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ProjectBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorHomeBean;
import fr.cnrs.opentheso.bean.rightbody.viewhome.ViewEditorThesoHomeBean;
import fr.cnrs.opentheso.bean.search.SearchBean;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import fr.cnrs.opentheso.repositories.ThesaurusRepository;
import fr.cnrs.opentheso.repositories.UserGroupLabelRepository;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.primefaces.PrimeFaces;



@SessionScoped
@Named(value = "selectedTheso")
public class SelectedTheso implements Serializable {

    @Inject private LanguageBean languageBean;
    @Inject private Connect connect;
    @Inject private IndexSetting indexSetting;
    @Inject private TreeGroups treeGroups;
    @Inject private TreeConcepts treeConcepts;
    @Inject private Tree tree;
    @Inject private ListIndex listIndex;
    @Inject private ConceptView conceptBean;
    @Inject private SearchBean searchBean;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private ViewEditorThesoHomeBean viewEditorThesoHomeBean;
    @Inject private ViewEditorHomeBean viewEditorHomeBean;
    @Inject private RightBodySetting rightBodySetting;
    @Inject private MenuBean menuBean;
    @Inject private PropositionBean propositionBean;
    @Inject private UserGroupLabelRepository userGroupLabelRepository;
    @Inject private ThesaurusRepository thesaurusRepository;
    @Inject private CurrentUser currentUser;
    @Inject private ProjectBean projectBean;

    private static final long serialVersionUID = 1L;

    private List<UserGroupLabel> projects;

    private boolean isFromUrl;

    private String selectedIdTheso;
    private String currentIdTheso;
    private String optionThesoSelected;

    private ArrayList<NodeLangTheso> nodeLangs;

    private String selectedLang; // la langue qu'on vient de séléctionner
    private String currentLang; // la langue en cours dans la session
    private boolean isActionFromConcept;

    private String idConceptFromUri;
    private String idThesoFromUri;
    private String idGroupFromUri;

    private boolean isUriRequest = false;

    private String thesoName;
    private boolean sortByNotation;
    
    private boolean isNetworkAvailable;

    private String localUri;

    private String projectIdSelected;
    private List<UserGroupLabel> projectsList;
    private List<ResultatAlignement> resultAlignementList;

    private boolean haveActiveCorpus;

    @PreDestroy
    public void destroy(){
        clear();
    }

    public void clear(){
        if(nodeLangs!= null){
            nodeLangs.clear();
            nodeLangs = null;
        }
        selectedIdTheso = null;
        currentIdTheso = null;
        selectedLang = null;
        currentLang = null;
        idThesoFromUri = null;      
        thesoName = null;   
        localUri = null;
        projectIdSelected = "-1";
    }
    
    @PostConstruct
    public void initializing() {

        if (!connect.isConnected()) {
            System.err.println("Erreur de connexion BDD");
            return;
        }
        
        isNetworkAvailable = true;
        roleOnThesoBean.showListTheso();
        sortByNotation = false; 

        loadProject();
    }

    public void init() {
        selectedLang = null;
        currentLang = null;
        nodeLangs = null;
        selectedIdTheso = null;
        currentIdTheso = null;
        thesoName = null;
        localUri = null;
    }

    private void initIdsFromUri() {
        idConceptFromUri = null;
        idThesoFromUri = null;
    }

    /**
     * capte les actions qui proviennent de la vue concept pour éviter
     * d'initialiser la vue ! sert pour replacer l'arbre
     */
    public void actionFromConceptToOn() {
        isActionFromConcept = true;
    }

    public void setSelectedOptionTheso() {
        switch(optionThesoSelected) {
            case "Option1":
                viewEditorHomeBean.reset();
                break;
            case "Option2":
                viewEditorHomeBean.initText();
                break;
            case "Option3":
                viewEditorHomeBean.initGoogleAnalytics();
                break;
        }
    }

    public String getUriOfTheso(NodePreference nodePreference){
        String contextPath = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
        String serverAdress = FacesContext.getCurrentInstance().getExternalContext().getRequestServerName();
        String protocole = FacesContext.getCurrentInstance().getExternalContext().getRequestScheme();
        String baseUrl = protocole + "://" + serverAdress + contextPath;

        if(nodePreference == null) {
            return baseUrl + "/?idt=" + currentIdTheso;
        }
        else {
            String idArk = new ThesaurusHelper().getIdArkOfThesaurus(connect.getPoolConnexion(), currentIdTheso);
            if(StringUtils.isEmpty(idArk)){
                return baseUrl + "/?idt=" + currentIdTheso;
            } else {
                return baseUrl + "/api/ark:/" + idArk;
            }
        }
    }

    private void thesoHaveActiveCorpus(){
        haveActiveCorpus = new CorpusHelper().isHaveActiveCorpus(connect.getPoolConnexion(), getSelectedIdTheso());
    }    
    
    /**
     * Permet de charger le thésaurus sélectionné C'est le point d'entrée de
     * l'application
     * @throws java.io.IOException
     */
    public void setSelectedTheso() throws IOException {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        localUri = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()+"/";  
        connect.setLocalUri(localUri);
        
      //  currentUser.getUserPermissions();
        
        thesoHaveActiveCorpus();
        
        viewEditorThesoHomeBean.reset();
        viewEditorHomeBean.reset();
        if (isUriRequest) {
            isUriRequest = false;
            
            if (searchBean.isBarVisisble()) {
                searchBean.setNodeConceptSearchs(new ArrayList<>());
                searchBean.setBarVisisble(false);
                PrimeFaces.current().executeScript("disparaitre();");
            }
            menuBean.redirectToThesaurus();
            return;
        }

        if (StringUtils.isEmpty(selectedIdTheso)) {
            currentUser.resetUserPermissionsForThisTheso();
            treeGroups.reset();
            tree.reset();
            treeConcepts.reset();
            listIndex.reset();
            conceptBean.init();
            init();
            
            indexSetting.setIsSelectedTheso(false);
           
            if ("-1".equals(projectIdSelected)) {
                indexSetting.setProjectSelected(false); 
            } else {
                indexSetting.setProjectSelected(true);                
            }
            projectBean.init();

            roleOnThesoBean.setSelectedThesoForSearch(new ArrayList());
            for (RoleOnThesoBean.ThesoModel thesoModel : roleOnThesoBean.getListTheso()) {
                roleOnThesoBean.getSelectedThesoForSearch().add(thesoModel.getId());
            }
            
            if (searchBean.isBarVisisble()) {
                searchBean.setNodeConceptSearchs(new ArrayList<>());
                searchBean.setBarVisisble(false);
                PrimeFaces.current().executeScript("disparaitre();");
            }

            menuBean.redirectToThesaurus();
            return;
        }

        // après un raffraichissement F5
        if (selectedIdTheso.equalsIgnoreCase(currentIdTheso)) {
            if (!selectedLang.equalsIgnoreCase(currentLang)) {
                startNewLang();
            }
            
            if (searchBean.isBarVisisble()) {
                searchBean.setNodeConceptSearchs(new ArrayList<>());
                searchBean.setBarVisisble(false);
                PrimeFaces.current().executeScript("disparaitre();");
            }
            
            menuBean.redirectToThesaurus();
            return;
        }

        sortByNotation = false;
        startNewTheso(null);
        currentUser.initUserPermissionsForThisTheso(selectedIdTheso);
        indexSetting.setIsSelectedTheso(true);
        indexSetting.setIsValueSelected(false);
        indexSetting.setIsHomeSelected(true);
        indexSetting.setIsThesoActive(true);
        
        propositionBean.searchNewPropositions();

        roleOnThesoBean.setUserRoleOnThisTheso();

        for (RoleOnThesoBean.ThesoModel thesoModel : roleOnThesoBean.getListTheso()) {
            if (selectedIdTheso.equals(thesoModel.getId())) {
                roleOnThesoBean.setSelectedThesoForSearch(Collections.singletonList(selectedIdTheso));
            }
        }
        
        if (searchBean.isBarVisisble()) {
            searchBean.setNodeConceptSearchs(new ArrayList<>());
            searchBean.setBarVisisble(false);
            PrimeFaces.current().executeScript("disparaitre();");
        }

        indexSetting.setProjectSelected(false);
        menuBean.redirectToThesaurus();
    }

    public void redirectToTheso() throws IOException{
        menuBean.redirectToThesaurus();
    }

    
    /**
     * Récupération de tous les projets en fonction de l'utilisateur :
     * - mode non connecté = on charge tous les projets
     * - mode connecté = on charge uniquement les projets de l'utilisateur
     */
    public void loadProject() {

        if (ObjectUtils.isEmpty(currentUser.getNodeUser())) {
            currentUser.initAllProject();
            currentUser.initAllTheso();
            projectIdSelected = ""+currentUser.getUserPermissions().getSelectedProject();
            selectedIdTheso = currentUser.getUserPermissions().getSelectedTheso();
            projectsList = userGroupLabelRepository.getProjectsByThesoStatus(false);
        } else {
            if (currentUser.getNodeUser().isSuperAdmin()) {
                projectsList = userGroupLabelRepository.getAllProjects();
            } else {
                projectsList = userGroupLabelRepository.getProjectsByUserId(currentUser.getNodeUser().getIdUser());
            }
        }
        if(projectsList == null || projectsList.isEmpty()) {
            projectIdSelected = "-1";
        }
    }    
    
    public void setSelectedProject() {
        projectBean.setLangCodeSelected(languageBean.getIdLangue());
        if (CollectionUtils.isEmpty(projectBean.getAllLangs())) {
            projectBean.setAllLangs(new LanguageHelper().getAllLanguages(connect.getPoolConnexion()));
        }
        if ("-1".equals(projectIdSelected)) {
            currentUser.resetUserPermissionsForThisProject();
            currentUser.reloadAllThesoOfAllProject();
            roleOnThesoBean.showListTheso();
           // currentIdTheso = null;
           // selectedIdTheso = "";
           if(StringUtils.isEmpty(selectedIdTheso))
                indexSetting.setSelectedTheso(false); 
            indexSetting.setProjectSelected(false);
        } else {
            currentUser.initUserPermissionsForThisProject(Integer.parseInt(projectIdSelected));
            boolean isConnect = ObjectUtils.isNotEmpty(currentUser.getNodeUser());
            projectBean.initProject(projectIdSelected, !isConnect);

            if (!projectBean.getListeThesoOfProject().isEmpty()) {
                roleOnThesoBean.setAuthorizedTheso(projectBean.getListeThesoOfProject().stream()
                        .map(NodeIdValue::getId)
                        .collect(Collectors.toList()));
            } else {
                roleOnThesoBean.setAuthorizedTheso(Collections.emptyList());
            }
            roleOnThesoBean.addAuthorizedThesoToHM();
            roleOnThesoBean.setUserRoleOnThisTheso();

            if (CollectionUtils.isNotEmpty(projectBean.getListeThesoOfProject())) {
                if (projectBean.getListeThesoOfProject().stream()
                        .filter(element -> StringUtils.equalsIgnoreCase(element.getId(), currentIdTheso))
                        .findFirst()
                        .isEmpty()) {
                    selectedIdTheso = null;
                    currentIdTheso = null;
                }
            } else {
                selectedIdTheso = null;
                currentIdTheso = null;
            }

            if (StringUtils.isEmpty(selectedIdTheso)) {
                indexSetting.setProjectSelected(true);
            }

            projectBean.init();
        }
    }

    
    public void setSelectedThesoForSearch() throws IOException {
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        localUri = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath()+"/";  
        connect.setLocalUri(localUri);

        viewEditorThesoHomeBean.reset();
        viewEditorHomeBean.reset();

        // après un raffraichissement F5
        if (selectedIdTheso.equalsIgnoreCase(currentIdTheso)) {
            if (!selectedLang.equalsIgnoreCase(currentLang)) {
                startNewLang();
            }
            return;
        }

        sortByNotation = false;
        startNewTheso(null);
        indexSetting.setIsSelectedTheso(true);
        indexSetting.setIsValueSelected(true);
        indexSetting.setIsHomeSelected(false);
        indexSetting.setIsThesoActive(true);
        indexSetting.setProjectSelected(false);
    }

    /**
     * permet de recharger l'arbre des collections 
     */
    public void reloadGroups(){
        treeGroups.reset();
        treeGroups.initialise(selectedIdTheso, selectedLang);   
        rightBodySetting.setIndex("1");
    }
    
    /**
     * permet de recharger l'arbre des collections 
     */
    public void reloadConceptTree(){
        treeConcepts.reset();
        treeConcepts.initialise(selectedIdTheso, selectedLang);        
    }    
    
    /**
     * Permet de Re-charger le thésaurus sélectionné, pour activer des mises à jour non prises en compte
     * @throws java.io.IOException
     */
    public void reloadSelectedTheso() throws IOException {
        loadProject();
        roleOnThesoBean.showListTheso();

        searchBean.reset();
        viewEditorThesoHomeBean.reset();
        viewEditorHomeBean.reset();
        treeGroups.reset();

        if (selectedIdTheso == null || selectedIdTheso.isEmpty()) {
            return;
        }
        startNewTheso(null);
        tree.setIdConceptSelected(null);

        indexSetting.setIsSelectedTheso(true);
        indexSetting.setIsValueSelected(false);
        indexSetting.setIsHomeSelected(true);

        menuBean.redirectToThesaurus();
    }

    /**
     * permet de changer la langue du thésaurus et recharger les données
     */
    public void changeLang() {
        if (isUriRequest) {
            isUriRequest = false;
            return;
        }
        if (selectedLang == null || selectedIdTheso == null) {
            return;
        }

        if (selectedLang.equalsIgnoreCase(currentLang)) {
            isActionFromConcept = false;
            return;
        }
        if (selectedLang.equalsIgnoreCase("all")) {
            isActionFromConcept = false;
            return;
        }        
        startNewLang();
    }

    /**
     * initialise le nouveau thésaurus avec l'identifiant de thésaurus
     * sélectionné si la langue est fournie, on initialise dans cette langue,
     * sinon, on prend la langue source du thésaurus
     *
     * @param idLang
     */
    private void startNewTheso(String idLang) {
        currentIdTheso = selectedIdTheso;
        // setting des préférences du thésaurus sélectionné
        roleOnThesoBean.initNodePref();
        if (StringUtils.isEmpty(idLang)) {
            idLang = getIdLang();
        }
        if (StringUtils.isEmpty(idLang)) {
            return;
        }

        nodeLangs = new ThesaurusHelper().getAllUsedLanguagesOfThesaurusNode(
                connect.getPoolConnexion(),
                selectedIdTheso,
                languageBean.getIdLangue());

        currentLang = idLang;
        selectedLang = idLang;
        thesoName = new ThesaurusHelper().getTitleOfThesaurus(connect.getPoolConnexion(),
                selectedIdTheso, selectedLang);

        // initialisation de l'arbre des groupes
        treeGroups.reset();
        treeGroups.initialise(selectedIdTheso, selectedLang);

        treeConcepts.reset();
        treeConcepts.initialise(selectedIdTheso, selectedLang);

        tree.reset();
        tree.initialise(selectedIdTheso, selectedLang);

        listIndex.reset();
        conceptBean.clear();
        conceptBean.init();
    }

    private void startNewLang() {
        currentLang = selectedLang;
        
        treeGroups.reset();
        tree.reset();
        listIndex.reset();

        // initialisation de l'arbre des groupes
        treeGroups.initialise(selectedIdTheso, selectedLang);
        treeConcepts.initialise(selectedIdTheso, selectedLang);
        tree.initialise(selectedIdTheso, selectedLang);
        
        if (!isActionFromConcept) {
            conceptBean.init();
        }
        isActionFromConcept = false;

        searchBean.onSelectConcept(selectedIdTheso, tree.getIdConceptSelected(), selectedLang);
        
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.executeScript("srollToSelected()");
        }
        
        if (searchBean.isBarVisisble()) {
            searchBean.setNodeConceptSearchs(new ArrayList<>());
            searchBean.setBarVisisble(false);
            PrimeFaces.current().executeScript("disparaitre();");
        }
    }

    private String getIdLang() {
        String idLang = connect.getWorkLanguage();
        if (roleOnThesoBean.getNodePreference() != null) {
            idLang = roleOnThesoBean.getNodePreference().getSourceLang();
        }
        return idLang;
    }

    /**
     * Pour sélectionner un thésaurus ou un concept en passant par l'URL
     */
    public void preRenderView() throws IOException {

        if (idThesoFromUri == null) {
            isFromUrl = false;
            return;
        }

        isFromUrl = true;
        if (idThesoFromUri.equalsIgnoreCase(selectedIdTheso)) {
            if (idConceptFromUri == null || idConceptFromUri.isEmpty()) {
                //test si c'est une collection 
                if (idGroupFromUri == null || idGroupFromUri.isEmpty()) {
                    initIdsFromUri();
                    return;
                } else {
                    // sélectionner le groupe ou collection
                    treeGroups.selectThisGroup(idGroupFromUri.trim());
                    initIdsFromUri();
                    return;
                }

            }
            if (currentLang == null) {
                String idLang = getIdLang();
                if (idLang == null || idLang.isEmpty()) {
                    return;
                }
                currentLang = idLang;
                selectedLang = idLang;
            }
            thesoHaveActiveCorpus();
            conceptBean.getConcept(selectedIdTheso, idConceptFromUri, currentLang);
            actionFromConceptToOn();
            initIdsFromUri();
            thesoName = new ThesaurusHelper().getTitleOfThesaurus(connect.getPoolConnexion(), selectedIdTheso, selectedLang);
            return;
        }

        // gestion de l'accès par thésaurus d'un identifiant différent 
        if (!idThesoFromUri.equalsIgnoreCase(selectedIdTheso)) {
            if (isValidTheso(idThesoFromUri)) {
                currentUser.resetUserPermissionsForThisTheso();
                /// chargement du thésaurus
                selectedIdTheso = idThesoFromUri;
                roleOnThesoBean.initNodePref(selectedIdTheso);
                startNewTheso(roleOnThesoBean.getNodePreference().getSourceLang());//currentLang);
                indexSetting.setIsSelectedTheso(true);
                indexSetting.setIsThesoActive(true);
                rightBodySetting.setIndex("0");  
                thesoHaveActiveCorpus();
                if (idConceptFromUri != null && !idConceptFromUri.isEmpty()) {
                    // chargement du concept puisqu'il est renseigné
                    conceptBean.getConcept(currentIdTheso, idConceptFromUri, currentLang);
                    actionFromConceptToOn();
                    
                } else {
                    //cas d'appel pour une collection
                    if(idGroupFromUri != null && !idGroupFromUri.isEmpty()) {
                        treeGroups.selectThisGroup(idGroupFromUri.trim());
                        rightBodySetting.setIndex("1");                        
                        initIdsFromUri();
                     //   return;
                    } else 
                        indexSetting.setIsHomeSelected(true);
                }
            } else {
                return;
            }
        }
        thesoHaveActiveCorpus();
        currentUser.initUserPermissionsForThisTheso(selectedIdTheso);
        initIdsFromUri();
    }

    private boolean isValidTheso(String idTheso) {
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        return !thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), idTheso);
    }

    public String getIdConceptFromUri() {
        return idConceptFromUri;
    }

    public void setIdConceptFromUri(String idConceptFromUri) {
        this.idConceptFromUri = idConceptFromUri;
    }

    public String getIdGroupFromUri() {
        return idGroupFromUri;
    }

    public void setIdGroupFromUri(String idGroupFromUri) {
        this.idGroupFromUri = idGroupFromUri;
    }

    public String getIdThesoFromUri() {
        return idThesoFromUri;
    }

    public void setIdThesoFromUri(String idThesoFromUri) {
        this.idThesoFromUri = idThesoFromUri;
    }

    public ArrayList<NodeLangTheso> getNodeLangs() {
        return nodeLangs;
    }

    public void setNodeLangs(ArrayList<NodeLangTheso> nodeLangs) {
        this.nodeLangs = nodeLangs;
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public String getCurrentLang() {
        return currentLang;
    }

    public void setCurrentLang(String currentLang) {
        this.currentLang = currentLang;
    }

    public String getCurrentIdTheso() {
        return currentIdTheso;
    }

    public void setCurrentIdTheso(String currentIdTheso) {
        this.currentIdTheso = currentIdTheso;
    }

    public String getThesoName() {
        return thesoName;
    }

    public void setThesoName(String thesoName) {
        this.thesoName = thesoName;
    }

    public String getSelectedIdTheso() {
        return selectedIdTheso;
    }

    public void setSelectedIdTheso(String selectedIdTheso) {
        this.selectedIdTheso = selectedIdTheso;
    }

    public boolean isSortByNotation() {
        return sortByNotation;
    }

    public void setSortByNotation(boolean sortByNotation) {
        this.sortByNotation = sortByNotation;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        this.localUri = localUri;
    }

    public String getOptionThesoSelected() {
        return optionThesoSelected;
    }

    public void setOptionThesoSelected(String optionThesoSelected) {
        this.optionThesoSelected = optionThesoSelected;
    }

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean LanguageBean) {
        this.languageBean = LanguageBean;
    }

    public Connect getConnect() {
        return connect;
    }

    public void setConnect(Connect connect) {
        this.connect = connect;
    }

    public IndexSetting getIndexSetting() {
        return indexSetting;
    }

    public void setIndexSetting(IndexSetting indexSetting) {
        this.indexSetting = indexSetting;
    }

    public TreeGroups getTreeGroups() {
        return treeGroups;
    }

    public void setTreeGroups(TreeGroups treeGroups) {
        this.treeGroups = treeGroups;
    }

    public TreeConcepts getTreeConcepts() {
        return treeConcepts;
    }

    public void setTreeConcepts(TreeConcepts treeConcepts) {
        this.treeConcepts = treeConcepts;
    }

    public Tree getTree() {
        return tree;
    }

    public void setTree(Tree tree) {
        this.tree = tree;
    }

    public ListIndex getListIndex() {
        return listIndex;
    }

    public void setListIndex(ListIndex listIndex) {
        this.listIndex = listIndex;
    }

    public ConceptView getConceptBean() {
        return conceptBean;
    }

    public void setConceptBean(ConceptView conceptBean) {
        this.conceptBean = conceptBean;
    }

    public SearchBean getSearchBean() {
        return searchBean;
    }

    public void setSearchBean(SearchBean searchBean) {
        this.searchBean = searchBean;
    }

    public RoleOnThesoBean getRoleOnThesoBean() {
        return roleOnThesoBean;
    }

    public void setRoleOnThesoBean(RoleOnThesoBean roleOnThesoBean) {
        this.roleOnThesoBean = roleOnThesoBean;
    }

    public ViewEditorThesoHomeBean getViewEditorThesoHomeBean() {
        return viewEditorThesoHomeBean;
    }

    public void setViewEditorThesoHomeBean(ViewEditorThesoHomeBean viewEditorThesoHomeBean) {
        this.viewEditorThesoHomeBean = viewEditorThesoHomeBean;
    }

    public ViewEditorHomeBean getViewEditorHomeBean() {
        return viewEditorHomeBean;
    }

    public void setViewEditorHomeBean(ViewEditorHomeBean viewEditorHomeBean) {
        this.viewEditorHomeBean = viewEditorHomeBean;
    }

    public RightBodySetting getRightBodySetting() {
        return rightBodySetting;
    }

    public void setRightBodySetting(RightBodySetting rightBodySetting) {
        this.rightBodySetting = rightBodySetting;
    }

    public MenuBean getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(MenuBean menuBean) {
        this.menuBean = menuBean;
    }

    public PropositionBean getPropositionBean() {
        return propositionBean;
    }

    public void setPropositionBean(PropositionBean propositionBean) {
        this.propositionBean = propositionBean;
    }

    public UserGroupLabelRepository getUserGroupLabelRepository() {
        return userGroupLabelRepository;
    }

    public void setUserGroupLabelRepository(UserGroupLabelRepository userGroupLabelRepository) {
        this.userGroupLabelRepository = userGroupLabelRepository;
    }

    public ThesaurusRepository getThesaurusRepository() {
        return thesaurusRepository;
    }

    public void setThesaurusRepository(ThesaurusRepository thesaurusRepository) {
        this.thesaurusRepository = thesaurusRepository;
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public ProjectBean getProjectBean() {
        return projectBean;
    }

    public void setProjectBean(ProjectBean projectBean) {
        this.projectBean = projectBean;
    }

    public List<UserGroupLabel> getProjects() {
        return projects;
    }

    public void setProjects(List<UserGroupLabel> projects) {
        this.projects = projects;
    }

    public boolean isIsFromUrl() {
        return isFromUrl;
    }

    public void setIsFromUrl(boolean isFromUrl) {
        this.isFromUrl = isFromUrl;
    }

    public boolean isIsActionFromConcept() {
        return isActionFromConcept;
    }

    public void setIsActionFromConcept(boolean isActionFromConcept) {
        this.isActionFromConcept = isActionFromConcept;
    }

    public boolean isIsUriRequest() {
        return isUriRequest;
    }

    public void setIsUriRequest(boolean isUriRequest) {
        this.isUriRequest = isUriRequest;
    }

    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }

    public void setIsNetworkAvailable(boolean isNetworkAvailable) {
        this.isNetworkAvailable = isNetworkAvailable;
    }

    public String getProjectIdSelected() {
        return projectIdSelected;
    }

    public void setProjectIdSelected(String projectIdSelected) {
        this.projectIdSelected = projectIdSelected;
    }

    public List<UserGroupLabel> getProjectsList() {
        return projectsList;
    }

    public void setProjectsList(List<UserGroupLabel> projectsList) {
        this.projectsList = projectsList;
    }

    public List<ResultatAlignement> getResultAlignementList() {
        return resultAlignementList;
    }

    public void setResultAlignementList(List<ResultatAlignement> resultAlignementList) {
        this.resultAlignementList = resultAlignementList;
    }

    public boolean isHaveActiveCorpus() {
        return haveActiveCorpus;
    }

    public void setHaveActiveCorpus(boolean haveActiveCorpus) {
        this.haveActiveCorpus = haveActiveCorpus;
    }
}
