package dds.service.sample;

import dds.service.pubsub.nats.NatsPubSub;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class Lele {

    public static void main(String[] args) {
        NatsPubSub<App.Address> ts = NatsPubSub.of(App.Address.class, "real", "system");
        ArrayList<CountDownLatch> latches = new ArrayList<>();
        ArrayList<Instant> nows = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CountDownLatch l = new CountDownLatch(App.COUNT);
            latches.add(l);
            System.out.println("Started " + i);
            Instant now = Instant.now();
            nows.add(now);
            ts.subscribe((topic) -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("Topic: " + l.getCount());
                l.countDown();
            });
        }

        try {
            for (int i = 0; i < latches.size(); i++) {
                latches.get(i).await();
                System.err.println("END " + i + " " + Duration.between(Instant.now(), nows.get(i)).toMillis() + "ms");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
