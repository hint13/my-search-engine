package searchengine.services;

import searchengine.model.Page;
import searchengine.model.Site;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;

public class PageIndexer extends RecursiveTask<Map<String, Page>> {
    private static volatile Set<String> urlCache = new ConcurrentSkipListSet<>();
    private final Page page;

    private final Map<String, Page> urls;

    public PageIndexer(Site site, String pagePath) {
        this.urls = new HashMap<>();
        page = new Page();
        page.setSite(site);
        page.setPath(pagePath);
    }

    @Override
    protected Map<String, Page> compute() {
        Map<String, Page> result = new HashMap<>();
//        if (urlCache.contains(page.getFullPath())) {
        return result;
    }
}
