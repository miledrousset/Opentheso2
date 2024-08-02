/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.profile;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "profileBean")
@SessionScoped
public class ProfileBean implements Serializable {
    @Inject private MyAccountBean myAccountBean;
 
    private boolean isMyAccountActive;
    private boolean isMyProjectActive;
    private boolean isUsersActive;
    private boolean isProjectsActive; 
    private boolean isThesaurusActive;     
    
    private String myAccountColor;
    private String myProjectColor;
    private String usersColor;
    private String projectsColor;
    private String thesaurusColor;    

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        myAccountColor = null;
        myProjectColor = null;
        usersColor = null;
        projectsColor = null;
        thesaurusColor = null;       
    }
    
    @PostConstruct
    public void postInit(){
    }    
    
    public ProfileBean() {
    }

    public void reset() {
        isMyAccountActive = true;
        isMyProjectActive = false;
        isUsersActive = false;
        isProjectsActive = false;
        isThesaurusActive = false;

        
        // activation de la couleur par defaut
        resetColor();
        myAccountColor = "white";
        myAccountBean.reset();
    }

    public boolean isIsMyAccountActive() {
        return isMyAccountActive;
    }

    public void setIsMyAccountActive(boolean isMyAccountActive) {
        this.isMyAccountActive = isMyAccountActive;
        isMyProjectActive = false;
        isUsersActive = false;
        isProjectsActive = false;
        isThesaurusActive = false;
        resetColor();
        myAccountColor = "white";      
    }

    public boolean isIsMyProjectActive() {
        return isMyProjectActive;
    }

    public void setIsMyProjectActive(boolean isMyProjectActive) {
        this.isMyProjectActive = isMyProjectActive;
        isMyAccountActive = false;
        isUsersActive = false;
        isProjectsActive = false;
        isThesaurusActive = false;
        resetColor();
        myProjectColor = "white";          
    }

    public boolean isIsUsersActive() {
        return isUsersActive;
    }

    public void setIsUsersActive(boolean isUsersActive) {
        this.isUsersActive = isUsersActive;
        isMyAccountActive = false;
        isMyProjectActive = false;
        isProjectsActive = false;
        isThesaurusActive = false;
        resetColor();
        usersColor = "white";          
    }

    public boolean isIsProjectsActive() {
        return isProjectsActive;
    }

    public void setIsProjectsActive(boolean isProjectsActive) {
        this.isProjectsActive = isProjectsActive;
        isMyAccountActive = false;
        isMyProjectActive = false;
        isThesaurusActive = false;
        isUsersActive = false;
        resetColor();
        projectsColor = "white";           
    }

    public boolean isIsThesaurusActive() {
        return isThesaurusActive;
    }

    public void setIsThesaurusActive(boolean isThesaurusActive) {
        this.isThesaurusActive = isThesaurusActive;
        isMyAccountActive = false;
        isMyProjectActive = false;
        isProjectsActive = false;
        isUsersActive = false;
        resetColor();
        thesaurusColor = "white";           
    }

    public String getProjectsColor() {
        return projectsColor;
    }

    public void setProjectsColor(String projectsColor) {
        this.projectsColor = projectsColor;
    }

    public String getThesaurusColor() {
        return thesaurusColor;
    }

    public void setThesaurusColor(String thesaurusColor) {
        this.thesaurusColor = thesaurusColor;
    }


    
    

    public String getUsersColor() {
        return usersColor;
    }

    public void setUsersColor(String usersColor) {
        this.usersColor = usersColor;
    }
    

    public String getMyProjectColor() {
        return myProjectColor;
    }

    public void setMyProjectColor(String myProjectColor) {
        this.myProjectColor = myProjectColor;
    }

    public String getMyAccountColor() {
        return myAccountColor;
    }

    public void setMyAccountColor(String myAccountColor) {
        this.myAccountColor = myAccountColor;
    }
    
    private void resetColor(){
        myAccountColor = "#B3DDC4";
        myProjectColor = "#B3DDC4";
        usersColor = "#B3DDC4";
        projectsColor = "#B3DDC4";
        thesaurusColor = "#B3DDC4";
    }
    
    
}
