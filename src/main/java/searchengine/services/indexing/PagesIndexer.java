package searchengine.services.indexing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import searchengine.config.Bot;
import searchengine.model.*;
import searchengine.services.DataAccessManager;
import searchengine.utils.TextFilter;
import searchengine.utils.UrlFilter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class PagesIndexer extends RecursiveTask<Integer> {
    private static final Logger log = LogManager.getLogger(PagesIndexer.class);
    private static final Set<String> urlCache = new ConcurrentSkipListSet<>();

    private final Bot botConfig;
    private final DataAccessManager dam;

    private PageEntity page;
    private String siteUrl;

    private Integer urlsCount;

    public PagesIndexer(Bot botConfig, DataAccessManager dam) {
        urlsCount = 0;
        this.dam = dam;
        this.botConfig = botConfig;
    }

    public void init(SiteEntity site, String pagePath) {
        page = new PageEntity();
        page.setSite(site);
        page.setPath(pagePath);
        siteUrl = site.getUrl();
    }

    static boolean updateUrlCache(String url) {
        synchronized (urlCache) {
            if (urlCache.contains(url)) {
                return false;
            }
            urlCache.add(url);
        }
        return true;
    }

    public static void removeSiteFromUrlCache(String siteUrl) {
        synchronized (urlCache) {
            urlCache.stream()
                    .filter(url -> url.startsWith(siteUrl))
                    .forEach(urlCache::remove);
        }
    }

    @Override
    protected Integer compute() {
        String url = page.getFullPath();
        if (!updateUrlCache(url)) {
            return 0;
        }
        Set<String> links;
        try {
            Document doc = loadPage();
            links = getLinksFromPageDoc(doc);
            indexLemmas(doc);
        } catch (IOException ex) {
            log.warn("Error loading page " + url + ": " + ex.getMessage());
            return 0;
        }
        urlsCount += 1;
        List<PagesIndexer> tasks = new LinkedList<>();
        for (String link : links) {
            PagesIndexer task = new PagesIndexer(botConfig, dam);
            task.init(page.getSite(), link);
            try {
                Thread.sleep(botConfig.getTimeout());
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                return 0;
            }
            task.fork();
            tasks.add(task);
        }
        int count = 0;
        do {
            for (PagesIndexer task : tasks) {
                if (isCancelled()) {
                    if (!task.isCancelled() && !task.isDone()) {
                        task.cancel(true);
                        count++;
                    }
                } else if (task.isDone()) {
                    count++;
                    urlsCount += task.join();
                }
            }
        } while (count >= tasks.size());
        return urlsCount;
    }

    private Document loadPage() throws IOException {
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
        page.setId(0);
        page = dam.savePage(page);
        log.debug("Page " + page.getFullPath() + " saved.");
        return doc;
    }

    private void indexLemmas(Document doc) {
        TextFilter textFilter = new TextFilter(doc);
        Map<String, Integer> lemmasMap = textFilter.calcLemmas();
        Map<String, LemmaEntity> lemmaEntityMap = new HashMap();
        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            LemmaEntity lemmaEntity = dam.getLemmaEntityBySite(page.getSite(), entry.getKey());
            lemmaEntity.setFrequency(lemmaEntity.getFrequency() + entry.getValue());
            lemmaEntityMap.merge(entry.getKey(), lemmaEntity, (prev, val) -> {
                prev.setFrequency(prev.getFrequency() + val.getFrequency());
                return prev;
            });
            log.debug(lemmaEntity);
//            IndexEntity indexEntity = new IndexEntity(page, lemmaEntity, Float.valueOf(entry.getValue()));
//            index.saveAndFlush(indexEntity);
        }
        dam.saveLemmas(lemmaEntityMap.values());
        log.debug("For page " + page.getFullPath() + " added " + lemmasMap.size() + " lemmas to index.");
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
