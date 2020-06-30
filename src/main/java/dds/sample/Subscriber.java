package dds.sample;

import dds.service.Serde;
import dds.service.TopicService;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Subscriber {
    static int SUBSCRIBER_COUNT = 1000;
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
        ArrayList<CountDownLatch> countDownLatches = new ArrayList<>(SUBSCRIBER_COUNT);
        for (int i = 0; i < SUBSCRIBER_COUNT; i++) {
            countDownLatches.add(new CountDownLatch(Publisher.THREAD_COUNT * Publisher.PER_THREAD_TOPIC - 1));
        }
        TopicService<Address> ts = TopicService.createFor(Address.class, TopicService.Mode.VOLATILE, "scopee");
        AtomicInteger idxx = new AtomicInteger();
        Connection connect = Nats.connect();
        Dispatcher dispatcher = connect.createDispatcher(message -> {
        });
        for (int i = 0; i < 100; i++) {


            for (int j = 0; j < 10; j++) {


                    final int idx = idxx.getAndIncrement();
                    Consumer<Address> addressConsumer = add -> {

                        System.out.println(idx + ": " + countDownLatches.get(idx).getCount());
                        countDownLatches.get(idx).countDown();
                    };

                    ts.subscribe(addressConsumer);

            }
        }
        System.err.println(idxx.get());
        for (int i = 0; i < SUBSCRIBER_COUNT; i++) {
            countDownLatches.get(i).await();
        }
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
