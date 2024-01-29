package io.gigabyte.labs.sensebot.service.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javafaker.Faker;
import io.gigabyte.labs.sensebot.producer.KafkaProducerService;
import io.gigabyte.labs.sensebot.service.concurrent.BackpressureThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.IntStream;

@Service
public class GeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorService.class);

    private final BackpressureThreadPoolExecutor backpressureThreadPoolExecutor;
    private final ThreadPoolExecutor threadPoolExecutor;// Use the custom executor
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Faker faker = new Faker();
    private static final List<SensorData> options;

    static {
        options = new ArrayList<>();
        options.add(new SensorData("temperature", "C"));
        options.add(new SensorData("temperature", "F"));
        options.add(new SensorData("altitude, longitude", "RAD"));
        options.add(new SensorData("pressure", "Bars"));
        options.add(new SensorData("humidity", "Relative humidity"));
    }

    public GeneratorService(@Qualifier("backpressureThreadPoolExecutor") BackpressureThreadPoolExecutor backpressureThreadPoolExecutor, KafkaProducerService kafkaProducerService, @Qualifier("threadPoolExecutorSynchronousQueue") ThreadPoolExecutor threadPoolExecutor) {
        this.backpressureThreadPoolExecutor = backpressureThreadPoolExecutor;
        this.kafkaProducerService = kafkaProducerService;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void produceMessages(String topicName, int numberOfMessages, String executor) {
//        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        if (executor.equalsIgnoreCase("SYNCHRONOUS_QUEUE")) {
            for (int i = 0; i < numberOfMessages; i++) {
                CompletableFuture.runAsync(() -> message(topicName, "COMPLETABLE_FUTURE_WITH_SYNCHRONOUS_QUEUE"), threadPoolExecutor);
            }
        } else if (executor.equalsIgnoreCase("BACK_PRESSURE")){
            for (int i = 0; i < numberOfMessages; i++) {
                CompletableFuture.runAsync(() -> message(topicName, "COMPLETABLE_FUTURE"), backpressureThreadPoolExecutor);
            }
        } else {
            for (int i = 0; i < numberOfMessages; i++) {
                message(topicName, "BLOCKING_MAIN_THREAD");
            }
        }
    }

    public void produceMessagesParallelStreams(String topicName, int numberOfMessages, int sleep) {
        IntStream.range(0, numberOfMessages)
          .parallel() // Convert to a parallel stream
          .forEach(i -> message(topicName, "PARALLEL_STREAMS"));
    }

    private void message(String topicName, String tag) {
        try {
            Map<String, Object> json2Publish = new HashMap<>();
            String sensor = faker.internet().macAddress("sensor_");
            json2Publish.put("sensor_name", sensor);
            json2Publish.put("sensor_metadata", getSensorField());
            json2Publish.put("value", faker.number().randomDouble(5, 0, 100));
            String message = objectMapper.writeValueAsString(json2Publish);
            LOGGER.info("GeneratorService::generatedJsonString::{} json={}", tag, message);
            kafkaProducerService.sendMessage(topicName, sensor, message, System.currentTimeMillis());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private SensorData getSensorField() {
        // Create a Random object
        Random random = new Random();

        // Generate a random index within the bounds of the list
        int randomIndex = random.nextInt(options.size());

        // Retrieve the randomly selected option
        return options.get(randomIndex);
    }

}

record SensorData (
    String field,
    String unitOfMeasure
){
}
