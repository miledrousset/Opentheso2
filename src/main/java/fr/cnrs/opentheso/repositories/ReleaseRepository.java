package fr.cnrs.opentheso.repositories;

import fr.cnrs.opentheso.SessionFactoryMaker;
import fr.cnrs.opentheso.bean.notification.dto.ReleaseDto;
import fr.cnrs.opentheso.entites.Release;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;


@ApplicationScoped
public class ReleaseRepository {


    public Release toRelease(ReleaseDto releaseDto) {
        Release release = new Release();
        release.setVersion(releaseDto.getTag_name());
        release.setUrl(releaseDto.getHtml_url());
        release.setDate(toLocalDate(releaseDto.getPublished_at()));
        release.setDescription(releaseDto.getBody());
        return release;
    }

    private LocalDate toLocalDate(String date) {
        if (StringUtils.isNotEmpty(date)) {
            Instant instant = Instant.parse(date);
            return instant.atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            return null;
        }
    }

    public void saveRelease(Release release) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            session.save(release);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Erreur lors de l'enregistrement de la release : " + ex.getMessage());
        }
    }

    public void saveRelease(List<Release> releases) {
        for (Release release : releases) {
            if (ObjectUtils.isEmpty(getReleaseByVersion(release.getVersion()))) {
                saveRelease(release);
            }
        }
    }

    public List<Release> getAllReleases() {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            Query rleaseQuery = session.createQuery("SELECT res FROM Release res", Release.class);
            return rleaseQuery.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Release getReleaseByVersion(String version) {
        try (Session session = SessionFactoryMaker.getFactory().openSession()) {
            String query = "SELECT res FROM Release res WHERE res.version = :value";
            TypedQuery<Release> typedQuery = session.createQuery(query, Release.class)
                    .setParameter("value", version)
                    .setMaxResults(1);
            Release release = typedQuery.getSingleResult();
            return release;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
