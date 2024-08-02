package fr.cnrs.opentheso.bean.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAutoCompletion;

@FacesConverter("termConverterGroup")
public class TermeConverterGroup implements Converter{
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            NodeAutoCompletion nac = new NodeAutoCompletion();
            nac.setIdConcept(value);
            nac.setIdGroup(value);
            return nac;
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
            return ((NodeAutoCompletion)o).getIdGroup();
        }
    }
}
