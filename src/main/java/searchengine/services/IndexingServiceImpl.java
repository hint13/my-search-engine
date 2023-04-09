package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import searchengine.config.Bot;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseError;
import searchengine.model.*;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class IndexingServiceImpl implements IndexingService {
    @Autowired private SiteRepository sites;
    @Autowired private PageRepository pages;
    @Autowired private SitesList sitesConfig;
    @Autowired private Bot botConfig;

    private static final Logger log = LogManager.getLogger();
    private final ExecutorService executor;
    private final List<Future<Integer>> siteIndexers;
    private final AtomicBoolean isIndexing;

    public IndexingServiceImpl(SiteRepository sites, PageRepository pages, SitesList sitesConfig, Bot botConfig) {
        isIndexing = new AtomicBoolean(false);
        siteIndexers = new LinkedList<>();
        executor = Executors.newWorkStealingPool(sitesConfig.getSites().size());
    }

    private void start() {
        isIndexing.set(true);
        for (Site site : sitesConfig.getSites()) {
            SiteEntity siteEntity = insertOrGetSite(site);
            deletePagesBySiteId(siteEntity);
            siteIndexers.add((Future<Integer>) executor.submit(new SiteIndexer(siteEntity, pages, botConfig)));
        }
    }

    private SiteEntity insertOrGetSite(Site site) {
        Optional<SiteEntity> entity = sites.findByUrlIgnoreCase(site.getUrl());
        SiteEntity siteEntity = entity.orElseGet(SiteEntity::new);
        siteEntity.setUrl(site.getUrl());
        siteEntity.setName(site.getName());
        siteEntity.setStatus(SiteStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setLastError("");
        sites.save(siteEntity);
        sites.flush();
        return siteEntity;
    }

    private void deletePagesBySiteId(SiteEntity site) {
        List<PageEntity> pageEntityList = pages.findAllBySiteId(site.getId());
        if (!pageEntityList.isEmpty()) {
            log.info("delete Pages by siteId: " + site.getId() + "(" + pageEntityList.size() + ")");
            pages.deleteAllInBatch(pageEntityList);
            pages.flush();
        }
    }

    @Override
    public IndexingResponse startIndexing() {
        IndexingResponse response = !isIndexing.get() ? new IndexingResponse() : new IndexingResponseError(true);
        if (!isIndexing.get()) {
            start();
        }
        return response;
    }

    private void stop() {
        isIndexing.set(false);
        executor.shutdown();
        siteIndexers.clear();
    }

    @Override
    public IndexingResponse stopIndexing() {
        IndexingResponse response = isIndexing.get() ? new IndexingResponse() : new IndexingResponseError(false);
        if (isIndexing.get()) {
            stop();
        }
        return response;
    }

}
