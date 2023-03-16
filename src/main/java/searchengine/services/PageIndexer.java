package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;


public class PageIndexer extends RecursiveTask<Map<String, Page>> {
//    @Value("${bot.useragent}")
    private static final String userAgent = "HintSearchBot/1.0.0";
//    @Value("${bot.referrer}")
    private static final String referrer = "https://www.ya.ru";
    private static final Logger log = LogManager.getLogger();
    private static final Set<String> urlCache = new ConcurrentSkipListSet<>();
    private Page page;

    private final Map<String, Page> urls;

    public PageIndexer() {
        this.urls = new HashMap<>();
    }

    public void init(Site site, String pagePath) {
        page = new Page();
        page.setSite(site);
        page.setPath(pagePath);
    }

    @Override
    protected Map<String, Page> compute() {
        Map<String, Page> result = new HashMap<>();
        String url = page.getFullPath();
        synchronized (urlCache) {
            if (urlCache.contains(url)) {
                return result;
            }
            urlCache.add(url);
        }
        try {
            loadPage();
        } catch (IOException ex) {
            log.warn("Error loading page " + page.getFullPath() + ": " + ex.getMessage());
            return result;
        }
        result.put(page.getPath(), page);
        return result;
    }

    private void loadPage() throws IOException {
        Connection conn = Jsoup.connect(page.getFullPath())
                .userAgent(userAgent)
                .referrer(referrer);
        Connection.Response response = conn.execute();
        if (response.statusCode() != 200) {
            throw new IOException("Bad status code");
        }
        Document doc = response.parse();
        page.setCode(response.statusCode());
        page.setContent(doc.toString());
    }
}
