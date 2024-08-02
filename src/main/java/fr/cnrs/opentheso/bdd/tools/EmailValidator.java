/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.tools;

/**
 *
 * @author miled.rousset
 */

import java.util.Map;
import java.util.regex.Pattern;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import org.primefaces.validate.ClientValidator;
 
/**
 * Custom JSF Validator for Email input
 */
@FacesValidator("custom.emailValidator")
public class EmailValidator implements Validator, ClientValidator {
 
    private Pattern pattern;
  
    private static final String EMAIL_PATTERN = /*"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                                                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";*/
                                                "^[A-Za-z0-9+_.-]+@(.+)$";
  
    public EmailValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }
 
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        if(value == null) {
            return;
        }
         
        if(!pattern.matcher(value.toString()).matches()) {
          //  FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error :" , "is not a valid email;"));
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", 
                        value + " is not a valid email;"));

        }
    }
 
    public Map<String, Object> getMetadata() {
        return null;
    }
 
    public String getValidatorId() {
        return "custom.emailValidator";
    }

}
