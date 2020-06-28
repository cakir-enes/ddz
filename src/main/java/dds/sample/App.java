package dds.sample;

import dds.service.Serde;
import dds.service.TopicService;
import dds.service.pubsub.PubSubFactory;
import dds.service.pubsub.nats.NatsPubSub;
import dds.service.store.InmemoryTopicStore;
import io.nats.client.Connection;
import io.nats.client.Nats;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class App {
    static int COUNT = 100;
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

    public static void main(String[] args) throws Exception {

        Connection connect = Nats.connect();
        Supplier<Connection> s = () -> connect;
        Serde<App.Address> json = Serde.SerdeOptions.json(App.Address.class);

        CountDownLatch latch = new CountDownLatch(100_000);
        Instant now = Instant.now();
//
//        connect.createDispatcher(msg -> {
//            System.out.println("REC " + latch.getCount());
//            latch.countDown();
//        }).subscribe("abc");
//        latch.await();

//        for (int j = 0; j < 1_000_000; j++) {
////            ts.publish(j + "", new App.Address(j));
//            connect.publish("abc", json.serialize(new App.Address()));
//        }
        TopicService<Address> ts =  new TopicService<>(
                App.Address.class,
                TopicService.Mode.TRANSIENT,
                "system",
                Serde.SerdeOptions.json(App.Address.class),
                new NatsPubSub(s),
                new InmemoryTopicStore<>(),
                new InmemoryTopicStore<>());


        ts.subscribe(add -> {
            System.out.println("REC: " + latch.getCount());
            latch.countDown();
        });
        latch.await();
        System.out.println("Received in " + TimeUnit.MILLISECONDS.toSeconds(Duration.between(now, Instant.now()).toMillis()) + "sec");
    }

    static void sync() {
//        NatsTopicService<Address> ts = NatsTopicService.of(Address.class, "real", "system");
        Instant now = Instant.now();
        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        ExecutorService pool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

//        NatsPubSub<Address> ts = PubSubFactory.createFor(Address.class, "real", "system");
//
//        try {
//
//            Connection connect = Nats.connect();
//            CountDownLatch latch = new CountDownLatch(COUNT);
//            for (int i = 0; i < COUNT; i++) {
//                int finalI = i;
//
//                    ts.publish(new Address(finalI));
//                    latch.countDown();
//                    Thread.sleep(2);
//            }
//            latch.await();
//            System.out.println("Ended in " + Duration.between(now, Instant.now()).toMillis() + "ms");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }



    }

}
