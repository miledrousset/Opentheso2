
package fr.cnrs.opentheso.bean.galleriaimage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.model.ResponsiveOption;

/**
 *
 * @author miledrousset
 */

@Named
@ViewScoped
public class GalleriaView implements Serializable {

//    private List<Photo> photos;

    private List<ResponsiveOption> responsiveOptions1;

    private List<ResponsiveOption> responsiveOptions2;

    private List<ResponsiveOption> responsiveOptions3;

    private int activeIndex = 0;
//
//    @Inject
//    private PhotoService service;

    @PostConstruct
    public void init() {
//        photos = service.getPhotos();

        responsiveOptions1 = new ArrayList<>();
        responsiveOptions1.add(new ResponsiveOption("1024px", 5));
        responsiveOptions1.add(new ResponsiveOption("768px", 3));
        responsiveOptions1.add(new ResponsiveOption("560px", 1));

        responsiveOptions2 = new ArrayList<>();
        responsiveOptions2.add(new ResponsiveOption("1024px", 5));
        responsiveOptions2.add(new ResponsiveOption("960px", 4));
        responsiveOptions2.add(new ResponsiveOption("768px", 3));
        responsiveOptions2.add(new ResponsiveOption("560px", 1));

        responsiveOptions3 = new ArrayList<>();
        responsiveOptions3.add(new ResponsiveOption("1500px", 5));
        responsiveOptions3.add(new ResponsiveOption("1024px", 3));
        responsiveOptions3.add(new ResponsiveOption("768px", 2));
        responsiveOptions3.add(new ResponsiveOption("560px", 1));
    }

    public void changeActiveIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        this.activeIndex = Integer.valueOf(params.get("index"));
    }

/*    public List<Photo> getPhotos() {
        return photos;
    }*/

    public List<ResponsiveOption> getResponsiveOptions1() {
        return responsiveOptions1;
    }

    public List<ResponsiveOption> getResponsiveOptions2() {
        return responsiveOptions2;
    }

    public List<ResponsiveOption> getResponsiveOptions3() {
        return responsiveOptions3;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }
/*
    public void setService(PhotoService service) {
        this.service = service;
    }*/


}
