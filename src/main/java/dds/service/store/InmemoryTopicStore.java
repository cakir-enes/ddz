//package dds.service.store;
//
//import com.sun.xml.internal.ws.util.CompletedFuture;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.stream.Stream;
//
//public class InmemoryTopicStore<T> implements TopicStore<T> {
//    private Map<String, T> topics = new ConcurrentHashMap<>();
//    static final ScheduledExecutorService SCHEDULER = new ScheduledThreadPoolExecutor(0);
//
//    private static Executor delayedExecutor(long delay, TimeUnit unit)
//    {
//        return delayedExecutor(delay, unit, ForkJoinPool.commonPool());
//    }
//    private static Executor delayedExecutor(long delay, TimeUnit unit, Executor executor)
//    {
//        return r -> SCHEDULER.schedule(() -> executor.execute(r), delay, unit);
//    }
//
//    @Override
//    public CompletableFuture<Void> put(String key, T topic, Duration duration) {
//        topics.put(key, topic);
//        if (!duration.equals(NO_EXPIRATION)) {
//            CompletableFuture.runAsync(() -> topics.remove(key), delayedExecutor(duration.toMillis(), TimeUnit.MILLISECONDS));
//        }
//        return CompletableFuture.completedFuture(null);
//    }
//
//    @Override
//    public CompletableFuture<T> get(String key) {
//        return CompletableFuture.completedFuture(topics.get(key));
//    }
//
//    @Override
//    public CompletableFuture<List<T>> get(Iterable<String> keys) {
//        return CompletableFuture.supplyAsync(() -> {
//            List<T> topicList = new ArrayList<>();
//            keys.forEach(k -> topicList.add(topics.get(k)));
//            return topicList;
//        });
//    }
//
//    @Override
//    public CompletableFuture<Void> delete(String key) {
//        topics.remove(key);
//        return CompletableFuture.completedFuture(null);
//    }
//
//    @Override
//    public CompletableFuture<Void> delete(Set<String> keys) {
//        return CompletableFuture.runAsync(() -> {
//            keys.forEach(k -> topics.remove(k));
//        });
//    }
//
//    @Override
//    public Stream<String> keys() {
//        return topics.keySet().stream();
//    }
//
//    @Override
//    public Stream<T> all() {
//        return topics.values().stream();
//    }
//
//    @Override
//    public CompletableFuture<Void> drop() {
//        topics.clear();
//        return CompletableFuture.completedFuture(null);
//    }
//}
