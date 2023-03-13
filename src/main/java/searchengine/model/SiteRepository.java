package searchengine.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteRepository extends JpaRepository<Site, Integer> {

    Optional<Site> findByUrlIgnoreCase(String url);

    @Override
    long count();

    @Override
    Site save(Site entity);
}
