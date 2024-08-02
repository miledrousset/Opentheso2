/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import com.sun.mail.smtp.SMTPTransport;
import fr.cnrs.opentheso.bean.forgetpassword.ForgetPassBean;
import java.util.Properties;
import jakarta.inject.Inject;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class SendMailTest {
    @Inject ForgetPassBean forgetPassBean;
    
    public SendMailTest() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void sendMail() {
        Properties props = getPrefMail();
        sendMail__("miled.rousset@mom.fr", "pass", "pseudo", props);
        int i = 0;
    }
 
    private Properties getPrefMail(){
        Properties props;
        props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.smtp.host", "smtp.cnrs.fr");//"smtp-relay.gmail.com");//"smtprelay.cnrs.fr");
        props.setProperty("mail.smtp.port", "25");
        props.setProperty("mail.smtp.auth", "false");
//        props.setProperty("mailFrom", "opentheso@mom.fr");
 //       props.setProperty("transportMail", "smtp");        
        return props;            
    }
    
    
    private boolean sendMail__(String email, String pass, String pseudo, Properties props) {
        try {
            Session session = Session.getInstance(props);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("opentheso@mom.fr"));//props.getProperty("mailFrom")));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            msg.setSubject("Mot de passe oublié"); /// mot.titlePass

            msg.setText("Veuillez-trouver ci-joint vos coordonnées pour vous connecter à opentheso : " + "\n"
                    + "Votre pseudo : " + pseudo + "\n votre passe : " + pass);

            SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");//props.getProperty("transportMail"));
            transport.connect();
            transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
            transport.close();
            return true;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return false;
    }
}
