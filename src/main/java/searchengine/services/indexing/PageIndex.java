package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.services.DataAccessManager;

@Service
@RequiredArgsConstructor
public class PageIndex {
    private DataAccessManager dam;

    public void indexOnePage(Document doc, SiteEntity site, PageEntity page) {

    }

}
