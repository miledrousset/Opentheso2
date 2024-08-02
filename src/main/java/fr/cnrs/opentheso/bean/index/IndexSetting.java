package fr.cnrs.opentheso.bean.index;

import java.io.IOException;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.annotation.PreDestroy;


@SessionScoped
@Named(value = "indexSetting")
public class IndexSetting implements Serializable {

    // si un thésaurus est sélectionné 
    private boolean isSelectedTheso;

    // si un profil est connecté
    private boolean isConnected;

    // si un concept est sélectionné
    private boolean isValueSelected;
    
    // si un thésaurus est sélectionné, on affiche les infos de Home
    private boolean isHomeSelected;    
    
    // si une facette est sélectionnée
    private boolean isFacetSelected;

    private boolean isProjectSelected;

////// variables pour les vues du menu
    private boolean isThesoActive;
    private boolean isCandidateActive;
    private boolean isProfileActive;
    private boolean isSettingActive;
    private boolean isToolBoxActive;

    // variables pour les couleurs de boutons
    private String thesoColor;
    private String candidateColor;
    private String profileColor;
    private String settingColor;
    private String toolBoxColor;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    
    public void clear(){
        thesoColor = null;
        candidateColor = null;
        profileColor = null;  
        settingColor = null; 
        toolBoxColor = null; 
    }      
    
    public IndexSetting() {
        isThesoActive = true;
        thesoColor = "white";
        candidateColor = "#B3DDC4";
        profileColor = "#B3DDC4";
        settingColor = "#B3DDC4";
        toolBoxColor = "#B3DDC4";
    }

    public void reset() {
        isThesoActive = true;
        resetColor();
        thesoColor = "white";
    }

    private void resetColor() {
        thesoColor = "#B3DDC4";
        candidateColor = "#B3DDC4";
        profileColor = "#B3DDC4";
        settingColor = "#B3DDC4";
        toolBoxColor = "#B3DDC4";
    }

    public void setIsSelectedTheso(boolean isSelectedTheso) {
        this.isSelectedTheso = isSelectedTheso;

    }
    
    public boolean isSelectedTheso() {
        return isSelectedTheso;
    }

    public void setIsThesoActive(boolean isThesoActive) {        
        this.isThesoActive = isThesoActive;
        isCandidateActive = false;
        isProfileActive = false;
        isSettingActive = false;
        isToolBoxActive = false;
        resetColor();
        thesoColor = "white";
    }

    public void setIsCandidateActive(boolean isCandidateActive) throws IOException {
        this.isCandidateActive = isCandidateActive;
        isThesoActive = false;
        isProfileActive = false;
        isSettingActive = false;
        isToolBoxActive = false;
        resetColor();
        candidateColor = "white";
    }

    public void setIsProfileActive(boolean isProfileActive) throws IOException {
        this.isProfileActive = isProfileActive;
        isThesoActive = false;
        isCandidateActive = false;
        isSettingActive = false;
        isToolBoxActive = false;
        resetColor();
        profileColor = "white";
    }

    public void setIsSettingActive(boolean isSettingActive) {
        this.isSettingActive = isSettingActive;
        isThesoActive = false;
        isCandidateActive = false;
        isProfileActive = false;
        isToolBoxActive = false;
        resetColor();
        settingColor = "white";
    }

    public void setIsToolBoxActive(boolean isToolBoxActive) {
        this.isToolBoxActive = isToolBoxActive;
        isThesoActive = false;
        isCandidateActive = false;
        isProfileActive = false;
        isSettingActive = false;
        resetColor();
        toolBoxColor = "white";
        
    }
    
    public void setIsValueSelected(boolean isValueSelected) {
        this.isValueSelected = isValueSelected;
        isFacetSelected = false;
        isHomeSelected = false;
    }

    public void setIsHomeSelected(boolean isHomeSelected) {
        this.isHomeSelected = isHomeSelected;
        isFacetSelected = false;
        isValueSelected = false;
    }

    public void setIsFacetSelected(boolean isFacetSelected) {
        this.isFacetSelected = isFacetSelected;
        isValueSelected = false;
        isHomeSelected = false;
    }

    public void setSelectedTheso(boolean selectedTheso) {
        isSelectedTheso = selectedTheso;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean isValueSelected() {
        return isValueSelected;
    }

    public void setValueSelected(boolean valueSelected) {
        isValueSelected = valueSelected;
    }

    public boolean isHomeSelected() {
        return isHomeSelected;
    }

    public void setHomeSelected(boolean homeSelected) {
        isHomeSelected = homeSelected;
    }

    public boolean isFacetSelected() {
        return isFacetSelected;
    }

    public void setFacetSelected(boolean facetSelected) {
        isFacetSelected = facetSelected;
    }

    public boolean isProjectSelected() {
        return isProjectSelected;
    }

    public void setProjectSelected(boolean projectSelected) {
        isProjectSelected = projectSelected;
    }

    public boolean isThesoActive() {
        return isThesoActive;
    }

    public void setThesoActive(boolean thesoActive) {
        isThesoActive = thesoActive;
    }

    public boolean isCandidateActive() {
        return isCandidateActive;
    }

    public void setCandidateActive(boolean candidateActive) {
        isCandidateActive = candidateActive;
    }

    public boolean isProfileActive() {
        return isProfileActive;
    }

    public void setProfileActive(boolean profileActive) {
        isProfileActive = profileActive;
    }

    public boolean isSettingActive() {
        return isSettingActive;
    }

    public void setSettingActive(boolean settingActive) {
        isSettingActive = settingActive;
    }

    public boolean isToolBoxActive() {
        return isToolBoxActive;
    }

    public void setToolBoxActive(boolean toolBoxActive) {
        isToolBoxActive = toolBoxActive;
    }

    public String getThesoColor() {
        return thesoColor;
    }

    public void setThesoColor(String thesoColor) {
        this.thesoColor = thesoColor;
    }

    public String getCandidateColor() {
        return candidateColor;
    }

    public void setCandidateColor(String candidateColor) {
        this.candidateColor = candidateColor;
    }

    public String getProfileColor() {
        return profileColor;
    }

    public void setProfileColor(String profileColor) {
        this.profileColor = profileColor;
    }

    public String getSettingColor() {
        return settingColor;
    }

    public void setSettingColor(String settingColor) {
        this.settingColor = settingColor;
    }

    public String getToolBoxColor() {
        return toolBoxColor;
    }

    public void setToolBoxColor(String toolBoxColor) {
        this.toolBoxColor = toolBoxColor;
    }
}
