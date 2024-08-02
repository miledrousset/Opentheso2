package fr.cnrs.opentheso.bean.converter;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("searchConverterIdValue")
public class SearchConverterIdValue implements Converter{

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(value);
            return nodeIdValue;
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
            return ((NodeIdValue)o).getId();
        }
    }
}
