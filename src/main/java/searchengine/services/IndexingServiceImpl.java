package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import searchengine.config.Bot;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.*;
import searchengine.model.*;
import searchengine.services.indexing.PageIndex;
import searchengine.services.indexing.SiteIndexer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
                               SitesList sitesConfig,
                               Bot botConfig) {
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
            SiteEntity siteEntity = getOrAddSite(site);
            dam.deletePagesBySite(siteEntity);
            SiteIndexer siteIndexer = new SiteIndexer(siteEntity, dam, botConfig);
            siteIndexers.add(siteIndexer);
            siteIndexer.start();
        }
    }

    private SiteEntity getOrAddSite(Site site) {
        SiteEntity entity = dam.getSiteByUrl(site.getUrl());
        SiteEntity siteEntity;
        if (entity == null) {
            siteEntity = new SiteEntity(site.getUrl(), site.getName());
        } else {
            siteEntity = entity;
        }
        siteEntity.updateStatus(SiteStatus.INDEXING);
        siteEntity = dam.saveSite(siteEntity);
        return siteEntity;
    }

    private PageEntity getOrCreatePageForSite(SiteEntity site, String pagePath) {
        PageEntity entity = dam.getPageForSiteByPath(site, pagePath);
        PageEntity pageEntity;
        if (entity == null) {
            pageEntity = new PageEntity(site, pagePath);
        } else {
            pageEntity = entity;
        }
        return pageEntity;
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
            log.debug("Start indexing one page: " + url);
            startIndexingPage(url);
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

        Optional<Site> siteItem = sitesConfig.getSite(siteUrl);
        if (siteItem.isPresent()) {
            SiteEntity site = getOrAddSite(siteItem.get());
            site.updateStatus(SiteStatus.INDEXING);
            dam.saveSite(site);
            log.debug("### site: " + site);
            PageEntity page = getOrCreatePageForSite(site, pagePath);
            log.debug("### page: " + page);
            // TODO: update lemmas frequency for site if page already exists
            Optional<Document> result = page.loadContent(botConfig.getUseragent(), botConfig.getReferrer());
            if (result.isPresent()) {
                page = dam.savePage(page);
                PageIndex pageIndex = new PageIndex(dam, page);
                log.debug("### parsing url: " + pageUrl + ":: site(" + siteUrl + "), page(" + pagePath + ").");
                pageIndex.indexOnePage(result.get());
                site.updateStatus(SiteStatus.INDEXED);
            } else {
                site.updateStatus(SiteStatus.FAILED, "Ошибка при индексации страницы " + pagePath);
            }
            dam.saveSite(site);
        }
    }
}
