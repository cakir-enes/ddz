package dds.service.sample;

import dds.service.pubsub.PubSubFactory;
import dds.service.pubsub.nats.NatsPubSub;
import io.nats.client.Connection;
import io.nats.client.Nats;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;

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

    public static void main(String[] args) {

        for (int i = 0; i < 1; i++) {
            sync();
        }

    }

    static void sync() {
//        NatsTopicService<Address> ts = NatsTopicService.of(Address.class, "real", "system");
        Instant now = Instant.now();
        ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        ExecutorService pool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());

        NatsPubSub<Address> ts = PubSubFactory.createFor(Address.class, "real", "system");

        try {

            Connection connect = Nats.connect();
            CountDownLatch latch = new CountDownLatch(COUNT);
            for (int i = 0; i < COUNT; i++) {
                int finalI = i;

                    ts.publish(new Address(finalI));
                    latch.countDown();
                    Thread.sleep(2);
            }
            latch.await();
            System.out.println("Ended in " + Duration.between(now, Instant.now()).toMillis() + "ms");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    }

}
