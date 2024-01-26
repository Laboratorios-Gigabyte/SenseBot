package io.gigabyte.labs.sensebot.config;

import io.gigabyte.labs.sensebot.service.concurrent.BackpressureThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    // Define backpressure thresholds
    private static final int HIGH_WATERMARK = 80; // Adjust as needed
    private static final int LOW_WATERMARK = 40;  // Adjust as needed

    @Bean
    public BackpressureThreadPoolExecutor threadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
          4, // core pool size
          10, // maximum pool size
          60L, // idle thread keep-alive time
          TimeUnit.SECONDS, // keep-alive time unit
          new LinkedBlockingQueue<>(100) // work queue
        );

        return new BackpressureThreadPoolExecutor(executor, HIGH_WATERMARK, LOW_WATERMARK);
    }

}
