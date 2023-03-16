package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import searchengine.model.Site;

import java.util.concurrent.ForkJoinPool;

public class SiteIndexer extends Thread {
    private final static Logger log = LogManager.getLogger();
    private final Site site;
    private final ForkJoinPool pool;

    public SiteIndexer(Site site) {
        this.site = site;
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(coreCount);
    }

    public void startIndexing() {
        log.info("siteIndexer(" + site.getUrl() + ")");
        try {
            PageIndexer task = new PageIndexer();
            task.init(site, "/");
            pool.invoke(task);
            log.info("Pool for site " + site.getName() + " started.");
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
