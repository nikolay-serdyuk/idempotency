package org.example.idempotency;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class Producer {
  @Autowired
  private KafkaTemplate<String, Object> kafkaTemplate;

  public void send(String topic, String value) {
    kafkaTemplate.send(topic, value);
  }

  public void send(String topic, String key, String value) {
    kafkaTemplate.send(topic, key, value);
  }
}
