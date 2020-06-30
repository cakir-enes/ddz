package dds.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum Pool {
    ;
    public static ExecutorService notifierPool = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors());
}
