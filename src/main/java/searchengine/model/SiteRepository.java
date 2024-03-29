package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    SiteEntity findSiteEntityByUrlEqualsIgnoreCase(String url);

    @Query(value = "SELECT COUNT(*) FROM page WHERE site_id = :siteId", nativeQuery = true)
    int countPagesBySiteId(int siteId);

    @Query(value = "SELECT COUNT(*) FROM lemma WHERE site_id = :siteId", nativeQuery = true)
    int countLemmasBySiteId(int siteId);
}
