package searchengine.services.indexing;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;

class PageIndexerTest {
    private PageIndexer pageIndexer;
    private final String[] urls = {"/", "/index.html", "/doc1.html", "doc2.html", "doc3.html"};
    private final String site1 = "http://localhost";
    private final String site2 = "http://www.site.url";

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        pageIndexer = new PageIndexer(null, null);
        for (String url : urls) {
            PageIndexer.addUrlToCache(site1 + url);
            PageIndexer.addUrlToCache(site2 + url);
        }
    }

    @AfterEach
    void tearDown() {
        PageIndexer.clearUrlCache();
    }

    @Test
    void clearUrlCacheForSite() {
        Set<String> expectedUrlCache = new ConcurrentSkipListSet<>();
        for (String url : urls) {
            expectedUrlCache.add(site2 + url);
        }
        PageIndexer.clearUrlCacheForSite(site1);
        Set<String> actualUrlCache = PageIndexer.getUrlCache();
        assertEquals(expectedUrlCache, actualUrlCache);
    }
}