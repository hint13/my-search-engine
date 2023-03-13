package searchengine.services;

import org.hibernate.service.spi.Stoppable;
import searchengine.model.Site;


public class SiteIndexer implements Runnable, Stoppable {
    private final Site site;
    private volatile boolean isIndexing;

    public SiteIndexer(Site site) {
        this.site = site;
    }

    private boolean startIndexing() {
        isIndexing = true;
        return true;
    }

    private boolean stopIndexing() {
        isIndexing = false;
        return true;
    }

    @Override
    public void run() {
        startIndexing();
    }

    @Override
    public void stop() {
        stopIndexing();
    }
}
