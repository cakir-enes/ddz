package dds.sample;

import dds.service.Serde;
import dds.service.TopicService;
import dds.service.pubsub.PubSubFactory;
import dds.service.pubsub.nats.NatsPubSub;
import dds.service.store.InmemoryTopicStore;
import io.nats.client.Connection;
import io.nats.client.Nats;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Lele {

    public static void main(String[] args) throws Exception {

        Connection connect = Nats.connect();
        Supplier<Connection> s = () -> connect;
        TopicService<App.Address> ts =  new TopicService<>(
                App.Address.class,
                TopicService.Mode.TRANSIENT,
                "system",
                Serde.SerdeOptions.json(App.Address.class),
                new NatsPubSub(s),
                new InmemoryTopicStore<>(),
                new InmemoryTopicStore<>());


//        TransferQueue<String> transferQueue = new LinkedTransferQueue<>();
//
        AtomicInteger i = new AtomicInteger(0);

        Instant now = Instant.now();

        Serde<App.Address> json = Serde.SerdeOptions.json(App.Address.class);
        CountDownLatch latch = new CountDownLatch(100_000);
        Executors.newSingleThreadExecutor().execute(() -> {
            for (int j = 0; j < 50_000; j++) {
            ts.publish(j + "", new App.Address(j));
//                connect.publish("abc", json.serialize(new App.Address()));
                latch.countDown();
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Executors.newSingleThreadExecutor().execute(() -> {
            for (int j = 0; j < 50_000; j++) {
            ts.publish(j + "", new App.Address(j));
//                connect.publish("abc", json.serialize(new App.Address()));
                latch.countDown();
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

//        -119519ms

        latch.await();
        System.out.println("Pub in " + TimeUnit.MILLISECONDS.toSeconds(Duration.between(now, Instant.now()).toMillis()) + "sec");

//
//        Executors.newSingleThreadExecutor().execute(() -> {
//            try {
//                while (true) {
//                    transferQueue.transfer(i.incrementAndGet() + "");
//                }
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//
//        Executors.newSingleThreadExecutor().execute(() -> {
//            try {
//                while (true) {
//                    String take = transferQueue.take();
//                    System.out.println("Here: " + take);
//                    Thread.sleep(1000);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
    }
}
