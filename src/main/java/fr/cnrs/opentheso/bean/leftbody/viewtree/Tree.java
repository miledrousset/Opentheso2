package fr.cnrs.opentheso.bean.leftbody.viewtree;


import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.DataService;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptTree;
import fr.cnrs.opentheso.bean.diagram.ConceptsDiagramBean;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import java.io.IOException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */
@Named(value = "tree")
@SessionScoped
public class Tree implements Serializable {

    @Inject private Connect connect;
    @Inject private RightBodySetting rightBodySetting;
    @Inject private LeftBodySetting leftBodySetting;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    
    @Inject 
    private ConceptsDiagramBean conceptsDiagramBean;

    private DataService dataService;

    private TreeNode selectedNode; // le neoud sélectionné par clique
    private TreeNode root;

    private String idTheso;
    private String idLang;
    private boolean noedSelected, diagramVisisble;
    
    private TreeNodeData treeNodeDataSelect;

    ArrayList<TreeNode> selectedNodes; // enregistre les noeuds séléctionnés apres une recherche

    public void reset() {
        root = null;
        selectedNode = null;
        rightBodySetting.init();
    }

    public void initialise(String idTheso, String idLang) {
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
        if(nodeUser == null) return false;
        if(roleOnThesoBean == null) return false;
        if(roleOnThesoBean.isIsSuperAdmin() || roleOnThesoBean.isIsAdminOnThisTheso() || roleOnThesoBean.isIsManagerOnThisTheso())
            return true;
        else
            return false;
    }
    
