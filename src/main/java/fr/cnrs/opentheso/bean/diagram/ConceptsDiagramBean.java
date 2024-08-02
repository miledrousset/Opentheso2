package fr.cnrs.opentheso.bean.diagram;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;

import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.primefaces.PrimeFaces;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.overlay.ArrowOverlay;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


@Named("conceptsDiagramBean")
@SessionScoped
public class ConceptsDiagramBean implements Serializable {

    private final int HEIGHT_ELEMENT = 15;
    private final int WIDTH_ELEMENT = 40;
    
    private final float GAP_BETWEEN_NODES = 3.1f;
    private final float GAP_BETWEEN_LEVELS = 4;

    @Inject private Connect connect;
    @Inject private SelectedTheso selectedTheso;
    
    private String elementSelected;
    private ConceptHelper conceptHelper;
    private DefaultDiagramModel model;
    private NodeConcept nodeConceptSelected;
    private List<ElementDiagram> elements;
    private Map<TextInBox, List> elementsTreeMap;
    private DefaultTreeForTreeLayout<TextInBox> defaultTreeForTreeLayout;

    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeConceptSelected != null){
            nodeConceptSelected.clear();
            nodeConceptSelected = null;
        }
        if(elements != null){
            elements.clear();
            elements = null;
        }      
        if(elementsTreeMap != null){
            elementsTreeMap.clear();
            elementsTreeMap = null;
        }        
        elementSelected = null;
        model = null;
        defaultTreeForTreeLayout = null;
        conceptHelper = null;
    }     
    
    @PostConstruct
    public void postInit(){
    }
    
    /**
     * Initialisation du diagram des concepts
     * Appeler au moment du chargement de l'interface du diagram
     * Affiche seulement l'élément "racine"
     *
     * @param conceptId
     * @param idTheso
     * @param idLang
     */
    public void init(String conceptId, String idTheso, String idLang) {
        if(conceptHelper == null) 
            conceptHelper = new ConceptHelper();
        nodeConceptSelected = conceptHelper.getConcept(connect.getPoolConnexion(), conceptId, idTheso, idLang, -1, -1);
        elementSelected = nodeConceptSelected.getTerm().getLexical_value();
        TextInBox root = new TextInBox(nodeConceptSelected.getTerm().getLexical_value(),
                WIDTH_ELEMENT, HEIGHT_ELEMENT);
        elementsTreeMap = new HashMap<>();
        defaultTreeForTreeLayout = new DefaultTreeForTreeLayout<>(root);
        drowDiagram();
    }

    /**
     * Déssiner le diagram
     * Appeler lors de chaque modification de la structure du diagram
     */
    private void drowDiagram() {

        model = new DefaultDiagramModel();
        model.clearElements();
        model.clear();
        model.setMaxConnections(-1);
        model.setConnectionsDetachable(false);

        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#C7B097',lineWidth:1}");
        model.setDefaultConnector(connector);

        DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<>(0, 0);

        TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();

        TreeLayout<TextInBox> treeLayout = new TreeLayout<>(defaultTreeForTreeLayout,
                nodeExtentProvider, configuration);

        TextInBoxTreePane panel = new TextInBoxTreePane(treeLayout);

        elements = panel.calculePositions();

        for (int i = 0; i < elements.size(); i++) {
            Element root = new Element(elements.get(i).name, (elements.get(i).x * GAP_BETWEEN_NODES) + "px", ((elements.get(i).y - 10) * GAP_BETWEEN_LEVELS) + "px");
            root.setId(i+"");

            if (isRoot(panel, elements.get(i).name)) {
                root.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
            } else {
                root.addEndPoint(createEndPoint(EndPointAnchor.TOP));

                String idConcept = conceptHelper.getConceptIdFromPrefLabel(connect.getPoolConnexion(), elements.get(i).name,
                        selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang());

                ArrayList<NodeConceptTree> childs = conceptHelper.getListConcepts(connect.getPoolConnexion(),
                        idConcept, selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());

                if (!CollectionUtils.isEmpty(childs)) {
                    root.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
                }
            }
            model.addElement(root);
        }


        elements.forEach(element -> {
            try {
                if (!isRoot(panel, element.name)) {
                    Element elementParent = findElement(getParentElement(element.name).text);
                    Element elementDiagram = findElement(element.name);
                    model.connect(createConnection(elementParent.getEndPoints().get(
                            elementParent.getEndPoints().size() > 1 ? 1 : 0), elementDiagram.getEndPoints().get(0)));
                }
            } catch (Exception e) {

            }
        });

        Element elementToCenter = null;
        if (!StringUtils.isEmpty(elementSelected)) {
            Element elementParent = findElement(elementSelected);
            if (elementParent != null) {
                elementToCenter = elementParent;
                elementParent.setStyleClass("ui-diagram-element-selected");
            }
        }

        PrimeFaces.current().ajax().update("diagram");
        PrimeFaces.current().ajax().update("dialogDiagram");

        float posX = 0;
        float posY = 0;
        try {
            posX = Float.parseFloat(elementToCenter.getX().replace("px", ""));
            posY = Float.parseFloat(elementToCenter.getY().replace("px", ""));
        } catch (Exception ex) { }
        PrimeFaces.current().executeScript("setScrollPosition("+posX+", "+posY+");");

    }

    /**
     * Rechercher un élément dans le diagram à partir de son nom
     * @param name
     * @return
     */
    public Element findElement(String name) {
        Element elementSearch = null;

        if (!CollectionUtils.isEmpty(model.getElements())) {
            for(int i = 0; i < model.getElements().size(); ++i) {
                Element el = this.model.getElements().get(i);
                if (el.getData().equals(name)) {
                    elementSearch = el;
                    break;
                }
            }
        }
        return elementSearch;
    }

    /**
     * Créer une connection entre deux noeuds dans le diagram
     * @param from
     * @param to
     * @return
     */
    private Connection createConnection(EndPoint from, EndPoint to) {
        Connection conn = new Connection(from, to);
        conn.getOverlays().add(new ArrowOverlay(8, 8, 1, 1));
        return conn;
    }

    /**
     * Rechercher le parent d'un élément
     * @param nameChild
     * @return
     */
    private TextInBox getParentElement(String nameChild) {

        if (elementsTreeMap.isEmpty()) {
            return defaultTreeForTreeLayout.getRoot();
        }

        TextInBox parentElement = null;
        for (Map.Entry mapEntry : elementsTreeMap.entrySet()) {
            List<TextInBox> tmp = (List<TextInBox>) elementsTreeMap.get(mapEntry.getKey());
            for (TextInBox element : tmp) {
                if (element.text.equalsIgnoreCase(nameChild)) {
                    parentElement = (TextInBox) mapEntry.getKey();
                    break;
                }
            }
        }
        return parentElement;
    }

    /**
     * Vérifie si un élément est le racine
     * @param panel
     * @param elementDiagramName
     * @return
     */
    private boolean isRoot(TextInBoxTreePane panel, String elementDiagramName) {

        if (panel.getTreeLayout().getTree().getRoot() == null) {
            return false;
        }

        return panel.getTreeLayout().getTree().getRoot().text.equals(elementDiagramName);
    }

    /**
     * Vérifie si un élément contient des éléments enfants ratachés
     * @param panel
     * @param elementName
     * @return
     */
    private boolean hasChilds(TextInBoxTreePane panel, String elementName) {
        TextInBox textInBox = panel.findElementInTree(elementName);

        if (textInBox == null) return false;

        Iterable<TextInBox> iterable = panel.getTreeLayout().getTree().getChildren(textInBox);

        if (iterable instanceof Collection<?>) {
            return !((Collection<?>)iterable).isEmpty();
        } else {
            return false;
        }
    }

    /**
     * Créer le point de connexion d'un élément de diagram
     * Le point de connexion sert à relier deux éléments du diagram
     * @param anchor
     * @return
     */
    private EndPoint createEndPoint(EndPointAnchor anchor) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setRadius(4);
        endPoint.setStyle("{fillStyle:'#404a4e'}");
        endPoint.setHoverStyle("{fillStyle:'#20282b'}");

        return endPoint;
    }

    /**
     * Afficher un message dans l'interface
     * @param type
     * @param message
     */
    private void showMessage(FacesMessage.Severity type, String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(type, "", message));
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

    /**
     * Rechercher un élément de type TextInBox dans le diagram
     * @param elementName
     * @return
     */
    private TextInBox getElementFromTree(String elementName) {

        if (elementsTreeMap.isEmpty()) {
            return defaultTreeForTreeLayout.getRoot();
        }

        TextInBox parentElement = null;

        for (Map.Entry mapEntry : elementsTreeMap.entrySet()) {

            if (((TextInBox) mapEntry.getKey()).text.equalsIgnoreCase(elementName)) {
                parentElement = (TextInBox) mapEntry.getKey();
                break;
            }

            List<TextInBox> tmp = (List<TextInBox>) elementsTreeMap.get(mapEntry.getKey());
            for (TextInBox element : tmp) {
                if (element.text.equalsIgnoreCase(elementName)) {
                    parentElement = element;
                    break;
                }
            }
        }
        return parentElement;
    }

    /**
     * Evenement lors de la selection d'un élément dans le diagram
     */
    public void onElementClicked() {
        String str = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("elementId");
        elementSelected = str.substring(8);
        elementSelected = elements.get(Integer.parseInt(elementSelected)).name;

        TextInBox parentElement = getElementFromTree(elementSelected);

        if (parentElement != null) {
            
            String idConcept = conceptHelper.getConceptIdFromPrefLabel(connect.getPoolConnexion(), elementSelected,
                    selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang());
            
            nodeConceptSelected = conceptHelper.getConcept(connect.getPoolConnexion(), 
                    idConcept, selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang(), -1, -1);

            List<TextInBox> temp = defaultTreeForTreeLayout.getChildrenList(parentElement);
            if (!CollectionUtils.isEmpty(temp)) {
                drowDiagram();
                return;
            }
            
            ArrayList<NodeConceptTree> childs = conceptHelper.getListConcepts(connect.getPoolConnexion(),
                    idConcept, selectedTheso.getSelectedIdTheso(), selectedTheso.getCurrentLang(), selectedTheso.isSortByNotation());

            if (CollectionUtils.isEmpty(childs)) {
                showMessage(FacesMessage.SEVERITY_INFO, "Le concept '" + elementSelected + "' n'a pas d'enfant !");
                drowDiagram();
                return;
            }

            List<TextInBox> textInBoxes = new ArrayList<>();

            for (NodeConceptTree child : childs) {
                TextInBox childElement = new TextInBox(child.getTitle(), WIDTH_ELEMENT, HEIGHT_ELEMENT);
                textInBoxes.add(childElement);
                defaultTreeForTreeLayout.addChild(parentElement, childElement);
            }

            elementsTreeMap.put(parentElement, textInBoxes);

            drowDiagram();
        }
    }


    /**
     * Appeler par l'interface pour supprimer un élement du graphe
     */
    public void closeNoeud() {

        TextInBox elementToDelete = getElementFromTree(elementSelected);

        if (elementToDelete != null) {

            PrimeFaces.current().executeScript("initScrollPosition();");

            Map<TextInBox, List> elementsTreeMapTemp = new HashMap<>();
            DefaultTreeForTreeLayout<TextInBox> treeTemp = new DefaultTreeForTreeLayout<>(defaultTreeForTreeLayout.getRoot());


            TextInBox elementParent = defaultTreeForTreeLayout.getRoot();

            if (elementToDelete.text.equalsIgnoreCase(elementParent.text)) {
                defaultTreeForTreeLayout = treeTemp;
                elementsTreeMap = elementsTreeMapTemp;
            }

            List<TextInBox> childs = defaultTreeForTreeLayout.getChildrenList(elementParent);
            deleteConstractTree(treeTemp, elementsTreeMapTemp, childs, elementParent, elementToDelete);

            defaultTreeForTreeLayout = treeTemp;
            elementsTreeMap = elementsTreeMapTemp;

            drowDiagram();
        }
    }

    /**
     * Permet de supprimer un noeud du graphe
     * @param treeTemp
     * @param elementsTreeMapTemp
     * @param childs
     * @param elementParent
     * @param elementToDelete
     */
    private void deleteConstractTree(DefaultTreeForTreeLayout<TextInBox> treeTemp, Map<TextInBox, List> elementsTreeMapTemp,
                                     List<TextInBox> childs, TextInBox elementParent, TextInBox elementToDelete) {

        childs.forEach(child -> {
            treeTemp.addChild(elementParent, child);
            if (!elementToDelete.text.equalsIgnoreCase(child.text)) {
                List<TextInBox> childsTmp = defaultTreeForTreeLayout.getChildrenList(child);
                if (!CollectionUtils.isEmpty(childsTmp)) {
                    deleteConstractTree(treeTemp, elementsTreeMapTemp, childsTmp, child, elementToDelete);
                }
            }
        });

        elementsTreeMapTemp.put(elementParent, childs);
    }

    public DiagramModel getModel() {
        return model;
    }

    public NodeConcept getNodeConceptSelected() {
        return nodeConceptSelected;
    }

    public void setNodeConceptSelected(NodeConcept nodeConceptSelected) {
        this.nodeConceptSelected = nodeConceptSelected;
    }
}