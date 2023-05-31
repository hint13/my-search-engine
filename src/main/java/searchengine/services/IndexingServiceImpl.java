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

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class IndexingServiceImpl implements IndexingService {
    private static final Logger log = LogManager.getLogger(IndexingServiceImpl.class);

    private final DataAccessManager dam;
    private final SitesList sitesConfig;
    private final Bot botConfig;

    private final List<Thread> siteIndexers;

    @Autowired
    public IndexingServiceImpl(DataAccessManager dam,
                               @NotNull SitesList sitesConfig, Bot botConfig) {
        this.dam = dam;
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
            dam.deletePagesBySite(siteEntity);
            SiteIndexer siteIndexer = new SiteIndexer(siteEntity, dam, botConfig);
            siteIndexers.add(siteIndexer);
            siteIndexer.start();
        }
    }

    private SiteEntity insertOrGetSite(Site site) {
        SiteEntity entity = dam.getSiteByUrl(site.getUrl());
        SiteEntity siteEntity = entity == null ? new SiteEntity() : entity;
        siteEntity.setUrl(site.getUrl());
        siteEntity.setName(site.getName());
        siteEntity.setStatus(SiteStatus.INDEXING);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setLastError("");
        return dam.saveSite(siteEntity);
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

    @Override
    public IndexingResponse indexPage(String url) {
        boolean urlInSites = sitesConfig.isUrlInSites(url);
        IndexingResponse response = urlInSites ?
                new IndexingResponse() :
                new IndexingResponseError(IndexingResponseError.msgBadPageUrl);
        if (urlInSites) {
            startIndexingPage(url);
            log.debug("Start indexing one page: " + url);
        } else {
            log.debug("Indexing not started. Page " + url + " not in sites.");
        }
        return  response;
    }

    public void startIndexingPage(String pageUrl) {
        // TODO: add code for index one page by url
        URL url;
        try {
            url = new URL(pageUrl);
        } catch (MalformedURLException e) {
            log.error("Bad page url format: " + pageUrl);
            return;
        }
        String siteUrl = url.getProtocol() + "://" + url.getAuthority();
        String pagePath = url.getPath();
//        log.debug("### parsing url: " + pageUrl + ":: site(" + siteUrl + "), page(" + pagePath + ").");
        SiteEntity site = dam.getSiteByUrl(siteUrl);
        log.debug("### site: " + site);
//        PageEntity page = pages.findFirstBySiteIdAndPath(site.getId(), pagePath).orElseGet(PageEntity::new);
//        log.debug("### page: " + page);
    }
}
