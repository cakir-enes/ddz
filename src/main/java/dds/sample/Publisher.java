package dds.sample;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.ByteBufferOutput;
import com.esotericsoftware.kryo.io.ByteBufferOutputStream;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import dds.service.TopicService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Publisher {

    public static int THREAD_COUNT = 100;
    public static int PER_THREAD_TOPIC = 400;

    public static void main(String[] args) throws Exception {

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
                ts.publish("a", new Subscriber.Address(3));
                System.out.printf("[%s] publishing %d\n", Thread.currentThread().getName(), countDownLatch.getCount());
                countDownLatch.countDown();
            }, 0, 2, TimeUnit.MILLISECONDS);
        }

        latch.await();
        long duration = Duration.between(now, Instant.now()).toMillis();
        long durationSec = TimeUnit.MILLISECONDS.toSeconds(duration);
        System.out.println("Pub in " + durationSec + "sec");
        System.out.println((THREAD_COUNT * PER_THREAD_TOPIC) / durationSec + "msg/sec");
    }
}
