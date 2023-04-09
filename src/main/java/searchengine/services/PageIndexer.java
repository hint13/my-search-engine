package searchengine.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import searchengine.config.Bot;
import searchengine.model.PageEntity;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.utils.UrlFilter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class PageIndexer extends RecursiveTask<Integer> {
    private final Bot botConfig;
    private final PageRepository pages;
    private static final Logger log = LogManager.getLogger();
    private static final Set<String> urlCache = new ConcurrentSkipListSet<>();
    private PageEntity page;
    private String siteUrl;

    private Integer urlsCount;

    public PageIndexer(Bot botConfig, PageRepository pages) {
        urlsCount = 0;
        this.pages = pages;
        this.botConfig = botConfig;
    }

    public void init(SiteEntity site, String pagePath) {
        page = new PageEntity();
        page.setSite(site);
        page.setPath(pagePath);
        siteUrl = site.getUrl();
    }

    @Override
    protected Integer compute() {
        String url = page.getFullPath();
        synchronized (urlCache) {
            if (urlCache.contains(url)) {
                return 0;
            }
            urlCache.add(url);
        }
        Set<String> links;
        try {
            links = loadPage();
        } catch (IOException ex) {
            log.warn("Error loading page " + page.getFullPath() + ": " + ex.getMessage());
            return 0;
        }
        urlsCount += 1;
        page.setId(0);
        pages.save(page);
        List<PageIndexer> tasks = new LinkedList<>();
        for (String link : links) {
            PageIndexer task = new PageIndexer(botConfig, pages);
            task.init(page.getSite(), link);
            try {
                Thread.sleep(botConfig.getTimeout());
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
            task.fork();
            tasks.add(task);
        }
        for (PageIndexer task : tasks) {
            urlsCount += task.join();
        }
        return urlsCount;
    }

    private Set<String> loadPage() throws IOException {
        Connection conn = Jsoup.connect(page.getFullPath())
                .userAgent(botConfig.getUseragent())
                .referrer(botConfig.getReferrer());
        Connection.Response response = conn.execute();
        if (response.statusCode() != 200) {
            throw new IOException("Bad status code");
        }
        Document doc = response.parse();
        page.setCode(response.statusCode());
        page.setContent(doc.toString());
        return getLinksFromPageDoc(doc);
    }

    private Set<String> getLinksFromPageDoc(Document doc) {
        Set<String> urls = new TreeSet<>();
        if (doc == null) {
            return urls;
        }
        Elements elements = doc.getElementsByTag("a");
        elements.forEach(el -> {
            String href = el.attr("abs:href");
            if (href.startsWith(siteUrl)) {
                urls.add(href);
            }
        });
        return getFilteredUrls(urls);
    }

    private Set<String> getFilteredUrls(Set<String> urls) {
        return urls.stream()
                .map(u -> UrlFilter.filter(u.substring(siteUrl.length()), true))
                .collect(Collectors.toSet());
    }
}
