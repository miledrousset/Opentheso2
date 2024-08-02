/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.DeprecateHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeConceptType;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.core.exports.csv.CsvWriteHelper;
import fr.cnrs.opentheso.ws.handle.HandleHelper;
import fr.cnrs.opentheso.ws.handlestandard.HandleService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import net.handle.hdllib.HandleException;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

/**
 *
 * @author miledrousset
 */
@Named(value = "editConcept")
//@javax.enterprise.context.RequestScoped
//// on ne peut pas relancer plusieurs actions avec cette déclaration

@jakarta.enterprise.context.SessionScoped
public class EditConcept implements Serializable {
    @Inject private Connect connect;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private Tree tree;
    @Inject private ConceptView conceptBean;
    @Inject private CurrentUser currentUser;    
    
    private String prefLabel;
    private String notation;
    private String source;
    private boolean applyToBranch;
    private boolean reciprocalRelationship;
    
    private boolean isCreated;
    private boolean duplicate;
    private boolean forDelete;

    private boolean inProgress;
    
    private boolean isReplacedByRTrelation;
    
    private ArrayList<NodeConceptType> nodeConceptTypes;
    private String selectedConceptType;

    // dépréciation
    private ArrayList<NodeIdValue> nodeReplaceBy;
    
    private NodeSearchMini searchSelected;    
    
    private ArrayList<NodeIdValue> nodeIdValues;
    
