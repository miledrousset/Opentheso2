package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.SessionFactoryMaker;
import fr.cnrs.opentheso.entites.Thesaurus;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;


@ApplicationScoped
public class ThesaurusRepository {


    public List<Thesaurus> getThesaurusByProjectAndStatus(int idProject, boolean isPrivate) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            TypedQuery<Thesaurus> typedQuery = session.createQuery("SELECT the "
                                    + "FROM Thesaurus the, UserGroupThesaurus group "
                                    + "WHERE the.privateTheso = :isPrivate "
                                    + "AND group.idGroup = :idProject "
                                    + "AND group.idThesaurus = the.thesaurusId",
                            Thesaurus.class)
                    .setParameter("isPrivate", isPrivate)
                    .setParameter("idProject", idProject);
            return typedQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


    public List<Thesaurus> getThesaurusByProject(int idProject) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            TypedQuery<Thesaurus> typedQuery = session.createQuery("SELECT the "
                                    + "FROM Thesaurus the, UserGroupThesaurus group "
                                    + "WHERE group.idGroup = :idProject "
                                    + "AND group.idThesaurus = the.thesaurusId",
                            Thesaurus.class)
                    .setParameter("idProject", idProject);
            return typedQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
