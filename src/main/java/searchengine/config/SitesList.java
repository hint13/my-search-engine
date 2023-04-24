package searchengine.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

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
}
