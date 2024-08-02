/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.ValidateActionHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "cutAndPaste")
@SessionScoped
//@ViewScoped
public class CutAndPaste implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;
    @Inject private Tree tree;


    /*   private String movedFromId;
    private boolean isBranch = true;
    
    // pour distinguer un group d'un concept 
    private boolean isCopyOfGroup = false;

     */
    private boolean isCopyOn;
    private boolean isValidPaste;
    private NodeConcept nodeConceptDrag;
    private ArrayList<NodeBT> nodeBTsToCut;

    private NodeConcept nodeConceptDrop;

    private boolean isDropToRoot;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        nodeConceptDrag = null;
        nodeConceptDrop = null;        
        if(nodeBTsToCut!= null){
            nodeBTsToCut.clear();
            nodeBTsToCut = null;
        }    
    }      
    
    public CutAndPaste() {
    }

    public void reset() {
//        movedFromId = null;
//        isBranch = true;
//        isCopyOfGroup = false;

        if(nodeBTsToCut != null)
            nodeBTsToCut.clear();
        isCopyOn = false;
        isValidPaste = false;
        nodeConceptDrag = null;
        nodeConceptDrop = null;

        isDropToRoot = false;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Copy and paste !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void initInfo() {

    }


    public void setBTsToCut() {
        if (nodeConceptDrag == null) {
            return;
        }
        if(nodeBTsToCut == null)
            nodeBTsToCut = new ArrayList<>();
        for (NodeBT nodeBT : nodeConceptDrag.getNodeBT()) {
            NodeBT nodeBT1 = new NodeBT();
            nodeBT1.setIdConcept(nodeBT.getIdConcept());
            nodeBT1.setTitle(nodeBT.getTitle());
            nodeBT1.setIsSelected(true);
            nodeBTsToCut.add(nodeBT1);
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
     * permet de préparer le concept ou la branche pour le déplacement vers un autre endroit #MR
     *
     * @param nodeConcept
     */
    public void onStartCut(NodeConcept nodeConcept) {
        if (nodeConcept == null) {
            return;
        }
        
        // controler les déplacements non autorisés 
        
        FacesMessage msg;
        nodeConceptDrag = nodeConcept;
        isCopyOn = true;
        setBTsToCut();
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Couper "
                + nodeConceptDrag.getTerm().getLexical_value() + " (" + nodeConceptDrag.getConcept().getIdConcept() + ")");
        FacesContext.getCurrentInstance().addMessage(null, msg);
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

        // si le concept n'est pas déployé à droite, alors on ne fait rien
        if(conceptBean.getNodeConcept() != null){
            conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                    nodeConceptDrag.getConcept().getIdConcept(),
                    conceptBean.getSelectedLang());
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formRightTab");
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
            pf.ajax().update("formLeftTab:tabTree:tree");
        }
        pf.executeScript("srollToSelected();");
    }
    
    /**
     * permet de coller la branche copiée précédement sous le concept en cours
     * déplacements valides: - d'un concept à un concept - de la racine à un
     * concept ou TopConcept #MR
     * Ne marche que pour Couper/coller (pas de Drag and drop)
     *
     */
    public void paste() {
        FacesMessage msg;  
     
        
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
        PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
        reset();
    }
  
    public void rollBackAfterErrorOrCancelDragDrop() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, " ", "Déplacement annulé ");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().executeScript("PF('cutAndPaste').hide();");
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



}
