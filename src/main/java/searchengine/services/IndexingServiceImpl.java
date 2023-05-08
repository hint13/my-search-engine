package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import searchengine.config.Bot;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.*;
import searchengine.model.*;
import searchengine.services.indexing.SiteIndexer;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class IndexingServiceImpl implements IndexingService {
    private static final Logger log = LogManager.getLogger(IndexingServiceImpl.class);

    private final SiteRepository sites;
    private final PageRepository pages;
    private final SitesList sitesConfig;
    private final Bot botConfig;

    private final List<Thread> siteIndexers;

    @Autowired
    public IndexingServiceImpl(SiteRepository sites, PageRepository pages, @NotNull SitesList sitesConfig, Bot botConfig) {
        this.sites = sites;
        this.pages = pages;
        this.sitesConfig = sitesConfig;
        this.botConfig = botConfig;
        this.siteIndexers = new LinkedList<>();

    }

    private boolean checkIndexingStatus() {
        AtomicBoolean indexing = new AtomicBoolean(false);
        siteIndexers.forEach(i -> indexing.compareAndSet(false, ((SiteIndexer)i).isIndexing()));
        log.debug("Global indexing status: " + indexing.get());
        return indexing.get();
    }

    @Override
    public IndexingResponse startIndexing() {
        // FIXME: potential bad code?
        boolean isIndexing = checkIndexingStatus();
        IndexingResponse response = !isIndexing ? new IndexingResponse() : new IndexingResponseError(true);
        if (!isIndexing) {
            log.debug("Start indexing process.");
            startIndexingProcess();
        }
        return response;
    }

    private void startIndexingProcess() {
        for (Site site : sitesConfig.getSites()) {
            SiteEntity siteEntity = insertOrGetSite(site);
            deletePagesBySiteId(siteEntity);
            SiteIndexer siteIndexer = new SiteIndexer(siteEntity, sites, pages, botConfig);
            siteIndexers.add(siteIndexer);
            siteIndexer.start();
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
        sites.saveAndFlush(siteEntity);
        return siteEntity;
    }

    private void deletePagesBySiteId(SiteEntity site) {
        List<PageEntity> pageEntityList = pages.findAllBySiteId(site.getId());
        if (!pageEntityList.isEmpty()) {
            log.debug("delete Pages by siteId: " + site.getId() + "(" + pageEntityList.size() + ")");
            pages.deleteAllInBatch(pageEntityList);
            pages.flush();
        }
    }

    @Override
    public IndexingResponse stopIndexing() {
        // FIXME: potential bad code?
        boolean isIndexing = checkIndexingStatus();
        IndexingResponse response = isIndexing ? new IndexingResponse() : new IndexingResponseError(false);
        if (isIndexing) {
            log.debug("Stop indexing process.");
            stopIndexingProcess();
        }
        return response;
    }

    private void stopIndexingProcess() {
        for (Thread task: siteIndexers) {
            task.interrupt();
        }
        siteIndexers.clear();
    }
}
