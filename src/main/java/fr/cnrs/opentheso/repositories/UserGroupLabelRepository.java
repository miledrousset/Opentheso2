package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.SessionFactoryMaker;
import fr.cnrs.opentheso.entites.UserGroupLabel;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;


@ApplicationScoped
public class UserGroupLabelRepository {


    public List<UserGroupLabel> getAllProjects() {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Query releaseQuery = session.createQuery("SELECT label FROM UserGroupLabel label ORDER BY lower(label.label) ASC", UserGroupLabel.class);
            return releaseQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<UserGroupLabel> getProjectsByThesoStatus(boolean status) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Query releaseQuery = session.createQuery("SELECT label "
                    + "FROM Thesaurus the, UserGroupThesaurus grp, UserGroupLabel label "
                    + "WHERE the.privateTheso = :status "
                    + "AND grp.idThesaurus = the.thesaurusId "
                    + "AND label.id = grp.idGroup "
                    + "ORDER BY lower(label.label) ASC", UserGroupLabel.class)
                    .setParameter("status", status);
            return releaseQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<UserGroupLabel> getProjectsByUserId(int userId) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            TypedQuery<UserGroupLabel> typedQuery = session.createQuery("SELECT lab "
                            + "FROM UserRoleGroup grp, UserGroupLabel lab "
                            + "WHERE grp.idGroup = lab.id "
                            + "AND grp.idUser = :userId "
                            + "ORDER BY lower(lab.label) ASC", UserGroupLabel.class)
                    .setParameter("userId", userId);
            return typedQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
