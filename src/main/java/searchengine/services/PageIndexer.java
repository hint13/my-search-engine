package searchengine.services;

import searchengine.model.Page;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;

public class PageIndexer extends RecursiveTask<Map<String, Page>> {
    private static volatile Set<String> urlCache = new ConcurrentSkipListSet<>();
    private final String siteUrl;
    private final Page page;

    private final Map<String, Page> urls;

    public PageIndexer(String siteUrl, Page page) {
        this.siteUrl = siteUrl;
        this.page = page;
        this.urls = new HashMap<>();
    }

    @Override
    protected Map<String, Page> compute() {
        return null;
    }
}
