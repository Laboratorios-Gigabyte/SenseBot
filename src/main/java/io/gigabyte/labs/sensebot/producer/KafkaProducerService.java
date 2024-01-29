package io.gigabyte.labs.sensebot.producer;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String key, String message, long start) {
        CompletableFuture<SendResult<String, String>> sendResultCompletableFuture = kafkaTemplate.send(topic, key, message);
        sendResultCompletableFuture.whenComplete((sendResult, throwable) -> {
            if (ObjectUtils.isEmpty(throwable)) {
                //success
                RecordMetadata recordMetadata = sendResult.getRecordMetadata();
                log.info("Time Taken to Produce Data: {}", System.currentTimeMillis() - start);
//                log.info("Kafka::producer topic={}, partition={}, offset={}", topic, recordMetadata.partition(), recordMetadata.offset());
            } else {
                //failure
                log.info("Kafka::producer::exception topic={}", topic, throwable);
            }
        });
    }
}
