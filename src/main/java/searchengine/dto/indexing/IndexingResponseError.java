package searchengine.dto.indexing;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IndexingResponseError extends IndexingResponse {
    private String error;
    public IndexingResponseError(boolean isIndexing) {
        setResult(false);
        error = isIndexing ? "Индексация уже запущена" : "Индексация не запущена";
    }
}
