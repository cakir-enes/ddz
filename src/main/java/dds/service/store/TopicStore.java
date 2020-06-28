package dds.service.store;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;


public interface TopicStore<T> {

    Duration NO_EXPIRATION = Duration.ZERO;

    CompletableFuture<Void> put(String key, T topic, Duration duration);

    CompletableFuture<T> get(String key);

    CompletableFuture<Map<String, T>> get(Set<String> keys);

    CompletableFuture<Void> delete(String key);

    CompletableFuture<Void> delete(Set<String> keys);

    Stream<String> keys();

    Stream<Map.Entry<String, T>> all();

    CompletableFuture<Void> drop();
}