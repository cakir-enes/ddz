package dds.sample;

import dds.service.Serde;
import dds.service.TopicService;
import dds.service.pubsub.nats.NatsPubSub;

import io.nats.client.Connection;
import io.nats.client.Nats;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class Publisher {

    public static void main(String[] args) throws Exception {

        TopicService<Subscriber.Address> ts = TopicService.createFor(Subscriber.Address.class, TopicService.Mode.TRANSIENT, "scopee");

        AtomicInteger i = new AtomicInteger(0);

        Instant now = Instant.now();
        Serde<Subscriber.Address> json = Serde.SerdeOptions.json(Subscriber.Address.class);
        CountDownLatch latch = new CountDownLatch(100_000);
        Executors.newSingleThreadExecutor().execute(() -> {
            for (int j = 0; j < 50_000; j++) {
            ts.publish(j + "", new Subscriber.Address(j));
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
            ts.publish(j + "", new Subscriber.Address(j));
//                connect.publish("abc", json.serialize(new App.Address()));
                latch.countDown();
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        latch.await();
        System.out.println("Pub in " + TimeUnit.MILLISECONDS.toSeconds(Duration.between(now, Instant.now()).toMillis()) + "sec");
    }
}
