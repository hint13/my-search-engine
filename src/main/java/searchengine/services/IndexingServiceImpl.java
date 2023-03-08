package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseError;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final Indexer indexer;

    @Override
    public IndexingResponse start() {
        IndexingResponse response = !indexer.isIndexing() ? new IndexingResponse() : new IndexingResponseError(true);
        indexer.startIndexing();
        return response;
    }

    @Override
    public IndexingResponse stop() {
        IndexingResponse response = indexer.isIndexing() ? new IndexingResponse() : new IndexingResponseError(false);
        indexer.stopIndexing();
        return response;
    }
}
