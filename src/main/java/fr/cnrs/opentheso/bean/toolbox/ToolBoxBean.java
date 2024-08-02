package fr.cnrs.opentheso.bean.toolbox;

import fr.cnrs.opentheso.bean.toolbox.atelier.AtelierThesBean;
import fr.cnrs.opentheso.bean.toolbox.edition.ViewEditionBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "toolBoxBean")
@SessionScoped
public class ToolBoxBean implements Serializable {
    @Inject private ViewEditionBean viewEditionBean;
    @Inject private AtelierThesBean atelierThesBean;
    
    private boolean isEditionActive;
    private boolean isAtelierActive;
    private boolean isServiceActive;    
    private boolean isStatisticActive;

    private String editionColor;
    private String atelierColor;
    private String serviceColor;    
    private String statisticColor;

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        editionColor = null;
        atelierColor = null;
        serviceColor = null;
        statisticColor = null;
    }      
    
    public void reset() {
        isEditionActive = true;
        isStatisticActive = false;
        isAtelierActive = false;
        isServiceActive = false;
        
        // activation de la couleur pour edition
        resetColor();
        editionColor = "white";
        viewEditionBean.init();
      
    }
    
    public boolean isIsEditionActive() {
        return isEditionActive;
    }

    public void setIsEditionActive(boolean isEditionActive) {
        this.isEditionActive = isEditionActive;
        isStatisticActive = false;
        isAtelierActive = false;
        isServiceActive = false;
        resetColor();
        editionColor = "white";          
    }

    public boolean isIsStatisticActive() {
        return isStatisticActive;
    }

    public void setIsStatisticActive(boolean isStatisticActive) {
        this.isStatisticActive = isStatisticActive;
        isEditionActive = false;
        isAtelierActive = false;
        isServiceActive = false;
        resetColor();
        statisticColor = "white";          
    }

    public boolean isIsAtelierActive() {
        return isAtelierActive;
    }

    public void setIsAtelierActive(boolean isAtelierActive) {
        this.isAtelierActive = isAtelierActive;
        isEditionActive = false;
        isStatisticActive = false;
        isServiceActive = false;
        resetColor();
        atelierColor = "white";
        atelierThesBean.init();
    }

    public boolean isIsServiceActive() {
        return isServiceActive;
    }

    public void setIsServiceActive(boolean isServiceActive) {
        this.isServiceActive = isServiceActive;
        isAtelierActive = false;
        isEditionActive = false;
        isStatisticActive = false;
        resetColor();
        serviceColor = "white";
        atelierThesBean.init();        
    }
    
    private void resetColor(){
        editionColor = "#B3DDC4";
        atelierColor = "#B3DDC4";
        serviceColor = "#B3DDC4";        
        statisticColor = "#B3DDC4";
    }

    public String getEditionColor() {
        return editionColor;
    }

    public void setEditionColor(String editionColor) {
        this.editionColor = editionColor;
    }

    public String getStatisticColor() {
        return statisticColor;
    }

    public void setStatisticColor(String statisticColor) {
        this.statisticColor = statisticColor;
    }

    public String getAtelierColor() {
        return atelierColor;
    }

    public void setAtelierColor(String atelierColor) {
        this.atelierColor = atelierColor;
    }

    public String getServiceColor() {
        return serviceColor;
    }

    public void setServiceColor(String serviceColor) {
        this.serviceColor = serviceColor;
    }
    
    
    
}
