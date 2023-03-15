package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import searchengine.model.Site;

import java.util.concurrent.ForkJoinPool;

public class SiteIndexer implements Runnable {
    private final static Logger log = LogManager.getLogger();
    private final Site site;
    private final ForkJoinPool pool;

    public SiteIndexer(Site site) {
        this.site = site;
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(coreCount);
    }

    private void startIndexing() {
        // TODO: Add code for start ForkJoinPool(PageIndexer)
        try {
            pool.invoke(new PageIndexer(site, "/"));
        } catch (Exception ex) {
            log.info(ex.getMessage());
        }
    }

    private void stopIndexing() {
        // TODO: Add code for manual thread interrupt
    }

    @Override
    public void run() {
        startIndexing();
    }
}