    private NodeConceptType nodeConceptTypeToDelete;
    private NodeConceptType nodeConceptTypeToAdd;    

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        if (nodeReplaceBy != null) {
            nodeReplaceBy.clear();
            nodeReplaceBy = null;
        }
        inProgress = false;
        prefLabel = null;
        notation = null;
        source = null;
        selectedConceptType = null;
        nodeIdValues = null;
        nodeConceptTypeToDelete = null;
        nodeConceptTypeToAdd = null;
    }

    public EditConcept() {
    }

    public void reset(String label) {
        isCreated = false;
        duplicate = false;
        prefLabel = label;
        notation = "";
        forDelete = false;
        isReplacedByRTrelation = false;
        inProgress = false;
        nodeIdValues = null;
        nodeConceptTypeToDelete = null;
        nodeConceptTypeToAdd = null;

        nodeReplaceBy = conceptView.getNodeConcept().getReplacedBy();
    }
    public void initForConceptType(){
        ConceptHelper conceptHelper = new ConceptHelper();
        nodeConceptTypes = conceptHelper.getAllTypesOfConcept(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        selectedConceptType = conceptView.getNodeConcept().getConcept().getConceptType();
        applyToBranch = false;
        reciprocalRelationship = false;
        nodeConceptTypeToDelete = null;
        nodeConceptTypeToAdd = null;
    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour modifier Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de changer les informations dans la table du type de concept
     * @param nodeConceptType
     * @param idUser 
     */
    public void applyChangeForConceptType(NodeConceptType nodeConceptType, int idUser){
        if(nodeConceptType == null || StringUtils.isEmpty(nodeConceptType.getCode())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de sélecion !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        
        ConceptHelper conceptHelper = new ConceptHelper();
        if(!conceptHelper.applyChangeForConceptType(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), nodeConceptType)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant le changement !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        initForConceptType();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Changement réussi !");
        FacesContext.getCurrentInstance().addMessage(null, msg);   
    }
    
    /**
     * permet de supprimer un type de concept
     */
    public void deleteCustomRelationship(){
        if(nodeConceptTypeToDelete == null || StringUtils.isEmpty(nodeConceptTypeToDelete.getCode())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de sélecion !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;            
        }    
        ConceptHelper conceptHelper = new ConceptHelper();
        if(!conceptHelper.deleteConceptType(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), nodeConceptTypeToDelete)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant la suppression !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        initForConceptType();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Suppression réussie !");
        FacesContext.getCurrentInstance().addMessage(null, msg);         
    }
    
    /**
     * permet d'ajouter un nouveau type de concept
     */
    public void addNewConceptType(){
        if(nodeConceptTypeToAdd == null || StringUtils.isEmpty(nodeConceptTypeToAdd.getCode())){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de sélecion !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;            
        }    
        if(StringUtils.isEmpty( selectedTheso.getCurrentIdTheso())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "pas de sélecion !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;              
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        
        nodeConceptTypeToAdd.setCode(new StringPlus().unaccentLowerString(nodeConceptTypeToAdd.getCode()));
        nodeConceptTypeToAdd.setCode(nodeConceptTypeToAdd.getCode().replaceAll(" ", ""));
        
        if(conceptHelper.isConceptTypeExist(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), nodeConceptTypeToAdd)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le type de concept existe déjà !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        
        if(!conceptHelper.addNewConceptType(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), nodeConceptTypeToAdd)){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Erreur pendant l'ajout !");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
            return;
        }
        initForConceptType();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Ajout réussi !");
        FacesContext.getCurrentInstance().addMessage(null, msg);         
    }    
    
    /**
     * permet de chnager le type du concept
     * @param idUser 
     */
    public void updateTypeConcept(int idUser) {
        PrimeFaces pf = PrimeFaces.current();

        if (selectedConceptType == null || selectedConceptType.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "aucune relation n'est définie !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        ConceptHelper conceptHelper = new ConceptHelper();

        if(isApplyToBranch()) {
            ArrayList<String> allId  = conceptHelper.getIdsOfBranch(
                    connect.getPoolConnexion(),
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso());

            if( (allId == null) || (allId.isEmpty())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "aucun concept sélectionné !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }                
                return;
            }
            for (String conceptId : allId) {
                if(!conceptHelper.setConceptType(connect.getPoolConnexion(),
                        selectedTheso.getCurrentIdTheso(),
                        conceptId,
                        selectedConceptType,
                        idUser)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Une erreur s'est produite !!");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                    if (pf.isAjaxRequest()) {
                        pf.ajax().update("messageIndex");
                    }
                    return;            
                }             
            }
        } else {
            if(!conceptHelper.setConceptType(connect.getPoolConnexion(),
                    selectedTheso.getCurrentIdTheso(),
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    selectedConceptType,
                    idUser)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Une erreur s'est produite !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;            
            }            
        }

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////          
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());
        
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le changement a réussi");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }       
    }
    
    
    /**
     * permet d'ajouter un nouveau top concept si le groupe = null, on ajoute un
     * TopConcept sans groupe si l'id du concept est fourni, il faut controler
     * s'il est unique
     *
     * @param idLang
     * @param idTheso
     * @param idUser
     */
    public void updateLabel(
            String idTheso,
            String idLang,
            int idUser) {

        duplicate = false;
        PrimeFaces pf = PrimeFaces.current();

        if (prefLabel == null || prefLabel.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "Le label est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
                PrimeFaces.current().ajax().update("containerIndex:rightTab:idRenameConcept");                
            }
            return;
        }

        TermHelper termHelper = new TermHelper();

        // vérification si le term à ajouter existe déjà, s oui, on a l'Id, sinon, on a Null
        String idTerm = termHelper.isTermEqualTo(connect.getPoolConnexion(),
                prefLabel.trim(),
                idTheso,
                idLang);

        if (idTerm != null) {
            String label = termHelper.getLexicalValue(connect.getPoolConnexion(), idTerm, idTheso, idLang);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", label + " : existe déjà ! voulez-vous continuer ?");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            duplicate = true;
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
                PrimeFaces.current().ajax().update("containerIndex:renameConceptMessage");
                PrimeFaces.current().executeScript("PF('renameConcept').show();");
            }
            return;
        }
        if (termHelper.isAltLabelExist(connect.getPoolConnexion(), idTerm, idTheso, idLang)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", " un synonyme existe déjà ! voulez-vous continuer ?");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            duplicate = true;
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
                PrimeFaces.current().ajax().update("containerIndex:renameConceptMessage");
                PrimeFaces.current().executeScript("PF('renameConcept').show();");
            }
            return;
        }

        updateForced(idTheso, idLang, idUser);
    }

    public void updateForced(
            String idTheso,
            String idLang,
            int idUser) {

        PrimeFaces pf = PrimeFaces.current();
        TermHelper termHelper = new TermHelper();
        String idTerm = termHelper.getIdTermOfConcept(connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(), idTheso);
        if (idTerm == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("messageIndex");
            }
            return;
        }

        // on vérifie si la tradcution existe, on la met à jour, sinon, on en ajoute une
        if (termHelper.isTermExistInThisLang(connect.getPoolConnexion(), idTerm, idLang, idTheso)) {
            if (!termHelper.updateTraduction(connect.getPoolConnexion(),
                    prefLabel, idTerm, idLang, idTheso, idUser)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
        } else {
            if (!termHelper.addTraduction(connect.getPoolConnexion(),
                    prefLabel, idTerm, idLang, "", "", idTheso, idUser)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur!", "Erreur de cohérence de BDD !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");
                }
                return;
            }
        }

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(), idTheso,
                conceptView.getNodeConcept().getConcept().getIdConcept(), idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////
        conceptView.getConcept(idTheso, conceptView.getNodeConcept().getConcept().getIdConcept(), idLang);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été modifié");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            PrimeFaces.current().ajax().update("containerIndex::idRenameConcept");                
            PrimeFaces.current().executeScript("PF('renameConcept').hide();");
        }      
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptView.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(), idTheso, idLang);
            }
            ((TreeNodeData) tree.getSelectedNode().getData()).setName(prefLabel);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
            }
        }        
