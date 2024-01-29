package io.gigabyte.labs.sensebot.config;

import io.gigabyte.labs.sensebot.service.concurrent.BackpressureThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    // Define backpressure thresholds
    private static final int HIGH_WATERMARK = 240; // Adjust as needed
    private static final int LOW_WATERMARK = 120;  // Adjust as needed

    @Bean("backpressureThreadPoolExecutor")
    public BackpressureThreadPoolExecutor backpressureThreadPoolExecutor() {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
          15, // core pool size
          20, // maximum pool size
          60L, // idle thread keep-alive time
          TimeUnit.SECONDS, // keep-alive time unit
          new LinkedBlockingQueue<>(300) // work queue
        );
        return new BackpressureThreadPoolExecutor(executor, HIGH_WATERMARK, LOW_WATERMARK);
    }

    @Bean("threadPoolExecutorSynchronousQueue")
    public ThreadPoolExecutor threadPoolExecutor () {
        return new ThreadPoolExecutor(
          30, // core pool size
          60, // maximum pool size
          60L, // idle thread keep-alive time
          TimeUnit.SECONDS, // keep-alive time unit
          new SynchronousQueue<>() // work queue
        );
    }

}
