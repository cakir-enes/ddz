package dds.service.history.fdb;

import dds.service.history.HistoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class FdbHistoryService<T> implements HistoryService<T> {

    @Override
    public CompletableFuture<Boolean> append(String key, T data) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> append(String key, T data, LocalDateTime time) {
        return null;
    }

    @Override
    public Stream<T> inInterval(LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return null;
    }

    @Override
    public void clear() {

    }
}
