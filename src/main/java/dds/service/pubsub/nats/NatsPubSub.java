package dds.service.pubsub.nats;

import dds.service.Serde;
import dds.service.pubsub.PubSub;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NatsPubSub<T> implements PubSub<T> {

    private final Connection nats;
    private final Serde<T> serde;
    private final String topicName;
    private final List<Consumer<T>> consumers;
    private Dispatcher dispatcher;


    public NatsPubSub(Connection nats, Serde<T> serde, String topicName) {
        this.nats = nats;
        this.serde = serde;
        this.topicName = topicName;
        this.consumers = new ArrayList<>();
    }

    public void publish(T topic) {
        nats.publish(topicName, serde.serialize(topic));
    }

    public void subscribe(Consumer<T> handler) {
        getDispatcher();
        synchronized (consumers) {
            consumers.add(handler);
        }
    }

    public void unsubscribe(Consumer<T> handler) {
        synchronized (consumers) {
            consumers.remove(handler);
            if (consumers.isEmpty()) {
                nats.closeDispatcher(dispatcher);
            }
        }
    }

    private Dispatcher getDispatcher() {
        if (dispatcher == null || !dispatcher.isActive()) {
            dispatcher = nats.createDispatcher(msg -> {
                T topic = serde.deserialize(msg.getData());
                notifyListeners(topic);
            });
            dispatcher.subscribe(topicName);
        }
        return dispatcher;
    }

    private void notifyListeners(T topic) {
        consumers.forEach(c -> {
            System.out.println("[" + Thread.currentThread().getName() + "] executing...");
            pool.execute(() -> c.accept(topic));
        });
    }
}
