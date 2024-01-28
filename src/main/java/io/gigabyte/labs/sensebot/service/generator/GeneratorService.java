package io.gigabyte.labs.sensebot.service.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.javafaker.Faker;
import io.gigabyte.labs.sensebot.producer.KafkaProducerService;
import io.gigabyte.labs.sensebot.service.concurrent.BackpressureThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

@Service
public class GeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratorService.class);

    private final BackpressureThreadPoolExecutor threadPoolExecutor; // Use the custom executor
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

    public GeneratorService(BackpressureThreadPoolExecutor threadPoolExecutor, KafkaProducerService kafkaProducerService) {
        this.threadPoolExecutor = threadPoolExecutor;
        this.kafkaProducerService = kafkaProducerService;
    }

    public void produceMessages(String topicName, int numberOfMessages, int sleep) {
//        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        for (int i = 0; i < numberOfMessages; i++) {
            CompletableFuture.runAsync(() -> message(topicName, sleep, "COMPLETABLE_FUTURE"), threadPoolExecutor);
        }
    }

    public void produceMessagesParallelStreams(String topicName, int numberOfMessages, int sleep) {
        IntStream.range(0, numberOfMessages)
          .parallel() // Convert to a parallel stream
          .forEach(i -> message(topicName, sleep, "PARALLEL_STREAMS"));
    }

    private void message(String topicName, int sleep, String tag) {
        try {
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
            Map<String, Object> json2Publish = new HashMap<>();
            String sensor = faker.internet().macAddress("sensor_");
            json2Publish.put("sensor_name", sensor);
            json2Publish.put("sensor_metadata", getSensorField());
            json2Publish.put("value", faker.number().randomDouble(5, 0, 100));
            String message = objectMapper.writeValueAsString(json2Publish);
            LOGGER.info("GeneratorService::generatedJsonString::{} json={}", tag, message);
            kafkaProducerService.sendMessage(topicName, sensor, message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Proper interruption handling
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
