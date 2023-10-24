package searchengine.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import searchengine.model.*;

import java.util.Collection;
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
        return sites.findSiteEntityByUrlEqualsIgnoreCase(siteUrl);
    }

    public PageEntity getPageForSiteByPath(SiteEntity site, String pagePath) {
        return pages.findPageEntityBySiteAndPath(site, pagePath);
    }

    @Transactional
    public SiteEntity saveSite(SiteEntity site) {
        return sites.saveAndFlush(site);
    }

    @Transactional
    public PageEntity savePage(PageEntity page) {
        return pages.saveAndFlush(page);
    }

    @Transactional
    public Iterable<PageEntity> savePages(Collection<PageEntity> pageEntities) {
        return pages.saveAllAndFlush(pageEntities);
    }

    @Transactional
    public Iterable<LemmaEntity> saveLemmas(Collection<LemmaEntity> lemmaEntities) {
        log.debug("###! save " + lemmaEntities.size() + " lemmas");
        return lemmas.saveAllAndFlush(lemmaEntities);
    }

    @Transactional
    public LemmaEntity saveLemma(LemmaEntity lemma) {
        return lemmas.saveAndFlush(lemma);
    }

    @Transactional
    public IndexEntity saveIndex(IndexEntity index) {
        return indexes.saveAndFlush(index);
    }

    @Transactional
    public Iterable<IndexEntity> saveIndexes(Collection<IndexEntity> indexEntities) {
        return indexes.saveAllAndFlush(indexEntities);
    }

    public LemmaEntity getLemmaEntityBySite(SiteEntity site, String lemma) {
        return lemmas.findFirstLemmaEntityBySiteIdAndLemma(site.getId(), lemma);
    }

    @Transactional
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

    @Transactional
    public void deletePage(PageEntity page) {
        lemmas.updateLemmasCountByPageIdForSiteId(page.getId(), page.getSite().getId());
        lemmas.clearDummyLemmasForSiteId(page.getSite().getId());
        lemmas.flush();
        deleteLemmasFromIndexByPageId(page.getId());
        log.debug("Deleting page " + page);
        pages.delete(page);
        pages.flush();
    }

    // TODO : Add method for create new PageEntity object natively
    public PageEntity newPageEntity() {
        return null;
    }

    @Transactional
    public void deleteLemmasFromIndexByPageId(Integer pageId) {
        List<IndexEntity> indexEntityList = indexes.findAllByPageId(pageId);
        log.debug("Deleting " + indexEntityList.size() + " lemmas for page(" + pageId + ") in index table");
        indexes.deleteAllInBatch(indexEntityList);
        indexes.flush();
    }

    @Transactional
    public void deleteLemmasFromIndexByPage(PageEntity page) {
        deleteLemmasFromIndexByPageId(page.getId());
    }

    @Transactional
    public void deleteLemmasFromLemmaBySiteId(Integer siteId) {
        List<LemmaEntity> lemmaEntityList = lemmas.findAllLemmaEntitiesBySiteId((siteId));
        log.debug("Deleting " + lemmaEntityList.size() + " lemmas for site(" + siteId + ") in lemma table");
        lemmas.deleteAllInBatch(lemmaEntityList);
        lemmas.flush();
    }

    @Override
    public String toString() {
        return "Total: sites(" + sites.count() +
                "), pages(" + pages.count() +
                "), lemmas(" + lemmas.count() + ")";
    }
}
