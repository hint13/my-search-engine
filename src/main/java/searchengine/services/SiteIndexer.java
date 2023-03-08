package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.model.Site;
import searchengine.model.SiteRepository;
import searchengine.model.SiteStatus;

import java.time.LocalDateTime;

public class SiteIndexer {

    private final Site site;
    private volatile boolean isIndexing;

    public SiteIndexer(String siteUrl, String siteName) {
        site = new Site();
        site.setUrl(siteUrl);
        site.setName(siteName);
    }

    public void startIndexing() {
        isIndexing = true;
        site.setStatus(SiteStatus.INDEXING);
        updateStatusTime();
    }

    public void stopIndexing() {
        if (isIndexing) {
            ;
        }
    }

    public void updateStatusTime() {
        site.setStatusTime(LocalDateTime.now());
    }

    public Site getSite() {
        return site;
    }
}
