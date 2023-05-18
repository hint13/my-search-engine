package searchengine.dto.indexing;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IndexingResponseError extends IndexingResponse {
    public static final String msgIndexingAlreadyStarted = "Индексация уже запущена";
    public static final String msgIndexingNotStarted = "Индексация не запущена";
    public static final String msgBadPageUrl =
            "Данная страница находится за пределами сайтов, указанных в конфигурационном файле";

    private String error;
    public IndexingResponseError(boolean isIndexing) {
        setResult(false);
        error = isIndexing ? msgIndexingAlreadyStarted : msgIndexingNotStarted;
    }

    public IndexingResponseError(String errorMessage) {
        setResult(false);
        error = errorMessage;
    }
}
