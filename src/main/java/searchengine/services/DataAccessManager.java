package searchengine.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import searchengine.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class DataAccessManager {
    private static final Logger log = LogManager.getLogger(DataAccessManager.class);

    private SiteRepository sites;
    private PageRepository pages;
    private LemmaRepository lemmas;
    private IndexRepository indexes;

    public SiteEntity getSiteByUrl(String siteUrl) {
        return sites.findSiteEntityByUrlIgnoreCase(siteUrl);
    }

    @Transactional
    public SiteEntity saveSite(SiteEntity site) {
        return sites.save(site);
    }

    @Transactional
    public PageEntity savePage(PageEntity page) {
        return pages.save(page);
    }

    @Transactional
    public Iterable<LemmaEntity> saveLemmas(Collection<LemmaEntity> lemmaEntityList) {
        return lemmas.saveAll(lemmaEntityList);
    }

    public LemmaEntity getLemmaEntityBySite(SiteEntity site, String lemma) {
        return lemmas.findLemmaEntityBySiteIdAndLemma(site.getId(), lemma)
                .orElse(new LemmaEntity(site, lemma));
    }

    public void deletePagesBySite(SiteEntity site) {
        List<PageEntity> pageEntityList = pages.findAllBySiteId(site.getId());
        if (!pageEntityList.isEmpty()) {
            log.debug("delete Pages by siteId: " + site.getId() + "(" + pageEntityList.size() + ")");
            for (PageEntity page : pageEntityList) {
                deleteLemmasFromIndexByPageId(page.getId());
            }
            deleteLemmasFromLemmaBySiteId(site.getId());
            pages.deleteAllInBatch(pageEntityList);
            pages.flush();
        }
    }

    private void deleteLemmasFromIndexByPageId(Integer pageId) {
        List<IndexEntity> indexEntityList = indexes.findAllByPageId(pageId);
        log.debug("Deleting " + indexEntityList.size() + " lemmas for page(" + pageId + ") in index table");
        indexes.deleteAllInBatch(indexEntityList);
        indexes.flush();
    }

    private void deleteLemmasFromLemmaBySiteId(Integer siteId) {
        // Delete all lemmas for site_id in table:lemma
        List<LemmaEntity> lemmaEntityList = lemmas.findAllBySiteId((siteId));
        log.debug("Deleting " + lemmaEntityList.size() + " lemmas for site(" + siteId + ") in lemma table");
        lemmas.deleteAllInBatch(lemmaEntityList);
        lemmas.flush();
    }
}
