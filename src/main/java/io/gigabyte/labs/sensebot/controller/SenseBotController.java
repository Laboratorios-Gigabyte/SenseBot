package io.gigabyte.labs.sensebot.controller;


import io.gigabyte.labs.sensebot.model.MessageRequest;
import io.gigabyte.labs.sensebot.service.generator.GeneratorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/generator")
public class SenseBotController {

    private final GeneratorService generatorService;

    public SenseBotController(GeneratorService generatorService) {
        this.generatorService = generatorService;
    }


    @PostMapping("publishes")
    public String publishMessages(@RequestBody MessageRequest messageRequest) {
        String topicName = messageRequest.topicName();
        int numberOfMessages = messageRequest.numberOfMessages();
//        generatorService.produceMessages(topicName, numberOfMessages, messageRequest.queue());
        generatorService.produceMessagesParallelStreams(topicName, numberOfMessages, messageRequest.sleep());
        return "Messages %d published to topic: %s".formatted(numberOfMessages, topicName);
    }



}
