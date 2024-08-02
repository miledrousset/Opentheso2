package fr.cnrs.opentheso.utils;

import jakarta.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.ResourceBundle;


public class LDAPUtils {

    private final static String LDAPS_PROTOCOL = "ldaps://";

    public boolean authentificationLdapCheck(String login, String password) {

        ResourceBundle resourceBundle = getBundlePool();
        if(resourceBundle == null){
            return false;
        }

        System.setProperty("javax.net.ssl.keyStore", resourceBundle.getString("key.store.path"));
        System.setProperty("javax.net.ssl.trustStorePassword", resourceBundle.getString("key.store.password"));

        System.setProperty("javax.net.ssl.trustStore", resourceBundle.getString("trust.store.path"));
        System.setProperty("javax.net.ssl.keyStorePassword", resourceBundle.getString("trust.store.password"));

        //1er tentative de connexion avec la valeur de DN = ou=people,dc=huma-num,dc=fr
        try {
            new InitialDirContext(createEnvironnementBean(resourceBundle,
                    "uid="+login+",ou=people,dc=huma-num,dc=fr", password));
            return true;
        } catch (Exception e) {
            System.out.println(">> Erreur 1 : " + e.getMessage());
        }

        //2eme tentative de connexion avec la valeur de DN = ou=people,ou=notre-dame-paris,dc=huma-num,dc=fr
        try {
            new InitialDirContext(createEnvironnementBean(resourceBundle,
                    "uid="+login+",ou=people,ou=notre-dame-paris,dc=huma-num,dc=fr", password));
            return true;
        } catch (Exception e) {
            System.out.println(">> Erreur 2 : " + e.getMessage());}

        //3eme tentative de connexion sans la valeur de DN
        try {
            new InitialDirContext(createEnvironnementBean(resourceBundle, login, password));
            return true;
        } catch (Exception e) {
            System.out.println(">> Erreur 3 : " + e.getMessage());}

        return false;
    }

    private ResourceBundle getBundlePool(){
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            ResourceBundle bundlePool = context.getApplication().getResourceBundle(context, "conHikari");
            return bundlePool;
        } catch (Exception e) {
            return null;
        }
    }

    private Hashtable<String,String> createEnvironnementBean(ResourceBundle resourceBundle, String login, String password) {
        Hashtable<String,String> env = new Hashtable <String,String>();
        env.put(Context.SECURITY_AUTHENTICATION, resourceBundle.getString("ldap.security.authentication"));
        env.put(Context.SECURITY_PRINCIPAL, login);
        env.put(Context.SECURITY_CREDENTIALS, password);
        env.put(Context.INITIAL_CONTEXT_FACTORY, resourceBundle.getString("ldap.initial.context.factory"));
        env.put(Context.PROVIDER_URL, LDAPS_PROTOCOL + resourceBundle.getString("ldap.server.url"));
        return env;
    }

}
