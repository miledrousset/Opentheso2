package fr.cnrs.opentheso.bean.language;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.candidat.enumeration.LanguageEnum;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;

@Named(value = "langueBean")
@SessionScoped
public class LanguageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject private Connect connect;

    private String currentBundle;
    private String idLangue;

    private ResourceBundle getBundleLangue(String l) {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundleLangue = context.getApplication().getResourceBundle(context, l);
        return bundleLangue;
    }

    /**
     * Constructeur
     */
    public LanguageBean() {
        //    FacesContext context = FacesContext.getCurrentInstance();
    }

    @PostConstruct
    public void InitLanguageBean() {
        currentBundle = "langue_" + connect.getWorkLanguage();
        idLangue = connect.getWorkLanguage().toUpperCase();
    }

    public void changeLangue(String l) {
        currentBundle = "langue_" + l;
        idLangue = l.toUpperCase();
        FacesContext.getCurrentInstance().getViewRoot().setLocale(new Locale(l));

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "", 
                LanguageEnum.valueOf(l.toUpperCase()).getLanguage() + " !"));
        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("containerIndex");
            pf.ajax().update("menuBar");
        }
    }

    public String getMsg(String msg) {
        return getBundleLangue(currentBundle).getString(msg);
    }

    public String getIdLangue() {
        return idLangue.toLowerCase();
    }

    public void setIdLangue(String idLangue) {
        this.idLangue = idLangue;
    }

    public String getCurrentBundle() {
        return currentBundle;
    }

    public void setCurrentBundle(String currentBundle) {
        this.currentBundle = currentBundle;
    }

}
