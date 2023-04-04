package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    @Autowired private SitesList sitesList;

    private static final Logger log = LogManager.getLogger();
    private final List<FutureTask<SiteEntity>> siteIndexers;
    private final Executor executor;
    private final AtomicBoolean isIndexing;

    public IndexingServiceImpl(SiteRepository sites, PageRepository pages, SitesList sitesList) {
        isIndexing = new AtomicBoolean(false);
        siteIndexers = new LinkedList<>();
        executor = Executors.newFixedThreadPool(sitesList.getSites().size());
    }

    private void startIndexing() {
        isIndexing.set(true);
        for (Site site : sitesList.getSites()) {
            SiteEntity siteEntity = insertOrGetSite(site);
            deletePagesBySiteId(siteEntity);
            FutureTask<SiteEntity> siteIndexer = new FutureTask<>(new SiteIndexer(siteEntity, pages));
            siteIndexers.add(siteIndexer);
            executor.execute(siteIndexer);
        }
//        while (true) {
//            AtomicBoolean isAllDone = new AtomicBoolean(false);
//            siteIndexers.forEach(s -> isAllDone.set(isAllDone.get() & s.isDone()));
//            if (isAllDone.get()) {
//                for (FutureTask<SiteEntity> indexer : siteIndexers) {
//                    SiteEntity site;
//                    try {
//                        site = indexer.get();
//                        sites.saveAndFlush(site);
//                    } catch (InterruptedException e) {
//                        log.error("Indexer interrupted: " + e.getMessage());
//                    } catch (ExecutionException e) {
//                        log.error("Error indexer execution: " + e.getMessage());
//                    }
//                }
//                ((ThreadPoolExecutor)executor).shutdown();
//                log.info("All indexing is done.");
//                return;
//            }
//        }
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
    public IndexingResponse start() {
        IndexingResponse response = !isIndexing.get() ? new IndexingResponse() : new IndexingResponseError(true);
        if (!isIndexing.get()) {
            startIndexing();
        }
        return response;
    }

    private void stopIndexing() {
        isIndexing.set(false);
        // for (Thread indexer : siteIndexers) {
        //     if (indexer.isAlive()) {
        //         log.info(indexer.getName());
        //         indexer.interrupt();
        //     }
        // }
        ((ThreadPoolExecutor)executor).shutdown();
        siteIndexers.clear();
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
