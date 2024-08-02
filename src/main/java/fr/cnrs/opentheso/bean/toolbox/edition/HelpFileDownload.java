package fr.cnrs.opentheso.bean.toolbox.edition;

import java.io.InputStream;
import java.io.Serializable;
import jakarta.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


@Named(value = "helpfileDownload")
@jakarta.faces.view.ViewScoped

public class HelpFileDownload implements Serializable{

    
    private String resourcePath;
    private String resourceType;
    
    public HelpFileDownload() {        
/*        InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(resourcePath);//"/resources/demo/images/boromir.jpg");
        file = new DefaultStreamedContent(stream, "image/jpg", resourcePath.substring(resourcePath.lastIndexOf("/"), resourcePath.length()));//"downloaded_boromir.jpg");
  */  }
    
    public StreamedContent downloadCSVSample() {
        resourcePath = "/samples/sampleCSV_avecCollections.csv";
    //    this.getClass().getResourceAsStream("/samples/maquetteCSV.csv");
        InputStream stream = this.getClass().getResourceAsStream(resourcePath);//"/resources/demo/images/boromir.jpg");
 //       filee = new DefaultStreamedContent(stream, "txt/CSV", resourcePath.substring(resourcePath.lastIndexOf("/"), resourcePath.length()));//"downloaded_boromir.jpg");
        
        StreamedContent file = DefaultStreamedContent.builder()
                .contentType("txt/CSV")
                .name("sampleCSV_avecCollections.csv")
                .stream(()->stream)
                .build();
        return file;
    }
    
    public StreamedContent downloadCSVSampleWithoutCollections() {
        resourcePath = "/samples/sampleCSV_sansCollections.csv";
    //    this.getClass().getResourceAsStream("/samples/maquetteCSV.csv");
        InputStream stream = this.getClass().getResourceAsStream(resourcePath);//"/resources/demo/images/boromir.jpg");
 //       filee = new DefaultStreamedContent(stream, "txt/CSV", resourcePath.substring(resourcePath.lastIndexOf("/"), resourcePath.length()));//"downloaded_boromir.jpg");
        
        StreamedContent file = DefaultStreamedContent.builder()
                .contentType("txt/CSV")
                .name("sampleCSV_sansCollections.csv")
                .stream(()->stream)
                .build();
        return file;
    }    
    

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

}
