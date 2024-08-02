package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.ExternalResourcesHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.language.LanguageBean;
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
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "externalResources")
@SessionScoped
public class ExternalResources implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private CurrentUser currentUser;    

    private String uri;
    private String description;
    
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
        description = null;
    }      
    
    public ExternalResources() {
    }

    public void reset() {
        nodeImages = conceptBean.getNodeConcept().getNodeExternalResources();
        uri = null;
        description = null;

        prepareImageForEdit();
    }
   
    public void prepareImageForEdit(){
        nodeImagesForEdit = new ArrayList<>();
        for (NodeImage nodeImage : nodeImages) {
            nodeImage.setOldUri(nodeImage.getUri());
            nodeImagesForEdit.add(nodeImage);
        }
    }    
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour ressources !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
 
    /**
     * permet d'ajouter un 
     * @param idUser 
     */
    public void addNewExternalResource(int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(uri == null || uri.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        StringPlus stringPlus = new StringPlus();
        if(!stringPlus.urlValidator(uri)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'URL n'est pas valide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }
        
        ExternalResourcesHelper externalResourcesHelper = new ExternalResourcesHelper();
        if(!externalResourcesHelper.addExternalResource(connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                description,
                uri,
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant l'ajout de la ressource !");
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
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Image ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        reset();

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void updateExternalResource(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        StringPlus stringPlus = new StringPlus();
        if(!stringPlus.urlValidator(nodeImage.getUri())){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " L'URL n'est pas valide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;            
        }        

        ExternalResourcesHelper externalResourcesHelper = new ExternalResourcesHelper();
        if(!externalResourcesHelper.setExternalResourceUri(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),               
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                nodeImage.getOldUri(),
                nodeImage.getUri(),
                nodeImage.getImageName(),
                idUser)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la modification de l'URI !");
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

        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex:formRightTab");
        }        
    }       
    
    /**
     * permet d'ajouter un
     * @param nodeImage
     * @param idUser 
     */
    public void deleteExternalResource(NodeImage nodeImage, int idUser) {
        FacesMessage msg;
        PrimeFaces pf = PrimeFaces.current();    
        
        if(nodeImage == null || nodeImage.getUri().isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Erreur !", " pas de sélection !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ExternalResourcesHelper externalResourcesHelper = new ExternalResourcesHelper();
        if(!externalResourcesHelper.deleteExternalResource(
                connect.getPoolConnexion(),
                conceptBean.getNodeConcept().getConcept().getIdConcept(),
                selectedTheso.getCurrentIdTheso(),
                nodeImage.getUri())) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur pendant la suppression de la ressource !");
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ArrayList<NodeImage> getNodeImages() {
        return nodeImages;
    }

    public void setNodeImages(ArrayList<NodeImage> nodeImages) {
        this.nodeImages = nodeImages;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public ArrayList<NodeImage> getNodeImagesForEdit() {
        return nodeImagesForEdit;
    }

    public void setNodeImagesForEdit(ArrayList<NodeImage> nodeImagesForEdit) {
        this.nodeImagesForEdit = nodeImagesForEdit;
    }



}