/*
        if (CollectionUtils.isNotEmpty(tree.getClickselectedNodes())) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).getNodeId().equalsIgnoreCase(
                    conceptView.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(), idTheso, idLang);
            }
            ((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).setName(prefLabel);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
            }
        }*/
        reset("");
    }

    public void cancel() {
        duplicate = false;
    }

    public void infosDelete() {
        String message;
        if (conceptView.getNodeConcept().getNodeNT().isEmpty()) { // pas d'enfants
            message = languageBean.getMsg("rightbody.conceptdialog.infoDeleteConcept");// rightbody.conceptdialog.infoDeleteConcept "La suppression du concept est définitive !!";
        } else {
            message = languageBean.getMsg("rightbody.conceptdialog.infoDeleteBranch"); //"La suppression de la branche est définitive !!";
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention !", message);
        FacesContext.getCurrentInstance().addMessage("formRightTab:viewTabConcept:deleteConceptForm:currentPrefLabelToDelete", msg);
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
        }  
        forDelete = true;
    }

    /**
     * permet de supprimer un concept
     *
     * @param idTheso
     * @param idUser
     */
    public void deleteConcept(
            String idTheso,
            int idUser) {

        ConceptHelper conceptHelper = new ConceptHelper();
        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        if (conceptView.getNodeConcept().getNodeNT().isEmpty()) {
            // suppression du concept
            if (!conceptHelper.deleteConcept(connect.getPoolConnexion(),
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    idTheso,
                    idUser)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression a échoué !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        } else {
            /// suppression d'une branche
            if(!conceptHelper.deleteBranchConcept(connect.getPoolConnexion(),
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    idTheso, idUser)){
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression a échoué, vérifier la poly-hiérarchie pour le concept : " 
                        + conceptHelper.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                
            }
        }

        // mise à jour
        PrimeFaces pf = PrimeFaces.current();
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptView.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(),
                        idTheso,
                        selectedTheso.getCurrentLang());
            }
            TreeNode parent = tree.getSelectedNode().getParent();
            if (parent != null) {
                parent.getChildren().remove(tree.getSelectedNode());

                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabTree:tree");
                }
            }
        }        
        /*
        if (CollectionUtils.isNotEmpty(tree.getClickselectedNodes())) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getClickselectedNodes().get(0).getData()).getNodeId().equalsIgnoreCase(
                    conceptView.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptView.getNodeConcept().getConcept().getIdConcept(),
                        idTheso,
                        selectedTheso.getCurrentLang());
            }
            TreeNode parent = tree.getClickselectedNodes().get(0).getParent();
            if (parent != null) {
                parent.getChildren().remove(tree.getClickselectedNodes().get(0));

                if (pf.isAjaxRequest()) {
                    pf.ajax().update("formLeftTab:tabTree:tree");
                }
            }
        }*/
        if (!conceptView.getNodeConcept().getNodeBT().isEmpty()) {
            conceptView.getConcept(idTheso,
                    conceptView.getNodeConcept().getNodeBT().get(0).getIdConcept(),
                    selectedTheso.getCurrentLang());
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été supprimé");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }
        PrimeFaces.current().executeScript("PF('deleteConcept').hide();");
        reset("");
    }

