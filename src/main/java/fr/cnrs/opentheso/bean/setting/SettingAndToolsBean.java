/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.setting;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "settingAndToolsBean")
@SessionScoped
public class SettingAndToolsBean implements Serializable {
    @Inject private PreferenceBean preferenceBean;
    
    private boolean isPreferenceActive;
    private boolean isIdentifierActive;
    private boolean isCorpusActive;    
    private boolean isMaintenanceActive;
    
    private String preferenceColor;
    private String identifierColor;
    private String corpusColor;
    private String maintenanceColor;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        preferenceColor = null;
        identifierColor = null;        
        corpusColor = null;
        maintenanceColor = null;          
    }     
    
    public SettingAndToolsBean() {
    }

    public void reset() {
        isPreferenceActive = true;
        isMaintenanceActive = false;
        isIdentifierActive = false;
        isCorpusActive = false;
        
        // activation de la couleur pour edition
        resetColor();
        preferenceColor = "white";
        preferenceBean.init();
      
    }

    public boolean isIsPreferenceActive() {
        return isPreferenceActive;
    }

    public void setIsPreferenceActive(boolean isPreferenceActive) {
        this.isPreferenceActive = isPreferenceActive;
        isMaintenanceActive = false;
        isIdentifierActive = false;
        isCorpusActive = false;
        resetColor();
        preferenceColor = "white";         
    }

   
    public boolean isIsMaintenanceActive() {
        return isMaintenanceActive;
    }

    public void setIsMaintenanceActive(boolean isMaintenanceActive) {
        this.isMaintenanceActive = isMaintenanceActive;
        isPreferenceActive = false;
        isIdentifierActive = false;  
        isCorpusActive = false;
        resetColor();
        maintenanceColor = "white";   
    }

    public boolean isIsIdentifierActive() {
        return isIdentifierActive;
    }

    public void setIsIdentifierActive(boolean isIdentifierActive) {
        this.isIdentifierActive = isIdentifierActive;
        isPreferenceActive = false;
        isMaintenanceActive = false;
        isCorpusActive = false;
        resetColor();
        identifierColor = "white";        
    }

    public boolean isIsCorpusActive() {
        return isCorpusActive;
    }

    public void setIsCorpusActive(boolean isCorpusActive) {
        this.isCorpusActive = isCorpusActive;
        isPreferenceActive = false;
        isMaintenanceActive = false;
        isIdentifierActive = false;
        resetColor();
        corpusColor = "white";          
    }
    
    
    
    
    private void resetColor(){
        preferenceColor = "#B3DDC4";
        maintenanceColor = "#B3DDC4";
        identifierColor = "#B3DDC4";   
        corpusColor = "#B3DDC4";
    }

    public String getPreferenceColor() {
        return preferenceColor;
    }

    public void setPreferenceColor(String preferenceColor) {
        this.preferenceColor = preferenceColor;
    }

    public String getMaintenanceColor() {
        return maintenanceColor;
    }

    public void setMaintenanceColor(String maintenanceColor) {
        this.maintenanceColor = maintenanceColor;
    }

    public String getIdentifierColor() {
        return identifierColor;
    }

    public void setIdentifierColor(String identifierColor) {
        this.identifierColor = identifierColor;
    }

    public String getCorpusColor() {
        return corpusColor;
    }

    public void setCorpusColor(String corpusColor) {
        this.corpusColor = corpusColor;
    }
    
    
    
}
