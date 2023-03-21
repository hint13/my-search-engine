package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SiteIndexer implements Runnable, Future<Boolean> {
    private final static Logger log = LogManager.getLogger();
    private final AtomicBoolean isIndexing;
    private final PageRepository pageRepository;
    private final SiteEntity site;
    private final ForkJoinPool pool;

    public SiteIndexer(SiteEntity site, PageRepository pageRepository) {
        this.isIndexing = new AtomicBoolean(false);
        this.site = site;
        this.pageRepository = pageRepository;
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(coreCount);
    }

    public boolean startIndexing() {
        log.info("siteIndexer(" + site.getUrl() + ")");
        isIndexing.set(true);
        try {
            PageIndexer task = new PageIndexer(pageRepository);
            task.init(site, "/");
            log.info("Pool for site " + site.getName() + " started.");
            int count = pool.invoke(task);
            stopIndexing();
            log.info("Pool for site " + site.getName() + " finished. Parsed " + count + " urls.");
        } catch (Exception ex) {
            log.error("Error start indexing for site " + site.getUrl() + ": " + ex.getMessage());
        }
        return isDone();
    }

    public void stopIndexing() {
        // TODO: Add code for manual thread interrupt
        isIndexing.set(false);
        pool.shutdown();
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
        return !isIndexing.get();
    }

    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return isIndexing.get();
    }

    @Override
    public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return false;
    }
}
