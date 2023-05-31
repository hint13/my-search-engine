package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {

    @Query(value = "SELECT * FROM lemma WHERE site_id = :siteId AND lemma = :lemmaValue", nativeQuery = true)
    Optional<LemmaEntity> findLemmaEntityBySiteIdAndLemma(Integer siteId, String lemmaValue);

    @Query(value = "SELECT * FROM lemma WHERE site_id = :siteId", nativeQuery = true)
    List<LemmaEntity> findAllBySiteId(int siteId);
}
