package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    @Query(value = "SELECT * FROM page WHERE site_id = :siteId", nativeQuery = true)
    List<PageEntity> findAllBySiteId(int siteId);

}
