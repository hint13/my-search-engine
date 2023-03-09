package searchengine.services;

import searchengine.dto.data.SiteData;

public class SiteIndexer {
    private final SiteData site;
    private volatile boolean isIndexing;

    public SiteIndexer(String name, String url) {
        this.site = new SiteData(url, name);
    }

    public boolean startIndexing() {
        isIndexing = true;
        return true;
    }

    public boolean stopIndexing() {
        isIndexing = false;
        return true;
    }

}
