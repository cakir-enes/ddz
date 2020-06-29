package dds.sample;

import dds.service.TopicService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Publisher {

    public static int THREAD_COUNT = 100;
    public static int PER_THREAD_TOPIC = 1000;

    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("RUNNNIING");
            }
        });

        Instant now = Instant.now();
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ThreadFac threadFac = new ThreadFac("PUBLISHER");


        for (int i = 0; i < THREAD_COUNT; i++) {
            TopicService<Subscriber.Address> ts = TopicService.createFor(Subscriber.Address.class, TopicService.Mode.VOLATILE, "scopee");
            CountDownLatch countDownLatch = new CountDownLatch(PER_THREAD_TOPIC);
            Executors.newSingleThreadScheduledExecutor(threadFac).scheduleAtFixedRate(() -> {
                if (countDownLatch.getCount() == 1) {
                    latch.countDown();
                    System.out.printf("[%s] Done. Remaining: %d\n", Thread.currentThread().getName(), latch.getCount());
                } else if (countDownLatch.getCount() == 0) {
                    return;
                }
                System.out.printf("[%s] publishing %d\n", Thread.currentThread().getName(), countDownLatch.getCount());
                ts.publish("a", new Subscriber.Address(3));
                countDownLatch.countDown();
            }, 0, 2, TimeUnit.MILLISECONDS);
        }
        latch.await();
        System.out.println("Pub in " + TimeUnit.MILLISECONDS.toSeconds(Duration.between(now, Instant.now()).toMillis()) + "sec");
    }
}
