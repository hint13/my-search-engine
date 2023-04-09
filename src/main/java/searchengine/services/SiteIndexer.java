package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import searchengine.config.Bot;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;

import java.util.concurrent.*;

public class SiteIndexer implements RunnableFuture<Integer> {
    private final static Logger log = LogManager.getLogger();
    private final PageRepository pages;
    private final Bot botConfig;
    private final SiteEntity site;
    private final ForkJoinPool pool;

    public SiteIndexer(SiteEntity site, PageRepository pages, Bot botConfig) {
        this.site = site;
        this.pages = pages;
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(coreCount);
        this.botConfig = botConfig;
    }

    public SiteEntity getSite() {
        return site;
    }

    public void startIndexing() {
        log.info("siteIndexer(" + site.getUrl() + ")");
        try {
            PageIndexer task = new PageIndexer(botConfig, pages);
            task.init(site, "/");
            PageIndexer.clearUrlCacheForSite(site.getUrl());
            log.info("Pool for site " + site.getName() + " started.");
            /* TODO: Добавить код для циклического опроса результата индексирования,
             * а также отслеживание принудительной отмены индексиирования
             */
            int count = pool.invoke(task);
            stopIndexing();
            site.setStatus(SiteStatus.INDEXED);
            log.info("Pool for site " + site.getName() + " finished. Parsed " + count + " urls.");
        } catch (Exception ex) {
            log.error("Error start indexing for site " + site.getUrl() + ": " + ex.getMessage());
        }
    }

    public void stopIndexing() {
        // TODO: Add code for manual thread interrupt
        pool.shutdownNow();
    }

    @Override
    public void run() {
        startIndexing();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public Integer get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
