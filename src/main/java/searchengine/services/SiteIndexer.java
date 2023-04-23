package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import searchengine.config.Bot;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteRepository;
import searchengine.model.SiteStatus;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SiteIndexer extends Thread {
    private final static Logger log = LogManager.getLogger(SiteIndexer.class);

    private final SiteRepository sites;
    private final PageRepository pages;
    private final Bot botConfig;

    private final SiteEntity site;
    private final ForkJoinPool pool;
    private final AtomicBoolean isIndexing;

    public SiteIndexer(SiteEntity site, SiteRepository sites,  PageRepository pages, Bot botConfig) {
        this.site = site;
        this.sites = sites;
        this.pages = pages;
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(coreCount);
        this.botConfig = botConfig;
        isIndexing = new AtomicBoolean(false);
    }

    public boolean isIndexing() {
        return isIndexing.get();
    }

    private void isIndexing(boolean status) {
        isIndexing.set(status);
    }

    @Override
    public void run() {
        startIndexing();
    }

    public SiteEntity getSite() {
        return site;
    }

    public void startIndexing() {
        log.debug("siteIndexer(" + site.getUrl() + ")");
        isIndexing(true);
        PageIndexer task = new PageIndexer(botConfig, pages);
        task.init(site, "/");
        PageIndexer.clearUrlCacheForSite(site.getUrl());
        log.debug("Pool for site " + site.getName() + " started.");
        ForkJoinTask<Integer> futureTask = pool.submit(task);
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                site.setLastError("Индексация остановлена пользователем");
                site.setStatus(SiteStatus.FAILED);
                sites.saveAndFlush(site);
                log.error("Thread for site " + site.getName() + ": " + e.getMessage());
                stopIndexing();
                return;
            }
        } while (pool.getRunningThreadCount() > 0);
        int count = 0;
        try {
            count = futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            log.debug(e.getMessage());
        }
        stopIndexing();
        site.setStatus(SiteStatus.INDEXED);
        sites.saveAndFlush(site);
        log.debug("Pool for site " + site.getName() + " finished. Parsed " + count + " urls.");
    }

    public void stopIndexing() {
        // TODO: Add code for manual thread interrupt
        log.debug("Stop indexing process for site " + site.getName());
        isIndexing(false);
        if (pool.getActiveThreadCount() > 0) {
            pool.shutdownNow();
        }
    }


}
