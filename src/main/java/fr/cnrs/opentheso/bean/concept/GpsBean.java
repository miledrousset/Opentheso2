/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import fr.cnrs.opentheso.entites.Gps;
import fr.cnrs.opentheso.repositories.GpsRepository;

import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import lombok.Data;
import org.primefaces.PrimeFaces;


@Data
@SessionScoped
@Named(value = "gpsBean")
public class GpsBean implements Serializable {

    @Inject private Connect connect;
    @Inject private ConceptView conceptView;
    @Inject private SelectedTheso selectedTheso;
    @Inject private GpsRepository gpsRepository;

    private Gps gpsSelected;


    public void addNewCoordinateGps() {

        gpsRepository.saveNewGps(gpsSelected);

        conceptView.getNodeConcept().setNodeGps(gpsRepository.getGpsByConceptAndThesorus(
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso()));

        conceptView.createMap(conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Coordonnée GPS ajoutée avec succèe");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().ajax().update("messageIndex");
        PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    /**
     * permet de mettre à jour les coordonnées GPS
     */
    public void updateCoordinateGps(Gps gps) {

        gpsRepository.saveNewGps(Gps.builder()
                .idConcept(conceptView.getNodeConcept().getConcept().getIdConcept())
                .idTheso(selectedTheso.getCurrentIdTheso())
                .latitude(gps.getLatitude())
                .longitude(gps.getLongitude())
                .build());

        conceptView.getNodeConcept().setNodeGps(gpsRepository.getGpsByConceptAndThesorus(
                conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso()));

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Coordonnée GPS supprimé avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
            PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
            PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        }
    }


    /**
     * permet de supprimer les coordonnées GPS d'un concept
     */
    public void deleteCoordinateGps(Gps gps) {

        if(gps == null) return;

        gpsRepository.removeGps(gps);

        conceptView.createMap(conceptView.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(), Boolean.FALSE);

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Suppression des coordonnées GPS réussie");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("messageIndex");
            PrimeFaces.current().ajax().update("containerIndex:formLeftTab");
            PrimeFaces.current().ajax().update("containerIndex:formRightTab");
        }
    }

    public void init() {
        gpsSelected = new Gps();
        gpsSelected.setIdConcept(conceptView.getNodeConcept().getConcept().getIdConcept());
        gpsSelected.setIdTheso(selectedTheso.getCurrentIdTheso());
    }

}
