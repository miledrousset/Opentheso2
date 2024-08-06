package fr.cnrs.opentheso.bean.graph;

import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.UriBuilder;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.EagerResult;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.QueryConfig;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named
@SessionScoped
public class DataGraphView implements Serializable {

    @Inject
    private Connect connect;
    private List<GraphObject> graphObjects;

    private GraphObject selectedGraph;

    @Inject
    private GraphService graphService;

    private int selectedViewId;
    private String newViewName;
    private String newViewDescription;
    private String newViewDataToAdd;
    private List<ImmutablePair<String, String>> newViewExportedData;
    
    private String selectedIdTheso;
    private NodeSearchMini searchSelected;    

    private Properties getPrefOfNeo4j(){
        ResourceBundle resourceBundle = getBundlePool();
        if(resourceBundle == null){
            return null;
        }
        Properties props = new Properties();
        props.setProperty("neo4j.serverName", resourceBundle.getString("neo4j.serverName"));
        props.setProperty("neo4j.serverPort", resourceBundle.getString("neo4j.serverPort"));
        props.setProperty("neo4j.user", resourceBundle.getString("neo4j.user"));
        props.setProperty("neo4j.password", resourceBundle.getString("neo4j.password"));
        props.setProperty("neo4j.databaseName", resourceBundle.getString("neo4j.databaseName"));        
        
        return props;
        
    }
    private ResourceBundle getBundlePool(){
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            ResourceBundle bundlePool = context.getApplication().getResourceBundle(context, "conHikari");
            return bundlePool;
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return null;
    }    
    
    
    /**
     * permet de retourner la liste des concepts possibles pour ajouter une
     * relation NT (en ignorant les relations interdites) on ignore les concepts
     * de type TT on ignore les concepts de type RT
     *
     * @param value
     * @return
     */
    public List<NodeSearchMini> getAutoComplete(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        String idLang = preferencesHelper.getWorkLanguageOfTheso(connect.getPoolConnexion(), selectedIdTheso);
        
        if (selectedIdTheso != null && idLang != null) {
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.getPoolConnexion(),
                    value,
                    idLang,
                    selectedIdTheso, true);
        }
        return liste;
    }  

    public String getSelectedIdTheso() {
        return selectedIdTheso;
    }

    public void setSelectedIdTheso(String selectedIdTheso) {
        this.selectedIdTheso = selectedIdTheso;
    }

    public NodeSearchMini getSearchSelected() {
        return searchSelected;
    }

    public void setSearchSelected(NodeSearchMini searchSelected) {
        this.searchSelected = searchSelected;
    }


    public void actionAjax(){
    }
    
    
    
    
    public int getSelectedViewId() {
        return selectedViewId;
    }

    public void setSelectedViewId(int selectedViewId) {
        this.selectedViewId = selectedViewId;
    }

    public String getNewViewDataToAdd() {
        return newViewDataToAdd;
    }

    public void setNewViewDataToAdd(String newViewDataToAdd) {
        this.newViewDataToAdd = newViewDataToAdd;
    }

    public String getNewViewName() {
        return newViewName;
    }

    public void setNewViewName(String newViewName) {
        this.newViewName = newViewName;
    }

    public String getNewViewDescription() {
        return newViewDescription;
    }

    public void setNewViewDescription(String newViewDescription) {
        this.newViewDescription = newViewDescription;
    }

    public List<ImmutablePair<String, String>> getNewViewExportedData() {
        return newViewExportedData;
    }

    public void setNewViewExportedData(List<ImmutablePair<String, String>> newViewExportedData) {
        this.newViewExportedData = newViewExportedData;
    }

    public void init() {
        graphObjects = new ArrayList<>(graphService.getViews().values());
        selectedIdTheso = null;
        searchSelected = null;
    }

    public void initNewViewDialog() {
        selectedViewId = -1;
        newViewName = null;
        newViewDescription = null;
        newViewDataToAdd = null;
        newViewExportedData = new ArrayList<>();
    }

    public void initEditViewDialog(String id) {
        GraphObject viewToEdit = graphService.getView(id);
        if (viewToEdit == null) {
            return;
        }
        selectedViewId = viewToEdit.getId();
        newViewName = viewToEdit.getName();
        newViewDescription = viewToEdit.getDescription();
        newViewDataToAdd = null;
        newViewExportedData = viewToEdit.getExportedData();
    }

    public List<GraphObject> getGraphObjects() {
        return graphObjects;
    }

    public void setService(GraphService graphService) {
        this.graphService = graphService;
    }

    public GraphObject getSelectedGraphObject() {
        return selectedGraph;
    }

    public void setSelectedProduct(GraphObject selectedGraph) {
        this.selectedGraph = selectedGraph;
    }

    public void clearMultiViewState() {
        FacesContext context = FacesContext.getCurrentInstance();
        String viewId = context.getViewRoot().getViewId();
        PrimeFaces.current().multiViewState().clearAll(viewId, true, this::showMessage);
    }

    public void redirectToGraphVisualization(String viewId) throws IOException {
    //    System.out.println("visualisation " + viewId);
        GraphObject view = graphService.getView(viewId);

        if (view == null) {
            return;
        }
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

        String opethesoUrl = context.getRequestScheme() + "://" + context.getRequestServerName()
                + (Objects.equals(context.getRequestServerName(), "localhost") ? ":" + context.getRequestServerPort() : "")
                + context.getApplicationContextPath();

        final String baseDataURL = opethesoUrl + "/openapi/v1/graph/getData";
        UriBuilder url = UriBuilder.fromUri(baseDataURL);
        url.queryParam("lang", "fr");
        if (!view.getExportedData().isEmpty()) {
            view.getExportedData().forEach(data -> {
                url.queryParam("idThesoConcept", data.getRight() == null ? data.getLeft() : data.getLeft() + "," + data.getRight());
            });
        }

        String urlString = url.build().toString();
        context.redirect(UriBuilder.fromUri(context.getRequestContextPath() + "/d3js/index.xhtml").queryParam("dataUrl", urlString).queryParam("format", "opentheso").build().toString());
    }

    public void exportToNeo4J(String viewId) {
        
        Properties properties = getPrefOfNeo4j();
        if(properties == null){
            showMessage(FacesMessage.SEVERITY_ERROR, "La base de données Neo4J n'est pas paramétrée, ouvrez hikari.properties et faire le changement !");
            return;
        }
        
        //System.out.println("export " + viewId);
        
        GraphObject view = graphService.getView(viewId);
        if (view == null) {
            return;
        }
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        //       String openthesoUrl = "http://localhost:8080/opentheso2";
        String openthesoUrl = context.getRequestScheme() + "://" + context.getRequestServerName()
                + (Objects.equals(context.getRequestServerName(), "localhost") ? ":" + context.getRequestServerPort() : "")
                + context.getApplicationContextPath();

        final String dbUri = "neo4j://" + properties.getProperty("neo4j.serverName") + ":" + properties.getProperty("neo4j.serverPort"); //"neo4j://localhost:7687";
        final String dbUser = properties.getProperty("neo4j.user");//"neo4j";
        final String dbPassword = properties.getProperty("neo4j.password");//"neo4j1234";
        final String dbName = properties.getProperty("neo4j.databaseName");//"neo4j"; //TODO mettre Neo4j avant de commit

        final String thesaurusImportURIWithPlaceholder = openthesoUrl + "/openapi/v1/thesaurus/%THESO_ID%";
        final String branchImportURIWithPlaceholder = openthesoUrl + "/openapi/v1/concept/%THESO_ID%/%TOP_CONCEPT_ID%/expansion?way=down";

        try (var driver = GraphDatabase.driver(dbUri, AuthTokens.basic(dbUser, dbPassword))) {
            driver.verifyConnectivity();
            //System.out.println("Connection estabilished.");

            StringBuilder builder = new StringBuilder();
            view.getExportedData().forEach((data) -> {

                String importURL;
                if (data.right == null) {
                    importURL = thesaurusImportURIWithPlaceholder.replace("%THESO_ID%", data.left);
                } else {
                    importURL = branchImportURIWithPlaceholder.replace("%THESO_ID%", data.left).replace("%TOP_CONCEPT_ID%", data.right);
                }

                builder.append("CALL n10s.rdf.import.fetch(\""); 
                builder.append(importURL);
                builder.append("\", \"RDF/XML\", {headerParams: { Accept: \"application/rdf+xml;charset=utf-8\"}});\n");
            });

           // System.out.println(builder);

            EagerResult result = driver.executableQuery("CALL apoc.cypher.runMany('" + builder.toString() + "', {}, {statistics:false,timeout:10})")
                    .withConfig(QueryConfig.builder().withDatabase(dbName).build())
                    .execute();

            List<org.neo4j.driver.Record> records = result.records();

            if (!records.isEmpty()) {
                records.forEach(System.out::println);
            }
            showMessage(FacesMessage.SEVERITY_INFO, "Export réussi vers Neo4J !");
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur de connexion à la base de données Neo4J !");
            e.printStackTrace();
        }
    }

    public void removeView(String viewId) {
        System.out.println("suppression " + viewId);
        graphService.deleteView(viewId);
        showMessage(FacesMessage.SEVERITY_INFO, "Vue supprimée avec succès");
        init();
    }

    public void addDataToNewViewList() {
        if (selectedViewId == -1) {
            return;
        }

        ImmutablePair<String, String> tuple;
        
        String idConcept;
        if(searchSelected == null || StringUtils.isEmpty(searchSelected.getIdConcept())) {
            idConcept = null;
        } else {
            idConcept = searchSelected.getIdConcept();
        }
        
        tuple = new ImmutablePair<>(selectedIdTheso, idConcept);
        
        if(graphService.isExistDatas(selectedViewId, selectedIdTheso, idConcept)){
            showMessage(FacesMessage.SEVERITY_WARN, "Cette combinaison existe déjà !");
            init();
            return;
        }
        
        graphService.addDataToView(selectedViewId, tuple);
        newViewExportedData.add(tuple);
        newViewDataToAdd = null;
        init();
    }

    public void applyView() {
        if (newViewName.isEmpty() || newViewDescription.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Une vue doit possèder un nom et une description");
            return;
        }
        if (selectedViewId == -1) {
            int newViewId = graphService.createView(new GraphObject(newViewName, newViewDescription, new ArrayList<>()));
            if(newViewId == -1){
                showMessage(FacesMessage.SEVERITY_ERROR, "La création de la vue a échoué");
                init();
                return;
            }
            selectedViewId = newViewId;
            showMessage(FacesMessage.SEVERITY_INFO, "Vue créée avec succès");
        } else {
            graphService.saveView(new GraphObject(selectedViewId, newViewName, newViewDescription, newViewExportedData));
            showMessage(FacesMessage.SEVERITY_INFO, "Vue modifiée avec succès");
        }
        init();
    }

    public void removeExportedDataRow(String left, String right) {
        Optional<ImmutablePair<String, String>> optTuple = newViewExportedData.stream().filter(data -> {
            if (data.right == null) {
                return data.left.equals(left);
            } else {
                return data.right.equals(right) && data.left.equals(left);
            }
        }).findFirst();
        optTuple.ifPresent(tuple -> {
            graphService.removeDataFromView(selectedViewId, tuple);
            newViewExportedData.remove(tuple);
        });
        init();
    }

    private void showMessage(String clientId) {
        FacesContext.getCurrentInstance()
                .addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, clientId + " multiview state has been cleared out", null));
    }

    public void showMessage(FacesMessage.Severity messageType, String messageValue) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(messageType, "", messageValue));
        PrimeFaces.current().ajax().update("messageIndex");
    }

}
