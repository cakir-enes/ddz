package dds.service.store.infinispan;

import dds.service.Serde;
import dds.service.store.TopicStore;
import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.client.hotrod.impl.query.RemoteQueryFactory;
import org.infinispan.commons.api.CacheContainerAdmin;
import org.infinispan.commons.util.InfinispanCollections;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransientTopicStore<T> implements TopicStore<T> {

    private final RemoteCache<String, byte[]> cache;
    private final Serde<T> serde;

    public TransientTopicStore(RemoteCache<String, byte[]> cache, Serde<T> serde) {
        this.cache = cache;
        this.serde = serde;
    }

    @Override
    public CompletableFuture<Void> put(String key, T topic, Duration duration) {

        return cache.putAsync(key, serde.serialize(topic), duration.toMillis(), TimeUnit.MILLISECONDS).thenAccept(c -> {});
    }

    @Override
    public CompletableFuture<T> get(String key) {
        return cache.getAsync(key).thenApply(serde::deserialize);
    }

    @Override
    public CompletableFuture<Map<String, T>> get(Set<String> keys) {
        return cache.getAllAsync(keys)
                .thenApply(m -> m.entrySet()
                        .stream()
                        .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), serde.deserialize(e.getValue())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    @Override
    public CompletableFuture<Void> delete(Set<String> keys) {
        List<Void> collect = keys.stream().map(k -> cache.removeAsync(k).thenAccept(x -> {
        })).map(CompletableFuture::join).collect(Collectors.toList());
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> delete(String key) {
        return cache.removeAsync(key).thenAccept(i -> {});
    }

    @Override
    public Stream<String> keys() {
        return cache.keySet().stream();
    }

    @Override
    public Stream<Map.Entry<String, T>> all() {
        return cache.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), serde.deserialize(e.getValue())));
    }

    @Override
    public CompletableFuture<Void> drop() {
        return cache.clearAsync();
    }

    static class Address {
//        private static AtomicInteger i = new AtomicInteger(1);

        private String addr = UUID.randomUUID().toString();
        int id;
        public Address() {
            this.id = 2;
        }
        public Address(int i) {
            id = i;
        }

    }
}
