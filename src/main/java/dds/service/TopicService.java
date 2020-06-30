package dds.service;

import dds.sample.ThreadFac;
import dds.service.pubsub.PubSub;
import dds.service.pubsub.nats.NatsPubSub;
import dds.service.store.TopicStore;
import dds.service.store.infinispan.InfinispanTopicStore;
import dds.service.store.infinispan.TransientTopicStore;
import io.nats.client.Connection;
import io.nats.client.Nats;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TopicService<T> {

    public enum Mode {
        VOLATILE,
        TRANSIENT,
        PERSISTENT
    }

//    private final static ExecutorService executorService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
    //    private final static ExecutorService executorService = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
//    private final static ExecutorService executorService = Executors.newFixedThreadPool(8, new ThreadFac("Sub"));
    private final Mode mode;
    private final TopicStore<T> transientStore;
    private final TopicStore<T> persistentStore;
    private final Serde<T> serde;
    private final PubSub pubsub;
    private final String topicName;
    private final List<Consumer<T>> consumers;
    private AtomicBoolean listening;
    private static Connection connect;

    static {

        try {
            connect = Nats.connect();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static <R> TopicService<R> createFor(Class<R> tClass, Mode mode, String scope) throws Exception {

        NatsPubSub natsPubSub = new NatsPubSub(() -> connect);
//        TransientTopicStore<R> topicStore = InfinispanTopicStore.createFor(tClass.getName() + "-" + mode.name() + "-" + scope, Serde.SerdeOptions.json(tClass));
//        TransientTopicStore<R> topicStore2 = InfinispanTopicStore.createFor(tClass.getName() + "-PP-" + mode.name() + "-" + scope, Serde.SerdeOptions.json(tClass));
        TransientTopicStore<R> topicStore = null;
        TransientTopicStore<R> topicStore2 = null;
        return new TopicService<>(tClass, mode, scope, Serde.SerdeOptions.kryo(tClass), natsPubSub, topicStore, topicStore2);
    }

    public TopicService(Class<T> clazz, Mode mode, String scope, Serde<T> serde, PubSub pubSub, TopicStore<T> transientStore, TopicStore<T> persistentStore) {
        this.mode = mode;
        this.transientStore = transientStore;
        this.persistentStore = persistentStore;
        this.pubsub = pubSub;
        this.serde = serde;
        this.topicName = clazz.getName() + "-" + mode.name() + "-" + scope;
        this.consumers = new ArrayList<>();
        this.listening = new AtomicBoolean(false);
    }

    public void publish(String key, T topic) {
        publish(key, topic, TopicStore.NO_EXPIRATION);
    }

    public void publish(String key, T topic, Duration duration) {
        pubsub.publish(topicName, serde.serialize(topic));
        switch (mode) {
            case VOLATILE:
                break;
            case TRANSIENT:
                transientStore.put(key, topic, duration);
                break;
            case PERSISTENT:
                persistentStore.put(key, topic, duration);
                break;
        }
    }

    public void subscribe(Consumer<T> consumer) {
        notifyWithPast(consumer);
        synchronized (consumers) {
            consumers.add(consumer);
            checkChannel();
        }
    }

    public void unsubscribe(Consumer<T> consumer) {
        synchronized (consumers) {
            consumers.remove(consumer);
        }
    }

    public void delete(String key) {
        transientStore.delete(key);
        persistentStore.delete(key);
    }

    private final static ExecutorService executorService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
    private void checkChannel() {
        if (listening.get())
            return;
        listening.set(true);
        final TransferQueue<byte[]> queue = pubsub.subscribe(topicName);
        CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    if (consumers.isEmpty()) {
                        listening.compareAndSet(true, false);
                        pubsub.unsubscribe(topicName);
                        System.out.println("DONE");
                        return;
                    }
                    byte[] serialized = queue.take();
                    T topic = serde.deserialize(serialized);
                    synchronized (consumers) {
                        consumers.forEach(c ->
                            // If I run this with statically allocated `executorService.execute` it doesnt execute.
                            executorService.execute(() -> c.accept(topic))
                    );
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void notifyWithPast(Consumer<T> consumer) {
        switch (mode) {
            case VOLATILE:
                break;
            case TRANSIENT:
                transientStore.all().forEach(e -> consumer.accept(e.getValue()));
                break;
            case PERSISTENT:
                persistentStore.all().forEach(e -> consumer.accept(e.getValue()));
                break;
        }
    }
}