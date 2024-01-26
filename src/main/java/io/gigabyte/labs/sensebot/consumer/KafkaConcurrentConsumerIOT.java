package io.gigabyte.labs.sensebot.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConcurrentConsumerIOT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConcurrentConsumerIOT.class);

    @KafkaListener(topics = "STREAMING_TOPIC_SENSOR_DATA", groupId = "IOT_DATA")
    public void listen(String message) {
        LOGGER.info("Received message in group {}: {}", "IOT_DATA", message);
    }

}
