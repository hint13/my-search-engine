package searchengine.services.indexing;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import searchengine.model.*;
import searchengine.services.DataAccessManager;
import searchengine.utils.TextFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PageIndex {
    private static final Logger log = LogManager.getLogger(PageIndex.class);

    private final DataAccessManager dam;
    private final PageEntity page;

    public void indexOnePage(Document doc) {
        dam.deleteLemmasFromIndexByPage(page);
        TextFilter textFilter = new TextFilter(doc);
        Map<String, Integer> lemmasMap = textFilter.calcLemmas();
        List<IndexEntity> indexEntities = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : lemmasMap.entrySet()) {
            LemmaEntity lemmaEntity = updateOrCreateLemmaEntity(entry.getKey(), entry.getValue());
            indexEntities.add(new IndexEntity(page, lemmaEntity, Float.valueOf(entry.getValue())));
        }
        dam.saveIndexes(indexEntities);
        log.debug("For page " + page.getFullPath() + " added " + lemmasMap.size() + " lemmas to index.");
    }

    private LemmaEntity updateOrCreateLemmaEntity(String lemma, Integer frequency) {
        LemmaEntity lemmaEntity = dam.getLemmaEntityBySite(page.getSite(), lemma);
        if (lemmaEntity == null) {
            lemmaEntity = new LemmaEntity(page.getSite(), lemma, frequency);
        } else {
            lemmaEntity.setFrequency(lemmaEntity.getFrequency() + frequency);
        }
        return dam.saveLemma(lemmaEntity);
    }
}
