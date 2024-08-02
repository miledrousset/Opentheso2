package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bdd.datas.DCMIResource;
import fr.cnrs.opentheso.bdd.datas.DcElement;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.DcElementHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUser;
import fr.cnrs.opentheso.bean.candidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.mail.MailBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;
import fr.cnrs.opentheso.core.exports.csv.CsvWriteHelper;
import fr.cnrs.opentheso.core.exports.csv.StatistiquesRapportCSV;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author miledrousset
 */
@Named(value = "processCandidateBean")
@SessionScoped
public class ProcessCandidateBean implements Serializable {

    @Inject private Connect connect;
    @Inject private CandidatBean candidatBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private MailBean mailBean;  
    @Inject private CurrentUser currentUser;
    
    private CandidatDto selectedCandidate;
    private String adminMessage;
    

    @PreDestroy
    public void destroy() {
        clear();
    }

    public void clear() {
        selectedCandidate = null;
        adminMessage = null;
    }

    public ProcessCandidateBean() {
    }
    
    public void action(){
    }

    public void reset(CandidatDto candidatSelected) {
        this.selectedCandidate = candidatSelected;
        adminMessage = null;
    }

    public StreamedContent exportProcessedCandidates(List<CandidatDto> candidatDtos) {
        CsvWriteHelper csvWriteHelper = new CsvWriteHelper();
        byte[] datas;
        
        datas = csvWriteHelper.writeProcessedCandidates(candidatDtos, ';');

        if (datas == null) {
            return null;
        }

        PrimeFaces.current().executeScript("PF('waitDialog').hide();");

        try ( ByteArrayInputStream input = new ByteArrayInputStream(datas)) {
            return DefaultStreamedContent.builder()
                    .contentType("text/csv")
                    .name(selectedTheso.getThesoName() + "_candidats" + ".csv")
                    .stream(() -> input)
                    .build();
        } catch (IOException ex) {
        }
        PrimeFaces.current().executeScript("PF('waitDialog').hide();");
        return new DefaultStreamedContent();

    }    
    
    
    
    public void insertCandidat(int idUser, NodePreference nodePreference) {
        if (selectedCandidate == null) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();

        if (!candidatService.insertCandidate(connect, selectedCandidate, adminMessage, idUser)) {
            printErreur("Erreur d'insertion");
            return;
        }
        // envoie de mail au créateur du candidat si l'option mail est activée
        UserHelper userHelper = new UserHelper();
        NodeUser nodeUser = userHelper.getUser(connect.getPoolConnexion(), selectedCandidate.getCreatedById());
        if(nodeUser.isAlertMail())
            sendMailCandidateAccepted(nodeUser.getMail(), selectedCandidate);
        
        generateArk(nodePreference, selectedCandidate);
        
        new ConceptHelper().updateDateOfConcept(connect.getPoolConnexion(), selectedCandidate.getIdThesaurus(),
                selectedCandidate.getIdConcepte(), idUser);

        ///// insert DcTermsData to add contributor
        new DcElementHelper().addDcElementConcept(connect.getPoolConnexion(),
                new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                selectedCandidate.getIdConcepte(), selectedCandidate.getIdThesaurus());
        ///////////////             
        
        
        printMessage("Canditat inséré avec succès");
        reset(null);
        candidatBean.getAllCandidatsByThesoAndLangue();
        try {
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        candidatBean.initCandidatModule();

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex:tabViewCandidat");
    }
    
    private void generateArk(NodePreference nodePreference, CandidatDto selectedCandidateTemp){
        if (nodePreference != null) {
            ConceptHelper conceptHelper = new ConceptHelper();
            conceptHelper.setNodePreference(nodePreference);

            // création de l'identifiant Handle
            if (nodePreference.isUseHandle()) {
                if (!conceptHelper.generateIdHandle(connect.getPoolConnexion(), selectedCandidateTemp.getIdConcepte(),
                        selectedCandidateTemp.getIdThesaurus())) {
                    printErreur("La création Handle a échouée");
                    Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Handle a échoué");
                }
            }     
            // serveur Ark
            if (nodePreference.isUseArk()) {
                if (!conceptHelper.generateArkId(connect.getPoolConnexion(), 
                        selectedCandidateTemp.getIdThesaurus(), selectedCandidateTemp.getIdConcepte(),
                        selectedCandidateTemp.getLang())) {
                    printErreur("La création Ark a échoué");
                    Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création Ark a échoué");
                }
            }
            // ark Local
            if (nodePreference.isUseArkLocal()) {
                ArrayList<String> idConcepts = new ArrayList<>();
                idConcepts.add(selectedCandidateTemp.getIdConcepte());
                if (!conceptHelper.generateArkIdLocal(connect.getPoolConnexion(),
                        selectedCandidateTemp.getIdThesaurus(),
                        idConcepts)) {
                    printErreur("La création du Ark local a échoué");
                    Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, "La création du Ark local a échoué");
                }
            }                
        }          
    }
    
