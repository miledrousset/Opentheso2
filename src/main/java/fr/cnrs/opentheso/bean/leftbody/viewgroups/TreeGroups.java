/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.leftbody.viewgroups;

import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.DataService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.LeftBodySetting;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;

import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */
@Named(value = "treeGroups")
@SessionScoped

public class TreeGroups implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private RightBodySetting rightBodySetting;
    @Inject
    private ConceptView conceptView;
    @Inject
    private GroupView groupView;
    @Inject
    private LeftBodySetting leftBodySetting;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject
    private PropositionBean propositionBean;

    private DataService dataService;
    private TreeNode root, selectedNode;
    private String idTheso, idLang;
    private boolean sortByNotation;

    @PostConstruct
    public void postInit() {
        sortByNotation = false;
    }

    @PreDestroy
    public void destroy() {
        reset();
    }

    public void reset() {
        root = null;
        dataService = null;
        selectedNode = null;
        rightBodySetting.init();
        groupView.clear();
    }

    public void initialise(String idTheso, String idLang) {
        this.idTheso = idTheso;
        this.idLang = idLang;
        dataService = new DataService();
        root = dataService.createRoot();
        addFirstNodes();
    }

    private boolean addFirstNodes() {

        // liste des groupes de premier niveau
        List<NodeGroup> racineNode = new GroupHelper().getListRootConceptGroup(connect.getPoolConnexion(), idTheso, idLang, isSortByNotation());

        for (NodeGroup nodeGroup : racineNode) {
            TreeNodeData data = new TreeNodeData(
                    nodeGroup.getConceptGroup().getIdgroup(),
                    nodeGroup.getLexicalValue(),
                    nodeGroup.getConceptGroup().getNotation(),
                    true,//isgroup
                    false,//isSubGroup
                    false,//isConcept
                    false,//isTopConcept
                    "group"
            );
            if (nodeGroup.isIsHaveChildren()) {
                dataService.addNodeWithChild("group", data, root);
            } else {
                dataService.addNodeWithoutChild("group", data, root);
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

    public void onNodeExpand(NodeExpandEvent event) {
        DefaultTreeNode parent = (DefaultTreeNode) event.getTreeNode();
        if (parent.getChildCount() == 1 && ((TreeNode) parent.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            parent.getChildren().remove(0);
            addGroupsChild(parent);
            addConceptsChild(parent);
        }
    }

    private boolean addGroupsChild(TreeNode parent) {

        ArrayList<NodeGroup> listeSubGroup = new GroupHelper().getListChildsOfGroup(
                connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(),
                idTheso,
                idLang, isSortByNotation());
        if (listeSubGroup == null) {
            parent.setType("group");
            return true;
        }

        for (NodeGroup nodeGroup : listeSubGroup) {
            TreeNodeData data = new TreeNodeData(
                    nodeGroup.getConceptGroup().getIdgroup(),
                    nodeGroup.getLexicalValue(),
                    nodeGroup.getConceptGroup().getNotation(),
                    false,//isgroup
                    true,//isSubGroup
                    false,//isConcept
                    false,//isTopConcept                    
                    "subGroup"
            );
            if (nodeGroup.isIsHaveChildren()) {
                dataService.addNodeWithChild("subGroup", data, parent);
            } else {
                dataService.addNodeWithoutChild("subGroup", data, parent);
            }
        }
        return true;
    }

    private boolean addConceptsChild(TreeNode parent) {
        ConceptHelper conceptHelper = new ConceptHelper();
        TreeNodeData data;

        ArrayList<NodeIdValue> listeConceptsOfGroup = conceptHelper.getListConceptsOfGroup(
                connect.getPoolConnexion(),
                idTheso,
                idLang,
                ((TreeNodeData) parent.getData()).getNodeId(),
                selectedTheso.isSortByNotation());
        if (listeConceptsOfGroup == null || listeConceptsOfGroup.isEmpty()) {
            parent.setType("group");
            return true;
        }
        int i = 0;
        for (NodeIdValue nodeGroup : listeConceptsOfGroup) {
            if (i == 4000) { // pour limiter l'affichage dans l'arbre de plus de 2000 concepts à la suite
                data = new TreeNodeData(
                        "....",
                        "....",
                        "",
                        false,//isgroup
                        false,//isSubGroup
                        true,//isConcept
                        false,//isTopConcept
                        "concept"
                );
                dataService.addNodeWithoutChild("file", data, parent);
                return true;
            }
            data = new TreeNodeData(
                    nodeGroup.getId(),
                    nodeGroup.getValue(),
                    nodeGroup.getNotation(),
                    false,//isgroup
                    false,//isSubGroup
                    true,//isConcept
                    false,//isTopConcept
                    "concept"
            );
            dataService.addNodeWithoutChild("file", data, parent);
            i++;
        }
        return true;
    }

    /**
     * pour l'ajout d'un nouveau Group après le chargement de l'arbre
     *
     * @param idGroup
     * @param idTheso
     * @param idLang
     */
    public void addNewGroupToTree(String idGroup, String idTheso, String idLang) {

        NodeGroup nodeGroup = new GroupHelper().getThisConceptGroup(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        if (nodeGroup == null) {
            return;
        }

        String label;
        if (nodeGroup.getLexicalValue().isEmpty()) {
            label = "(" + idGroup + ")";
        } else {
            label = nodeGroup.getLexicalValue();
        }

        TreeNodeData data = new TreeNodeData(
                idGroup,
                label,
                nodeGroup.getConceptGroup().getNotation(),
                true,//isgroup
                false,//isSubGroup
                false,//isConcept
                false,//isTopConcept
                "group"
        );
        dataService.addNodeWithoutChild("group", data, root);
    }

    /**
     * pour l'ajout d'un nouveau Group après le chargement de l'arbre
     *
     * @param parent
     * @param idGroup
     * @param idTheso
     * @param idLang
     */
    public void addNewSubGroupToTree(TreeNode parent, String idGroup, String idTheso, String idLang) {

        NodeGroup nodeGroup = new GroupHelper().getThisConceptGroup(connect.getPoolConnexion(), idGroup, idTheso, idLang);
        if (nodeGroup == null) {
            return;
        }

        String label;
        if (nodeGroup.getLexicalValue().isEmpty()) {
            label = "(" + idGroup + ")";
        } else {
            label = nodeGroup.getLexicalValue();
        }

        TreeNodeData data = new TreeNodeData(
                idGroup,
                label,
                nodeGroup.getConceptGroup().getNotation(),
                false,//isgroup
                true,//isSubGroup
                false,//isConcept
                false,//isTopConcept
                "group"
        );
        dataService.addNodeWithoutChild("group", data, parent);
    }

    public void expandGroupToPath(String idGroup, String idTheso, String idLang) {

        ArrayList<String> path = new PathHelper().getPathOfGroup(
                connect.getPoolConnexion(), idGroup, idTheso);

        if (root == null) {
            initialise(idTheso, idLang);
        }

        // cas de changement de langue pendant la navigation dans les concepts
        // il faut reconstruire l'arbre dès le début
        if (idLang != null && !idLang.equalsIgnoreCase(this.idLang)) {
            initialise(idTheso, idLang);
        }

        if (!path.isEmpty()) {
            // pour déselectionner les noeuds avant de séléctionner le neoud trouvé
            if (selectedNode != null) {
                selectedNode.setSelected(false);
            }
        }

        TreeNode treeNodeParent = root;
        treeNodeParent.setExpanded(true);
        for (String idGroup1 : path) {
            treeNodeParent = selectChildNode(treeNodeParent, idGroup1);
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
        selectedNode = treeNodeParent;
        PrimeFaces.current().executeScript("srollGroupToSelected();");        
    }

    /**
     * permet de controler si la branche est chargée, on se positionne sur le
     * concept, sinon, on récupère les fils et on se positionne sur le concept
     * Cas d'un noeud Facette : on zappe le neoud puisque le concept est sous
     * cette facette, ensuite, on se positionne sur le concept
     *
     * @param treeNodeParent
     * @param idGroupToFind
     * @return
     */
    private TreeNode selectChildNode(TreeNode treeNodeParent, String idGroupToFind) {
        // test si les fils ne sont pas construits
        if (treeNodeParent.getChildCount() == 1 && ((TreeNode) treeNodeParent.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            treeNodeParent.getChildren().remove(0);
            addGroupsChild(treeNodeParent);
            addConceptsChild(treeNodeParent);

        }
        List<TreeNode> treeNodes = treeNodeParent.getChildren();

        for (TreeNode treeNode : treeNodes) {
            if (((TreeNodeData) treeNode.getData()).getNodeId().equalsIgnoreCase(idGroupToFind)) {
                return treeNode;
            }
        }
        // pas de noeud trouvé dans les fils
        return null;
    }

    public void selectThisGroup(String idGroup) {
        rightBodySetting.setShowGroupToOn();
        groupView.getGroup(idTheso, idGroup, idLang);
        rightBodySetting.setIndex("1");
        leftBodySetting.setIndex("2");
        if (selectedNode != null) {
            selectedNode.setSelected(false);
        }
        expandGroupToPath(idGroup, idTheso, idLang);
    }

    public void onNodeSelect(NodeSelectEvent event) {

        propositionBean.setRubriqueVisible(false);
        rightBodySetting.setIndex("0");

        if (((TreeNodeData) selectedNode.getData()).isIsConcept()) {
            rightBodySetting.setShowConceptToOn();
            conceptView.getConceptForTree(idTheso,
                    ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            rightBodySetting.setIndex("0");
        }
        if (((TreeNodeData) selectedNode.getData()).isIsGroup() || ((TreeNodeData) selectedNode.getData()).isIsSubGroup()) {
            rightBodySetting.setShowGroupToOn();
            groupView.getGroup(idTheso, ((TreeNodeData) selectedNode.getData()).getNodeId(), idLang);
            rightBodySetting.setIndex("1");
        }
    }

    public String getNodeLabel() {
        if (selectedNode == null) {
            return null;
        }
        return ((TreeNodeData) selectedNode.getData()).getName();
    }

    public String getSelectedNodeId() {
        if (selectedNode == null) {
            return null;
        }
        return ((TreeNodeData) selectedNode.getData()).getNodeId();
    }

    public boolean isIsGroupNode() {
        if (selectedNode == null) {
            return false;
        }
        return ((TreeNodeData) selectedNode.getData()).isIsGroup() || ((TreeNodeData) selectedNode.getData()).isIsSubGroup();
    }

    public boolean isSortByNotation() {
        return sortByNotation;
    }

    public void setSortByNotation(boolean sortByNotation) {
        this.sortByNotation = sortByNotation;
    }

    /**
     * permet de savoir si le groupe/collection a un sous group/collection
     *
     * @return
     */
    public boolean isIsThisGroupHaveSubGroup() {
        if (selectedNode == null) {
            return false;
        }
        GroupHelper groupHelper = new GroupHelper();
        return groupHelper.isHaveSubGroup(connect.getPoolConnexion(),
                idTheso,
                ((TreeNodeData) selectedNode.getData()).getNodeId());
    }

}
