/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.group;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bean.leftbody.viewgroups.TreeGroups;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.bean.rightbody.viewgroup.GroupView;
import jakarta.inject.Named;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;

/**
 * @author miledrousset
 */
@Named(value = "removeConceptAndChildFromGroupBean")
@SessionScoped
public class RemoveConceptAndChildFromGroupBean implements Serializable {

    @Inject 
    private Connect connect;
    
    @Inject 
    private SelectedTheso selectedTheso;
    
    @Inject 
    private ConceptView conceptView;
    
    @Inject 
    private TreeGroups treeGroups;
    
    @Inject 
    private GroupView groupView;    

    private ArrayList <NodeGroup> nodeGroups;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeGroups!= null){
            nodeGroups.clear();
            nodeGroups = null;
        }    
    }

    public void init() {
        nodeGroups = conceptView.getNodeConcept().getNodeConceptGroup();
    }

    public void removeConceptAndChildFromGroup(String idGroup, int idUser) {
        GroupHelper groupHelper = new GroupHelper();
        
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();
        
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> allId  = conceptHelper.getIdsOfBranch(
                connect.getPoolConnexion(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
        
        if( (allId == null) || (allId.isEmpty())) return;         
        
        for (String idConcept : allId) {
            if (!groupHelper.deleteRelationConceptGroupConcept(
                    connect.getPoolConnexion(),
                    idGroup,
                    idConcept,
                    selectedTheso.getCurrentIdTheso(),
                    idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Erreur lors de la suppression des concepts de la collection !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                if (pf.isAjaxRequest()) {
                    pf.ajax().update("messageIndex");          
                }            
                return;
            }            
        }

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "La branche a bien été enlevée de la collection");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        conceptView.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                conceptView.getSelectedLang());
        init();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
            pf.ajax().update("conceptForm:listeConceptGroupeToDelete");      
        }
    }
    
    public void deleteGroup(String idGroup, int idUser){
        FacesMessage msg;
        try {
            GroupHelper groupHelper = new GroupHelper();
            
            Connection conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            if(!groupHelper.deleteConceptGroupRollBack(conn, idGroup, selectedTheso.getCurrentIdTheso(), idUser)) {
                conn.rollback();
                conn.close();
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Erreur lors de la suppression de la collection !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            conn.commit();
            conn.close();
            if(!groupHelper.removeAllConceptsFromThisGroup(connect.getPoolConnexion(), idGroup, selectedTheso.getCurrentIdTheso())){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "Erreur lors de la suppression de l'appartenance des concepts à la collection !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);                
            }

            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "La collection a bien été supprimée");
            FacesContext.getCurrentInstance().addMessage(null, msg);            
        } catch (SQLException ex) {
            Logger.getLogger(RemoveConceptAndChildFromGroupBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        PrimeFaces pf = PrimeFaces.current();
        if (treeGroups.getSelectedNode() != null) {
            TreeNode parent = treeGroups.getSelectedNode().getParent();
            if (parent != null) {
                parent.getChildren().remove(treeGroups.getSelectedNode());

                if (pf.isAjaxRequest()) {
                    pf.ajax().update("containerIndex:formLeftTab:tabGroups:treeGroups");
                }
            }
        }
        groupView.init();
     
    }

    public ArrayList<NodeGroup> getNodeGroups() {
        return nodeGroups;
    }

    public void setNodeGroups(ArrayList<NodeGroup> nodeGroups) {
        this.nodeGroups = nodeGroups;
    }
}
