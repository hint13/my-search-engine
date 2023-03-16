package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {

    Optional<SiteEntity> findByUrlIgnoreCase(String url);

    @Override
    long count();

    @Override
    SiteEntity save(SiteEntity entity);
}