///////////////////////////////////////////////////////////////////////////////
//////// gestion des concepts dépérciés    
///////////////////////////////////////////////////////////////////////////////    
    public void deprecateConcept(String idConcept, String idTheso, int idUser) {
        DeprecateHelper deprecateHelper = new DeprecateHelper();
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();

        if (!deprecateHelper.deprecateConcept(connect.getPoolConnexion(), idConcept, idTheso, idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le concept n'a pas été déprécié !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcept, idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                idConcept, selectedTheso.getCurrentIdTheso());
        ///////////////        
        conceptView.getConceptForTree(idTheso, idConcept, conceptView.getSelectedLang());
        
        
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(), idTheso, conceptBean.getSelectedLang());
            }
            ((DefaultTreeNode) tree.getSelectedNode()).setType("deprecated");
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
            }
        }         
        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "le concept est maintenant obsolète");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formLeftTab");
            pf.ajax().update("containerIndex:formRightTab");
        }
    }
    
    public void approveConcept(String idConcept, String idTheso, int idUser){
        DeprecateHelper deprecateHelper = new DeprecateHelper();
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        if (!deprecateHelper.approveConcept(connect.getPoolConnexion(), idConcept, idTheso, idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le concept n'a pas été approuvé !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (isReplacedByRTrelation) {
            RelationsHelper relationsHelper = new RelationsHelper();
            if (conceptView.getNodeConcept().getReplacedBy() != null && !conceptView.getNodeConcept().getReplacedBy().isEmpty()) {
                for (NodeIdValue nodeIdValue : nodeReplaceBy) {
                    relationsHelper.addRelationRT(connect.getPoolConnexion(), idConcept, idTheso, nodeIdValue.getId(), idUser);
                }
            }
        }

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcept, idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                idConcept, selectedTheso.getCurrentIdTheso());
        ///////////////        
        conceptView.getConceptForTree(idTheso, idConcept, conceptView.getSelectedLang());
        
        if (tree.getSelectedNode() != null) {
            // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
            if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(
                    conceptBean.getNodeConcept().getConcept().getIdConcept())) {
                tree.expandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(), idTheso, conceptBean.getSelectedLang());
            }
            if(((DefaultTreeNode) tree.getSelectedNode()).getChildCount() == 0)
                ((DefaultTreeNode) tree.getSelectedNode()).setType("file"); 
            else 
                ((DefaultTreeNode) tree.getSelectedNode()).setType("concept");
            if (pf.isAjaxRequest()) {
                pf.ajax().update("containerIndex:formLeftTab:tabTree:tree");
            }
        }          
        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "le concept n'est plus obsolète maintenant");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void addReplacedBy(String idConceptDeprecated, String idTheso, int idUser) {
        FacesMessage msg;
        
        if ( searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de concept sélectionné !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        DeprecateHelper deprecateHelper = new DeprecateHelper();

        if (!deprecateHelper.addReplacedBy(connect.getPoolConnexion(),
                idConceptDeprecated, idTheso, searchSelected.getIdConcept(), idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "le concept n'a pas été ajouté !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                idConceptDeprecated, idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                idConceptDeprecated, selectedTheso.getCurrentIdTheso());
        ///////////////        
        conceptView.getConceptForTree(idTheso, idConceptDeprecated, conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("containerIndex:rightTab:idReplaceBy containerIndex:rightTab:idDeprecatedLabel");
        }
    }
    
    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null){
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.getPoolConnexion(),
                    value,
                    conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(), false);
        }
        return liste;
    }      

    public void deleteReplacedBy(String idConceptDeprecated, String idTheso, String idConceptReplaceBy, int idUser){
        FacesMessage msg;        
        if(idConceptReplaceBy == null || idConceptReplaceBy.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Pas de concept sélectionné !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;              
        }
        DeprecateHelper deprecateHelper = new DeprecateHelper();

        if (!deprecateHelper.deleteReplacedBy(connect.getPoolConnexion(),
                idConceptDeprecated, idTheso, idConceptReplaceBy)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "la relation n'a pas été enlevée !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                idConceptDeprecated, idUser);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                idConceptDeprecated, selectedTheso.getCurrentIdTheso());
        ///////////////        
        conceptView.getConceptForTree(idTheso, idConceptDeprecated, conceptView.getSelectedLang());

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("containerIndex:idDeleteReplaceBy");
        }
        reset("");

    }

    ///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
