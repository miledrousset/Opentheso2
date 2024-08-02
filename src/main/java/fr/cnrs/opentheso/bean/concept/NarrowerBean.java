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
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.ValidateActionHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeNT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTypeRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@Named(value = "narrowerBean")
@SessionScoped
public class NarrowerBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private Tree tree;
    @Inject private CurrentUser currentUser;     

    private NodeSearchMini searchSelected;
    private ArrayList<NodeNT> nodeNTs;
    private ArrayList<NodeTypeRelation> typesRelationsNT;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeNTs != null){
            nodeNTs.clear();
            nodeNTs = null;
        }
        if(typesRelationsNT != null){
            typesRelationsNT.clear();
            typesRelationsNT = null;
        }
        searchSelected = null;
    }  
    
    public NarrowerBean() {
    }

    public void reset() {
        nodeNTs = conceptBean.getNodeConcept().getNodeNT();
        searchSelected = null;
    }
    
    public void initForChangeRelations(){
        RelationsHelper relationsHelper = new RelationsHelper();
        typesRelationsNT = relationsHelper.getTypesRelationsNT(connect.getPoolConnexion());
        nodeNTs = conceptBean.getNodeConcept().getNodeNT();        
    }
   
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Related !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
    /**
     * permet de retourner la liste des concepts possibles 
     * pour ajouter une relation NT
     * (en ignorant les relations interdites) 
     * on ignore les concepts de type TT
     * on ignore les concepts de type RT
     * @param value
     * @return 
     */
    public List<NodeSearchMini> getAutoComplet(String value) {
        List<NodeSearchMini> liste = new ArrayList<>();
        SearchHelper searchHelper = new SearchHelper();
        if (selectedTheso.getCurrentIdTheso() != null && conceptBean.getSelectedLang() != null){
            liste = searchHelper.searchAutoCompletionForRelation(
                    connect.getPoolConnexion(),
                    value,
                    conceptBean.getSelectedLang(),
                    selectedTheso.getCurrentIdTheso(), true);
        }
        return liste;
    }    
    
    /**
     * permet d'ajouter un 
     * @param idUser 
     */
    public void addNewNarrowerLink(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(searchSelected == null || searchSelected.getIdConcept() == null || searchSelected.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        /// vérifier la cohérence de la relation
        ValidateActionHelper validateActionHelper = new ValidateActionHelper();
        if(!validateActionHelper.isAddRelationNTValid(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                searchSelected.getIdConcept())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Relation non permise !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;    
        }        
        
        RelationsHelper relationsHelper = new RelationsHelper();
        try {
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            if(!relationsHelper.addRelationNT(
                    conn,
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso(), searchSelected.getIdConcept(), idUser)){
                conn.rollback();
                conn.close();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La création a échoué !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;                  
            } else {
                conn.commit();
                conn.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(SynonymBean.class.getName()).log(Level.SEVERE, null, ex);
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur SQL !", ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;                
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        
        // on vérifie si le concept qui a été ajouté était TopTerme, alors on le rend plus TopTerm pour éviter les boucles à l'infini
        if(conceptHelper.isTopConcept(
                connect.getPoolConnexion(),
                searchSelected.getIdConcept(),
                selectedTheso.getCurrentIdTheso())){
            if(!conceptHelper.setNotTopConcept(connect.getPoolConnexion(),
                    searchSelected.getIdConcept(),
                    selectedTheso.getCurrentIdTheso())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                        " erreur en enlevant le concept du TopConcept, veuillez utiliser les outils de coorection de cohérence !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());


        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Relation ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        /////////////// 

        if (pf.isAjaxRequest()) {
            //    pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }    
        
        tree.initAndExpandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getSelectedLang());
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");            
            pf.ajax().update("formLeftTab:tabTree:tree");
        }  

        PrimeFaces.current().executeScript("srollToSelected();");                                         
        PrimeFaces.current().executeScript("PF('addNarrowerLink').hide();");         
        reset();
    }
    
    /**
     * permet de supprimer une relation NT au concept
     * @param nodeNT
     * @param idUser 
     */
    public void deleteNarrowerLink(NodeNT nodeNT, int idUser){
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeNT == null || nodeNT.getIdConcept() == null || nodeNT.getIdConcept().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        RelationsHelper relationsHelper = new RelationsHelper();
        if(!relationsHelper.deleteRelationNT(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeNT.getIdConcept(),
                idUser)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La suppression a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }            
        ConceptHelper conceptHelper = new ConceptHelper();
        
        
        // on vérifie si le concept qui a été enlevé n'a plus de BT, on le rend TopTerme
        if(!relationsHelper.isConceptHaveRelationBT(connect.getPoolConnexion(), nodeNT.getIdConcept(), selectedTheso.getCurrentIdTheso())){
            if(!conceptHelper.setTopConcept(connect.getPoolConnexion(),nodeNT.getIdConcept(), selectedTheso.getCurrentIdTheso())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                        " erreur en passant le concept et TopConcept, veuillez utiliser les outils de coorection de cohérence !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }
     
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());


        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);     
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listNarrowerLink");
        }  
        
        tree.initAndExpandTreeToPath(conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    conceptBean.getSelectedLang());
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");            
            pf.ajax().update("formLeftTab:tabTree:tree");
        }  

        PrimeFaces.current().executeScript("srollToSelected();");                                         
        PrimeFaces.current().executeScript("PF('deleteNarrowerLink').hide();");          
    }

    
    public void updateRelation(NodeNT nodeNT, int idUser){
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();            
        
        RelationsHelper relationsHelper = new RelationsHelper();
        String inverseRelation = "BT";
        switch (nodeNT.getRole()) {
            case "NT" :
                inverseRelation = "BT";
                break;
            case "NTG":
                inverseRelation = "BTG";
                break;
            case "NTP":
                inverseRelation = "BTP";
                break;
            case "NTI":
                inverseRelation = "BTI";
                break;
        }
        try {
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            if(!relationsHelper.updateRelationNT(conn,
                    conceptBean.getNodeConcept().getConcept().getIdConcept(),
                    nodeNT.getIdConcept(),
                    selectedTheso.getCurrentIdTheso(),
                    nodeNT.getRole(),
                    inverseRelation, 
                    idUser)) {
                conn.rollback();
                conn.close();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                        " erreur modifiant la relation pour le concept !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            conn.commit();
            conn.close();
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !",
                    " erreur dans la base de données ! " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);  
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", " Relation modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        initForChangeRelations();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:changeRelationForm");
        }  
    }

    public NodeSearchMini getSearchSelected() {
        return searchSelected;
    }

    public void setSearchSelected(NodeSearchMini searchSelected) {
        this.searchSelected = searchSelected;
    }

    public ArrayList<NodeNT> getNodeNTs() {
        return nodeNTs;
    }

    public void setNodeNTs(ArrayList<NodeNT> nodeNTs) {
        this.nodeNTs = nodeNTs;
    }

    public ArrayList<NodeTypeRelation> getTypesRelationsNT() {
        return typesRelationsNT;
    }

    public void setTypesRelationsNT(ArrayList<NodeTypeRelation> typesRelationsNT) {
        this.typesRelationsNT = typesRelationsNT;
    }
}
