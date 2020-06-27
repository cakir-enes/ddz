package dds.service.store;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


public interface TopicStore<T> {

    Duration NO_EXPIRATION = Duration.ZERO;

    CompletableFuture<Void> put(String key, T topic, Duration duration);

    CompletableFuture<T> get(String key);

    CompletableFuture<List<T>> get(Iterable<String> keys);

    CompletableFuture<Void> delete(String key);

    CompletableFuture<Void> delete(Iterable<String> keys);

    Stream<String> keys();

    Stream<T> all();

    CompletableFuture<Void> drop();
}