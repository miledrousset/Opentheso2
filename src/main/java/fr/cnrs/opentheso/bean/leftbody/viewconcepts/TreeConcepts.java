package fr.cnrs.opentheso.bean.leftbody.viewconcepts;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.DataService;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.proposition.PropositionBean;
import fr.cnrs.opentheso.bean.rightbody.RightBodySetting;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;

import org.apache.commons.collections.CollectionUtils;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;

/**
 *
 * @author miledrousset
 */
@Named(value = "treeConcepts")
@SessionScoped
public class TreeConcepts implements Serializable {
    @Inject private Connect connect;
    @Inject private RightBodySetting rightBodySetting;
    @Inject private ConceptView conceptView;
    @Inject private GroupView groupView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private PropositionBean propositionBean;

    private DataService dataService;
    private TreeNode root, selectedNode;
    private String idTheso, idLang;

    @PreDestroy
    public void destroy(){
        reset();
    }
    
    public void reset() {
        root = null;
        selectedNode = null;
        rightBodySetting.init();
        dataService = null;
        idTheso = null;
        idLang = null;
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
        List<NodeGroup> racineNode = new GroupHelper().getListRootConceptGroup(connect.getPoolConnexion(), idTheso, idLang, selectedTheso.isSortByNotation());

        for (NodeGroup nodeGroup : racineNode) {
            TreeNodeData data = new TreeNodeData(nodeGroup.getConceptGroup().getIdgroup(), nodeGroup.getLexicalValue(),
                    nodeGroup.getConceptGroup().getNotation(),true,false,false,false,"group");

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
        if (parent.getChildCount() == 1 && ((TreeNode)parent.getChildren().get(0)).getData().toString().equals("DUMMY")) {
            parent.getChildren().remove(0);
            addGroupsChild(parent);
            addConceptsChild(parent);
        }
        addConceptSpecifique(parent);
    }

    private boolean addGroupsChild(TreeNode parent) {
        GroupHelper groupHelper = new GroupHelper();
        ArrayList<NodeGroup> listeSubGroup = groupHelper.getListChildsOfGroup(connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(), idTheso, idLang, selectedTheso.isSortByNotation());

        if (listeSubGroup == null) {
            parent.setType("group");
            return true;
        }

        for (NodeGroup nodeGroup : listeSubGroup) {
            TreeNodeData data = new TreeNodeData(nodeGroup.getConceptGroup().getIdgroup(), nodeGroup.getLexicalValue(),
                    nodeGroup.getConceptGroup().getNotation(),false,true,false,false,"subGroup");

            if (nodeGroup.isIsHaveChildren()) {
                dataService.addNodeWithChild("subGroup", data, parent);
            } else {
                dataService.addNodeWithoutChild("subGroup", data, parent);
            }
        }
        return true;
    }

    private boolean addConceptsChild(TreeNode parent) {

        // déséctivé par Miled 
/*        ArrayList<NodeIdValue> listeConceptsOfGroup = new ConceptHelper().getListConceptsOfGroup(connect.getPoolConnexion(),
                idTheso, idLang, ((TreeNodeData) parent.getData()).getNodeId(), false);*/
        
        // il faut ici récupérer les Topterms de la collection #MR        
        ArrayList<NodeIdValue> listeConceptsOfGroup = new ConceptHelper().getListTopConceptsOfGroup(
                connect.getPoolConnexion(),
                idTheso, idLang, ((TreeNodeData) parent.getData()).getNodeId(), selectedTheso.isSortByNotation());
        
        if (listeConceptsOfGroup == null || listeConceptsOfGroup.isEmpty()) {
            parent.setType("group");
            return true;
        }

        for (NodeIdValue nodeGroup : listeConceptsOfGroup) {

            TreeNodeData data = new TreeNodeData(nodeGroup.getId(), nodeGroup.getValue(), "", false, false,
                    true, false, "concept");

            ArrayList<NodeNT> childs = new RelationsHelper().getListNT(connect.getPoolConnexion(), nodeGroup.getId(), idTheso, idLang, -1, -1);
            if (CollectionUtils.isEmpty(childs)) {
                new DefaultTreeNode("file", data, parent);
            } else {
                TreeNode document = new DefaultTreeNode(data, parent);
                new DefaultTreeNode("DUMMY", document);
            }

        }
        return true;
    }

    private boolean addConceptSpecifique(TreeNode parent) {

        ArrayList<NodeNT> ConceptsId = new RelationsHelper().getListNT(connect.getPoolConnexion(),
                ((TreeNodeData) parent.getData()).getNodeId(), idTheso, idLang, -1, -1);

        if (ConceptsId == null || ConceptsId.isEmpty()) {
            parent.setType("file");
            return true;
        }

        for (NodeNT nodeNT : ConceptsId) {
            TreeNodeData data = new TreeNodeData(nodeNT.getIdConcept(), nodeNT.getTitle(),"", false,
                    false, true, true,"concept" );
            
            ArrayList<NodeNT> childs = new RelationsHelper().getListNT(connect.getPoolConnexion(), nodeNT.getIdConcept(),
                    idTheso, idLang, -1, -1);

            if (CollectionUtils.isEmpty(childs)) {
                new DefaultTreeNode("file", data, parent);
            } else {
                TreeNode document = new DefaultTreeNode(data, parent);
                new DefaultTreeNode("DUMMY", document);
            }
        }
        return true;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        
        propositionBean.setRubriqueVisible(false);
        propositionBean.setNewProposition(false);
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

}
