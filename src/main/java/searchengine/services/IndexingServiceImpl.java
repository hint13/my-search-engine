package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseError;
import searchengine.model.SiteRepository;
import searchengine.model.SiteStatus;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class IndexingServiceImpl implements IndexingService {
    @Autowired private SiteRepository sites;
    @Autowired private SitesList sitesList;

    private final List<Thread> siteIndexers;
    private final AtomicBoolean isIndexing;

    public IndexingServiceImpl(SiteRepository sites, SitesList sitesList) {
        isIndexing = new AtomicBoolean(false);
        siteIndexers = new LinkedList<>();

    }

    public void startIndexing() {
        isIndexing.set(true);
        for (Site site : sitesList.getSites()) {
            searchengine.model.Site siteEntity = insertOrGetSite(site);
            SiteIndexer siteIndexer = new SiteIndexer(siteEntity);
            siteIndexers.add(new Thread(siteIndexer));
        }
        for (Thread indexer : siteIndexers) {
            indexer.start();
        }
    }

    private searchengine.model.Site insertOrGetSite(Site site) {
        Optional<searchengine.model.Site> entity = sites.findByUrlIgnoreCase(site.getUrl());
        if (entity.isPresent()) {
            return entity.get();
        }
        searchengine.model.Site siteEntity = new searchengine.model.Site();
        siteEntity.setUrl(site.getUrl());
        siteEntity.setName(site.getName());
        siteEntity.setStatus(SiteStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setLastError("");
        sites.save(siteEntity);
        return siteEntity;
    }

    public void stopIndexing() {
        isIndexing.set(false);
        for (Thread indexer : siteIndexers) {
            if (indexer.isAlive()) {
                indexer.interrupt();
            }
        }
    }

    @Override
    public IndexingResponse start() {
        IndexingResponse response = !isIndexing.get() ? new IndexingResponse() : new IndexingResponseError(true);
        if (!isIndexing.get()) {
            startIndexing();
        }
        return response;
    }

    @Override
    public IndexingResponse stop() {
        IndexingResponse response = isIndexing.get() ? new IndexingResponse() : new IndexingResponseError(false);
        if (isIndexing.get()) {
            stopIndexing();
        }
        return response;
    }

}
