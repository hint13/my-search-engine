package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class Indexer {
    private SiteRepository sites;

    private SitesList sitesList;

    private final List<SiteIndexer> siteIndexers;
    private volatile AtomicBoolean isIndexing;

    public Indexer() {
        siteIndexers = new LinkedList<>();
        for (Site site : sitesList.getSites()) {
            SiteIndexer siteIndexer = new SiteIndexer(site.getUrl(), site.getName());
            siteIndexers.add(siteIndexer);
        }
    }

    public void startIndexing() {
        isIndexing.set(true);
        for (SiteIndexer indexer : siteIndexers) {
            indexer.startIndexing();
        }
    }

    public void stopIndexing() {
        isIndexing.set(false);
        for (SiteIndexer indexer : siteIndexers) {
            indexer.stopIndexing();
        }
    }

    public boolean isIndexing() {
        return isIndexing.get();
    }
}
