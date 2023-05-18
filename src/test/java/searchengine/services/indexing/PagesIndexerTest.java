package searchengine.services.indexing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;

class PagesIndexerTest {
    private PagesIndexer pagesIndexer;
    private final String[] urls = {"/", "/index.html", "/doc1.html", "doc2.html", "doc3.html"};
    private final String site1 = "http://localhost";
    private final String site2 = "http://www.site.url";

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        pagesIndexer = new PagesIndexer(null, null);
        for (String url : urls) {
            PagesIndexer.addUrlToCache(site1 + url);
            PagesIndexer.addUrlToCache(site2 + url);
        }
    }

    @AfterEach
    void tearDown() {
        PagesIndexer.clearUrlCache();
    }

    @Test
    void clearUrlCacheForSite() {
        Set<String> expectedUrlCache = new ConcurrentSkipListSet<>();
        for (String url : urls) {
            expectedUrlCache.add(site2 + url);
        }
        PagesIndexer.clearUrlCacheForSite(site1);
        Set<String> actualUrlCache = PagesIndexer.getUrlCache();
        assertEquals(expectedUrlCache, actualUrlCache);
    }
}