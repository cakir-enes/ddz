package dds.service.history;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface HistoryService<T> {

    default CompletableFuture<Boolean> append(String key, T data) {
        return append(key, data, LocalDateTime.now());
    }

    CompletableFuture<Boolean> append(String key, T data, LocalDateTime time);

    Stream<T> inInterval(LocalDateTime startInclusive, LocalDateTime endExclusive);

    void clear();
}
