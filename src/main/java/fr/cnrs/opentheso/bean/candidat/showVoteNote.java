/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.candidat;

import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTabVote;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeVote;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.candidat.dao.CandidatDao;
import fr.cnrs.opentheso.bean.candidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import java.io.Serializable;
import java.util.ArrayList;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "showVoteNote")
@SessionScoped
public class showVoteNote implements Serializable {
    @Inject private Connect connect;
    @Inject private SelectedTheso selectedTheso;

    private String userName;
    private String candidat;
    private ArrayList<NodeTabVote> nodeTabVotes;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){

    }
    
    public showVoteNote() {
    }

    public void reset() {

    }
    
    /**
     * permet de préparer la vue pour les votes sur les notes pour un candidat
     * 
     * @param selectedCandidate
     */
    public void prepareVoteNote(CandidatDto selectedCandidate){
        String idCandidate = selectedCandidate.getIdConcepte();
        nodeTabVotes = new ArrayList<>();
        
        UserHelper userHelper = new UserHelper();
        NoteHelper noteHelper = new NoteHelper();
        NodeNote nodeNote;
        
 //       int createdBy = selectedCandidate.getCreatedById();
 //       userName = userHelper.getNameUser(connect.getPoolConnexion(), createdBy);
        userName = selectedCandidate.getCreatedBy();
        candidat = selectedCandidate.getNomPref();
        
        CandidatDao candidatDao = new CandidatDao();
        ArrayList<NodeVote> nodeVotes = candidatDao.getAllVoteNotes(connect.getPoolConnexion(), idCandidate, selectedTheso.getCurrentIdTheso());
        
        for (NodeVote nodeVote : nodeVotes) {
            NodeTabVote nodeTabVote = new NodeTabVote();
            nodeTabVote.setIdUser(nodeVote.getIdUser());
            nodeTabVote.setUserName(userHelper.getNameUser(connect.getPoolConnexion(), nodeVote.getIdUser()));
            
            /// pour récupérer les notes
            try {
                nodeNote = noteHelper.getNoteByIdNote(connect.getPoolConnexion(), Integer.parseInt(nodeVote.getIdNote()));
                nodeTabVote.setTypeNote(nodeNote.getNotetypecode());
                nodeTabVote.setNoteValue(nodeNote.getLexicalvalue());
            } catch (Exception e) {
            }
            nodeTabVotes.add(nodeTabVote);
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCandidat() {
        return candidat;
    }

    public void setCandidat(String candidat) {
        this.candidat = candidat;
    }

    public ArrayList<NodeTabVote> getNodeTabVotes() {
        return nodeTabVotes;
    }

    public void setNodeTabVotes(ArrayList<NodeTabVote> nodeTabVotes) {
        this.nodeTabVotes = nodeTabVotes;
    }
 
    
}
