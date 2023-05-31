package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.model.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sitesConfig;
    private final SiteRepository sites;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sitesConfig.getSites().size());
        total.setIndexing(false);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for (Site site : sitesConfig.getSites()) {
            SiteEntity entity = sites.findSiteEntityByUrlIgnoreCase(site.getUrl());
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            if (entity != null) {
                SiteEntity siteEntity = entity;
                int pagesCount = sites.countPagesBySiteId(siteEntity.getId());
                int lemmasCount = sites.countLemmasBySiteId(siteEntity.getId());
                item.setPages(pagesCount);
                item.setLemmas(lemmasCount);
                item.setStatus(siteEntity.getStatus().toString());
                item.setError(siteEntity.getLastError());
                item.setStatusTimeFromLocalDateTime(siteEntity.getStatusTime());
                total.setPages(total.getPages() + pagesCount);
                total.setLemmas(total.getLemmas() + lemmasCount);
            }
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
