package fr.cnrs.opentheso.bean.candidat.dao;

import fr.cnrs.opentheso.bean.candidat.CandidatBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

@Named(value = "definitionBean")
@SessionScoped
public class DefinitionBean implements Serializable {

    @Inject
    private CandidatBean candidatBean;

    private String definition;
    private String oldDefinition;

    public void init() {
        this.definition = "";
    }

    public void init(String definition) {
        this.definition = definition;
        this.oldDefinition = definition;
    }

    public void editDefinition() {
        int pos = candidatBean.getCandidatSelected().getDefenitions().indexOf(oldDefinition);

        if (pos > -1) {
            candidatBean.getCandidatSelected().getDefenitions().set(pos, definition.replaceAll("(\r\n|\n)", "<br />"));
        }
        definition = "";
        
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, "Définition modifiée avec succès");
            
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("candidatForm");
    }
    
    public void addDefinition() {
        
        if (StringUtils.isEmpty(definition)) {
            return;
        }
        
        for (String str : candidatBean.getCandidatSelected().getDefenitions()) {
            if (str.equals(definition)) {
                definition = "";
                return;
            }
        }
        candidatBean.getCandidatSelected().getDefenitions().add(definition.replaceAll("(\r\n|\n)", "<br />"));
        definition = "";
        
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, "Définition ajoutée avec succès");
            
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("candidatForm");
    }
    
    public void deleteDefinition() {
        
        if (StringUtils.isEmpty(definition)) {
            return;
        }
        
        for (String str : candidatBean.getCandidatSelected().getDefenitions()) {
            if (str.equals(definition)) {
                candidatBean.getCandidatSelected().getDefenitions().remove(str);
                definition = "";
                return;
            }
        }
        definition = "";
        
        candidatBean.showMessage(FacesMessage.SEVERITY_INFO, "Définition supprimée avec succès");
            
        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("candidatForm");
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getOldDefinition() {
        return oldDefinition;
    }

    public void setOldDefinition(String oldDefinition) {
        this.oldDefinition = oldDefinition;
    }

}
