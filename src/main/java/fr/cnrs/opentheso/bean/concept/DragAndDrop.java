package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.ValidateActionHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;
import org.primefaces.event.TreeDragDropEvent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */
@Named(value = "dragAndDrop")
@SessionScoped
public class DragAndDrop implements Serializable {

    @Inject
    private Connect connect;

    @Inject
    private ConceptView conceptBean;

    @Inject
    private SelectedTheso selectedTheso;

    @Inject
    private CurrentUser currentUser;

    @Inject
    private Tree tree;

    private boolean isCopyOn;
    private boolean isValidPaste;
    private NodeConcept nodeConceptDrag;
    private ArrayList<NodeBT> nodeBTsToCut;

    private ArrayList<NodeGroup> nodeGroupsToCut;
    private ArrayList<NodeGroup> nodeGroupsToAdd;

    private List<TreeNode> nodesToConfirme;
    private List<GroupNode> groupNodeToAdd;
    private List<GroupNode> groupNodeToCut;
    private List<BTNode> groupNodeBtToCut;

    private NodeConcept nodeConceptDrop;

    private boolean isdragAndDrop;
    private boolean isDropToRoot;
    
    private boolean isGroupToCut;

    private TreeNode dragNode;
    private TreeNode dropNode;    
    
    public DragAndDrop() {
    }

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeBTsToCut != null){
            nodeBTsToCut.clear();
            nodeBTsToCut = null;
        }
        if(nodeGroupsToCut != null){
            nodeGroupsToCut.clear();
            nodeGroupsToCut = null;
        }   
        if(nodeGroupsToAdd != null){
            nodeGroupsToAdd.clear();
            nodeGroupsToAdd = null;
        }
        if(nodeConceptDrag != null){
            nodeConceptDrag.clear();
            nodeConceptDrag = null;
        }    
        if(nodeConceptDrop != null){
            nodeConceptDrop.clear();
            nodeConceptDrop = null;
        }             
        
        dragNode = null;
        dropNode = null;        
    } 
    
    @PostConstruct
    public void init(){
    }

    public void reset() {
        dragNode = null;
        dropNode = null;
        if(nodeBTsToCut != null)
            nodeBTsToCut.clear();
        if(nodeGroupsToCut != null)
            nodeGroupsToCut.clear();
        if(nodeGroupsToAdd != null)
            nodeGroupsToAdd.clear();          
        isCopyOn = false;
        isValidPaste = false;
        
        if(nodeConceptDrag != null)
            nodeConceptDrag.clear();
        if(nodeConceptDrop != null)
            nodeConceptDrop.clear();
        nodeConceptDrop = null;
        
        isdragAndDrop = false;
        isDropToRoot = false;
        isGroupToCut = false;
    }
    
    public void validateCheck(){
        
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Copy and paste !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }

    public void setBTsToCut() {
        if (nodeConceptDrag == null) {
            return;
        }
        if (nodeBTsToCut == null) {
            nodeBTsToCut = new ArrayList<>();
        }
        for (NodeBT nodeBT : nodeConceptDrag.getNodeBT()) {
            NodeBT nodeBT1 = new NodeBT();
            nodeBT1.setIdConcept(nodeBT.getIdConcept());
            nodeBT1.setTitle(nodeBT.getTitle());
            nodeBT1.setIsSelected(true);
            nodeBTsToCut.add(nodeBT1);
        }
    }

    public List<NodeBT> setBTsToCut(NodeConcept concept) {

        List<NodeBT> Bts = new ArrayList<>();

        for (NodeBT nodeBT : concept.getNodeBT()) {
            NodeBT nodeBT1 = new NodeBT();
            nodeBT1.setIdConcept(nodeBT.getIdConcept());
            nodeBT1.setTitle(nodeBT.getTitle());
            nodeBT1.setIsSelected(true);
            Bts.add(nodeBT1);
        }
        return Bts;
    }

    public void setGroupsToCut() {
        if (nodeConceptDrag == null) {
            return;
        }
        if(nodeGroupsToCut == null)
            nodeGroupsToCut = new ArrayList<>();
        for (NodeGroup nodeGroup : nodeConceptDrag.getNodeConceptGroup()) {
            NodeGroup nodeGroup1 = new NodeGroup();
            nodeGroup1.getConceptGroup().setIdgroup(nodeGroup.getConceptGroup().getIdgroup());
            nodeGroup1.setLexicalValue(nodeGroup.getLexicalValue());
            nodeGroup1.setIsSelected(true);
            nodeGroupsToCut.add(nodeGroup1);
        }
        if(nodeGroupsToAdd == null)
            nodeGroupsToAdd = new ArrayList<>();
        if(nodeConceptDrop != null) {
            for (NodeGroup nodeGroup : nodeConceptDrop.getNodeConceptGroup()) {
                NodeGroup nodeGroup1 = new NodeGroup();
                nodeGroup1.getConceptGroup().setIdgroup(nodeGroup.getConceptGroup().getIdgroup());
                nodeGroup1.setLexicalValue(nodeGroup.getLexicalValue());
                nodeGroup1.setIsSelected(true);
                nodeGroupsToAdd.add(nodeGroup1);
            }
        }
    }

    public List<NodeGroup> setGroupsToCut(NodeConcept concept) {

        List<NodeGroup> groupsToCut = new ArrayList<>();

        for (NodeGroup nodeGroup : concept.getNodeConceptGroup()) {
            NodeGroup nodeGroup1 = new NodeGroup();
            nodeGroup1.getConceptGroup().setIdgroup(nodeGroup.getConceptGroup().getIdgroup());
            nodeGroup1.setLexicalValue(nodeGroup.getLexicalValue());
            nodeGroup1.setIsSelected(true);
            groupsToCut.add(nodeGroup1);
        }

        return groupsToCut;
    }

    public List<NodeGroup> setGroupsToAdd(NodeConcept concept) {

        List<NodeGroup> groupsToAdd = new ArrayList<>();
        for (NodeGroup nodeGroup : concept.getNodeConceptGroup()) {
            NodeGroup nodeGroup1 = new NodeGroup();
            nodeGroup1.getConceptGroup().setIdgroup(nodeGroup.getConceptGroup().getIdgroup());
            nodeGroup1.setLexicalValue(nodeGroup.getLexicalValue());
            nodeGroup1.setIsSelected(true);
            groupsToAdd.add(nodeGroup1);
        }
        return groupsToAdd;
    }    
    
    /**
     * permet de préparer le concept ou la branche pour le déplacement vers un autre endroit #MR
     *
     * @param nodeConcept
     */
    public void onStartCut(NodeConcept nodeConcept) {
        reset();
        if (nodeConcept == null) {
            return;
        }
        nodeConceptDrag = nodeConcept;
        isCopyOn = true;
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Couper "
                + nodeConceptDrag.getTerm().getLexical_value() + " (" + nodeConceptDrag.getConcept().getIdConcept() + ")");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }    
    
    /**
     * permet de coller la branche copiée précédement sous le concept en cours
     * déplacements valides: - d'un concept à un concept - de la racine à un
     * concept ou TopConcept #MR
     * Ne marche que pour Couper/coller (pas de Drag and drop)
     *
     * @param dropppedConcept
     */
    public void paste(NodeConcept dropppedConcept) {
        if(dropppedConcept != null) {
            this.nodeConceptDrop = dropppedConcept;
            if(!nodeConceptDrop.getConcept().getIdConcept().equalsIgnoreCase( ((TreeNodeData) dropNode.getData()).getNodeId() )) {
                pasteWitoutTreeControl();
                return;            
            }            
        } else
            dropNode = tree.getRoot();

        // controle de cohérence entre les noeuds sélectionnés dans l'arbre et les concepts à déplacer
        if(!nodeConceptDrag.getConcept().getIdConcept().equalsIgnoreCase( ((TreeNodeData) dragNode.getData()).getNodeId() )) {
            pasteWitoutTreeControl();
            return;
        }

       isdragAndDrop = false;        
       pasteWithTreeControl();
     }
    
    
    private void pasteWithTreeControl(){
        if(!isDropToRoot) {
            validatePaste();
            if(!isValidPaste) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
              //  rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;
            }         
        }
        /// préparer le noeud à couper
        setBTsToCut();
        
        /// préparer les collections à couper        
        setGroupsToCut();
        
        FacetHelper facetHelper = new FacetHelper();
        // cas de déplacement vers une facette 
        if(((TreeNodeData) dropNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
            // cas d'une branche, pas permis
            if(!nodeConceptDrag.getNodeNT().isEmpty()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;
            }
            String idFacet = ((TreeNodeData) dropNode.getData()).getNodeId();

            if(idFacet == null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;                
            }

            if(!facetHelper.addConceptToFacet(connect.getPoolConnexion(),
                    idFacet,
                    selectedTheso.getCurrentIdTheso(),
                    ((TreeNodeData) dragNode.getData()).getNodeId())){
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Erreur dans le déplacement !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                return;                
            }
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concept ajouté à la facette");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            // on vérifie si le noeud à déplacer est un membre d'une facette, alors on supprime cette appartenance
            if(((TreeNodeData) dragNode.getData()).getNodeType().equalsIgnoreCase("facetmember")){
                try {
                    String idFacetParent = ((TreeNodeData) dragNode.getData()).getIdFacetParent();
                    if(!facetHelper.deleteConceptFromFacet(connect.getPoolConnexion(),
                            idFacetParent,
                            ((TreeNodeData) dragNode.getData()).getNodeId(),
                            selectedTheso.getCurrentIdTheso())) {
                        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                        FacesContext.getCurrentInstance().addMessage(null, msg);
                        rollBackAfterErrorOrCancelDragDrop();
                        return; 
                    }
                } catch (Exception e) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    rollBackAfterErrorOrCancelDragDrop();
                    return;                      
                }
                
            }
            
            // cas de changement de groupe, on propose de garder / supprimer / ajouter le groupe
            if(isDroppedToAnotherGroup()) {
                // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                ///choix de l'option pour deplacer la collection ou non 
                isGroupToCut = true;
                PrimeFaces pf = PrimeFaces.current();
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("idDragAndDrop");
                 //   pf.ajax().update("dragAndDropForm");
                    
                }
                pf.executeScript("PF('dragAndDrop').show();");
                return;
            }
            
            // on controle s'il y a plusieurs branches, 
            if (nodeConceptDrag.getNodeBT().size() < 2) {
                // sinon, on applique le changement direct 
                drop();
            } else {
                // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                PrimeFaces pf = PrimeFaces.current();
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("idDragAndDrop");
               //     pf.ajax().update("dragAndDropForm");
                }
                pf.executeScript("PF('dragAndDrop').show();");
            }
        }        
    }
    
    private void pasteWitoutTreeControl(){
        FacesMessage msg;  
        setBTsToCut();        
        validatePaste();
        if(!isValidPaste) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            rollBackAfterErrorOrCancelDragDrop();
            updateMessage();
            return;
        }        
        
        
        if(isDropToRoot) {
            // cas de déplacement d'un concept à la racine   
            if(!moveFromConceptToRoot()) return;
        } else {
            if (nodeConceptDrop == null) {
                return;
            }

            if (nodeConceptDrop.getConcept().getIdConcept().equalsIgnoreCase(nodeConceptDrag.getConcept().getIdConcept())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Impossible de coller au même endroit ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            // cas de déplacement d'un concept à concept        
            if ((!nodeConceptDrag.getConcept().isTopConcept())) {
                if(!moveFromConceptToConcept()) return;
            }

            // cas de déplacement de la racine à un concept
            if ((nodeConceptDrag.getConcept().isTopConcept())) {
                if(!moveFromRootToConcept()) return;
            }
        }
        
        reloadConcept();
        reloadTree();
        
        if(isDropToRoot)
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                    nodeConceptDrag.getTerm().getLexical_value()
                            + " -> "
                            + "Root");
        else
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                nodeConceptDrag.getTerm().getLexical_value()
                        + " -> "
                        + nodeConceptDrop.getTerm().getLexical_value());
                
        FacesContext.getCurrentInstance().addMessage(null, msg);        
 //       PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
        reset();        
    }
    
    
    public void validatePaste() {
        FacesMessage msg;
        isValidPaste = false;
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> descendingConcepts = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                nodeConceptDrag.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        if((descendingConcepts != null) && (!descendingConcepts.isEmpty())) {
            if(descendingConcepts.contains(nodeConceptDrop.getConcept().getIdConcept())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Opération non autorisée, elle peut provoquer des boucles infinies !!! ");
                FacesContext.getCurrentInstance().addMessage(null, msg);            
                return;
            }
        }
        
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<String> listBT = relationsHelper.getListIdOfBT(connect.getPoolConnexion(),
                nodeConceptDrag.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        if((listBT != null) && (!listBT.isEmpty())) {
            if(listBT.contains(nodeConceptDrop.getConcept().getIdConcept())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Opération non autorisée, elle peut provoquer des boucles infinies !!! ");
                FacesContext.getCurrentInstance().addMessage(null, msg);            
                return;
            }
        }
        ValidateActionHelper validateActionHelper = new ValidateActionHelper();
        if(!validateActionHelper.isMoveConceptToConceptValid(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeConceptDrag.getConcept().getIdConcept(),
                nodeConceptDrop.getConcept().getIdConcept())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", validateActionHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            isValidPaste = false;
            return;
        }
        isValidPaste = true;
    }
    
    /**
     * Fonction pour récupérer l'évènement drag drop de l'arbre
     *
     * @param event
     */
    public void onDragDrop(TreeDragDropEvent event) {
        reset();
        dragNode = (TreeNode) event.getDragNode();
        dropNode = (TreeNode) event.getDropNode();
        
 //       TreeNode[] dragNodes = (TreeNode[]) event.getDragNodes();
        
        
        FacesMessage msg;   
        
        // à corriger pour traiter le déplacement des facettes par Drag and Drop
        if("facet".equalsIgnoreCase(dragNode.getType())){
            if("facet".equalsIgnoreCase(dropNode.getType())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " déplacement non permis !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                isValidPaste = false;
                return;
            }        
            new FacetHelper().updateFacetParent(connect.getPoolConnexion(),
                    ((TreeNodeData) dropNode.getData()).getNodeId(),//termeParentAssocie.getId(),
                    ((TreeNodeData) dragNode.getData()).getNodeId(),//facetSelected.getIdFacet(),
                    selectedTheso.getCurrentIdTheso());

            tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
            tree.expandTreeToPath2(
                    ((TreeNodeData) dropNode.getParent().getData()).getNodeId(),//facetSelected.getIdConceptParent(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getSelectedLang(),
                    ((TreeNodeData) dragNode.getData()).getNodeId() + "");
            PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("formRightTab:facetView");
                pf.ajax().update("formLeftTab:tabTree:tree");
            }
        
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Facette déplacée avec succès !!!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        

        ConceptHelper conceptHelper = new ConceptHelper();
        nodeConceptDrag = conceptHelper.getConcept(connect.getPoolConnexion(),
                ((TreeNodeData) dragNode.getData()).getNodeId(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang(), -1, -1);

        isdragAndDrop = true;
        
        /// préparer le noeud à couper
        setBTsToCut();

        
        if (dropNode.getParent() == null) {
            // déplacement à la racine
            isDropToRoot = true;
        } else {
            nodeConceptDrop = conceptHelper.getConcept(connect.getPoolConnexion(),
                    ((TreeNodeData) dropNode.getData()).getNodeId(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(), -1, -1);
            /// Vérifier si le dépalcement est valide (controle des relations interdites)
            ValidateActionHelper validateActionHelper = new ValidateActionHelper();
            if(nodeConceptDrop != null) {
                if(!validateActionHelper.isMoveConceptToConceptValid(
                        connect.getPoolConnexion(),
                        selectedTheso.getCurrentIdTheso(),
                        nodeConceptDrag.getConcept().getIdConcept(),
                        nodeConceptDrop.getConcept().getIdConcept())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");

                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", validateActionHelper.getMessage());
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    isValidPaste = false;
                    reloadTree();
                    return;    
                }
            }
        }
       
        
        /// préparer les collections à couper        
        setGroupsToCut();
        
        FacetHelper facetHelper = new FacetHelper();
        // cas de déplacement vers une facette 
        if(((TreeNodeData) dropNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
            // cas d'une branche, pas permis
            if(!nodeConceptDrag.getNodeNT().isEmpty()) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;
            }
            String idFacet = ((TreeNodeData) dropNode.getData()).getNodeId();

            if(idFacet == null) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                updateMessage();
                return;                
            }

            if(!facetHelper.addConceptToFacet(connect.getPoolConnexion(),
                    idFacet,
                    selectedTheso.getCurrentIdTheso(),
                    ((TreeNodeData) dragNode.getData()).getNodeId())){
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Erreur dans le déplacement !!!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                rollBackAfterErrorOrCancelDragDrop();
                return;                
            }
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concept ajouté à la facette");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            // on vérifie si le noeud à déplacer est un membre d'une facette, alors on supprime cette appartenance
            if(((TreeNodeData) dragNode.getData()).getNodeType().equalsIgnoreCase("facetmember")){
                try {
                    String idFacetParent = ((TreeNodeData) dragNode.getData()).getIdFacetParent();
                    if(!facetHelper.deleteConceptFromFacet(connect.getPoolConnexion(),
                            idFacetParent,
                            ((TreeNodeData) dragNode.getData()).getNodeId(),
                            selectedTheso.getCurrentIdTheso())) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                        FacesContext.getCurrentInstance().addMessage(null, msg);
                        rollBackAfterErrorOrCancelDragDrop();
                        return; 
                    }
                } catch (Exception e) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Action non permise !!!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    rollBackAfterErrorOrCancelDragDrop();
                    return;                      
                }
                
            }
            
            // cas de changement de groupe, on propose de garder / supprimer / ajouter le groupe
            if(isDroppedToAnotherGroup()) {
                // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                ///choix de l'option pour deplacer la collection ou non 
                isGroupToCut = true;
                PrimeFaces pf = PrimeFaces.current();
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("containerIndex:formLeftTab:idDragAndDrop");
              //      pf.ajax().update("containerIndex:formLeftTab:dragAndDropForm");
                }
                pf.executeScript("PF('dragAndDrop').show();");
                return;
            }
            
            // on controle s'il y a plusieurs branches, 
            if (nodeConceptDrag.getNodeBT().size() < 2) {
                // sinon, on applique le changement direct 
                drop();
            } else {
                // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                PrimeFaces pf = PrimeFaces.current();
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("idDragAndDrop");
            //        pf.ajax().update("dragAndDropForm");
                }
                pf.executeScript("PF('dragAndDrop').show();");
            }
        }
    }
    
    public void onDragDropMultiple(TreeDragDropEvent event) {
        reset();
        dragNode = (TreeNode) event.getDragNode();
        dropNode = (TreeNode) event.getDropNode();
        

        nodesToConfirme = new ArrayList<>();

        List<TreeNode> nodes = tree.getClickselectedNodes();
        groupNodeToAdd = new ArrayList<>();
        groupNodeToCut = new ArrayList<>();
        groupNodeBtToCut = new ArrayList<>();

        for (TreeNode node : tree.getClickselectedNodes()) {
            // à corriger pour traiter le déplacement des facettes par Drag and Drop
            if ("facet".equalsIgnoreCase(node.getType())) {
                if ("facet".equalsIgnoreCase(dropNode.getType())) {
                    showMessage(FacesMessage.SEVERITY_ERROR, "Déplacement non permis !");
                    isValidPaste = false;
                    return;
                }
                new FacetHelper().updateFacetParent(connect.getPoolConnexion(),
                        ((TreeNodeData) dropNode.getData()).getNodeId(),
                        ((TreeNodeData) node.getData()).getNodeId(),
                        selectedTheso.getCurrentIdTheso());

                tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
                tree.expandTreeToPath2(((TreeNodeData) dropNode.getParent().getData()).getNodeId(),
                        selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang(),
                        ((TreeNodeData) node.getData()).getNodeId());

                PrimeFaces.current().ajax().update("formRightTab:facetView");
                PrimeFaces.current().ajax().update("formLeftTab:tabTree:tree");
                showMessage(FacesMessage.SEVERITY_WARN, "Facette déplacée avec succès !!!");
                return;
            }

            ConceptHelper conceptHelper = new ConceptHelper();
            nodeConceptDrag = conceptHelper.getConcept(connect.getPoolConnexion(),
                    ((TreeNodeData) node.getData()).getNodeId(), selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(), -1, -1);

            isdragAndDrop = true;
            nodesToConfirme.add(node);

            /// préparer le noeud à couper
            setBTsToCut();
            //setBTsToCut();       
            List<NodeBT> nodeBTs = setBTsToCut(nodeConceptDrag);
            for (NodeBT nodeBT : nodeBTs) {
                groupNodeBtToCut.add(new BTNode(node, nodeBT));
            }

            if (dropNode.getParent() == null) {
                // déplacement à la racine
                isDropToRoot = true;
            } else {
                nodeConceptDrop = conceptHelper.getConcept(connect.getPoolConnexion(),
                        ((TreeNodeData) dropNode.getData()).getNodeId(), selectedTheso.getCurrentIdTheso(),
                        selectedTheso.getCurrentLang(), -1, -1);
                /// Vérifier si le dépalcement est valide (controle des relations interdites)
                ValidateActionHelper validateActionHelper = new ValidateActionHelper();
                if (nodeConceptDrop != null) {
                    if (!validateActionHelper.isMoveConceptToConceptValid(
                            connect.getPoolConnexion(),
                            selectedTheso.getCurrentIdTheso(),
                            nodeConceptDrag.getConcept().getIdConcept(),
                            nodeConceptDrop.getConcept().getIdConcept())) {
                        
                        showMessage(FacesMessage.SEVERITY_ERROR, "Relation non permise : "
                                + validateActionHelper.getMessage());
                        isValidPaste = false;
                        reloadTree();
                        return;
                    }
                }
            }

            // préparer les collections à couper  
            setGroupsToCut();
            //setGroupsToCut();
            List<NodeGroup> groupsToCut = setGroupsToCut(nodeConceptDrag);
            for (NodeGroup nodeGroup : groupsToCut) {
                groupNodeToCut.add(new GroupNode(node, nodeGroup));
            }
            List<NodeGroup> groupsToAdd = setGroupsToAdd(nodeConceptDrag);
            for (NodeGroup nodeGroup : groupsToAdd) {
                groupNodeToAdd.add(new GroupNode(node, nodeGroup));
            }

            FacetHelper facetHelper = new FacetHelper();
            // cas de déplacement vers une facette 
            if (((TreeNodeData) dropNode.getData()).getNodeType().equalsIgnoreCase("facet")) {
                // cas d'une branche, pas permis
                if (!nodeConceptDrag.getNodeNT().isEmpty()) {

                    showMessage(FacesMessage.SEVERITY_WARN, "Action non permise !!!");
                    rollBackAfterErrorOrCancelDragDrop();
                    return;
                }
                String idFacet = ((TreeNodeData) dropNode.getData()).getNodeId();

                if (idFacet == null) {

                    showMessage(FacesMessage.SEVERITY_WARN, "Action non permise !!!");
                    rollBackAfterErrorOrCancelDragDrop();
                    return;
                }

                if (!facetHelper.addConceptToFacet(connect.getPoolConnexion(),
                        idFacet,
                        selectedTheso.getCurrentIdTheso(),
                        ((TreeNodeData) node.getData()).getNodeId())) {

                    showMessage(FacesMessage.SEVERITY_WARN, "Erreur dans le déplacement !!!");
                    rollBackAfterErrorOrCancelDragDrop();
                    return;
                }

                showMessage(FacesMessage.SEVERITY_INFO, "Concept ajouté à la facette");
            } else {
                // on vérifie si le noeud à déplacer est un membre d'une facette, alors on supprime cette appartenance
                if (((TreeNodeData) node.getData()).getNodeType().equalsIgnoreCase("facetmember")) {
                    try {
                        String idFacetParent = ((TreeNodeData) node.getData()).getIdFacetParent();
                        if (!facetHelper.deleteConceptFromFacet(connect.getPoolConnexion(),
                                idFacetParent, ((TreeNodeData) node.getData()).getNodeId(),
                                selectedTheso.getCurrentIdTheso())) {

                            showMessage(FacesMessage.SEVERITY_WARN, "Action non permise !!!");
                            rollBackAfterErrorOrCancelDragDrop();
                            return;
                        }
                    } catch (Exception e) {
                        showMessage(FacesMessage.SEVERITY_WARN, "Action non permise !!!");
                        rollBackAfterErrorOrCancelDragDrop();
                        return;
                    }

                }

                // cas de changement de groupe, on propose de garder / supprimer / ajouter le groupe
                if (isDroppedToAnotherGroup()) {
                    // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                    ///choix de l'option pour deplacer la collection ou non 
                    isGroupToCut = true;
                    nodesToConfirme.add(node);
                    PrimeFaces.current().ajax().update("containerIndex:formLeftTab:idDragAndDropMultiple");
                    PrimeFaces.current().executeScript("PF('dragAndDropMultiple').show();");
                    continue;
                }

                // on controle s'il y a plusieurs branches, 
                if (nodeConceptDrag.getNodeBT().size() < 2) {
                    // sinon, on applique le changement direct 
                    drop();
                } else {
                    nodesToConfirme.add(node);
                    // si oui, on affiche une boite de dialogue pour choisir les branches à couper
                    PrimeFaces.current().ajax().update("idDragAndDropMultiple");
                    PrimeFaces.current().executeScript("PF('dragAndDropMultiple').show();");
                }
            }
        }
    }
    
    /**
     * permet de déposer la branche copiée précédement par Drag and Drop
     *
     */
    
    public void drop() {
        FacesMessage msg;  
        if(isDropToRoot) {
            // cas de déplacement d'un concept à la racine   
            if(!moveFromConceptToRoot()) return;
        } else {
            if (nodeConceptDrop == null) {
                return;
            }
            
            /// Vérifier si le dépalcement est valide (controle des relations interdites)
            ValidateActionHelper validateActionHelper = new ValidateActionHelper();
            if(!validateActionHelper.isMoveConceptToConceptValid(
                    connect.getPoolConnexion(),
                    selectedTheso.getCurrentIdTheso(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    nodeConceptDrop.getConcept().getIdConcept())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
                
                FacesContext.getCurrentInstance().addMessage(null, msg);
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", validateActionHelper.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                isValidPaste = false;
                reloadTree();
                return;    
            }            

            // cas de déplacement d'un concept à concept        
            if ((!nodeConceptDrag.getConcept().isTopConcept())) {
                if(!moveFromConceptToConcept()) return;
            }

            // cas de déplacement de la racine à un concept
            if ((nodeConceptDrag.getConcept().isTopConcept())) {
                if(!moveFromRootToConcept()) return;
            }
        }
        if(isGroupToCut) {
           addAndCutGroup();
        }
        
        reloadConcept();
        reloadTree();
        
        if(isDropToRoot)
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                    nodeConceptDrag.getTerm().getLexical_value()
                            + " -> "
                            + "Root");
        else
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ",
                nodeConceptDrag.getTerm().getLexical_value()
                        + " -> "
                        + nodeConceptDrop.getTerm().getLexical_value());
                
        FacesContext.getCurrentInstance().addMessage(null, msg);        
        PrimeFaces.current().executeScript("PF('dragAndDrop').hide();");
        reset();
    } 

    public void dropMultiple() {

        for (TreeNode node : nodesToConfirme) {

            nodeConceptDrag = nodeConceptDrop = new ConceptHelper().getConcept(connect.getPoolConnexion(),
                    ((TreeNodeData) node.getData()).getNodeId(), selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang(), -1, -1);

            if (nodeConceptDrop == null) {
                continue;
            }

            if (dropNode.getParent() == null) {
                // cas de déplacement d'un concept à la racine   
                if (!moveFromConceptToRootMultiple()) {
                    return;
                }
            } else {
                /// Vérifier si le dépalcement est valide (controle des relations interdites)
                // cas de déplacement d'un concept à concept        
                if ((!nodeConceptDrag.getConcept().isTopConcept())) {
                    if (!moveFromConceptToConcept()) {
                        continue;
                    }
                }

                // cas de déplacement de la racine à un concept
                if ((nodeConceptDrag.getConcept().isTopConcept())) {
                    if (!moveFromRootToConcept()) {
                        continue;
                    }
                }
            }

            addAndCutGroupMultiple();

            if (dropNode.getParent() == null) {
                showMessage(FacesMessage.SEVERITY_INFO, nodeConceptDrag.getTerm().getLexical_value() + " -> Root");
            } else {
                showMessage(FacesMessage.SEVERITY_INFO, nodeConceptDrag.getTerm().getLexical_value()
                        + " -> " + nodeConceptDrop.getTerm().getLexical_value());
            }
        }

        reloadConcept();
        reloadTree();
        PrimeFaces.current().executeScript("PF('dragAndDrop').hide();");
    }
    
    private boolean isDroppedToAnotherGroup(){
        if(nodeConceptDrag == null || nodeConceptDrop == null) return false;
        if(nodeConceptDrag.getNodeConceptGroup() == null || nodeConceptDrop.getNodeConceptGroup() == null) return false;
        
        //cas où les deux concepts n'ont pas de collections, donc pas de changement de collection
        if(nodeConceptDrag.getNodeConceptGroup().isEmpty() && nodeConceptDrop.getNodeConceptGroup().isEmpty())
            return false;
        
        //cas où l'un des deux concepts n'a pas de collection, alors il y a changement de collection
        if(nodeConceptDrag.getNodeConceptGroup().isEmpty() || nodeConceptDrop.getNodeConceptGroup().isEmpty())
            return true;
        
        //cas où il y a plusieurs collections, alors il peut y avoir changement de collection        
        if(nodeConceptDrag.getNodeConceptGroup().size() > 1 || nodeConceptDrop.getNodeConceptGroup().size() >1)
            return true;
        
        //cas où les deux collections sont identiques ou non 
        return !nodeConceptDrag.getNodeConceptGroup().get(0).getConceptGroup().getIdgroup().equalsIgnoreCase(
                nodeConceptDrop.getNodeConceptGroup().get(0).getConceptGroup().getIdgroup());
    }
    
    private void updateMessage(){
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }        
    }

    /**
     * permet de retourner les noms des collections/groupes 
     * @return 
     */
    public String getLabelOfGroupes() {
        if (nodeConceptDrag == null) {
            return null;
        }
        String labels = "";
        boolean first = true;
        for (NodeGroup nodeGroup : nodeConceptDrag.getNodeConceptGroup()) {
            if (!first) {
                labels = labels + ", " + nodeGroup.getLexicalValue();
            } else {
                labels = nodeGroup.getLexicalValue();
                first = false;
            }
        }
        return labels;
    }

    /**
     * deplacement entre concepts
     * @return 
     */
    private boolean moveFromConceptToConcept(){
        // cas de déplacement d'un concept à concept
        FacesMessage msg;
        ArrayList<String> oldBtToDelete = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        for (NodeBT nodeBT : nodeBTsToCut) {
            if (nodeBT.isIsSelected()) {
                // on prépare les BT sélectionné pour la suppression
                oldBtToDelete.add(nodeBT.getIdConcept());
            }
        }
        if (oldBtToDelete.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "aucun parent n'est sélectionné pour déplacement ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        if (!conceptHelper.moveBranchFromConceptToConcept(connect.getPoolConnexion(),
                nodeConceptDrag.getConcept().getIdConcept(),
                oldBtToDelete,
                nodeConceptDrop.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant la suppression des branches !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
    }
    
    
    private boolean moveFromConceptToConceptMultiple(){
        // cas de déplacement d'un concept à concept
        ArrayList<String> oldBtToDelete = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        for (BTNode nodeBT : groupNodeBtToCut) {
            if (((TreeNodeData) nodeBT.getNode().getData()).getNodeId()
                    .equals(nodeConceptDrag.getConcept().getIdConcept()) 
                    && nodeBT.nodeBT.isIsSelected()) {
                // on prépare les BT sélectionné pour la suppression
                oldBtToDelete.add(nodeBT.nodeBT.getIdConcept());
            }
        }
        if (oldBtToDelete.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_WARN, nodeConceptDrag.getTerm().getLexical_value() 
                    + " - Aucun parent n'est sélectionné pour déplacement");
            return false;
        }
        if (!conceptHelper.moveBranchFromConceptToConcept(connect.getPoolConnexion(),
                nodeConceptDrag.getConcept().getIdConcept(),
                oldBtToDelete,
                nodeConceptDrop.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser())) {

            showMessage(FacesMessage.SEVERITY_ERROR, nodeConceptDrag.getTerm().getLexical_value() 
                    + " - Erreur pendant la suppression des branches !!");
        }
        return true;
    }
    
    private boolean moveFromRootToConcept() {
        FacesMessage msg;
        ConceptHelper conceptHelper = new ConceptHelper();        
        if (!conceptHelper.moveBranchFromRootToConcept(connect.getPoolConnexion(),
                nodeConceptDrag.getConcept().getIdConcept(),
                nodeConceptDrop.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                currentUser.getNodeUser().getIdUser())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant le déplacement dans la base de données ");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return false;
        }
        return true;
    }
    
    private boolean moveFromConceptToRootMultiple(){
        FacesMessage msg;
        ConceptHelper conceptHelper = new ConceptHelper();         
        ArrayList<String> oldBtToDelete = new ArrayList<>();
        
        for (BTNode nodeBT : groupNodeBtToCut) {
            if (((TreeNodeData) nodeBT.getNode().getData()).getNodeId()
                    .equals(nodeConceptDrag.getConcept().getIdConcept())) {
                oldBtToDelete.add(nodeConceptDrag.getConcept().getIdConcept());
            }
        }
        // cas incohérent mais à corriger, c'est un concept qui est topTorm mais qui n'a pas l'info
        if (oldBtToDelete.isEmpty()) {
            if (!conceptHelper.setTopConcept(connect.getPoolConnexion(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
            return true;
        } 
        
        for (String oldIdBT : oldBtToDelete) {
            if (!conceptHelper.moveBranchFromConceptToRoot(connect.getPoolConnexion(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    oldIdBT,
                    selectedTheso.getCurrentIdTheso(),
                    currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
        }
        return true;
    }
    
    private boolean moveFromConceptToRoot(){
        FacesMessage msg;
        ConceptHelper conceptHelper = new ConceptHelper();         
        ArrayList<String> oldBtToDelete = new ArrayList<>();
        
        for (NodeBT nodeBT : nodeBTsToCut) {
            oldBtToDelete.add(nodeBT.getIdConcept());
        }
        // cas incohérent mais à corriger, c'est un concept qui est topTorm mais qui n'a pas l'info
        if (oldBtToDelete.isEmpty()) {
            if (!conceptHelper.setTopConcept(connect.getPoolConnexion(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
            return true;
        } 
        
        for (String oldIdBT : oldBtToDelete) {
            if (!conceptHelper.moveBranchFromConceptToRoot(connect.getPoolConnexion(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    oldIdBT,
                    selectedTheso.getCurrentIdTheso(),
                    currentUser.getNodeUser().getIdUser())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", " Erreur pendant le déplacement dans la base de données ");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return false;
            }
        }
        return true;
    }

    private void reloadConcept(){
        PrimeFaces pf = PrimeFaces.current();

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                nodeConceptDrag.getConcept().getIdConcept(), currentUser.getNodeUser().getIdUser());  
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                nodeConceptDrag.getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        /////////////// 
        
        // si le concept n'est pas déployé à doite, alors on ne fait rien
        if(conceptBean.getNodeConcept() != null){
            conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());
            if (pf.isAjaxRequest()) {
               // pf.ajax().update("containerIndex:formRightTab");
                pf.ajax().update("containerIndex:rightTab:conceptView");
            }
        }      
    }
    
    private void reloadTree(){
        PrimeFaces pf = PrimeFaces.current();
        String lang;
        if (conceptBean.getNodeConcept() != null) {
            lang = conceptBean.getSelectedLang();
        } else {
            lang = selectedTheso.getCurrentLang();
        }

        tree.initAndExpandTreeToPath(nodeConceptDrag.getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                lang);
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");        
        }
        pf.executeScript("srollToSelected();");
    }
    
    private void addAndCutGroupMultiple() {
        GroupHelper groupHelper = new GroupHelper();
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> allId = conceptHelper.getIdsOfBranch(
                    connect.getPoolConnexion(),
                    nodeConceptDrop.getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso());
        if( (allId == null) || (allId.isEmpty())) return;         
        
        for (GroupNode nodeGroup : groupNodeToCut) {
            if (((TreeNodeData) nodeGroup.getNode().getData()).getNodeId()
                    .equals(nodeConceptDrag.getConcept().getIdConcept())
                    && nodeGroup.getNodeGroup().isIsSelected()) {
                for (String idConcept : allId) {
                    groupHelper.deleteRelationConceptGroupConcept(
                        connect.getPoolConnexion(),
                        nodeGroup.getNodeGroup().getConceptGroup().getIdgroup(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso(),
                        currentUser.getNodeUser().getIdUser());      
                }
            }
            if (((TreeNodeData) nodeGroup.getNode().getData()).getNodeId()
                    .equals(nodeConceptDrag.getConcept().getIdConcept())
                    && !nodeGroup.getNodeGroup().isIsSelected()) {
                for (String idConcept : allId) {
                    groupHelper.addConceptGroupConcept(
                        connect.getPoolConnexion(),
                        nodeGroup.getNodeGroup().getConceptGroup().getIdgroup(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso());      
                }
            }            
        }
        for (GroupNode nodeGroup : groupNodeToAdd) {
            if (((TreeNodeData) nodeGroup.getNode().getData()).getNodeId()
                    .equals(nodeConceptDrag.getConcept().getIdConcept())
                    && !nodeGroup.getNodeGroup().isIsSelected()) {
                for (String idConcept : allId) {
                    groupHelper.deleteRelationConceptGroupConcept(
                        connect.getPoolConnexion(),
                        nodeGroup.getNodeGroup().getConceptGroup().getIdgroup(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso(),
                        currentUser.getNodeUser().getIdUser());      
                }
            }
            if (((TreeNodeData) nodeGroup.getNode().getData()).getNodeId()
                    .equals(nodeConceptDrag.getConcept().getIdConcept())
                    && nodeGroup.getNodeGroup().isIsSelected()) {
                for (String idConcept : allId) {
                    groupHelper.addConceptGroupConcept(
                        connect.getPoolConnexion(),
                        nodeGroup.getNodeGroup().getConceptGroup().getIdgroup(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso());      
                }
            }            
        }
    }
    
    private void addAndCutGroup() {
        GroupHelper groupHelper = new GroupHelper();
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> allId = conceptHelper.getIdsOfBranch(
                    connect.getPoolConnexion(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso());
        if( (allId == null) || (allId.isEmpty())) return;         
        
        for (NodeGroup nodeGroup : nodeGroupsToCut) {
            if(nodeGroup.isIsSelected()) {
                for (String idConcept : allId) {
                    groupHelper.deleteRelationConceptGroupConcept(
                        connect.getPoolConnexion(),
                        nodeGroup.getConceptGroup().getIdgroup(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso(),
                        currentUser.getNodeUser().getIdUser());      
                }
            }
            if(!nodeGroup.isIsSelected()) {
                for (String idConcept : allId) {
                    groupHelper.addConceptGroupConcept(
                        connect.getPoolConnexion(),
                        nodeGroup.getConceptGroup().getIdgroup(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso());      
                }
            }            
        }
        for (NodeGroup nodeGroup : nodeGroupsToAdd) {
            if(!nodeGroup.isIsSelected()) {
                for (String idConcept : allId) {
                    groupHelper.deleteRelationConceptGroupConcept(
                        connect.getPoolConnexion(),
                        nodeGroup.getConceptGroup().getIdgroup(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso(),
                        currentUser.getNodeUser().getIdUser());      
                }
            }
            if(nodeGroup.isIsSelected()) {
                for (String idConcept : allId) {
                    groupHelper.addConceptGroupConcept(
                        connect.getPoolConnexion(),
                        nodeGroup.getConceptGroup().getIdgroup(),
                        idConcept,
                        selectedTheso.getCurrentIdTheso());      
                }
            }            
        }
    }
  
    public void rollBackAfterErrorOrCancelDragDrop() {
       // if (isdragAndDrop) {
            reloadTree();
            reset();
       // }
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", "Déplacement annulé ");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('dragAndDrop').hide();");
    }

    public boolean isIsCopyOn() {
        return isCopyOn;
    }

    public void setIsCopyOn(boolean isCopyOn) {
        this.isCopyOn = isCopyOn;
    }

    public NodeConcept getCuttedConcept() {
        return nodeConceptDrag;
    }

    public void setCuttedConcept(NodeConcept cuttedConcept) {
        this.nodeConceptDrag = cuttedConcept;
    }

    public ArrayList<NodeBT> getNodeBTsToCut() {
        return nodeBTsToCut;
    }

    public void setNodeBTsToCut(ArrayList<NodeBT> nodeBTsToCut) {
        this.nodeBTsToCut = nodeBTsToCut;
    }


    public NodeConcept getDropppedConcept() {
        return nodeConceptDrop;
    }

    public void setDropppedConcept(NodeConcept dropppedConcept) {
        this.nodeConceptDrop = dropppedConcept;
    }

    public boolean isIsdragAndDrop() {
        return isdragAndDrop;
    }

    public void setIsdragAndDrop(boolean isdragAndDrop) {
        this.isdragAndDrop = isdragAndDrop;
    }

    public NodeConcept getNodeConceptDrag() {
        return nodeConceptDrag;
    }

    public void setNodeConceptDrag(NodeConcept nodeConceptDrag) {
        this.nodeConceptDrag = nodeConceptDrag;
    }

    public NodeConcept getNodeConceptDrop() {
        return nodeConceptDrop;
    }

    public void setNodeConceptDrop(NodeConcept nodeConceptDrop) {
        this.nodeConceptDrop = nodeConceptDrop;
    }

    public boolean isIsDropToRoot() {
        return isDropToRoot;
    }

    public void setIsDropToRoot(boolean isDropToRoot) {
        this.isDropToRoot = isDropToRoot;
    }

    public boolean isIsValidPaste() {
        return isValidPaste;
    }

    public void setIsValidPaste(boolean isValidPaste) {
        this.isValidPaste = isValidPaste;
    }

    public ArrayList<NodeGroup> getNodeGroupsToCut() {
        return nodeGroupsToCut;
    }

    public void setNodeGroupsToCut(ArrayList<NodeGroup> nodeGroupsToCut) {
        this.nodeGroupsToCut = nodeGroupsToCut;
    }

    public ArrayList<NodeGroup> getNodeGroupsToAdd() {
        return nodeGroupsToAdd;
    }

    public void setNodeGroupsToAdd(ArrayList<NodeGroup> nodeGroupsToAdd) {
        this.nodeGroupsToAdd = nodeGroupsToAdd;
    }

    public TreeNode getDragNode() {
        return dragNode;
    }

    public void setDragNode(TreeNode dragNode) {
        this.dragNode = dragNode;
    }

    public TreeNode getDropNode() {
        return dropNode;
    }

    public void setDropNode(TreeNode dropNode) {
        this.dropNode = dropNode;
    }
    
    public List<TreeNode> getNodesToConfirme() {
        return nodesToConfirme;
    }

    public void setNodesToConfirme(List<TreeNode> nodesToConfirme) {
        this.nodesToConfirme = nodesToConfirme;
    }
    
    public List<GroupNode> getGroupNodeToAdd() {
        return groupNodeToAdd;
    }

    public void setGroupNodeToAdd(List<GroupNode> groupNodeToAdd) {
        this.groupNodeToAdd = groupNodeToAdd;
    }

    public List<GroupNode> getGroupNodeToCut() {
        return groupNodeToCut;
    }

    public void setGroupNodeToCut(List<GroupNode> groupNodeToCut) {
        this.groupNodeToCut = groupNodeToCut;
    }

    public List<BTNode> getGroupNodeBtToCut() {
        return groupNodeBtToCut;
    }

    public void setGroupNodeBtToCut(List<BTNode> groupNodeBtToCut) {
        this.groupNodeBtToCut = groupNodeBtToCut;
    }

    public class GroupNode {

        private TreeNode node;
        private NodeGroup nodeGroup;

        public GroupNode(TreeNode node, NodeGroup nodeGroup) {
            this.node = node;
            this.nodeGroup = nodeGroup;
        }

        public TreeNode getNode() {
            return node;
        }

        public void setNode(TreeNode node) {
            this.node = node;
        }

        public NodeGroup getNodeGroup() {
            return nodeGroup;
        }

        public void setNodeGroup(NodeGroup nodeGroup) {
            this.nodeGroup = nodeGroup;
        }
    }

    public class BTNode {

        private TreeNode node;
        private NodeBT nodeBT;

        public BTNode(TreeNode node, NodeBT nodeBT) {
            this.node = node;
            this.nodeBT = nodeBT;
        }

        public TreeNode getNode() {
            return node;
        }

        public void setNode(TreeNode node) {
            this.node = node;
        }

        public NodeBT getNodeBT() {
            return nodeBT;
        }

        public void setNodeBT(NodeBT nodeBT) {
            this.nodeBT = nodeBT;
        }
    }
}
