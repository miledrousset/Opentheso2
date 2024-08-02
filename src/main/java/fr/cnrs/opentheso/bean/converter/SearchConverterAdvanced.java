package fr.cnrs.opentheso.bean.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;

@FacesConverter("searchConverterAdvanced")
public class SearchConverterAdvanced implements Converter{
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            NodeSearchMini nodeSearchMini = new NodeSearchMini();
            nodeSearchMini.setIdConcept(value);
            return nodeSearchMini;
        }
        else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object o) {
        if(o == null) {
            return null;
        } else {
            if( ((NodeSearchMini)o).isIsGroup())
                return ((NodeSearchMini)o).getIdConcept() + "####" + "isGroup";
            if( ((NodeSearchMini)o).isIsFacet())
                return ((NodeSearchMini)o).getIdConcept() + "####" + "isFacet";            
            return ((NodeSearchMini)o).getIdConcept();
        }
    }
}
