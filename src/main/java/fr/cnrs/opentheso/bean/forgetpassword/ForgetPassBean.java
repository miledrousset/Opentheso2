/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.forgetpassword;

import fr.cnrs.opentheso.bdd.helper.ToolsHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import fr.cnrs.opentheso.bean.mail.MailBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.io.Serializable;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "forgetPassBean")
@RequestScoped
public class ForgetPassBean implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private MailBean mailBean;    

    private String sendTo;
    public ForgetPassBean() {
    }

    public void sendMail() {
        if (sendTo == null || sendTo.isEmpty()) {
            printMessage("Veuillez-saisir une adresse mail");
            return;
        }

        UserHelper userHelper = new UserHelper();
        if (userHelper.isUserMailExist(connect.getPoolConnexion(), sendTo)) {
            ToolsHelper toolsHelper = new ToolsHelper();
            String password = toolsHelper.getNewId(10, false, false);
            String passwordMD5 = MD5Password.getEncodedPassword(password);
            String pseudo = userHelper.getNameUser(connect.getPoolConnexion(), sendTo);
            int idUser = userHelper.getIdUserFromMail(connect.getPoolConnexion(), sendTo);
            if (idUser == -1) {
                printMessage("Absence des préférences pour le serveur Mail");
                return;
            }
            String subject = "Mot de passe oublié";
            String message = "Veuillez-trouver ci-joint vos coordonnées pour vous connecter à opentheso : " + "\n"
                    + "Votre pseudo : " + pseudo + "\n votre passe : " + password;            
            
            
            if (!mailBean.sendMail(sendTo, subject,  message)) {
                printMessage("Erreur d'envoie de mail, veuillez contacter l'administrateur");
                return;
            }
            if (!userHelper.updatePwd(connect.getPoolConnexion(), idUser, passwordMD5)) {
                printMessage("Erreur base de données");
            }
        } else {
            printMessage("L'utilisateur n'existe pas");
        }
    }

    private void printMessage(String message) {
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_FATAL, "", message);
        FacesContext.getCurrentInstance().addMessage(null, fm);
    }

    public String getMail() {
        return sendTo;
    }

    public void setMail(String mail) {
        this.sendTo = mail;
    }

}
