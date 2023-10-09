package org.example.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class KafkaTest extends BaseIntegrationTest {

  private final static int TIMEOUT = 3;

  @Autowired
  private Consumer consumer;

  @Autowired
  private Producer producer;

  @Autowired
  private Counter counter;

  @Value("${test.testRequestTopic}")
  private String testRequestTopic;

  @BeforeEach
  void beforeEach() {
    counter.reset();
    consumer.reset();
  }

  @Test
  void requestWithReplyTest() throws Exception {
    String value = "Simple message #2";

    producer.send(testRequestTopic, value);
    boolean messageConsumed = consumer.getLatch().await(TIMEOUT, TimeUnit.SECONDS);
    assertTrue(messageConsumed);
    assertTrue(consumer.getKey().isEmpty());
    assertThat(consumer.getValue()).isEqualTo("Forwarded " + value);
    assertThat(counter.getCounter().get()).isEqualTo(1);

    consumer.reset();

    producer.send(testRequestTopic, value);
    messageConsumed = consumer.getLatch().await(TIMEOUT, TimeUnit.SECONDS);
    assertTrue(messageConsumed);
    assertTrue(consumer.getKey().isEmpty());
    assertThat(consumer.getValue()).isEqualTo("Forwarded " + value);
    assertThat(counter.getCounter().get()).isEqualTo(1);

  }
}
