package dds.service;

import dds.service.pubsub.PubSub;
import dds.service.store.TopicStore;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TransferQueue;
import java.util.function.Consumer;

public class TopicService<T> {

    public enum Mode {
        VOLATILE,
        TRANSIENT,
        PERSISTENT
    }
    private final static ExecutorService executorService = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

    private final Mode mode;
    private final TopicStore<T> transientStore;
    private final TopicStore<T> persistentStore;
    private final Serde<T> serde;
    private final PubSub pubsub;
    private final String topicName;
    private final List<Consumer<T>> consumers;
    private TransferQueue<byte[]> channel;

    public TopicService(Class<T> clazz, Mode mode, String scope, Serde<T> serde, PubSub pubSub, TopicStore<T> transientStore, TopicStore<T> persistentStore) {

        this.mode = mode;
        this.transientStore = transientStore;
        this.persistentStore = persistentStore;
        this.pubsub = pubSub;
        this.serde = serde;
        this.topicName = clazz.getName() + "-" + mode.name() + "-" + scope;
        this.consumers = new ArrayList<>();
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

    private void checkChannel() {
        if (channel != null)
            return;
        channel = pubsub.subscribe(topicName);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                while (true) {
                    if (consumers.isEmpty()) {
                        channel = null;
                        pubsub.unsubscribe(topicName);
                        return;
                    }
                    byte[] serialized = channel.take();
                    T topic = serde.deserialize(serialized);
                    executorService.execute(() -> {
                        synchronized (consumers) {
                            consumers.forEach(c -> executorService.execute(() -> c.accept(topic)));
                        }
                    });
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
                transientStore.all().forEach(consumer);
                break;
            case PERSISTENT:
                persistentStore.all().forEach(consumer);
                break;
        }
    }
}