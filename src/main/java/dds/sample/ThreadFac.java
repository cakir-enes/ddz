package dds.sample;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFac implements ThreadFactory {


    public final String prefix;
    private final AtomicInteger count = new AtomicInteger(1);

    public ThreadFac(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, prefix + "-" + count.getAndIncrement());
        return thread;
    }
}
