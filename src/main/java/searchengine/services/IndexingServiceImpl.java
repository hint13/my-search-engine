package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseError;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private boolean isIndexing = false;

    @Override
    public IndexingResponse start() {
        IndexingResponse response = isIndexing ? new IndexingResponse() : new IndexingResponseError(true);
        isIndexing = true;
        return response;
    }

    @Override
    public IndexingResponse stop() {
        IndexingResponse response = isIndexing ? new IndexingResponse() : new IndexingResponseError(false);
        isIndexing = false;
        return response;
    }
}
