package fr.cnrs.opentheso.bdd.tools;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.Email;

/**
 *
 * @author miledrousset
 */
@Named
@RequestScoped
public class CustomValidationView {
    
    private String text;

    @Email(message = "zzzmust be a valid email")
    private String email;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

