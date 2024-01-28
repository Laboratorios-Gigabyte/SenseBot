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
            // Sleep for a while to slow down task submission
            int maxDelayMillis = 60000; // Maximum delay of 60 seconds
            int delayMillis = 1000; // Initial delay of 1 second
            while (delayMillis < maxDelayMillis) {
                try {
                    LOGGER.info("Queue is full. Slowing down task submission: {} millis", delayMillis);
                    Thread.sleep(delayMillis);
                    delayMillis *= 2; // Double the delay for each retry
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        super.execute(command);

        if (queueSize <= lowWatermark) {
            LOGGER.info("Queue has room. Resuming task submission.");
        }
    }

}
