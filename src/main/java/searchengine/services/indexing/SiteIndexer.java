package searchengine.services.indexing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import searchengine.config.Bot;
import searchengine.model.*;
import searchengine.services.DataAccessManager;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SiteIndexer extends Thread {
    private final static Logger log = LogManager.getLogger(SiteIndexer.class);

    private final DataAccessManager dam;
    private final Bot botConfig;

    private final SiteEntity site;
    private final ForkJoinPool pool;
    private final AtomicBoolean isIndexing;

    public SiteIndexer(SiteEntity site, DataAccessManager dam, Bot botConfig) {
        this.site = site;
        this.dam = dam;
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
        PagesIndexer task = new PagesIndexer(botConfig, dam);
        task.init(site, "/");
        PagesIndexer.removeSiteFromUrlCache(site.getUrl());
        log.debug("Pool for site " + site.getName() + " started.");
        ForkJoinTask<Integer> futureTask = pool.submit(task);
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.warn("Thread for site " + site.getName() + ": " + e.getMessage());
                stopIndexing(false);
                return;
            }
        } while (pool.getRunningThreadCount() > 0);
        int count;
        try {
            count = futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            log.warn(e.getMessage());
            stopIndexing(false);
            return;
        }
        stopIndexing(true);
        log.debug("Pool for site " + site.getName() + " finished. Parsed " + count + " urls.");
    }

    public void stopIndexing(boolean normalInterrupt) {
        log.debug("Stop indexing process for site " + site.getName());
        isIndexing(false);
        if (!normalInterrupt) {
            site.setLastError("Индексация остановлена пользователем");
            site.setStatus(SiteStatus.FAILED);
        } else {
            site.setLastError("");
            site.setStatus(SiteStatus.INDEXED);
        }
        dam.saveSite(site);
        if (pool.getActiveThreadCount() > 0) {
            pool.shutdownNow();
        }
    }


}
