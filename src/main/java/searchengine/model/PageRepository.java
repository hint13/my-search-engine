package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {

    @Query(value = "SELECT * FROM page WHERE site_id = :siteId", nativeQuery = true)
    List<PageEntity> findAllBySiteId(int siteId);

    //@Query(value = "SELECT * FROM page WHERE site_id = :siteId AND 'path' = :path", nativeQuery = true)
    Optional<PageEntity> findPageEntityBySiteIdAndPath(Integer siteId, String path);

    PageEntity findPageEntityBySiteAndPath(SiteEntity site, String path);

    @Query(value = "SELECT `content` FROM page WHERE `site_id` = :siteId AND `path` = :url", nativeQuery = true)
    Optional<String> getContentForPage(int siteId, String url);
}
