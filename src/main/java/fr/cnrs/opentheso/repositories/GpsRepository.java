package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.SessionFactoryMaker;
import fr.cnrs.opentheso.entites.Gps;
import jakarta.persistence.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;


@ApplicationScoped
public class GpsRepository {

    public void removeGps(Gps gps) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery("delete from Gps where id = :idGps");
            query.setParameter("idGps", gps.getId());
            query.executeUpdate();
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void removeGpsByConcept(String idConcept, String idTheso) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery("delete from Gps where idConcept = :idConcept and idTheso = :idTheso");
            query.setParameter("idConcept", idConcept);
            query.setParameter("idTheso", idTheso);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void deleteGps(Gps gps) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            session.save(gps);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement de la release : " + ex.getMessage());
        }
    }


    public void saveNewGps(Gps gps) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            session.save(gps);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement de la release : " + ex.getMessage());
        }
    }

    public List<Gps> getGpsByConceptAndThesorus(String idConcept, String idTheso) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Query rleaseQuery = session.createQuery("SELECT gps FROM Gps gps WHERE gps.idConcept = :idConcept AND gps.idTheso = :idTheso ORDER BY position", Gps.class)
                    .setParameter("idConcept", idConcept)
                    .setParameter("idTheso", idTheso);
            return rleaseQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void updateGpsPosition(Integer idGps, int newPosition) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery("update Gps set position = :newPosition where id = :idGps");
            query.setParameter("newPosition", newPosition);
            query.setParameter("idGps", idGps);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateGps(Gps gps) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Query query = session.createQuery("update Gps set longitude = :newLongitude, latitude = :newLatitude, position = :newPosition where id = :idGps");
            query.setParameter("newLongitude", gps.getLongitude());
            query.setParameter("newLatitude", gps.getLatitude());
            query.setParameter("newPosition", gps.getPosition());
            query.setParameter("idGps", gps.getId());
            query.executeUpdate();
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
