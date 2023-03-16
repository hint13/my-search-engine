package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import searchengine.model.SiteEntity;

import java.util.concurrent.ForkJoinPool;

public class SiteIndexer implements Runnable {
    private final static Logger log = LogManager.getLogger();
    private final SiteEntity site;
    private final ForkJoinPool pool;

    public SiteIndexer(SiteEntity site) {
        this.site = site;
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(coreCount);
    }

    public void startIndexing() {
        log.info("siteIndexer(" + site.getUrl() + ")");
        try {
            PageIndexer task = new PageIndexer();
            task.init(site, "/");
            log.info("Pool for site " + site.getName() + " started.");
            int count = pool.invoke(task);
            pool.shutdown();
            log.info("Pool for site " + site.getName() + " finished. Parsed " + count + " urls.");
        } catch (Exception ex) {
            log.error("Error start indexing for site " + site.getUrl() + ": " + ex.getMessage());
        }
    }

    public void stopIndexing() {
        // TODO: Add code for manual thread interrupt
        pool.shutdown();
    }

    @Override
    public void run() {
        startIndexing();
    }
}
