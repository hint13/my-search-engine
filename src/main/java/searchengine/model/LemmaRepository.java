package searchengine.model;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

    //@Query(value = "SELECT * FROM lemma WHERE site_id = :siteId LIMIT 1", nativeQuery = true)
    LemmaEntity findFirstLemmaEntityBySiteIdAndLemma(Integer siteId, String lemmaValue);

//    @Query(value = "SELECT * FROM lemma WHERE site_id = :siteId", nativeQuery = true)
    List<LemmaEntity> findAllLemmaEntitiesBySiteId(int siteId);

    @Modifying
    @Query(value = "UPDATE lemma l SET l.frequency = l.frequency - 1 WHERE l.site_id = :siteId" +
            " AND l.id IN (SELECT i.lemma_id FROM `index` i WHERE i.page_id = :pageId);", nativeQuery = true)
    void updateLemmasCountByPageIdForSiteId(Integer pageId, Integer siteId);

    @Modifying
    @Query(value = "DELETE FROM lemma WHERE site_id = :siteId AND frequency <= 0;", nativeQuery = true)
    void clearDummyLemmasForSiteId(Integer siteId);
}