    private boolean addFirstNodes() {
        ConceptHelper conceptHelper = new ConceptHelper();
        TreeNodeData data;

        // la liste est triée par alphabétique ou notation
        ArrayList<NodeConceptTree> nodeConceptTrees
                = conceptHelper.getListOfTopConcepts(connect.getPoolConnexion(),
                        idTheso, idLang, selectedTheso.isSortByNotation());

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
            if (nodeConceptTree.isHaveChildren()) {
                dataService.addNodeWithChild("concept", data, root);
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

    private boolean addConceptsChild(TreeNode parent) {
        ConceptHelper conceptHelper = new ConceptHelper();
        TreeNodeData data;
        String label;
/*        ArrayList<String> conceptIds = conceptHelper.getListChildrenOfConcept(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso);*/

      
        ArrayList<NodeConceptTree> nodeConceptTrees = conceptHelper.getListConcepts(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                selectedTheso.getCurrentLang(),
                selectedTheso.isSortByNotation());        
        
        for (NodeConceptTree nodeConceptTree : nodeConceptTrees) {
            if (nodeConceptTree.getIdConcept() == null) continue;
        //    label = conceptHelper.getLexicalValueOfConcept(connect.getPoolConnexion(), conceptId, idTheso, idLang);
            label = nodeConceptTree.getTitle();
            if (nodeConceptTree.getTitle().isEmpty()) {
                label = "(" + nodeConceptTree.getIdConcept() + ")";
            }
                
            data = new TreeNodeData(
                    nodeConceptTree.getIdConcept(),
                    label,
                    nodeConceptTree.getNotation(),
                    false,//isgroup
                    false,//isSubGroup
                    true,//isConcept
                    false,//isTopConcept
                    "term"
            );
            if (conceptHelper.haveChildren(connect.getPoolConnexion(), idTheso, nodeConceptTree.getIdConcept())) {
                dataService.addNodeWithChild("concept", data, parent);
            } else {
                dataService.addNodeWithoutChild("file", data, parent);
            }
        }
        return true;
    }
    
    
    /////// pour l'ajout d'un fils supplementaire après un ajout de concept 
    
    public void addNewChild(TreeNode parent, String idConcept, String idTheso, String idLang) {
            ConceptHelper conceptHelper = new ConceptHelper();
            TreeNodeData data;
            String label = conceptHelper.getLexicalValueOfConcept(connect.getPoolConnexion(), idConcept, idTheso, idLang);
            if (label == null || label.isEmpty()) {
                label = "(" + idConcept + ")";
            }
            data = new TreeNodeData(
                    idConcept,
                    label,
                    "",
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

    public boolean isNoedSelected() {
        return noedSelected;
    }

    public void setNoedSelected(boolean noedSelected) {
        this.noedSelected = noedSelected;
    }

    public void onNodeExpand(NodeExpandEvent event) {

        if (noedSelected) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "",
                    "Un noeud est en cours de chargement !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
        } else {
            noedSelected = true;
            DefaultTreeNode parent = (DefaultTreeNode) event.getTreeNode();
            if (parent.getChildCount() == 1 && parent.getChildren().get(0).getData().toString().equals("DUMMY")) {
                parent.getChildren().remove(0);
                addConceptsChild(parent);
            }
            noedSelected = false;
        }
    }

    public void onNodeSelect(NodeSelectEvent event) {
        if (noedSelected) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "",
                    "Un noeud est en cours de chargement !"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
        } else {
            noedSelected = true;
            if (((TreeNodeData) selectedNode.getData()).isIsConcept()) {
                rightBodySetting.setShowConceptToOn();
                conceptBean.getConceptForTree(idTheso,
                        ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            }
            if (((TreeNodeData) selectedNode.getData()).isIsTopConcept()) {
                rightBodySetting.setShowConceptToOn();

                conceptBean.getConceptForTree(idTheso,
                        ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            }

            rightBodySetting.setIndex("0");
            noedSelected = false;
            
            treeNodeDataSelect = (TreeNodeData) selectedNode.getData();
        }
    }

    /**
     * permet de déplier l'arbre suivant le Path ou les paths en paramètre
     *
     * @param idConcept
     * @param idTheso
     * @param idLang #MR
     */
    public void expandTreeToPath(String idConcept, String idTheso, String idLang) {

        ArrayList<Path> paths = new PathHelper().getPathOfConcept(
                connect.getPoolConnexion(), idConcept, idTheso);

        if (root == null) {
            initialise(idTheso, idLang);
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
    }

    // deselectionner et fermer toutes les noeds de l'arbres
    private void initialiserEtatNoeuds(TreeNode nodeRoot) {
        for (TreeNode node : nodeRoot.getChildren()) {
            try {
                TreeNodeData treeNodeData = (TreeNodeData) node.getData();
                node.setExpanded(false);
                node.setSelected(false);

                if (!treeNodeData.isIsConcept()) {
                    initialiserEtatNoeuds (node);
                }
            } catch (Exception ex) {

            }
        }
    }
    
    /**
     * permet de déplier l'arbre suivant le Path ou les paths en paramètre
     * On reconstruit l'arbre dès le début suite à des modifications 
     *
     * @param idConcept
     * @param idTheso
     * @param idLang #MR
     */
    public void initAndExpandTreeToPath(
            String idConcept,
            String idTheso,
            String idLang) {

        ArrayList<Path> paths = new PathHelper().getPathOfConcept(
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


    private TreeNode selectChildNode(TreeNode treeNodeParent, String idConceptChildToFind) {
        // test si les fils ne sont pas construits
        if (treeNodeParent.getChildCount() == 1 && treeNodeParent.getChildren().get(0).getData().toString().equals("DUMMY")) {
            treeNodeParent.getChildren().remove(0);
            addConceptsChild(treeNodeParent);
        }
        List<TreeNode> treeNodes = treeNodeParent.getChildren();

        for (TreeNode treeNode : treeNodes) {
            if (((TreeNodeData) treeNode.getData()).getNodeId().equalsIgnoreCase(idConceptChildToFind)) {
                return treeNode;
            }
        }
        // pas de noeud trouvé dans les fils
        return null;
    }

    public boolean isDiagramVisisble() {
        return diagramVisisble;
    }

    public void setDiagramVisisble(boolean diagramVisisble) {
        this.diagramVisisble = diagramVisisble;
    }
    
    public void showDiagram(boolean status) throws IOException {
        if (treeNodeDataSelect == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "", "Vous devez selectioner un élement de l'arbre"));
            PrimeFaces pf = PrimeFaces.current();
            pf.ajax().update("messageIndex");
            return;
        } 
        
        diagramVisisble = status;
        
        conceptsDiagramBean.init(treeNodeDataSelect.getNodeId(), idTheso, idLang);
        
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public TreeNodeData getTreeNodeDataSelect() {
        return treeNodeDataSelect;
    }

    public void setTreeNodeDataSelect(TreeNodeData treeNodeDataSelect) {
        this.treeNodeDataSelect = treeNodeDataSelect;
    }
}
