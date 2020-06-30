package dds.service.cache.infinispan;

import dds.service.cache.ICacheService;
import org.infinispan.client.hotrod.RemoteCache;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InfinispanCacheService<V> implements ICacheService<String, V> {
    private final RemoteCache<String, byte[]> cache;
    private final Function<V, byte[]> serialize;
    private final Function<byte[], V> deserialize;


    public InfinispanCacheService(String namespace, Function<V, byte[]> serialize, Function<byte[], V> deserialize) {
        this.cache = RemoteCacheFactory.createCache(namespace);
        this.serialize = serialize;
        this.deserialize = deserialize;
    }

    public InfinispanCacheService(Function<V, byte[]> serialize, Function<byte[], V> deserialize) {
        this("default", serialize, deserialize);
    }

    @Override
    public CompletableFuture<Void> put(String key, V data) {
        return cache.putAsync(key, serialize.apply(data)).thenAccept(c -> {});
    }

    @Override
    public CompletableFuture<Void> put(String key, V data, Duration duration) {
        return cache.putAsync(key, serialize.apply(data), duration.toMillis(), TimeUnit.MILLISECONDS).thenAccept(c -> {});
    }

    @Override
    public CompletableFuture<V> get(String key) {
        return cache.getAsync(key).thenApply(deserialize);
    }

    @Override
    public CompletableFuture<Map<String, V>> get(Set<String> keys) {
        return cache.getAllAsync(keys)
                .thenApply(m -> m.entrySet()
                        .stream()
                        .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), deserialize.apply(e.getValue())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public CompletableFuture<Void> delete(String key) {
        return cache.removeAsync(key).thenAccept(c -> {});
    }

    @Override
    public CompletableFuture<Void> delete(Set<String> keys) {
                List<Void> collect = keys.stream().map(k -> cache.removeAsync(k).thenAccept(x -> {
        })).map(CompletableFuture::join).collect(Collectors.toList());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<V> getAndDelete(String key) {
        return cache.removeAsync(key).thenApply(deserialize);
    }

    @Override
    public Stream<String> keys() {
        return cache.keySet().stream();
    }

    @Override
    public CompletableFuture<Boolean> contains(String key) {
        return cache.containsKeyAsync(key);
    }

    @Override
    public Stream<Map.Entry<String, V>> all() {
        return cache.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), deserialize.apply(e.getValue())));
    }

    @Override
    public CompletableFuture<Void> drop() {
        return cache.clearAsync();
    }
}