    public void rejectCandidat(int idUser) {
        if (selectedCandidate == null) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();

        if (!candidatService.rejectCandidate(connect, selectedCandidate, adminMessage, idUser)) {
            printErreur("Erreur d'insertion");
            return;
        }

        // envoie de mail au créateur du candidat si l'option mail est activée
        
        UserHelper userHelper = new UserHelper();
        NodeUser nodeUser = userHelper.getUser(connect.getPoolConnexion(), selectedCandidate.getCreatedById());
        if(nodeUser.isAlertMail())
            sendMailCandidateRejected(nodeUser.getMail(), selectedCandidate);

        
        printMessage("Candidat(s) rejeté(s) avec succès");
        reset(null);
        candidatBean.getAllCandidatsByThesoAndLangue();
        try {
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        candidatBean.initCandidatModule();

        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
        pf.ajax().update("containerIndex:tabViewCandidat");
    }

    public void insertListCandidat(int idUser, NodePreference nodePreference) {
        if (candidatBean.getSelectedCandidates() == null || candidatBean.getSelectedCandidates().isEmpty()) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();
        ConceptHelper conceptHelper = new ConceptHelper();
        DcElementHelper dcElmentHelper = new DcElementHelper();        
        
        // envoie de mail au créateur du candidat si l'option mail est activée
        UserHelper userHelper = new UserHelper();
        NodeUser nodeUser;

        
        for (CandidatDto selectedCandidate1 : candidatBean.getSelectedCandidates()) {
            selectedCandidate1 = candidatBean.getAllInfosOfCandidate(selectedCandidate1);
            if (!candidatService.insertCandidate(connect, selectedCandidate1, adminMessage, idUser)) {
                printErreur("Erreur d'insertion pour le candidat : " + selectedCandidate1.getNomPref() + "(" + selectedCandidate1.getIdConcepte() + ")");
                return;
            }
            conceptHelper.updateDateOfConcept(connect.getPoolConnexion(), selectedCandidate1.getIdThesaurus(),
                    selectedCandidate1.getIdConcepte(), idUser);
            
            ///// insert DcTermsData to add contributor
            dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                    new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                    selectedCandidate1.getIdConcepte(), selectedCandidate1.getIdThesaurus());
            ///////////////              
            
            generateArk(nodePreference, selectedCandidate1);
            nodeUser = userHelper.getUser(connect.getPoolConnexion(), selectedCandidate1.getCreatedById());
            if(nodeUser.isAlertMail())
                sendMailCandidateAccepted(nodeUser.getMail(), selectedCandidate1);
        }


        printMessage("Candidats insérés avec succès");
        reset(null);
        candidatBean.initCandidatModule();
        candidatBean.getAllCandidatsByThesoAndLangue();
        try {
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }     
    
    public void rejectCandidatList(int idUser) {
        if (candidatBean.getSelectedCandidates() == null || candidatBean.getSelectedCandidates().isEmpty()) {
            printErreur("Pas de candidat sélectionné");
            return;
        }
        CandidatService candidatService = new CandidatService();
        ConceptHelper conceptHelper = new ConceptHelper();
        DcElementHelper dcElmentHelper = new DcElementHelper();          
        // envoie de mail au créateur du candidat si l'option mail est activée
        UserHelper userHelper = new UserHelper();
        
        NodeUser nodeUser;
        
        for (CandidatDto selectedCandidate1 : candidatBean.getSelectedCandidates()) {
            selectedCandidate1 = candidatBean.getAllInfosOfCandidate(selectedCandidate1);
            if (!candidatService.rejectCandidate(connect, selectedCandidate1, adminMessage, idUser)) {
                printErreur("Erreur pour le candidat : " + selectedCandidate1.getNomPref() + "(" + selectedCandidate1.getIdConcepte() + ")");
                return;
            }
            nodeUser = userHelper.getUser(connect.getPoolConnexion(), selectedCandidate1.getCreatedById());
            if(nodeUser.isAlertMail())
                sendMailCandidateRejected(nodeUser.getMail(), selectedCandidate1);    
            conceptHelper.updateDateOfConcept(connect.getPoolConnexion(), selectedCandidate1.getIdThesaurus(),
                    selectedCandidate1.getIdConcepte(), idUser); 
            ///// insert DcTermsData to add contributor
            dcElmentHelper.addDcElementConcept(connect.getPoolConnexion(),
                    new DcElement(DCMIResource.CONTRIBUTOR, currentUser.getNodeUser().getName(), null, null),
                    selectedCandidate1.getIdConcepte(), selectedCandidate1.getIdThesaurus());
            ///////////////               
        }

        printMessage("Candidats insérés avec succès");
        reset(null);
        candidatBean.initCandidatModule();
        candidatBean.getAllCandidatsByThesoAndLangue();
        try {
            candidatBean.setIsListCandidatsActivate(true);
        } catch (IOException ex) {
            Logger.getLogger(ProcessCandidateBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    private boolean sendMailCandidateAccepted(String mail, CandidatDto candidat) {
        if(adminMessage == null) adminMessage = ""; 
        try {
            String subject = "[" + selectedTheso.getThesoName() + "] Confirmation de l'acceptation de votre candidat (" + candidat.getNomPref() + ")";
            String contentFile = "<html><body>"
                    + "Cher(e) " + candidat.getCreatedBy() + ", <br/> "
                    + "<p> Votre candidat a été accepté par nos administrateurs, il est désormais intégré au thésaurus " + selectedTheso.getThesoName() + "<br/></p>"
                    + "Nous vous remercions de votre contribution à l'enrichissement du thésaurus <b>" + selectedTheso.getThesoName() + "</b> "
                    + "(concept : <a href=\"" + getPath() + "/?idc=" + candidat.getIdConcepte()
                    + "&idt=" + candidat.getIdThesaurus() + "\">" + candidat.getNomPref() + "</a>). "
                    
                    + "</b></b>"
                    + "Message de l'administrateur : " + "</b>"
                    + adminMessage
                    + "</b></b>"                    
                    
                    + "<br/><br/> Cordialement,<br/>"
                    + "L'équipe " + selectedTheso.getThesoName() + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            if(!sendEmail(mail, subject, contentFile)) {
                printErreur("!! votre propostion n'a pas été envoyée !!");
                return false;
            }
        } catch (IOException ex) {
            printErreur("Erreur detectée pendant l'envoie du mail de notification! \n votre propostion n'a pas été envoyée !");
            return false;
        }
        return true;
    }
    
    private boolean sendMailCandidateRejected(String mail, CandidatDto candidat) {
        if(adminMessage == null) adminMessage = "";
        try {
            String subject = "[" + selectedTheso.getThesoName() + "] Refus de votre candidat (" + candidat.getNomPref() + ")";
            String contentFile = "<html><body>"
                    + "Cher(e) " + candidat.getCreatedBy() + ", <br/> "
                    + "<p> Votre candidat a été refusé par nos administrateurs, il n'a pas été intégré au thésaurus " + selectedTheso.getThesoName() + "<br/></p>"
                    + "Nous vous remercions de votre contribution à l'enrichissement du thésaurus <b>" + selectedTheso.getThesoName() + "</b> "
                    
                    + "</b></b>"
                    + "Message de l'administrateur : " + "</b>"
                    + adminMessage
                    + "</b></b>"
                   
                    + "L'équipe " + selectedTheso.getThesoName() + ".<br/> <img src=\"" + getPath() + "/resources/img/icon_opentheso2.png\" height=\"106\"></body></html>";

            if(!sendEmail(mail, subject, contentFile)) {
                printErreur("!! votre propostion n'a pas été envoyée !!");
                return false;
            }
        } catch (IOException ex) {
            printErreur("Erreur detectée pendant l'envoie du mail de notification! \n votre propostion n'a pas été envoyée !");
            return false;
        }
        return true;
    }    
    
    private boolean sendEmail(String emailDestination, String subject, String contentFile) throws IOException {
        return mailBean.sendMail(emailDestination, subject,  contentFile);    
    }
    
    private String getPath(){
        if(FacesContext.getCurrentInstance() == null) {
            return "";
        }
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        path = path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        return path;
    }    

    private void printErreur(String message) {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

    private void printMessage(String message) {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "", message);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        PrimeFaces pf = PrimeFaces.current();
        pf.ajax().update("messageIndex");
    }

    public CandidatDto getSelectedCandidate() {
        return selectedCandidate;
    }

    public void setSelectedCandidate(CandidatDto selectedCandidate) {
        this.selectedCandidate = selectedCandidate;
    }

    public String getAdminMessage() {
        return adminMessage;
    }

    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }

}
