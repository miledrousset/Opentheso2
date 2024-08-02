/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.helper.HistoryHelper;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "historyBean")
@SessionScoped
public class HistoryBean implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;

    private ArrayList<HistoryHelper.HistoryValue> historyLabels;
    private ArrayList<HistoryHelper.HistoryValue> historySynonyms;
    private ArrayList<HistoryHelper.HistoryValue> historyRelations;      
    private ArrayList<HistoryHelper.HistoryValue> historyNotes;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(historyLabels != null){
            historyLabels.clear();
            historyLabels = null;
        }
        if(historySynonyms != null){
            historySynonyms.clear();
            historySynonyms = null;
        }
        if(historyRelations != null){
            historyRelations.clear();
            historyRelations = null;
        }
        if(historyNotes != null){
            historyNotes.clear();
            historyNotes = null;
        }        
    }       
    
    public HistoryBean() {
    }

    public void reset() {
        HistoryHelper historyHelper = new HistoryHelper();
        historyLabels = historyHelper.getLabelHistory(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                selectedTheso.getCurrentIdTheso());
        historySynonyms = historyHelper.getSynonymHistory(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                selectedTheso.getCurrentIdTheso());
        historyRelations = historyHelper.getRelationsHistory(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());        
        historyNotes = historyHelper.getNotesHistory(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getNodeConcept().getTerm().getId_term(),
                selectedTheso.getCurrentIdTheso());             
    }
   
 
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour images !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public ArrayList<HistoryHelper.HistoryValue> getHistoryLabels() {
        return historyLabels;
    }

    public void setHistoryLabels(ArrayList<HistoryHelper.HistoryValue> historyLabels) {
        this.historyLabels = historyLabels;
    }

    public ArrayList<HistoryHelper.HistoryValue> getHistorySynonyms() {
        return historySynonyms;
    }

    public void setHistorySynonyms(ArrayList<HistoryHelper.HistoryValue> historySynonyms) {
        this.historySynonyms = historySynonyms;
    }

    public ArrayList<HistoryHelper.HistoryValue> getHistoryRelations() {
        return historyRelations;
    }

    public void setHistoryRelations(ArrayList<HistoryHelper.HistoryValue> historyRelations) {
        this.historyRelations = historyRelations;
    }

    public ArrayList<HistoryHelper.HistoryValue> getHistoryNotes() {
        return historyNotes;
    }

    public void setHistoryNotes(ArrayList<HistoryHelper.HistoryValue> historyNotes) {
        this.historyNotes = historyNotes;
    }



    


}
