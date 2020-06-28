package dds.service.pubsub.nats;

import dds.service.Serde;
import dds.service.pubsub.PubSub;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class NatsPubSub implements PubSub {

    private final Supplier<Connection> nats;
    private Map<String, Dispatcher> dispatchers;

    public NatsPubSub(Supplier<Connection> nats) {
        this.nats = nats;
        this.dispatchers = new ConcurrentHashMap<>();
    }

    @Override
    public void publish(String topicName, byte[] topic) {
        nats.get().publish(topicName, topic);
    }

    @Override
    public TransferQueue<byte[]> subscribe(String topicName) {
        TransferQueue<byte[]> queue = new LinkedTransferQueue<>();
        dispatchers.computeIfAbsent(topicName, t -> {
            Dispatcher dispatcher = nats.get().createDispatcher(msg -> queue.transfer(msg.getData()));
            dispatcher.subscribe(topicName);
            return dispatcher;
        });
        return queue;
    }

    @Override
    public void unsubscribe(String topicName) {
        Dispatcher disp = dispatchers.remove(topicName);
        if (disp != null)
            nats.get().closeDispatcher(disp);
    }
}