package io.gigabyte.labs.sensebot.service.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackpressureThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackpressureThreadPoolExecutor.class);
    private final int highWatermark;
    private final int lowWatermark;

    public BackpressureThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor, int highWaterMark, int lowWaterMark) {
        super(
          threadPoolExecutor.getCorePoolSize(),
          threadPoolExecutor.getMaximumPoolSize(),
          threadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS),
          TimeUnit.SECONDS,
          threadPoolExecutor.getQueue()
        );
        this.highWatermark = highWaterMark;
        this.lowWatermark = lowWaterMark;
    }

    @Override
    public void execute(Runnable command) {
        int queueSize = getQueue().size();

        if (queueSize >= highWatermark) {
            LOGGER.info("Queue is full. Slowing down task submission.");
            // Sleep for a while to slow down task submission
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        super.execute(command);

        if (queueSize <= lowWatermark) {
            LOGGER.info("Queue has room. Resuming task submission.");
        }
    }

}
