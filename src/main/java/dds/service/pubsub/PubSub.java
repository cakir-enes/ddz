package dds.service.pubsub;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TransferQueue;
import java.util.function.Consumer;

public interface PubSub<T> {

    ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    void publish(T topic);

    void subscribe(Consumer<T> handler);

    void unsubscribe(Consumer<T> handler);

}
