package fr.cnrs.opentheso.bean.menu.theso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.AccessThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.StatisticHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserRoleGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.userpermissions.NodeProjectThesoRole;
import fr.cnrs.opentheso.bdd.helper.nodes.userpermissions.NodeThesoRole;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;

/**
 *
 * @author Miled Rousset
 */
@SessionScoped
@Named(value = "roleOnTheso")
public class RoleOnThesoBean implements Serializable {

    @Inject private Connect connect;
    @Inject private CurrentUser currentUser;
    @Inject private LanguageBean languageBean;
    @Inject private SelectedTheso selectedTheso;

    //liste des thesaurus public suivant les droits de l'utilisateur, n'inclus pas les thésaurus privés
    private List<ThesoModel> listTheso;    
    //liste des thesaurus For export or management avec droits admin pour l'utilisateur en cours
    private Map<String, String> listThesoAsAdmin;    
    
    // la liste des thésaurus autorisés pour l'utilisateur où il est au minimun manager pour pouvoir les modifier
    private ArrayList<NodeIdValue> nodeListTheso;
    
    // la liste des thésaurus autorisés pour l'utilisateur où il est admin pour avoir le droit de les exporter
    private ArrayList<NodeIdValue> nodeListThesoAsAdmin;    
    
    private ArrayList<NodeIdValue> nodeListThesoAsAdminFiltered;       

    private List<String> selectedThesoForSearch;

    //thesaurus à gérer
    private Thesaurus thesoInfos;

    private boolean isSuperAdmin = false;
    private boolean isAdminOnThisTheso = false;
    private boolean isManagerOnThisTheso = false;
    private boolean isContributorOnThisTheso = false;
    private boolean isNoRole = false;

    //liste des thesaurus pour l'utilisateur connecté suivant ses droits, inclus aussi ses thésaurus privés 
    private List<String> authorizedTheso;
    
    private List<String> authorizedThesoAsAdmin;
    
    private NodePreference nodePreference;

    private NodeUserRoleGroup nodeUserRoleGroup;

