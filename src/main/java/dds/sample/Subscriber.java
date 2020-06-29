package dds.sample;

import dds.service.Serde;
import dds.service.TopicService;
import io.nats.client.Connection;
import io.nats.client.Nats;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Subscriber {
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

        CountDownLatch latch = new CountDownLatch(Publisher.THREAD_COUNT * Publisher.PER_THREAD_TOPIC);
        Instant now = Instant.now();
        TopicService<Address> ts = TopicService.createFor(Address.class, TopicService.Mode.VOLATILE, "scopee");
        for (int i = 0; i < 100; i++) {
            int finalI = i;
            CompletableFuture.runAsync(() -> {
                Consumer<Address> addressConsumer = add -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("1: " + latch.getCount());
                    latch.countDown();
                };
                if (finalI % 2 == 0)
                    ts.subscribe(addressConsumer);
                else ts.unsubscribe(addressConsumer);
            });

//            ts.subscribe(add -> {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("2: " + latch.getCount());
//                latch.countDown();
//            });
        }

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
