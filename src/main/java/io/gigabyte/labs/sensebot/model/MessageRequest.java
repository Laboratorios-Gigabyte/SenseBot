package io.gigabyte.labs.sensebot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MessageRequest(
  @JsonProperty("topic_name")
  String topicName,
  @JsonProperty("number_of_messages_to_be_generated")
  Integer numberOfMessages,
  @JsonProperty("sleep_milliseconds")
  int sleep,

  @JsonProperty("queue_type")
  String queue

) {
    // No need to define constructors or getter/setter methods
}
