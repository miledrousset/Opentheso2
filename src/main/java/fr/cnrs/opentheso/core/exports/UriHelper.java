
package fr.cnrs.opentheso.core.exports;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import jakarta.faces.context.FacesContext;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author miledrousset
 */
public class UriHelper {

    private NodePreference nodePreference;
    private String idTheso;
    private HikariDataSource ds;
    
    public UriHelper(HikariDataSource ds, NodePreference nodePreference, String idTheso) {
        this.nodePreference = nodePreference;
        this.idTheso = idTheso;
        this.ds = ds;
    }
    
    
    /**
     * Cette fonction permet de retourner l'URI du thesaurus
     * Choix de l'URI pour l'export : 1 seule URI est possible pour l'export
     * 
     * @param idTheso
     * @param idArk
     * @param idHandle
     * @return 
     */
    public String getUriForTheso(String idTheso, String idArk, String idHandle) {
        String uri = "";
        if (idTheso == null || nodePreference == null) {
            return uri;
        }
        
        // type Ark
        if(nodePreference.isOriginalUriIsArk()) { 
            if (!StringUtils.isEmpty(idArk)) {
                uri = nodePreference.getOriginalUri()+ "/" + idArk;
                return uri;
            } else {
                uri = nodePreference.getCheminSite() + "?idt=" + idTheso;
                return uri;
            }
        }
        
        // type Handle
        if(nodePreference.isOriginalUriIsHandle()){
            if (!StringUtils.isEmpty(idHandle)) {
                uri = "https://hdl.handle.net/" + idHandle;
                return uri;
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        //http://localhost/opentheso2/?idt=th17
        if(!StringUtils.isEmpty(nodePreference.getOriginalUri())) {
            uri = nodePreference.getOriginalUri() + "/?idt=" + idTheso;
        } else {
            uri = getPath() + "/?idt=" + idTheso;
        }
        return uri;
    }      
    
    /**
     * Cette fonction permet de retourner l'URI du groupe
     * Choix de l'URI pour l'export : 1 seule URI est possible pour l'export par groupe
     * 
     * @param idGroup
     * @param idArk
     * @param idHandle
     * @return 
     */
    public String getUriForGroup(String idGroup, String idArk, String idHandle) {
        String uri = "";
        if (idTheso == null || nodePreference == null) {
            return uri;
        }
        
        // type Ark
        if(nodePreference.isOriginalUriIsArk()) { 
            if (!StringUtils.isEmpty(idArk)) {
                uri = nodePreference.getOriginalUri()+ "/" + idArk;
                return uri;
            }
        }
        
        // type Handle
        if(nodePreference.isOriginalUriIsHandle()){
            if (!StringUtils.isEmpty(idHandle)) {
                uri = "https://hdl.handle.net/" + idHandle;
                return uri;
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        if(!StringUtils.isEmpty(nodePreference.getOriginalUri())) {
            uri = nodePreference.getOriginalUri() + "/?idg=" + idGroup
                            + "&idt=" + idTheso;
        } else {
            uri = getPath() + "/?idg=" + idGroup
                            + "&idt=" + idTheso;
        }
        return uri;
    }    
    
    /**
     * Cette fonction permet de retourner l'URI du concept
     * Choix de l'URI pour l'export : 1 seule URI est possible pour l'export par concept 
     * @param idConcept
     * @param idArk
     * @param idHandle
     * @return 
     */
    public String getUriForConcept(String idConcept, String idArk, String idHandle) {
        String uri = "";
        if (idTheso == null || nodePreference == null) {
            return uri;
        }
        
        // type Ark
        if(nodePreference.isOriginalUriIsArk()) { 
            if (!StringUtils.isEmpty(idArk)) {
                uri = nodePreference.getOriginalUri()+ "/" + idArk;
                return uri;
            }
        }
        
        // type Handle
        if(nodePreference.isOriginalUriIsHandle()){
            if (!StringUtils.isEmpty(idHandle)) {
                uri = "https://hdl.handle.net/" + idHandle;
                return uri;
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        if(!StringUtils.isEmpty(nodePreference.getOriginalUri())) {
            uri = nodePreference.getOriginalUri() + "/?idc=" + idConcept
                            + "&idt=" + idTheso;
        } else {
            uri = getPath() + "/?idc=" + idConcept
                            + "&idt=" + idTheso;
        }
        return uri;
    }
    
    public String getIdArk(String idConcept) {
        if(nodePreference.isOriginalUriIsArk()){
            ConceptHelper conceptHelper = new ConceptHelper();
            return conceptHelper.getIdArkOfConcept(ds, idConcept, idTheso);
        }
        return null;
    }
    
    /**
     * permet de retourner le Path de l'application
     * exp:  //http://localhost:8082/opentheso2
     * @return
     */
    private String getPath(){
        if(FacesContext.getCurrentInstance() == null) {
            if(StringUtils.isNotEmpty(nodePreference.getOriginalUri())){
                if(nodePreference.getOriginalUri().endsWith("/")) {
                    return nodePreference.getOriginalUri().substring(0, nodePreference.getOriginalUri().length() - 1);
                } else 
                    return nodePreference.getOriginalUri();
            } else {
                if(nodePreference.getCheminSite().endsWith("/")) {
                    return nodePreference.getCheminSite().substring(0, nodePreference.getCheminSite().length() - 1);
                } else 
                    return nodePreference.getCheminSite();
            }
        }
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        return path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
    }    
    
}
