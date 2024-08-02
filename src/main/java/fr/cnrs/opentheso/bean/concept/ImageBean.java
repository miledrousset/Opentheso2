package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.ImagesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Data
@Named(value = "imageBean")
@SessionScoped
public class ImageBean implements Serializable {

    @Inject private Connect connect;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;    

    private String uri;
    private String copyright;
    private String name;
    private String creator;
    
    private ArrayList<NodeImage> nodeImages;
    private ArrayList<NodeImage> nodeImagesForEdit;    

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(nodeImages != null){
            nodeImages.clear();
            nodeImages = null;
        }
        if(nodeImagesForEdit != null){
            nodeImagesForEdit.clear();
            nodeImagesForEdit = null;
        }
        uri = null;
        copyright = null;
    }      
    
    public ImageBean() {
    }

    public void reset() {
        nodeImages = conceptBean.getNodeConcept().getNodeimages();
        uri = null;
        copyright = null;
        name = null;
        creator = null;

        prepareImageForEdit();
    }

    public void prepareImageForEdit(){
        nodeImagesForEdit = new ArrayList<>();
        ImagesHelper imagesHelper = new ImagesHelper();
        nodeImagesForEdit = imagesHelper.getExternalImages(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso());
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour images !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
    /**
     * permet d'ajouter un 
     * @param idUser 
     */
    public void addNewImage(int idUser) {
        
        if(StringUtils.isEmpty(uri)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Aucune URI insérée !");
            return;
        }

        if(!new ImagesHelper().addExternalImage(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                name,
                copyright,
                uri,
                creator,
                idUser)) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Erreur pendant l'ajout de l'image !");
            return;
        }
        
        conceptBean.getConcept(selectedTheso.getCurrentIdTheso(), conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        new ConceptHelper().updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);

        new DcElementHelper().addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());

        showMessage(FacesMessage.SEVERITY_INFO, "Image ajoutée avec succès");
        reset();
        PrimeFaces.current().ajax().update("containerIndex:formRightTab");
    }

    private void showMessage(FacesMessage.Severity severity, String message) {
        FacesMessage msg = new FacesMessage(severity, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces.current().ajax().update("messageIndex");
    }
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void updateImage(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ImagesHelper imagesHelper = new ImagesHelper();
        if(!imagesHelper.updateExternalImage(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeImage.getId(),
                nodeImage.getUri(),
                nodeImage.getCopyRight(),
                nodeImage.getImageName(),
                nodeImage.getCreator(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la modification de l'URI de l'image !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);  
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image_URI modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();
    }       
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void deleteImage(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ImagesHelper imagesHelper = new ImagesHelper();
        if(!imagesHelper.deleteExternalImage(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeImage.getUri())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la suppression de l'image !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        
        conceptBean.getConcept(
                selectedTheso.getCurrentIdTheso(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                conceptBean.getSelectedLang());

        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.updateDateOfConcept(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(), 
                conceptBean.getNodeConcept().getConcept().getIdConcept(), idUser);   
        ///// insert DcTermsData to add contributor
        DcElementHelper dcElmentHelper = new DcElementHelper();                
        dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                conceptBean.getNodeConcept().getConcept().getIdConcept(), selectedTheso.getCurrentIdTheso());
        ///////////////        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }

}
