/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.rightbody;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.annotation.PostConstruct;

/**
 *
 * @author miledrousset
 */
@Named(value = "rightBodySetting")
@SessionScoped
public class RightBodySetting implements Serializable {
    private boolean showConcept;
    private boolean showGroup;
    private boolean showResultSearch;
    private boolean showThesoHome;
    private boolean showhome;
    
    private String index; // pour initialiser la vue du tab
    /**
     * Creates a new instance of ViewsBean
     */
    
    @PostConstruct
    public void postInit(){
    }    
    public RightBodySetting() {
    }

    public void init(){
        showConcept = false;
        showGroup = false;
        showResultSearch = false;    
        showThesoHome = false;
        showhome = false;
        index = "0";
    }
    
    public void setShowGroupToOn() {
        showConcept = false;
        showGroup = true;
        showResultSearch = false;    
        showThesoHome = false;
        showhome = false;        
    }

    public void setShowConceptToOn() {
        showConcept = true;
        showGroup = false;
        showResultSearch = false;    
        showThesoHome = false;
        showhome = false;        
    }
    public void setShowThesoHomeToOn() {
        showConcept = false;
        showGroup = false;
        showResultSearch = false;    
        showThesoHome = true;
        showhome = false;        
    }
    public void setShowHomeToOn() {
        showConcept = false;
        showGroup = false;
        showResultSearch = false;    
        showThesoHome = false;
        showhome = true;        
    }
    
    public void setShowResultSearchToOn() {
        showConcept = false;
        showGroup = false;
        showResultSearch = true;    
        showThesoHome = false;
        showhome = false;        
    }    

    public boolean isShowConcept() {
        return showConcept;
    }

    public void setShowConcept(boolean showConcept) {
        this.showConcept = showConcept;
    }

    public boolean isShowGroup() {
        return showGroup;
    }

    public void setShowGroup(boolean showGroup) {
        this.showGroup = showGroup;
    }

    public boolean isShowResultSearch() {
        return showResultSearch;
    }

    public void setShowResultSearch(boolean showResultSearch) {
        this.showResultSearch = showResultSearch;
    }

    public boolean isShowThesoHome() {
        return showThesoHome;
    }

    public void setShowThesoHome(boolean showThesoHome) {
        this.showThesoHome = showThesoHome;
    }

    public boolean isShowhome() {
        return showhome;
    }

    public void setShowhome(boolean showhome) {
        this.showhome = showhome;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
    
    
}
