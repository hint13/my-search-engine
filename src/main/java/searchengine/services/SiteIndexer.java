package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;

import java.util.concurrent.*;

public class SiteIndexer implements Callable<SiteEntity> {
    private final static Logger log = LogManager.getLogger();
    private final PageRepository pageRepository;
    private final SiteEntity site;
    private final ForkJoinPool pool;

    public SiteIndexer(SiteEntity site, PageRepository pageRepository) {
        this.site = site;
        this.pageRepository = pageRepository;
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.pool = new ForkJoinPool(coreCount);
    }

    public SiteEntity getSite() {
        return site;
    }

    public SiteEntity startIndexing() {
        log.info("siteIndexer(" + site.getUrl() + ")");
        try {
            PageIndexer task = new PageIndexer(pageRepository);
            task.init(site, "/");
            log.info("Pool for site " + site.getName() + " started.");
            int count = pool.invoke(task);
            stopIndexing();
            site.setStatus(SiteStatus.INDEXED);
            log.info("Pool for site " + site.getName() + " finished. Parsed " + count + " urls.");
        } catch (Exception ex) {
            log.error("Error start indexing for site " + site.getUrl() + ": " + ex.getMessage());
        }
        return site;
    }

    public void stopIndexing() {
        // TODO: Add code for manual thread interrupt
        pool.shutdownNow();
    }


    @Override
    public SiteEntity call() {
        return startIndexing();
    }
}
