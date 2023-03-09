package searchengine.services;

import searchengine.dto.data.PageData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class PageIndexer extends RecursiveTask<Map<String, PageData>> {
    private final String siteUrl;
    private final PageData page;

    private final Map<String, PageData> urls;

    public PageIndexer(String siteUrl, PageData page) {
        this.siteUrl = siteUrl;
        this.page = page;
        this.urls = new HashMap<>();
    }

    @Override
    protected Map<String, PageData> compute() {
        return null;
    }
}
