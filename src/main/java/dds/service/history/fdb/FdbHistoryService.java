package dds.service.history.fdb;

import com.apple.foundationdb.FDB;
import com.apple.foundationdb.tuple.ByteArrayUtil;
import com.apple.foundationdb.tuple.Tuple;
import dds.service.history.HistoryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class FdbHistoryService<T> implements HistoryService<T> {
    FDB fdb = FDB.selectAPIVersion(620);


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
