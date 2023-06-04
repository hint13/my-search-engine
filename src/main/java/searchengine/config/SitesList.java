package searchengine.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<Site> sites;

    public List<Site> getSites() {
        List<Site> sitesList = new LinkedList<>();
        sites.forEach(site -> {
            if (!sitesList.contains(site)) {
                sitesList.add(site);
            }
        });
        return sitesList;
    }

    public boolean isUrlInSites(String url) {
        for (Site site : sites) {
            if (url.startsWith(site.getUrl())) {
                return true;
            }
        }
        return false;
    }

    public Optional<Site> getSite(String siteUrl) {
        for (Site site : sites) {
            if (site.getUrl().equals(siteUrl)) {
                return Optional.of(site);
            }
        }
        return Optional.empty();
    }
}
