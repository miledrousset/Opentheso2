package fr.cnrs.opentheso.bean.candidat;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.AlignmentHelper;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.ImagesHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeCandidateOld;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeProposition;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeTraductionCandidat;
import fr.cnrs.opentheso.bean.candidat.dao.CandidatDao;
import fr.cnrs.opentheso.bean.candidat.dao.DomaineDao;
import fr.cnrs.opentheso.bean.candidat.dao.TermeDao;
import fr.cnrs.opentheso.bean.candidat.dao.MessageDao;
import fr.cnrs.opentheso.bean.candidat.dao.NoteDao;
import fr.cnrs.opentheso.bean.candidat.dao.RelationDao;
import fr.cnrs.opentheso.bean.candidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.candidat.dto.DomaineDto;
import fr.cnrs.opentheso.bean.candidat.dto.TraductionDto;
import fr.cnrs.opentheso.bean.candidat.enumeration.VoteType;
import fr.cnrs.opentheso.bean.menu.connect.Connect;

import java.io.Serializable;
import java.sql.Connection;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


@Named(value = "candidatService")
@SessionScoped
public class CandidatService implements Serializable {

    private final Log LOG = LogFactory.getLog(CandidatService.class);

    public List<CandidatDto> getAllCandidats(Connect connect, String idThesaurus, String lang) {
        List<CandidatDto> temps = new ArrayList<>();
        HikariDataSource connection = connect.getPoolConnexion();
        try {
            CandidatDao condidatDao = new CandidatDao();
            temps = condidatDao.searchAllCondidats(connection, idThesaurus, lang);
            temps.forEach(candidatDto -> {
                try {
                    candidatDto.setStatut(condidatDao.searchCondidatStatus(connection, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrParticipant(condidatDao.searchParticipantCount(connection, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrDemande(condidatDao.searchDemandeCount(connection, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrVote(condidatDao.searchVoteCount(connection, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus(), VoteType.CANDIDAT.getLabel()));
                } catch (SQLException ex) {
                    Logger.getLogger(CandidatService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            connection.close();
        } catch (SQLException sqle) {
            LOG.error(sqle);
            System.err.println("Error >>> " + sqle);
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        return temps;
    }

    /**
     * permet de récupérer la liste des candidats qui sont en attente
     * @param connect
     * @param idThesaurus
     * @param lang
     * @param etat
     * @param statut
     * @return 
     */
    public List<CandidatDto> getCandidatsByStatus(Connect connect, String idThesaurus, String lang, int etat, String statut) {
        List<CandidatDto> candidatList = new ArrayList<>();
        HikariDataSource connection = connect.getPoolConnexion();
        try {
            CandidatDao condidatDao = new CandidatDao();
            candidatList = condidatDao.getCandidatsByStatus(connection, idThesaurus, lang, etat, statut);
            candidatList.forEach(candidatDto -> {
                try {
                    candidatDto.setNbrParticipant(condidatDao.searchParticipantCount(connection, candidatDto.getIdConcepte(),
                            idThesaurus));
                    candidatDto.setNbrDemande(condidatDao.searchDemandeCount(connection, candidatDto.getIdConcepte(),
                            idThesaurus));
                    candidatDto.setNbrVote(condidatDao.searchVoteCount(connection, candidatDto.getIdConcepte(),
                            idThesaurus, VoteType.CANDIDAT.getLabel()));
                    candidatDto.setNbrNoteVote(condidatDao.searchVoteCount(connection, candidatDto.getIdConcepte(),
                            idThesaurus, VoteType.NOTE.getLabel()));
                    candidatDto.setAlignments(new AlignmentHelper().getAllAlignmentOfConcept(connection, candidatDto.getIdConcepte(), idThesaurus));
                } catch (SQLException ex) {
                    Logger.getLogger(CandidatService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            connection.close();
        } catch (SQLException sqle) {
            LOG.error(sqle);
            System.err.println("Error >>> " + sqle);
            if (!connection.isClosed()) {
                connection.close();
            }
        }
        return candidatList;
    }    
    
    /**
     * permet de chercher des candidats
     * @param connect
     * @param value
     * @param idThesaurus
     * @param lang
     * @param etat
     * @param statut
     * @return 
     * #MR
     */
    public List<CandidatDto> searchCandidats(Connect connect, String value,
            String idThesaurus, String lang, int etat, String statut) {
        List<CandidatDto> temps = new ArrayList<>();
        HikariDataSource ds = connect.getPoolConnexion();
        try {
            CandidatDao condidatDao = new CandidatDao();
            temps = condidatDao.searchCandidatsByValue(ds, value, idThesaurus, lang, etat, statut);
            temps.forEach(candidatDto -> {
                try {
                    candidatDto.setStatut(condidatDao.searchCondidatStatus(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrParticipant(condidatDao.searchParticipantCount(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrDemande(condidatDao.searchDemandeCount(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus()));
                    candidatDto.setNbrVote(condidatDao.searchVoteCount(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus(), VoteType.CANDIDAT.getLabel()));
                    candidatDto.setNbrNoteVote(condidatDao.searchVoteCount(ds, candidatDto.getIdConcepte(),
                            candidatDto.getIdThesaurus(), VoteType.NOTE.getLabel()));
                } catch (SQLException ex) {
                    Logger.getLogger(CandidatService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            ds.close();
        } catch (SQLException sqle) {
            LOG.error(sqle);
            System.err.println("Error >>> " + sqle);
            if (!ds.isClosed()) {
                ds.close();
            }
        }
        return temps;
    }      
    
    public String getCandidatID(Connect connect) {
        int id = 0;
        HikariDataSource connection = connect.getPoolConnexion();
        try {
            id = new CandidatDao().getMaxCandidatId(connection);
        } catch (Exception e) {
            LOG.error(e);
        }
        connection.close();
        return (id + 1) + "";
    }

    public String saveNewCondidat(Connect connect, Concept concept, ConceptHelper conceptHelper)
            throws SQLException {

        Connection conn = null;
        String idNewCondidat = null;
        try {
            conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            idNewCondidat = conceptHelper.addConceptInTable(conn, concept, concept.getIdUser());
            if (idNewCondidat == null) {
                conn.rollback();
                conn.close();
                return null;
            }
            conn.commit();
            conn.close();
        } catch (SQLException ex) {
            try {
                Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
            }
        }            

        new CandidatDao().setStatutForCandidat(connect.getPoolConnexion(), 1, idNewCondidat, concept.getIdThesaurus(),
                concept.getIdUser() + "");
        return idNewCondidat;
    }

    public String saveNewTerm(Connect connect, Term term,
            String idConcept, int idUser) throws SQLException {
        Connection conn = null;
        String idTerm = null;
        try {
            conn = connect.getPoolConnexion().getConnection();
            conn.setAutoCommit(false);
            idTerm = new TermHelper().addTerm(connect.getPoolConnexion().getConnection(), term,
                idConcept, idUser);
            if (idTerm == null) {
                conn.rollback();
                conn.close();
                return null;
            }   
            conn.commit();
            conn.close();            
        } catch (SQLException ex) {
            try {
                Logger.getLogger(ConceptHelper.class.getName()).log(Level.SEVERE, null, ex);
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex1) {
            }
        }  
        return idTerm;
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus, String lang, String idTerm) throws SQLException {
        new TermeDao().updateIntitule(connect.getPoolConnexion(),
                intitule, idTerm, idThesaurus, lang);
    }

    public void updateDetailsCondidat(Connect connect, CandidatDto candidatSelected, int idUser) throws SQLException {

        //update domaine
        new DomaineDao().deleteAllDomaine(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte());
        
        for (NodeIdValue collection : candidatSelected.getCollections()) {
            new DomaineDao().addNewDomaine(
                    connect, collection.getId(),
                    candidatSelected.getIdThesaurus(),
                    candidatSelected.getIdConcepte());
        }

        // gestion des relations
        RelationDao relationDao = new RelationDao();
        TermeDao termeDao = new TermeDao();

        relationDao.deleteAllRelations(connect, candidatSelected.getIdConcepte(),
                candidatSelected.getIdThesaurus(), idUser);

        //update terme générique
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesGenerique())) {
            candidatSelected.getTermesGenerique().stream().forEach(nodeBT -> {
                relationDao.addRelationBT(connect,
                        candidatSelected.getIdConcepte(),
                        nodeBT.getId(),
                        candidatSelected.getIdThesaurus());
            });
        }

        //update terme associés
        if (!CollectionUtils.isEmpty(candidatSelected.getTermesAssocies())) {
            candidatSelected.getTermesAssocies().stream().forEach(nodeRT -> {
                relationDao.addRelationRT(connect,
                        candidatSelected.getIdConcepte(),
                        nodeRT.getId(),
                        candidatSelected.getIdThesaurus());
            });
        }

        // Employé pour
        termeDao.deleteEMByIdTermAndLang(connect.getPoolConnexion(), candidatSelected.getIdTerm(),
                candidatSelected.getIdThesaurus(), candidatSelected.getLang());
        
        if(!candidatSelected.getEmployePourList().isEmpty()) {
            candidatSelected.getEmployePourList().stream().forEach(employe -> {
                try {
                    termeDao.addNewEmployePour(connect,
                            employe, candidatSelected.getIdThesaurus(),
                            candidatSelected.getLang(),
                            candidatSelected.getIdTerm());
                } catch (SQLException ex) {
                    Logger.getLogger(CandidatService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }
    
    public List<DomaineDto> getDomainesList(Connect connect, String idThesaurus, String lang) {
        return new DomaineDao().getAllDomaines(connect.getPoolConnexion(), idThesaurus, lang);
    }

    public void getCandidatDetails(Connect connect, CandidatDto candidatSelected, String idThesaurus) {

        try (HikariDataSource connection = connect.getPoolConnexion()){
            candidatSelected.setIdTerm(new TermHelper().getIdTermOfConcept(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));
            
            candidatSelected.setCollections(new DomaineDao().getDomaineCandidatByConceptAndThesaurusAndLang(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), candidatSelected.getLang()));

            candidatSelected.setTermesGenerique(new RelationDao().getCandidatRelationsBT(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            candidatSelected.setTermesAssocies(new RelationDao().getCandidatRelationsRT(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));

            candidatSelected.setEmployePourList(new TermeDao().getEmployePour(connection,
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(),
                    candidatSelected.getLang()));
             
            candidatSelected.setNodeNotes(new NoteDao().getNotesCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus()));

            candidatSelected.getNodeNotes().forEach(note -> {
                try {
                    note.setVoted(getVote(connect, candidatSelected.getIdThesaurus(), candidatSelected.getIdConcepte(),
                        candidatSelected.getUserId(), note.getId_note()+"", VoteType.NOTE));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            candidatSelected.setTraductions(new TermHelper().getTraductionsOfConcept(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getLang()).stream().map(
                    term -> new TraductionDto(term.getLang(),
                            term.getLexicalValue(), term.getCodePays())).collect(Collectors.toList()));

            candidatSelected.setMessages(new MessageDao().getAllMessagesByCandidat(connection, candidatSelected.getIdConcepte(),
                    candidatSelected.getIdThesaurus(), candidatSelected.getUserId()));

            candidatSelected.setVoted(new CandidatDao().getVote(connection, candidatSelected.getUserId(),
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus(), null, 
                    VoteType.CANDIDAT.getLabel()));

            candidatSelected.setAlignments(new AlignmentHelper().getAllAlignmentOfConcept(connect.getPoolConnexion(),
                    candidatSelected.getIdConcepte(), idThesaurus));

            candidatSelected.setImages(new ImagesHelper().getExternalImages(connect.getPoolConnexion(),
                    candidatSelected.getIdConcepte(), candidatSelected.getIdThesaurus()));

            connect.getPoolConnexion().getConnection().close();
        } catch (SQLException e) {
            LOG.error(e);
        }
    }


///////// ajouté par Miled
    public void addVote(Connect connect, String idThesaurus, String idConcept, int idUser,
            String idNote, VoteType voteType) throws SQLException {
        new CandidatDao().addVote(connect.getPoolConnexion(), idThesaurus, idConcept, idUser, idNote, 
                voteType.getLabel());
    }

    public boolean getVote(Connect connect, String idThesaurus, String idConcept, int idUser, String idNote,
            VoteType voteType) throws SQLException {
        return new CandidatDao().getVote(connect.getPoolConnexion(), idUser, idConcept, idThesaurus, 
                idNote, voteType.getLabel());
    }

    public void removeVote(Connect connect, String idThesaurus, String idConcept, int idUser, String idNote,
            VoteType voteType) throws SQLException {
        new CandidatDao().removeVote(connect.getPoolConnexion(), idThesaurus, idConcept, idUser, idNote,
                voteType.getLabel());
    }
    
    public boolean insertCandidate(Connect connect, CandidatDto candidatDto, String adminMessage, int idUser) {
        return new CandidatDao().insertCandidate(connect.getPoolConnexion(), candidatDto, adminMessage, idUser);
    }
    
    public boolean rejectCandidate(Connect connect, CandidatDto candidatDto, String adminMessage, int idUser) {
        return new CandidatDao().rejectCandidate(connect.getPoolConnexion(), candidatDto, adminMessage, idUser);
    }     
    
    /**
     * permet de récupérer les anciens candidats saisies dans l'ancien module 
     * uniquement les candidats qui étatient en attente
     * @param connect
     * @param idTheso
     * @param idUser
     * @param nodePreference
     * @return 
     */    
    public String getOldCandidates(Connect connect, String idTheso, int idUser,
            NodePreference nodePreference) {
        CandidatDao candidatDao = new CandidatDao();
        StringBuilder messages = new StringBuilder();
        
        //// récupération des anciens candidats
        ArrayList<NodeCandidateOld> nodeCandidateOlds;
        try {
            nodeCandidateOlds = candidatDao.getCandidatesIdFromOldModule(connect.getPoolConnexion(), idTheso);
        } catch (SQLException e) {
            messages.append("Erreur : ").append(e.getMessage());
            return messages.toString();
        }
        if(nodeCandidateOlds == null || nodeCandidateOlds.isEmpty()) {
            return "Pas d'anciens candidats à récupérer";
        }
        
        for (NodeCandidateOld nodeCandidateOld : nodeCandidateOlds) {
            try {
                nodeCandidateOld.setNodeTraductions(
                    candidatDao.getCandidatesTraductionsFromOldModule(
                            connect.getPoolConnexion(), nodeCandidateOld.getIdCandidate(), idTheso));
                nodeCandidateOld.setNodePropositions(
                    candidatDao.getCandidatesMessagesFromOldModule(
                            connect.getPoolConnexion(), nodeCandidateOld.getIdCandidate(), idTheso));
            } catch (SQLException e) {
                messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                System.out.println(messages.toString());
            }
        }
        
        //// ajout des anciens candidats au nouveau module
        ConceptHelper conceptHelper = new ConceptHelper();
        TermHelper termHelper = new TermHelper();
        boolean exist = false;
        boolean first = true;
        Concept concept = new Concept();
        String idNewConcept = null;
        String idNewTerm = null;
        Term terme = new Term();
        
        for (NodeCandidateOld nodeCandidateOld : nodeCandidateOlds) {
            // ajout du candidat s'il n'existe pas dans le thésaurus en vérifiant langue par langue
            for (NodeTraductionCandidat nodeTraduction : nodeCandidateOld.getNodeTraductions()) {
                // en cas d'un nouveau candidat, verification dans les prefLabels
                if (termHelper.isPrefLabelExist(connect.getPoolConnexion(),
                        nodeTraduction.getTitle().trim(),
                        idTheso,
                        nodeTraduction.getIdLang())) {
                    messages.append("Candidat existe : ").append(nodeTraduction.getTitle());
                    System.out.println("Candidat existe : " + nodeTraduction.getTitle());
                    exist = true;
                    break;
                }
            }
            // si le candidat n'existe pas dans toutes les langues, on l'ajoute
            if(!exist) {
                concept.setIdConcept(null);
                concept.setIdThesaurus(idTheso);
                concept.setTopConcept(false);
                concept.setIdUser(idUser);
               // concept.setUserName(currentUser.getUsername());
                concept.setStatus("CA");

                conceptHelper.setNodePreference(nodePreference);

                try {
                    idNewConcept = saveNewCondidat(connect, concept, conceptHelper);
                } catch (SQLException e) {
                    messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                    System.out.println(messages.toString());
                }
                if (idNewConcept == null) {
                    messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                    System.out.println(messages.toString());                    
                    continue;
                }

                for (NodeTraductionCandidat nodeTraduction : nodeCandidateOld.getNodeTraductions()) {
                    if(first) {
                        terme.setId_thesaurus(idTheso);
                        terme.setLang(nodeTraduction.getIdLang());
                        terme.setContributor(idUser);
                        terme.setLexical_value(nodeTraduction.getTitle().trim());
                        terme.setSource("candidat");
                        terme.setStatus("D");
                        try {
                            idNewTerm = saveNewTerm(connect, terme, idNewConcept, idUser);                            
                        } catch (SQLException e) {
                            messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                            System.out.println(messages.toString());                            
                            continue;                            
                        }
                        first = false;
                    } else {
                        // ajout des traductions
                        if(!termHelper.addTraduction(connect.getPoolConnexion(),
                                nodeTraduction.getTitle(),
                                idNewTerm,
                                nodeTraduction.getIdLang(),
                                "candidat",
                                "D",
                                idTheso, idUser)) {
                            messages.append("Erreur : ").append(nodeCandidateOld.getIdCandidate());
                            System.out.println(messages.toString());                            
                        }
                    }
                }
                first = true;
                // ajout des messages  
                for (NodeProposition nodeProposition : nodeCandidateOld.getNodePropositions()) {
                            MessageDao messageDao = new MessageDao();
                    messageDao.addNewMessage(connect.getPoolConnexion(), 
                            nodeProposition.getNote(),
                            nodeProposition.getId_user(),
                            idNewConcept,
                            idTheso);
                }
                
                idNewConcept = null;
                idNewTerm = null;                
            }
            exist = false;
        }

        return "Import réussi\n" + messages.toString();
    }
    
    
    

}
