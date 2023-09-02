package fr.cnrs.opentheso.bdd.helper.nodes;

import java.io.Serializable;


public class NodeUser implements Serializable {

    private int idUser;
    private String name;
    private String mail;
    private boolean active;
    private boolean alertMail;
    private boolean superAdmin;
    private boolean passtomodify;


    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAlertMail() {
        return alertMail;
    }

    public void setAlertMail(boolean alertMail) {
        this.alertMail = alertMail;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    public boolean isPasstomodify() {
        return passtomodify;
    }

    public void setPasstomodify(boolean passtomodify) {
        this.passtomodify = passtomodify;
    }
}
