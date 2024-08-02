package fr.cnrs.opentheso.bean.leftbody.viewtree;

import com.zaxxer.hikari.HikariDataSource;

import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bean.alignment.AlignementElement;
import fr.cnrs.opentheso.bean.facet.EditFacet;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.DataService;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.dao.DaoResourceHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTree;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bean.alignment.AlignmentBean;
import fr.cnrs.opentheso.bean.alignment.AlignmentManualBean;
import fr.cnrs.opentheso.bean.index.IndexSetting;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */
@Named(value = "tree")
@SessionScoped
public class Tree implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private RightBodySetting rightBodySetting;

    @Inject
    private LeftBodySetting leftBodySetting;

    @Inject
    private ConceptView conceptBean;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private RoleOnThesoBean roleOnThesoBean;

    @Inject
    private IndexSetting indexSetting;

    @Inject
    private EditFacet editFacet;

    @Inject
    private AlignmentBean alignmentBean;

    @Inject
    private PropositionBean propositionBean;

    @Inject
    private AlignmentManualBean alignmentManualBean;

    private DataService dataService;
    private TreeNode selectedNode;
    private List<TreeNode> clickselectedNodes;
    private TreeNode root;
    private String idTheso, idConceptParent, idLang, idConceptSelected;
    private TreeNodeData treeNodeDataSelect;
    private ArrayList<TreeNode> selectedNodes; // enregistre les noeuds séléctionnés apres une recherche

    private boolean manySiblings = false;

    @PreDestroy
    public void destroy() {
        reset();
    }

    public void reset() {
        if (selectedNodes != null) {
            selectedNodes.clear();
            selectedNodes = null;
        }
        root = null;
        selectedNode = null;
        rightBodySetting.init();
        dataService = null;
        treeNodeDataSelect = null;
        idTheso = null;
        idConceptParent = null;
        idLang = null;
        manySiblings = false;
    }

    public void initialise(String idTheso, String idLang) {
        manySiblings = false;
        this.idTheso = idTheso;
        this.idLang = idLang;
        selectedTheso.setSelectedLang(idLang);
        selectedTheso.setCurrentLang(idLang);

        dataService = new DataService();
        root = dataService.createRoot();

        addFirstNodes();

        selectedNodes = new ArrayList<>();
        leftBodySetting.setIndex("0");
    }

    public boolean isDragAndDrop(NodeUser nodeUser) {
        if (nodeUser == null) {
            return false;
        }
        if (roleOnThesoBean == null) {
            return false;
        }
        if (roleOnThesoBean.isSuperAdmin() || roleOnThesoBean.isAdminOnThisTheso() || roleOnThesoBean.isManagerOnThisTheso()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean addFirstNodes() {
        ConceptHelper conceptHelper = new ConceptHelper();
        TreeNodeData data;
        FacetHelper facetHelper = new FacetHelper();
        // la liste est triée par alphabétique ou notation
        ArrayList<NodeConceptTree> nodeConceptTrees
                = conceptHelper.getListOfTopConcepts(connect.getPoolConnexion(),
                        idTheso, idLang, selectedTheso.isSortByNotation());

        if (nodeConceptTrees.size() >= 2000) {
            manySiblings = true;
        }

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            data = new TreeNodeData(
                    nodeConceptTree.getIdConcept(),
                    nodeConceptTree.getTitle(),
                    nodeConceptTree.getNotation(),
                    false,//isgroup
                    false,//isSubGroup
                    false,//isConcept
                    true,//isTopConcept
                    "topTerm"
            );
            if (conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso,
                    nodeConceptTree.getIdConcept())) {
                if (nodeConceptTree.getStatusConcept().equalsIgnoreCase("dep")) {
                    dataService.addNodeWithChild("deprecated", data, root);
                } else {
                    dataService.addNodeWithChild("concept", data, root);
                }
                continue;
            }

            if (facetHelper.isConceptHaveFacet(connect.getPoolConnexion(), nodeConceptTree.getIdConcept(), idTheso)) {
                if (nodeConceptTree.getStatusConcept().equalsIgnoreCase("dep")) {
                    dataService.addNodeWithChild("deprecated", data, root);
                } else {
                    dataService.addNodeWithChild("concept", data, root);
                }
                continue;
            }

            if (nodeConceptTree.getStatusConcept().equalsIgnoreCase("dep")) {
                dataService.addNodeWithoutChild("deprecated", data, root);
            } else {
                dataService.addNodeWithoutChild("file", data, root);
            }
        }

        return true;
    }

    public TreeNode getRoot() {
        return root;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public List<TreeNode> getClickselectedNodes() {
        return clickselectedNodes;
    }

    public void setClickselectedNodes(List<TreeNode> clickselectedNodes) {
        this.clickselectedNodes = clickselectedNodes;
    }

    public void onNodeExpand(NodeExpandEvent event) {
        DefaultTreeNode node = (DefaultTreeNode) event.getTreeNode();
        onNodeExpand_(node);
    }

    private void onNodeExpand_(DefaultTreeNode node) {
        leftBodySetting.setIndex("0");
        manySiblings = false;

        if (node.getChildCount() == 1 && ((TreeNode) node.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            node.getChildren().remove(0);
            idConceptParent = ((TreeNodeData) node.getData()).getNodeId();
    //        FacetHelper facetHelper = new FacetHelper();
            if ("facet".equals(node.getType())) {
                addMembersOfFacet(node);
            } else {
                // if (facetHelper.isConceptHaveFacet(connect.getPoolConnexion(), idConceptParent, idTheso)) {
                //     addConceptsChildWithFacets(node);
                // } else {
                addConceptsChild(node);
                // }
            }
        }
    }

    public SelectedTheso getSelectedTheso() {
        return selectedTheso;
    }

    /// l'évennement ne focntionne pas avec tree dynamic="true"
    public void onNodeCollapse(NodeCollapseEvent event) {
        leftBodySetting.setIndex("0");
        event.getTreeNode().setExpanded(false);
    }

    private void addMembersOfFacet(TreeNode parent) {
        ArrayList<NodeIdValue> nodeIdValues = new FacetHelper().getAllMembersOfFacetSorted(connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                selectedTheso.getCurrentLang(),
                idTheso);
        ConceptHelper conceptHelper = new ConceptHelper();
        for (NodeIdValue nodeIdValue : nodeIdValues) {
            TreeNodeData data = new TreeNodeData(
                    nodeIdValue.getId(),
                    nodeIdValue.getValue().isEmpty() ? "(" + nodeIdValue.getId() + ")" : nodeIdValue.getValue(),
                    null,
                    false,
                    false,
                    true,
                    false,
                    "facetMember");
            data.setIdFacetParent(((TreeNodeData) parent.getData()).getNodeId());
            boolean haveConceptChild = conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso,
                    nodeIdValue.getId());
            if (haveConceptChild) {
                if (conceptHelper.isDeprecated(connect.getPoolConnexion(), nodeIdValue.getId(), idTheso)) {
                    dataService.addNodeWithChild("deprecated", data, parent);
                } else {
                    dataService.addNodeWithChild("concept", data, parent);
                }
            } else {
                if (conceptHelper.isDeprecated(connect.getPoolConnexion(), nodeIdValue.getId(), idTheso)) {
                    dataService.addNodeWithoutChild("deprecated", data, parent);
                } else {
                    dataService.addNodeWithoutChild("file", data, parent);
                }
            }
        }
    }

    /**
     * Nouvelle méthode en utilisant Plpgsql beaucoup plus rapide
     * @param parent
     * @return 
     * #MR
     */
    private boolean addConceptsChild(TreeNode parent) {
        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();

        List<NodeConceptTree> nodeConceptTrees = daoResourceHelper.getConceptsNTForTree(
                connect.getPoolConnexion(),
                idTheso,
                ((TreeNodeData) parent.getData()).getNodeId(),
                selectedTheso.getCurrentLang(),
                selectedTheso.isSortByNotation());

        if (nodeConceptTrees.size() >= 2000) {
            manySiblings = true;
        }

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            if (nodeConceptTree.getIdConcept() == null) {
                continue;
            }
            if (nodeConceptTree.isFacet()) {
                TreeNodeData data = new TreeNodeData(
                        nodeConceptTree.getIdConcept() + "",
                        StringUtils.isEmpty(nodeConceptTree.getTitle()) ? "(" + nodeConceptTree.getIdConcept() + ")" : nodeConceptTree.getTitle(),
                        null,
                        false,
                        false,
                        true,
                        false,
                        "facet"
                );
                if (nodeConceptTree.isHaveChildren()) {
                    dataService.addNodeWithChild("facet", data, parent);
                } else {
                    dataService.addNodeWithoutChild("facet", data, parent);
                }
            }
            if (nodeConceptTree.isTerm()) {
                TreeNodeData data = new TreeNodeData(
                        nodeConceptTree.getIdConcept(),
                        StringUtils.isEmpty(nodeConceptTree.getTitle())? "(" + nodeConceptTree.getIdConcept() + ")" : nodeConceptTree.getTitle(),
                        nodeConceptTree.getNotation(),
                        false,//isgroup
                        false,//isSubGroup
                        true,//isConcept
                        false,//isTopConcept
                        "term"
                );
                if (nodeConceptTree.isHaveChildren()) {
                    if (nodeConceptTree.getStatusConcept().equalsIgnoreCase("dep")) {
                        dataService.addNodeWithChild("deprecated", data, parent);
                    } else {
                        dataService.addNodeWithChild("concept", data, parent);
                    }
                } else {
                    if (nodeConceptTree.getStatusConcept().equalsIgnoreCase("dep")) {
                        dataService.addNodeWithoutChild("deprecated", data, parent);
                    } else {
                        dataService.addNodeWithoutChild("file", data, parent);
                    }
                }
            }
        }
        return true;
    }

    private boolean addConceptsChildWithFacets(TreeNode parent) {
        ConceptHelper conceptHelper = new ConceptHelper();
        addFacettes(parent);

        ArrayList<NodeConceptTree> nodeConceptTrees = conceptHelper.getListConceptsIgnoreConceptsInFacets(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                selectedTheso.getCurrentLang(),
                selectedTheso.isSortByNotation());

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            if (nodeConceptTree.getIdConcept() == null) {
                continue;
            }

            TreeNodeData data = new TreeNodeData(
                    nodeConceptTree.getIdConcept(),
                    nodeConceptTree.getTitle().isEmpty() ? "(" + nodeConceptTree.getIdConcept() + ")" : nodeConceptTree.getTitle(),
                    nodeConceptTree.getNotation(),
                    false,//isgroup
                    false,//isSubGroup
                    true,//isConcept
                    false,//isTopConcept
                    "term"
            );

            boolean haveConceptChild = conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso,
                    nodeConceptTree.getIdConcept());
            if (haveConceptChild) {
                if (nodeConceptTree.getStatusConcept().equalsIgnoreCase("dep")) {
                    dataService.addNodeWithChild("deprecated", data, parent);
                } else {
                    dataService.addNodeWithChild("concept", data, parent);
                }
            } else {
                if (nodeConceptTree.getStatusConcept().equalsIgnoreCase("dep")) {
                    dataService.addNodeWithoutChild("deprecated", data, parent);
                } else {
                    dataService.addNodeWithoutChild("file", data, parent);
                }
            }
        }

        return true;
    }

    public List<NodeTree> searchAllConceptChilds(HikariDataSource connexion, String conceptParentId, String idTheso, String idLang) {
        ConceptHelper conceptHelper = new ConceptHelper();

        List<NodeTree> enfants = new ArrayList<>();

        ArrayList<NodeConceptTree> nodeConceptTrees = conceptHelper.getListConceptsIgnoreConceptsInFacets(
                connexion,
                conceptParentId,
                idTheso,
                idLang,
                true);

        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            if (nodeConceptTree.getIdConcept() == null) {
                continue;
            }

            NodeTree nodeTree = new NodeTree();
            nodeTree.setIdConcept(nodeConceptTree.getIdConcept());
            nodeTree.setIdParent(conceptParentId);
            nodeTree.setPreferredTerm(nodeConceptTree.getTitle().isEmpty() ? "(" + nodeConceptTree.getIdConcept() + ")" : nodeConceptTree.getTitle());
            enfants.add(nodeTree);
        }

        enfants.addAll(searchFacettesForTree(connexion, conceptParentId, idTheso, idLang));

        return enfants;
    }

    public List<NodeTree> searchFacettesForTree(HikariDataSource connexion, String conceptParentId, String idTheso, String idLang) {
        List<NodeTree> facaets = new ArrayList<>();
        List<NodeIdValue> nodeIdValues = new FacetHelper().getAllIdValueFacetsOfConcept(
                connexion,
                conceptParentId,
                idTheso,
                idLang);
        nodeIdValues.stream().forEach(facette -> {
            TreeNodeData data = new TreeNodeData(
                    facette.getId() + "",
                    facette.getValue().isEmpty() ? "(" + facette.getId() + ")" : facette.getValue(),
                    null,
                    false,
                    false,
                    true,
                    false,
                    "facet"
            );

            NodeTree nodeTree = new NodeTree();
            nodeTree.setIdConcept(data.getNodeId());
            nodeTree.setIdParent(conceptParentId);
            nodeTree.setPreferredTerm(data.getName());
            facaets.add(nodeTree);
        });
        return facaets;
    }

    /////// pour l'ajout d'un fils supplementaire après un ajout de concept
    public void addNewChild(TreeNode parent, String idConcept, String idTheso, String idLang, String notation) {
        ConceptHelper conceptHelper = new ConceptHelper();
        TreeNodeData data;
        String label = conceptHelper.getLexicalValueOfConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang);
        if (label == null || label.isEmpty()) {
            label = "(" + idConcept + ")";
        }
        data = new TreeNodeData(
                idConcept,
                label,
                notation,
                false,//isgroup
                false,//isSubGroup
                true,//isConcept
                false,//isTopConcept
                "term"
        );
        if (conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso, idConcept)) {
            dataService.addNodeWithChild("concept", data, parent);
        } else {
            dataService.addNodeWithoutChild("file", data, parent);
        }

    }

    public void addNewFacet(TreeNode parent, String facetName, String idFacet) {
        if (parent.getChildCount() == 1 && ((TreeNode)parent.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            return;
        }        
        TreeNodeData data = new TreeNodeData(
                idFacet,
                facetName,
                "",
                false,//isgroup
                false,//isSubGroup
                true,//isConcept
                false,//isTopConcept
                "facet");

        dataService.addNodeWithoutChild("facet", data, parent);
    }

    private void addFacettes(TreeNode parent) {
        FacetHelper facetHelper = new FacetHelper();
        List<NodeIdValue> nodeIdValues = facetHelper.getAllIdValueFacetsOfConcept(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                selectedTheso.getCurrentLang());
        nodeIdValues.stream().forEach(facette -> {
            TreeNodeData data = new TreeNodeData(
                    facette.getId() + "",
                    facette.getValue().isEmpty() ? "(" + facette.getId() + ")" : facette.getValue(),
                    null,
                    false,
                    false,
                    true,
                    false,
                    "facet"
            );

            if (facetHelper.isFacetHaveMembers(connect.getPoolConnexion(), facette.getId(), idTheso)) {
                dataService.addNodeWithChild("facet", data, parent);
            } else {
                dataService.addNodeWithoutChild("facet", data, parent);
            }
        });
    }

    public void selectThisFacet(String idFacet) {
        /// chargement de l'arbre jusqu'au concept Parent de la Facette
        FacetHelper facetHelper = new FacetHelper();
        String idConceptParentOfFacet = facetHelper.getIdConceptParentOfFacet(connect.getPoolConnexion(), idFacet, idTheso);
        expandTreeToPath(idConceptParentOfFacet, idTheso, idLang);
        onNodeExpand_((DefaultTreeNode) selectedNode);

        // rechercher la facette dans les fils et la sélectionner
        expandToFacet(idFacet);

        indexSetting.setIsFacetSelected(true);
        editFacet.initEditFacet(idFacet, idTheso, idLang);
        rightBodySetting.setIndex("0");
    }

    private void expandToFacet(String idFacet) {
        selectedNode.setExpanded(true);
        List<TreeNode> treeNodes = selectedNode.getChildren();
        for (TreeNode treeNode : treeNodes) {
            if (((TreeNodeData) treeNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
                try {
                    if (((TreeNodeData) treeNode.getData()).getNodeId().equalsIgnoreCase(idFacet)) {
                        selectedNode.setSelected(false);
                        selectedNode = treeNode;
                        selectedNode.setSelected(true);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * récupération du noeud sélectionné dans l'arbre et appliquer les actions
     *
     * @param event
     */
    public void onNodeSelect(NodeSelectEvent event) {
        DefaultTreeNode node = (DefaultTreeNode) event.getTreeNode();
        onNodeSelectByNode(node);
    }

    /**
     * appliquer les actions sur un noeud fourni par l'utilisateur
     *
     * @param node
     */
    public void onNodeSelectByNode(DefaultTreeNode node) {

//        alignmentManualBean.reset();
        propositionBean.setRubriqueVisible(false);

        treeNodeDataSelect = (TreeNodeData) selectedNode.getData();

        if (!"facet".equals(node.getType())) {
            indexSetting.setIsFacetSelected(false);
            idConceptParent = ((TreeNodeData) selectedNode.getData()).getNodeId();

            rightBodySetting.setShowConceptToOn();
            conceptBean.getConceptForTree(idTheso,
                    ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);

            idConceptSelected = ((TreeNodeData) selectedNode.getData()).getNodeId();
            if (rightBodySetting.getIndex().equalsIgnoreCase("2")) {
                indexSetting.setIsValueSelected(true);

                alignmentBean.initAlignementByStep(selectedTheso.getCurrentIdTheso(),
                        conceptBean.getNodeConcept().getConcept().getIdConcept(),
                        conceptBean.getSelectedLang());
                alignmentBean.getIdsAndValues2(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());

            /*    for (AlignementElement element : alignmentBean.getAllignementsList()) {
                    element.setValide(isURLAvailable(element.getTargetUri()));
                }*/

                alignmentBean.setAllAlignementFound(new ArrayList<>());
                alignmentBean.setAllAlignementVisible(true);
                alignmentBean.setPropositionAlignementVisible(false);
                alignmentBean.setManageAlignmentVisible(false);
                alignmentBean.setComparaisonVisible(false);
            } else {
                rightBodySetting.setIndex("0");
            }
        } else {
            indexSetting.setIsFacetSelected(true);
            editFacet.initEditFacet(((TreeNodeData) node.getData()).getNodeId(), idTheso, idLang);
        }

        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        PrimeFaces.current().ajax().update("indexTitle");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab:tabTree:graph");
    }

    public boolean isURLAvailable(String urlString) {
        if (StringUtils.isNotEmpty(urlString)) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("HEAD");
                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    public void onTabConceptChange(TabChangeEvent event) {
        if (event.getTab().getId().equals("viewTabAlignement")) {
            rightBodySetting.setIndex("2");
            indexSetting.setIsValueSelected(true);

            alignmentBean.setAllAlignementVisible(true);
            alignmentBean.setPropositionAlignementVisible(false);
            alignmentBean.setManageAlignmentVisible(false);
            alignmentBean.setComparaisonVisible(false);

            alignmentBean.initAlignementByStep(selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());

            alignmentBean.getIdsAndValues2(conceptBean.getSelectedLang(), selectedTheso.getCurrentIdTheso());
        }
        if (event.getTab().getId().equals("viewTabGroup")) {
            rightBodySetting.setIndex("1");
            indexSetting.setIsValueSelected(true);
        }
        if (event.getTab().getId().equals("viewTabConcept")) {
            conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());

            rightBodySetting.setIndex("0");
            indexSetting.setIsValueSelected(true);
        }
    }

    public void onTabTreeChange(TabChangeEvent event) {
        if (event.getTab().getId().equals("viewTabTree")) {
            leftBodySetting.setIndex("0");
        }
        if (event.getTab().getId().equals("viewTabList")) {
            leftBodySetting.setIndex("1");
        }
        if (event.getTab().getId().equals("viewTabGroups")) {
            leftBodySetting.setIndex("2");
        }
        if (event.getTab().getId().equals("viewTabConceptTree")) {
            leftBodySetting.setIndex("3");
        }
    }

    public String getIdConceptSelected() {
        return idConceptSelected;
    }

    public void setIdConceptSelected(String idConceptSelected) {
        this.idConceptSelected = idConceptSelected;
    }

    public boolean isGrapheLinkVisible() {
        return !StringUtils.isEmpty(idConceptSelected);
    }

    /**
     * permet de déplier l'arbre suivant le Path ou les paths en paramètre
     *
     * @param idConcept
     * @param idTheso
     * @param idLang #MR
     */
    public void expandTreeToPath(String idConcept, String idTheso, String idLang) {
        PathHelper pathHelper = new PathHelper();
        List<String> graphPaths = pathHelper.getGraphOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);
        List<List<String>> paths = pathHelper.getPathFromGraph(graphPaths);  

        if (root == null) {
            initialise(idTheso, idLang);
        }
        if (paths == null || paths.isEmpty()) {
            return;
        }
        
        // deselectionner et fermer toutes les noeds de l'arbres
//        initialiserEtatNoeuds(root);
        // cas de changement de langue pendant la navigation dans les concepts
        // il faut reconstruire l'arbre dès le début
        if (idLang != null && !idLang.equalsIgnoreCase(this.idLang)) {
            initialise(idTheso, idLang);
        }

        if (!paths.isEmpty()) {
            // pour déselectionner les noeuds avant de séléctionner le neoud trouvé
            selectedNodes.forEach((selectedNode1) -> {
                selectedNode1.setSelected(false);
            });
            if (selectedNode != null) {
                selectedNode.setSelected(false);
            }
            selectedNodes.clear();
        }

        TreeNode treeNodeParent = root;
        treeNodeParent.setExpanded(true);
        for (List<String> path : paths) {
            for (String idC : path) {
                treeNodeParent = selectChildNode(treeNodeParent, idC);
                if (treeNodeParent == null) {
                    // erreur de cohérence
                    return;
                }
                // compare le dernier élément au concept en cours, si oui, on expand pas, sinon, erreur ...
                if (!((TreeNodeData) treeNodeParent.getData()).getNodeId().equalsIgnoreCase(path.get(path.size() - 1))) {
                    treeNodeParent.setExpanded(true);
                }                
            }
            treeNodeParent.setSelected(true);
            selectedNodes.add(treeNodeParent);
            selectedNode = treeNodeParent;
            treeNodeParent = root;            
        }
        PrimeFaces.current().executeScript("srollToSelected();");
    }

    public void expandTreeToPath2(String idConcept, String idTheso, String idLang, String idFacette) {

        List<Path> paths = new PathHelper().getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);
        
        paths.get(0).getPath().add(idFacette);

        if (root == null) {
            initialise(idTheso, idLang);
        }

        if (idLang != null && !idLang.equalsIgnoreCase(this.idLang)) {
            initialise(idTheso, idLang);
        }

        if (!paths.isEmpty()) {
            // pour déselectionner les noeuds avant de séléctionner le neoud trouvé
            selectedNodes.forEach((selectedNode1) -> {
                selectedNode1.setSelected(false);
            });
            if (selectedNode != null) {
                selectedNode.setSelected(false);
            }

            selectedNodes.clear();
        }

        TreeNode treeNodeParent = root;
        treeNodeParent.setExpanded(true);
        for (Path thisPath : paths) {
            for (String idC : thisPath.getPath()) {
                treeNodeParent = selectChildNode(treeNodeParent, idC);
                if (treeNodeParent == null) {
                    // erreur de cohérence
                    return;
                }
                // compare le dernier élément au concept en cours, si oui, on expand pas, sinon, erreur ...
                if (!((TreeNodeData) treeNodeParent.getData()).getNodeId().equalsIgnoreCase(thisPath.getPath().get(thisPath.getPath().size() - 1))) {
                    treeNodeParent.setExpanded(true);
                }
            }
            treeNodeParent.setSelected(true);
            selectedNodes.add(treeNodeParent);
            selectedNode = treeNodeParent;
            treeNodeParent = root;
        }
        leftBodySetting.setIndex("0");
        PrimeFaces.current().executeScript("srollToSelected();");
    }

    public boolean isGraphNotVisible() {
        if (selectedNode == null || selectedNode.isLeaf()) {
            return true;
        }
        return false;
    }

    public String getColor() {
        if (isGraphNotVisible()) {
            return "#d1d3e2";
        } else {
            return "#F47B2A";
        }
    }

    /**
     * permet de déplier l'arbre suivant le Path ou les paths en paramètre On
     * reconstruit l'arbre dès le début suite à des modifications
     *
     * @param idConcept
     * @param idTheso
     * @param idLang #MR
     */
    public void initAndExpandTreeToPath(
            String idConcept,
            String idTheso,
            String idLang) {

        List<Path> paths = new PathHelper().getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);

        initialise(idTheso, idLang);

        if (!paths.isEmpty()) {
            // pour déselectionner les noeuds avant de séléctionner le neoud trouvé
            selectedNodes.forEach((selectedNode1) -> {
                selectedNode1.setSelected(false);
            });
            if (selectedNode != null) {
                selectedNode.setSelected(false);
            }
            /*
            if (CollectionUtils.isNotEmpty(clickselectedNodes)) {
                for (TreeNode treeNode : clickselectedNodes) {
                    treeNode.setSelected(false);
                }
            }*/
            selectedNodes.clear();
        }

        TreeNode treeNodeParent = root;
        treeNodeParent.setExpanded(true);
        for (Path thisPath : paths) {
            for (String idC : thisPath.getPath()) {
                treeNodeParent = selectChildNode(treeNodeParent, idC);
                if (treeNodeParent == null) {
                    // erreur de cohérence
                    return;
                }
                // compare le dernier élément au concept en cours, si oui, on expand pas, sinon, erreur ...
                if (!((TreeNodeData) treeNodeParent.getData()).getNodeId()
                        .equalsIgnoreCase(thisPath.getPath().get(thisPath.getPath().size() - 1))) {
                    treeNodeParent.setExpanded(true);
                }
            }
            treeNodeParent.setSelected(true);
            selectedNodes.add(treeNodeParent);
            selectedNode = treeNodeParent;
            treeNodeParent = root;
        }
        leftBodySetting.setIndex("0");
    }

    /**
     * permet de controler si la branche est chargée, on se positionne sur le
     * concept, sinon, on récupère les fils et on se positionne sur le concept
     * Cas d'un noeud Facette : on zappe le neoud puisque le concept est sous
     * cette facette, ensuite, on se positionne sur le concept
     *
     * @param treeNodeParent
     * @param idConceptChildToFind
     * @return
     */
    private TreeNode selectChildNode(TreeNode treeNodeParent, String idConceptChildToFind) {
        // test si les fils ne sont pas construits
        FacetHelper facetHelper = new FacetHelper();
        if (treeNodeParent.getChildCount() == 1 && ((TreeNode) treeNodeParent.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            treeNodeParent.getChildren().remove(0);

            if ("facet".equals(treeNodeParent.getType())) {
                addMembersOfFacet(treeNodeParent);
            } else {
              //  if (facetHelper.isConceptHaveFacet(connect.getPoolConnexion(),
              //          ((TreeNodeData) treeNodeParent.getData()).getNodeId(), idTheso)) {
              //      addConceptsChildWithFacets(treeNodeParent);
              //  } else {
                    addConceptsChild(treeNodeParent);
              //  }
            }
        }
        List<TreeNode> treeNodes = treeNodeParent.getChildren();

        for (TreeNode treeNode : treeNodes) {
            //// permet de sauter le noeud de type Facette et passer au concept membre
            if (((TreeNodeData) treeNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
                try {
                    //String idFacet = ((TreeNodeData) treeNode.getData()).getNodeId();
                    if (facetHelper.isFacetHaveThisMember(connect.getPoolConnexion(),
                            ((TreeNodeData) treeNode.getData()).getNodeId(),
                            idConceptChildToFind, idTheso)) {
                        if (treeNode.getChildCount() == 1 && ((TreeNode) treeNode.getChildren().get(0)).getData().toString().equals("DUMMY")) {
                            treeNode.getChildren().remove(0);
                            addMembersOfFacet(treeNode);
                        }
                        return selectChildNodeFromFacet(treeNode, idConceptChildToFind);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            //// fin 
            if (((TreeNodeData) treeNode.getData()).getNodeId().equalsIgnoreCase(idConceptChildToFind)) {
                return treeNode;
            }
        }
        // pas de noeud trouvé dans les fils
        return null;
    }

    private TreeNode selectChildNodeFromFacet(TreeNode treeNodeFacet, String idConceptChildToFind) {
        List<TreeNode> treeNodes = treeNodeFacet.getChildren();

        for (TreeNode treeNode : treeNodes) {
            if (((TreeNodeData) treeNode.getData()).getNodeId().equalsIgnoreCase(idConceptChildToFind)) {
                treeNodeFacet.setExpanded(true);
                return treeNode;
            }
        }
        // pas de noeud trouvé dans les fils
        return null;
    }

    public DataService getDataService() {
        return dataService;
    }

    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }

    public String getIdConcept() {
        return idConceptParent;
    }

    public void setIdConcept(String idConcept) {
        this.idConceptParent = idConcept;
    }

    public TreeNodeData getTreeNodeDataSelect() {
        return treeNodeDataSelect;
    }

    public void setTreeNodeDataSelect(TreeNodeData treeNodeDataSelect) {
        this.treeNodeDataSelect = treeNodeDataSelect;
    }

    public boolean isManySiblings() {
        return manySiblings;
    }

    public void setManySiblings(boolean manySiblings) {
        this.manySiblings = manySiblings;
    }

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
////// pour tester la mémoire occupée par l'arbre ////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
    /**
     * permet de déplier tout l'arbre
     *
     * #MR
     */
    public void expandAllNode() {
        dataService = null;
        dataService = new DataService();
        root = dataService.createRoot();
        addFirstNodes();
        List<TreeNode> treeNodes = root.getChildren();

        for (TreeNode treeNode : treeNodes) {
            expandedAllRecursively(treeNode, true);
        }
    }

    private void expandedAllRecursively(TreeNode node, boolean expanded) {
        if (node.getChildCount() == 1 && ((TreeNode) node.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            node.getChildren().remove(0);
            addConceptsChild(node);
        }
    }

//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
////// pour tester la mémoire occupée par l'arbre ////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////      
}
