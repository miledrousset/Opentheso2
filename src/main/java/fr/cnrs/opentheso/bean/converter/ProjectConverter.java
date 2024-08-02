package fr.cnrs.opentheso.bean.converter;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeUserGroup;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("projectConverter")
public class ProjectConverter implements Converter{
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        if(value != null && value.trim().length() > 0) {
            try {
                NodeUserGroup nodeUserGroup = new NodeUserGroup();
                nodeUserGroup.setIdGroup(Integer.parseInt(value));
                return nodeUserGroup;
            } catch(NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid Id Project"));
            }            
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
            return "" + ((NodeUserGroup)o).getIdGroup();
        }
    }
}