    @PreDestroy
    public void destroy(){
        clear();
    }      
    public void clear(){
        nodeUserRoleGroup = null;
        nodePreference = null;
        thesoInfos = null;
       
        if(listTheso != null){
            listTheso.clear();
            listTheso = null;
        }
        if(listThesoAsAdmin != null){
            listThesoAsAdmin.clear();
            listThesoAsAdmin = null;
        }
        if(nodeListTheso != null){
            nodeListTheso.clear();
            nodeListTheso = null;
        }
        if(nodeListThesoAsAdmin != null){
            nodeListThesoAsAdmin.clear();
            nodeListThesoAsAdmin = null;
        }
        if(authorizedTheso != null){
            authorizedTheso.clear();
            authorizedTheso = null;
        }
        if(authorizedThesoAsAdmin != null){
            authorizedThesoAsAdmin.clear();
            authorizedThesoAsAdmin = null;
        }
    }

    
    @PostConstruct
    public void init() {
    }

   
    //// restructuration de la classe le 05/04/2018 par Miled Rousset//////    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////// Nouvelles fonctions //////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////       
    /**
     * permet de récupérer les préférences pour le thésaurus sélectionné s'il y
     * en a pas, on les initialises par les valeurs par defaut
     *
     * #MR
     */
    public void initNodePref() {
        if (selectedTheso.getCurrentIdTheso() == null) {
            return;
        }
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        if (connect.getPoolConnexion() != null) {
            nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
            if (nodePreference == null) { // cas où il n'y a pas de préférence pour ce thésaurus, il faut les créer 
                preferencesHelper.initPreferences(connect.getPoolConnexion(),
                        selectedTheso.getCurrentIdTheso(), connect.getWorkLanguage());
                nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
            }
            return;
        }
        nodePreference = null;
    }
    public void initNodePref(String idTheso) {
        if (idTheso == null) {
            return;
        }
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        if (connect.getPoolConnexion() != null) {
            nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), idTheso);
            if (nodePreference == null) { // cas où il n'y a pas de préférence pour ce thésaurus, il faut les créer 
                preferencesHelper.initPreferences(connect.getPoolConnexion(),
                        idTheso, connect.getWorkLanguage());
                nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), idTheso);
            }
            return;
        }
        nodePreference = null;
    }    

    /**
     * met à jour les préférences après une modification
     */
    public void updatePreferences() {
        if (!new PreferencesHelper().updateAllPreferenceUser(connect.getPoolConnexion(), nodePreference, selectedTheso.getCurrentIdTheso())) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, languageBean.getMsg("error") + " :", languageBean.getMsg("error.BDD")));
            return;
        }
        initNodePref();
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(languageBean.getMsg("info") + " :", languageBean.getMsg("currentUser.info6")));
    }

    /**
     * permet d'initialiser la liste des thésaurus suivant les droits
     */
    public void showListTheso() {
        if (currentUser.getNodeUser() == null) {
            setPublicThesos();
        } else {
            setOwnerThesos();
        }
    }

    /**
     * fonction pour sortir une liste (sous forme de hashMap ) de thesaurus
     * correspondant à l'utilisateur connecté permet de charger les thésaurus
     * autorisés pour l'utilisateur en cours on récupère les id puis les
     * tradcutions (ceci permet de récupérer les thésaurus non traduits) #MR
     */
    public void setOwnerThesos() {
        if (currentUser.getNodeUser() == null) {
            this.listTheso = new ArrayList();
            setUserRoleOnThisTheso();
            return;
        }
        
        // nouvelle méthode de gestion des droitds
        currentUser.initUserPermissions();
        
        
        
        authorizedTheso = new ArrayList<>();
        if (currentUser.getNodeUser().isSuperAdmin()) {
            boolean withPrivateTheso = true;
            ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
            authorizedTheso = thesaurusHelper.getAllIdOfThesaurus(connect.getPoolConnexion(), withPrivateTheso);
            authorizedThesoAsAdmin = thesaurusHelper.getAllIdOfThesaurus(connect.getPoolConnexion(), withPrivateTheso);

        } else {
            UserHelper currentUserHelper = new UserHelper();
            authorizedTheso = currentUserHelper.getThesaurusOfUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());

            // récupération de la liste des thésaurus pour les utilisateurs qui n'ont pas des droits sur un projet, mais uniquement sur des thésaurus du projet
            List<String> listThesoTemp = new UserHelper().getListThesoLimitedRoleByUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
            for (String idThesoTemp : listThesoTemp) {
                if(!authorizedTheso.contains(idThesoTemp)) {
                    authorizedTheso.add(idThesoTemp);
                }
            }

            authorizedThesoAsAdmin = currentUserHelper.getThesaurusOfUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());         
            
            // récupération de la liste des thésaurus pour les utilisateurs avec les droits admin, mais qui n'ont pas des droits sur un projet, mais uniquement sur des thésaurus du projet
            listThesoTemp = new UserHelper().getListThesoLimitedRoleByUserAsAdmin(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
            for (String idThesoTemp : listThesoTemp) {
                if(!authorizedThesoAsAdmin.contains(idThesoTemp)) {
                    authorizedThesoAsAdmin.add(idThesoTemp);
                }
            }
            
        }
        addAuthorizedThesoToHM();
        initAuthorizedThesoAsAdmin();
        // permet de définir le role de l'utilisateur sur le group
        if (authorizedTheso.isEmpty()) {
            setUserRoleGroup();
        } else {
            setUserRoleOnThisTheso();
        }
    }

    /**
     * Permet de vérifier après une connexion, si le thésaurus actuel est dans la liste des thésaurus authorisés pour modification
     * sinon, on nettoie l'interface et le thésaurus. 
     */
    public void redirectAndCleanTheso(){
        if(!authorizedTheso.contains(selectedTheso.getCurrentIdTheso())){
            selectedTheso.setCurrentIdTheso(null);
            selectedTheso.setSelectedIdTheso(null);
            selectedTheso.setCurrentLang(null);
            selectedTheso.setSelectedLang(null);
            try {
                selectedTheso.setSelectedTheso();
            } catch (Exception e) {
            }
        }    
    }
    
    // on ajoute les thésaurus où l'utilisateur a le droit admin dessus
    private void initAuthorizedThesoAsAdmin() {
        if (authorizedThesoAsAdmin == null) {
            return;
        }
        if(nodeListThesoAsAdmin == null)
            nodeListThesoAsAdmin = new ArrayList<>();
        else
            nodeListThesoAsAdmin.clear();
        HashMap<String, String> authorizedThesoAsAdminHM = new LinkedHashMap();
        
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
     //   String title;
     //   String lang = connect.getWorkLanguage().toLowerCase();

        // ajout de code qui permet de charger une liste de thésaurus avec le nom en utilisant la langue préférée dans les préférences du thésaurus.
        // pour éviter d'afficher des Id quand on a un mélange des thésaurus avec des langues sources différentes.
     
    /*    String preferredIdLangOfTheso;
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        
      */  
        /*
        for (String idTheso1 : authorizedThesoAsAdmin) {
            preferredIdLangOfTheso = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso1);
            if (preferredIdLangOfTheso == null) {
                preferredIdLangOfTheso = lang;
            }

            title = thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(), idTheso1, preferredIdLangOfTheso);
            if (title == null) {
                authorizedThesoAsAdminHM.put("" + "(" + idTheso1 + ")", idTheso1);
            } else {
                authorizedThesoAsAdminHM.put(title + " (" + idTheso1 + ")", idTheso1);
            }
            // nouvel objet pour récupérer la liste des thésaurus autorisée pour l'utilisateur en cours
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso1);
            nodeIdValue.setValue(title);
            nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), idTheso1));
            nodeListThesoAsAdmin.add(nodeIdValue);
        }*/
        
        // si c'est superAdmin, on prend tous les thésaurus
        if(currentUser.getNodeUser().isSuperAdmin()){
            for (NodeIdValue listTheso1 : currentUser.getUserPermissions().getListThesos()) {
                NodeIdValue nodeIdValue = new NodeIdValue();
                nodeIdValue.setId(listTheso1.getId());
                nodeIdValue.setValue(listTheso1.getValue());
                nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), listTheso1.getId()));
                nodeListThesoAsAdmin.add(nodeIdValue);                
            }
        } else { // sinon, on prend les thésaurus où l'utilisateur a un role Admin 
            for (NodeProjectThesoRole nodeProjectsWithThesosRole : currentUser.getUserPermissions().getNodeProjectsWithThesosRoles()) {
                for (NodeThesoRole nodeThesoRole : nodeProjectsWithThesosRole.getNodeThesoRoles()) {
                    if(nodeThesoRole.getIdRole() == 2 || nodeThesoRole.getIdRole() == 1) {
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(nodeThesoRole.getIdTheso());
                        nodeIdValue.setValue(nodeThesoRole.getThesoName());
                        nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), nodeThesoRole.getIdTheso()));
                        nodeListThesoAsAdmin.add(nodeIdValue);
                    }
                }
            }
        }
        
        
               
        this.listThesoAsAdmin = authorizedThesoAsAdminHM;
    }    
            
            
    // on ajoute les titres + id, sinon l'identifiant du thésauurus
    public void addAuthorizedThesoToHM() {
        if (authorizedTheso == null) {
            return;
        }

        nodeListTheso = new ArrayList<>();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();

        // ajout de code qui permet de charger une liste de thésaurus avec le nom en utilisant la langue préférée dans les préférences du thésaurus.
        // pour éviter d'afficher des Id quand on a un mélange des thésaurus avec des langues sources différentes.
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        
        listTheso = new ArrayList<>();
        
        for (String idTheso1 : authorizedTheso) {
            String preferredIdLangOfTheso = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso1);
            if (StringUtils.isEmpty(preferredIdLangOfTheso)) {
                preferredIdLangOfTheso = connect.getWorkLanguage().toLowerCase();
            }
            
            ThesoModel thesoModel = new ThesoModel();
            thesoModel.setId(idTheso1);

            String title = thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(), idTheso1, preferredIdLangOfTheso);
            if (StringUtils.isEmpty(title)) {
                thesoModel.setNom("(" + idTheso1 + ")");
            } else {
                thesoModel.setNom(title + " (" + idTheso1 + ")");
            }

            thesoModel.setDefaultLang(preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso1));
            
            listTheso.add(thesoModel);
            
            // nouvel objet pour récupérer la liste des thésaurus autorisée pour l'utilisateur en cours
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso1);
            nodeIdValue.setValue(title);
            nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), idTheso1));
            nodeListTheso.add(nodeIdValue);
        }
        
        selectedThesoForSearch = new ArrayList<>();
        for (ThesoModel thesoModel : listTheso) {
            selectedThesoForSearch.add(thesoModel.getId());
        }
    }
    
    public class ThesoModel implements Serializable{
        private String id;
        private String nom;
        private String defaultLang;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getDefaultLang() {
            return defaultLang;
        }

        public void setDefaultLang(String defaultLang) {
            this.defaultLang = defaultLang;
        }
        
        
    }    

    /**
     * fonction pour récupérer la liste (sous forme de hashMap ) de thesaurus
     * tous les thésaurus sauf privés #MR
     *
     */
    public void setPublicThesos() {
        currentUser.initAllTheso();
        authorizedTheso = new ThesaurusHelper().getAllIdOfThesaurus(connect.getPoolConnexion(), false);
        addAuthorizedThesoToHM();
        setUserRoleOnThisTheso();
    }

    /**
     * permet de savoir si l'utilisateur a les droits sur ce thésaurus
     *
     * @return
     */
    public boolean isIsHaveWriteToCurrentThesaurus() {
        if (currentUser.getNodeUser() == null) {
            return false;
        }
        if (selectedTheso.getCurrentIdTheso() == null) {
            return false;
        }
        if (currentUser.getNodeUser().isSuperAdmin()) {
            return true;
        }
        return authorizedTheso.contains(selectedTheso.getCurrentIdTheso());
    }

    /**
     * Permet de définir le role d'un utilisateur sur le thésaurus en cours le
     * groupe du thésaurus est trouvé automatiquement, si l'utilisateur est
     * SuperAdmin, pas besoin du groupe
     *
     * #MR
     */
    public void setUserRoleOnThisTheso() {

        isSuperAdmin = false;
        isAdminOnThisTheso = false;
        isManagerOnThisTheso = false;
        isContributorOnThisTheso = false;

        if (ObjectUtils.isEmpty(currentUser.getNodeUser())) {
            nodeUserRoleGroup = null;
            return;
        }

        if (ObjectUtils.isEmpty(selectedTheso.getCurrentIdTheso())) {
            nodeUserRoleGroup = null;
            isAdminOnThisTheso = false;
            return;
        }
        int idGroup = new UserHelper().getGroupOfThisTheso(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        if (currentUser.getNodeUser().isSuperAdmin()) {
            nodeUserRoleGroup = getUserRoleOnThisGroup(-1); // cas de superadmin, on a accès à tous les groupes
            setRole();
        } else {

            nodeUserRoleGroup = getUserRoleOnThisGroup(idGroup);
        }

        if (ObjectUtils.isNotEmpty(nodeUserRoleGroup)) {
            setRole();
        } else {
            nodeUserRoleGroup = new UserHelper().getUserRoleOnThisTheso(connect.getPoolConnexion(),
                    currentUser.getNodeUser().getIdUser(), idGroup, selectedTheso.getCurrentIdTheso());
            if(ObjectUtils.isNotEmpty(nodeUserRoleGroup)) {
                setRole();
            } else
                isAdminOnThisTheso = false;
        }
    }

    private void setRole() {

        switch(nodeUserRoleGroup.getIdRole()) {
            case -1:
                isNoRole = true;
                break;
            case 1:
                isSuperAdmin = true;
                break;
            case 2:
                isAdminOnThisTheso = true;
                break;
            case 3:
                isManagerOnThisTheso = true;
                break;
            case 4:
                isContributorOnThisTheso = true;
                break;
        }
    }

    /**
     * retourne le role de l'utilisateur sur le group séléctionné
     *
     * #MR
     *
     * @param idGroup
     * @return
     */
    private NodeUserRoleGroup getUserRoleOnThisGroup(int idGroup) {
        UserHelper userHelper = new UserHelper();
        if (currentUser.getNodeUser().isSuperAdmin()) {// l'utilisateur est superAdmin
            return userHelper.getUserRoleForSuperAdmin(
                    connect.getPoolConnexion());
        }
        if (idGroup == -1) {
            return null;
        }
        return userHelper.getUserRoleOnThisGroup(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser(), idGroup);
    }    
    
    /**
     * permet de récuperer le role de l'utilisateur sur le group applelé en cas
     * où le group n'a aucun thésaurus pour que l'utilisateur puisse créer des
     * thésaurus et gérer les utilisateur pour le group il faut être Admin
     */
    private void setUserRoleGroup() {
        UserHelper currentUserHelper = new UserHelper();
        ArrayList<NodeUserRoleGroup> nodeUserRoleGroups = currentUserHelper.getUserRoleGroup(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
        for (NodeUserRoleGroup nodeUserRoleGroup1 : nodeUserRoleGroups) {
            if (nodeUserRoleGroup1.isIsAdmin()) {
                isAdminOnThisTheso = true;
            }
        }
    }

    /**
     * permet de savoir si le thésaurus en cours n'est plus dans la liste des thésaurus autorisés
     * alors on nettoie initialise et on nettoie l'écran
     */
    public void setAndClearThesoInAuthorizedList() throws IOException{
        // vérification si le thésaurus supprimé est en cours de consultation, alors il faut nettoyer l'écran
        if(!authorizedTheso.contains(selectedTheso.getCurrentIdTheso())) {
            selectedTheso.setSelectedIdTheso(null);
            selectedTheso.setSelectedLang(null);
            selectedTheso.setSelectedTheso();
        } else
            selectedTheso.redirectToTheso();
    }
    
    
    public void showInfosOfTheso(String idTheso) {
        StatisticHelper statisticHelper = new StatisticHelper();
        int conceptsCount = statisticHelper.getNbCpt(connect.getPoolConnexion(), idTheso);
        int candidatesCount = statisticHelper.getNbCandidate(connect.getPoolConnexion(), idTheso);        
        int deprecatedsCount = statisticHelper.getNbOfDeprecatedConcepts(connect.getPoolConnexion(), idTheso);        
        
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, languageBean.getMsg("info"),
                languageBean.getMsg("candidat.total_concepts") + " = " + conceptsCount + "\n" 
                + languageBean.getMsg("candidat.titre") + " = " + candidatesCount + "\n"
                + languageBean.getMsg("search.deprecated") + " = " + deprecatedsCount);
        
        PrimeFaces.current().dialog().showMessageDynamic(message);
    }
    
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    //////// fin des nouvelles fontions ////////////////////////////////
    ////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////          
    /**
     * Fonction accessAThesaurus fonction pour afficher un Thesaurus avec tous
     * ses champs, dans la variable privée thesaurus #JM
     *
     * @param idTheso
     */
    public void accessAThesaurus(String idTheso) {
        AccessThesaurusHelper ath = new AccessThesaurusHelper();
        this.thesoInfos = ath.getAThesaurus(connect.getPoolConnexion(), idTheso, connect.getWorkLanguage());
    }

    /**
     * fonction addVisbility #JM fonction pour update ou insert d'une valeur
     * dans visibility dans la table thesaurus via la classe
     * accessThesaurusHelper
     */
    /*   public void addVisibility(){
        boolean visible=this.thesoInfos.isVisibility();
        String id=theso.getEditTheso().getId_thesaurus();
        AccessThesaurusHelper ath=new AccessThesaurusHelper();
        int ret=ath.insertVisibility(this.connect.getPoolConnexion(),visible,id);
        System.out.println("valeur retour insertVibility ="+ret);
        theso.setEditTheso(new Thesaurus());
    }*/
    public void supprVisibility(String id) {
        AccessThesaurusHelper ath = new AccessThesaurusHelper();

        int ret = ath.supprVisibility(this.connect.getPoolConnexion(), id);
        System.out.println("valeur retour supprVibility =" + ret);
    }

    /**
     * Fonction cleanSession #JM Fonction pour remettre les valeurs par défauts
     * à ce bean et que l'on rappelle bien une nouvelle fois la base de données
     * Provoque une redirection via deco.xhtml, pour être en cohérence avec
     * l'implémentation précédente (c'était la façon de se déconnecté depuis
     * l'index dans les versions précédentes)
     *
     * @throws IOException
     */
    public void cleanSession() throws IOException {
        this.thesoInfos = null;
        setPublicThesos();
        currentUser.setNodeUser(null);
        FacesContext.getCurrentInstance().getExternalContext().redirect("deco.xhtml");
    }

    /**
     * Fonction getAccessThesaurus, #JM Si il n'y pas de valeur de thesaurus
     * dans la variable accessThesaurus alors c'est qu'on doit créer un
     * thésaurus vide
     *
     * @return
     */
    public Thesaurus getAccessThesaurus() {
        if (thesoInfos == null) {
            thesoInfos = new Thesaurus();
        }
        return thesoInfos;
    }

    /**
     * fonction setAccessThesaurus #JM fonction pour attribuer une valeur à au
     * thesaurus en accès, la varaible est accessThesaurus si on passe en
     * paramètre de ce setter une valeur à null, le setter créer un nouveau
     * thesaurus
     *
     * @param thesaurus
     */
    public void setAccessThesaurus(Thesaurus thesaurus) {
        if (thesaurus == null) {
            this.thesoInfos = new Thesaurus();
        } else {
            this.thesoInfos = thesaurus;
        }
    }

    /**
     * changeVisibility #jm fonction pour passer de visibilité privée à publique
     */
    public void changeVisibility() {
        boolean changeVisibility = this.thesoInfos.isPrivateTheso();
        this.thesoInfos.setPrivateTheso(!(changeVisibility));
    }
    
    public boolean alignementVisible() {
        return currentUser.getNodeUser() != null && (isManagerOnThisTheso || isAdminOnThisTheso || currentUser.getNodeUser().isSuperAdmin());
    }

    public Connect getConnect() {
        return connect;
    }

    public void setConnect(Connect connect) {
        this.connect = connect;
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    public void setSelectedTheso(SelectedTheso selectedTheso) {
        this.selectedTheso = selectedTheso;
    }

    public List<ThesoModel> getListTheso() {
        return listTheso;
    }

    public void setListTheso(List<ThesoModel> listTheso) {
        this.listTheso = listTheso;
    }

    public Map<String, String> getListThesoAsAdmin() {
        return listThesoAsAdmin;
    }

    public void setListThesoAsAdmin(Map<String, String> listThesoAsAdmin) {
        this.listThesoAsAdmin = listThesoAsAdmin;
    }

    public ArrayList<NodeIdValue> getNodeListTheso() {
        return nodeListTheso;
    }

    public void setNodeListTheso(ArrayList<NodeIdValue> nodeListTheso) {
        this.nodeListTheso = nodeListTheso;
    }

    public ArrayList<NodeIdValue> getNodeListThesoAsAdmin() {
        return nodeListThesoAsAdmin;
    }

    public void setNodeListThesoAsAdmin(ArrayList<NodeIdValue> nodeListThesoAsAdmin) {
        this.nodeListThesoAsAdmin = nodeListThesoAsAdmin;
    }

    public ArrayList<NodeIdValue> getNodeListThesoAsAdminFiltered() {
        return nodeListThesoAsAdminFiltered;
    }

    public void setNodeListThesoAsAdminFiltered(ArrayList<NodeIdValue> nodeListThesoAsAdminFiltered) {
        this.nodeListThesoAsAdminFiltered = nodeListThesoAsAdminFiltered;
    }

    public List<String> getSelectedThesoForSearch() {
        return selectedThesoForSearch;
    }

    public void setSelectedThesoForSearch(List<String> selectedThesoForSearch) {
        this.selectedThesoForSearch = selectedThesoForSearch;
    }

    public Thesaurus getThesoInfos() {
        return thesoInfos;
    }

    public void setThesoInfos(Thesaurus thesoInfos) {
        this.thesoInfos = thesoInfos;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }

    public boolean isAdminOnThisTheso() {
        return isAdminOnThisTheso;
    }

    public void setAdminOnThisTheso(boolean adminOnThisTheso) {
        isAdminOnThisTheso = adminOnThisTheso;
    }

    public boolean isManagerOnThisTheso() {
        return isManagerOnThisTheso;
    }

    public void setManagerOnThisTheso(boolean managerOnThisTheso) {
        isManagerOnThisTheso = managerOnThisTheso;
    }

    public boolean isContributorOnThisTheso() {
        return isContributorOnThisTheso;
    }

    public void setContributorOnThisTheso(boolean contributorOnThisTheso) {
        isContributorOnThisTheso = contributorOnThisTheso;
    }

    public List<String> getAuthorizedTheso() {
        return authorizedTheso;
    }

    public void setAuthorizedTheso(List<String> authorizedTheso) {
        this.authorizedTheso = authorizedTheso;
    }

    public List<String> getAuthorizedThesoAsAdmin() {
        return authorizedThesoAsAdmin;
    }

    public void setAuthorizedThesoAsAdmin(List<String> authorizedThesoAsAdmin) {
        this.authorizedThesoAsAdmin = authorizedThesoAsAdmin;
    }

    public NodePreference getNodePreference() {
        return nodePreference;
    }

    public void setNodePreference(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
    }

    public NodeUserRoleGroup getNodeUserRoleGroup() {
        return nodeUserRoleGroup;
    }

    public void setNodeUserRoleGroup(NodeUserRoleGroup nodeUserRoleGroup) {
        this.nodeUserRoleGroup = nodeUserRoleGroup;
    }
}
