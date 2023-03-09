package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseError;
import searchengine.model.SiteRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class IndexingServiceImpl implements IndexingService {
    @Autowired
    private SiteRepository sites;
    @Autowired
    private SitesList sitesList;

    private final List<SiteIndexer> siteIndexers;
    private volatile AtomicBoolean isIndexing;

    public IndexingServiceImpl(SiteRepository sites, SitesList sitesList) {
        isIndexing = new AtomicBoolean(false);
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

    @Override
    public IndexingResponse start() {
        IndexingResponse response = !isIndexing.get() ? new IndexingResponse() : new IndexingResponseError(true);
        startIndexing();
        return response;
    }

    @Override
    public IndexingResponse stop() {
        IndexingResponse response = isIndexing.get() ? new IndexingResponse() : new IndexingResponseError(false);
        stopIndexing();
        return response;
    }

}
