package dds.service;

import dds.service.pubsub.PubSub;
import dds.service.store.TopicStore;

import java.time.Duration;
import java.util.function.Consumer;

public class TopicService<T> {

    enum Mode {
        VOLATILE,
        TRANSIENT,
        PERSISTENT
    }

    private final Mode mode;
    private final TopicStore<T> transientStore;
    private final TopicStore<T> persistentStore;
    private final PubSub<T> pubsub;

    public TopicService(Mode mode, String scope, PubSub<T> pubSub, TopicStore<T> transientStore, TopicStore<T> persistentStore) {
        this.mode = mode;
        this.transientStore = transientStore;
        this.persistentStore = persistentStore;
        this.pubsub = pubSub;
    }

    public void publish(String key, T topic) {
        publish(key, topic, TopicStore.NO_EXPIRATION);
    }

    public void publish(String key, T topic, Duration duration) {
        this.pubsub.publish(topic);
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
        pubsub.subscribe(consumer);
    }

    public void unsubscribe(Consumer<T> consumer) {
        pubsub.unsubscribe(consumer);
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