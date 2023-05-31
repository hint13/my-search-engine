package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {

    @Query(value = "SELECT * FROM `index` WHERE page_id = :pageId", nativeQuery = true)
    List<IndexEntity> findAllByPageId(int pageId);
}