//////////// générer les identifiants Ark    //////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
    public void infosArk() {
        String message = "Permet de générer un identifiant Ark, si l'identifiant existe, il sera mise à jour !!";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de générer l'identifiant Ark, s'il n'existe pas, il sera créé,
     * sinon, il sera mis à jour.
     */
    public void generateArk() {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts = new ArrayList<>();
        idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateArkIds(conceptHelper, idConcepts);
    }

    /**
     * permet de générer les identifiants Ark pour les concepts qui n'en ont pas
     * si un identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateArkForConceptWithoutArk() {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts;
        idConcepts = conceptHelper.getAllIdConceptOfThesaurusWithoutArk(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        //idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateArkIds(conceptHelper, idConcepts);
    }
    
    /**
     * permet de générer les identifiants Ark pour cette branche, si un
     * identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateArkForThisBranch() {
        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        if (conceptView.getNodeConcept() == null) {
            return;
        }
        nodeIdValues = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts = conceptHelper.getIdsOfBranch(connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        nodeIdValues = generateArkIds(conceptHelper, idConcepts);
    }

    /**
     * permet de mettre à jour les URI des identifiants Ark pour cette branche,
     * cette fonction ne fait que la mise à jour de l'URL et ne permet de créer des identifiants ARK
     */
    public void updateUriArkForThisBranch(){
        if(roleOnThesoBean.getNodePreference() == null) return;
        if(conceptView.getNodeConcept() == null) return;

        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts = conceptHelper.getIdsOfBranch(connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        updateUriArkIds(conceptHelper, idConcepts);
    }

    /**
     * permet de générer la totalité des identifiants Ark, si un identifiant
     * n'existe pas, il sera créé, sinon, il sera mis à jour.
    */
    public void generateAllArk() {
       
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts;
        idConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        nodeIdValues = new ArrayList<>();
        //idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        nodeIdValues = generateArkIds(conceptHelper, idConcepts);
    }
    
    /**
     * permet de générer la totalité des identifiants Ark, si un identifiant
     * n'existe pas, il sera créé, sinon, il sera mis à jour.
    */
    public void generateAllArkFast() {
       
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts;
        idConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        nodeIdValues = new ArrayList<>();
        //idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        
        FacesMessage msg;
        nodeIdValues = conceptHelper.generateArkIdFast(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcepts,
                selectedTheso.getCurrentLang());

        if(nodeIdValues == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'opération est terminée avec succès !!");
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'opération est terminée, vérifier le fichier de résultat téléchargé !!");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }        
    }    

    private ArrayList<NodeIdValue> generateArkIds(ConceptHelper conceptHelper, ArrayList<String> idConcepts) {
        FacesMessage msg;
        nodeIdValues = conceptHelper.generateArkId(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcepts,
                selectedTheso.getCurrentLang());
        if(nodeIdValues == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'opération est terminée avec succès !!");
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "L'opération est terminée, vérifier le fichier de résultat téléchargé !!");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
        return nodeIdValues;
    }


    private void updateUriArkIds(ConceptHelper conceptHelper, ArrayList<String> idConcepts){
        FacesMessage msg;
        if(!conceptHelper.updateUriArk(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                idConcepts)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La mise à jour des URIs Ark a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La mise à jour des URIs Ark a réussi !!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }


    /**
     * récupération du résultat du traitement
     * @return 
     */
    public StreamedContent getResultOfProcess(){
        if(nodeIdValues == null) return null; 
        /// pour retourner le résultat du traitement
        CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
        byte[] datas = csvWriteHelper.writeCsvResultProcess(nodeIdValues, "idConcept", "Résultat");

        try ( ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
            return DefaultStreamedContent.builder()
                    .contentType("text/csv")
                    .name("resultat.csv")
                    .stream(() -> input)
                    .build();
        } catch (IOException ex) {
            System.out.println(ex.toString());
        }
        return new DefaultStreamedContent();            
    }    


///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
//////////// générer les identifiants Handle //////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
    public void infosHandle() {
        String message = "Permet de générer un identifiant Handle, si l'identifiant existe, il sera mise à jour !!";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void infosDeleteHandle() {
        String message = "Permet de supprimer un identifiant Handle, il sera définitivement supprimé !!";
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet de générer l'identifiant Handle, s'il n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void deleteHandle() {
        FacesMessage msg;
        if (roleOnThesoBean.getNodePreference() == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Pas de préférences pour le thésaurus !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if (conceptView.getNodeConcept().getConcept().getIdHandle() == null || conceptView.getNodeConcept().getConcept().getIdHandle().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Pas d'identifiant Handle à supprimer !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        
        if(roleOnThesoBean.getNodePreference().isUseHandleWithCertificat()) {
            HandleHelper handleHelper = new HandleHelper(roleOnThesoBean.getNodePreference());
            if (!handleHelper.deleteIdHandle(
                    conceptView.getNodeConcept().getConcept().getIdHandle(),
                    selectedTheso.getCurrentIdTheso())) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", handleHelper.getMessage());
                FacesContext.getCurrentInstance().addMessage(null, msg);
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La suppression de Handle a échoué !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }

            conceptHelper.updateHandleIdOfConcept(connect.getPoolConnexion(),
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    "");
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La suppression de Handle a réussi !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
        } else {
            HandleService hs = HandleService.getInstance();
            hs.applyNodePreference(roleOnThesoBean.getNodePreference());
            hs.connectHandle(); 
            try {
                hs.deleteHandle(conceptView.getNodeConcept().getConcept().getIdHandle());
            } catch (HandleException ex) {
                System.out.println(ex.toString());
            }    
            conceptHelper.updateHandleIdOfConcept(connect.getPoolConnexion(),
                    conceptView.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    "");            
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La suppression de Handle a réussi !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);                
        }
    }

    /**
     * permet de générer l'identifiant Handle, s'il n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateHandle() {
        ConceptHelper conceptHelper = new ConceptHelper();

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        ArrayList<String> idConcepts = new ArrayList<>();
        idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        generateHandleIds(conceptHelper, idConcepts);
    }

    /**
     * permet de générer les identifiants Handle pour les concepts qui n'en ont
     * pas si un identifiant n'existe pas, il sera créé, sinon, il sera mis à
     * jour.
     */
    public void generateHandleForConceptWithoutHandle() {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts;
        idConcepts = conceptHelper.getAllIdConceptOfThesaurusWithoutHandle(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        //idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateHandleIds(conceptHelper, idConcepts);
    }

    /**
     * permet de générer les identifiants Handle pour cette branche, si un
     * identifiant n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateHandleForThisBranch() {
        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        if (conceptView.getNodeConcept() == null) {
            return;
        }

        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts = conceptHelper.getIdsOfBranch(connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());

        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateHandleIds(conceptHelper, idConcepts);
    }

    /**
     * permet de générer la totalité des identifiants Handle, si un identifiant
     * n'existe pas, il sera créé, sinon, il sera mis à jour.
     */
    public void generateAllHandle() {
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idConcepts;
        idConcepts = conceptHelper.getAllIdConceptOfThesaurus(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());

        if (roleOnThesoBean.getNodePreference() == null) {
            return;
        }
        //idConcepts.add(conceptView.getNodeConcept().getConcept().getIdConcept());
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());
        generateHandleIds(conceptHelper, idConcepts);
    }

    private void generateHandleIds(ConceptHelper conceptHelper, ArrayList<String> idConcepts) {
        FacesMessage msg;
        if (!conceptHelper.generateHandleId(
                connect.getPoolConnexion(),
                idConcepts,
                selectedTheso.getCurrentIdTheso())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "La génération de Handle a échoué !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "La génération de Handle a réussi !!");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
        }
    }

    public void cancelDelete() {
        forDelete = false;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isIsCreated() {
        return isCreated;
    }

    public void setIsCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public boolean isForDelete() {
        return forDelete;
    }

    public void setForDelete(boolean forDelete) {
        this.forDelete = forDelete;
    }

    public ArrayList<NodeIdValue> getNodeReplaceBy() {
        return nodeReplaceBy;
    }

    public void setNodeReplaceBy(ArrayList<NodeIdValue> nodeReplaceBy) {
        this.nodeReplaceBy = nodeReplaceBy;
    }

    public boolean isIsReplacedByRTrelation() {
        return isReplacedByRTrelation;
    }

    public void setIsReplacedByRTrelation(boolean isReplacedByRTrelation) {
        this.isReplacedByRTrelation = isReplacedByRTrelation;
    }

    public NodeSearchMini getSearchSelected() {
        return searchSelected;
    }

    public void setSearchSelected(NodeSearchMini searchSelected) {
        this.searchSelected = searchSelected;
    }    

    public ArrayList<NodeConceptType> getNodeConceptTypes() {
        return nodeConceptTypes;
    }

    public void setNodeConceptTypes(ArrayList<NodeConceptType> nodeConceptTypes) {
        this.nodeConceptTypes = nodeConceptTypes;
    }

    public String getSelectedConceptType() {
        return selectedConceptType;
    }

    public void setSelectedConceptType(String selectedConceptType) {
        this.selectedConceptType = selectedConceptType;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isApplyToBranch() {
        return applyToBranch;
    }

    public void setApplyToBranch(boolean applyToBranch) {
        this.applyToBranch = applyToBranch;
    }

    public boolean isReciprocalRelationship() {
        return reciprocalRelationship;
    }

    public void setReciprocalRelationship(boolean reciprocalRelationship) {
        this.reciprocalRelationship = reciprocalRelationship;
    }

    public NodeConceptType getNodeConceptTypeToDelete() {
        return nodeConceptTypeToDelete;
    }

    public void setNodeConceptTypeToDelete(NodeConceptType nodeConceptTypeToDelete) {
        this.nodeConceptTypeToDelete = nodeConceptTypeToDelete;
    }

    public NodeConceptType getNodeConceptTypeToAdd() {
        return nodeConceptTypeToAdd;
    }

    public void initNodeConceptTypeToAdd(){
        nodeConceptTypeToAdd = new NodeConceptType();
    }
    public void setNodeConceptTypeToAdd(NodeConceptType nodeConceptTypeToAdd) {
        this.nodeConceptTypeToAdd = nodeConceptTypeToAdd;
    }
    
}