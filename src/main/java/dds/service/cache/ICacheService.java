package dds.service.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public interface ICacheService<K, V> {

    CompletableFuture<Void> put(K key, V data);

    CompletableFuture<Void> put(K key, V data, Duration duration);

    CompletableFuture<V> get(K key);

    CompletableFuture<Map<K, V>> get(Set<K> keys);

    CompletableFuture<Void> delete(K key);

    CompletableFuture<Void> delete(Set<K> keys);

    CompletableFuture<V> getAndDelete(K key);

    CompletableFuture<Boolean> contains(K key);

    Stream<K> keys();

    Stream<Map.Entry<K, V>> all();

    CompletableFuture<Void> drop();

}