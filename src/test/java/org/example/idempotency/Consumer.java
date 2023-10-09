package org.example.idempotency;

import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.idempotency.annotation.Idempotent;
import org.example.idempotency.annotation.IdempotentParameter;
import org.example.idempotency.annotation.IdempotentResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Getter
@Component
@Idempotent
@RequiredArgsConstructor
public class Consumer {

  private final IdempotencyProcessor idempotencyProcessor;
  private final Counter counter;
  private volatile CountDownLatch latch = new CountDownLatch(1);
  private volatile String key;
  private volatile String value;

  @IdempotentResource(resourceId = "testRequestTopic")
  @KafkaListener(topics = "${test.testRequestTopic}")
  @SendTo("${test.testReplyTopic}")
  public String receiveFromTestTopic(
      @IdempotentParameter ConsumerRecord<?, ?> consumerRecord
  ) {
    value = consumerRecord.value().toString();
    counter.inc();
    return "Forwarded " + value;
  }

  @KafkaListener(topics = "${test.testReplyTopic}")
  public void receiveFromTestReplyTopic(ConsumerRecord<?, ?> consumerRecord) {
    value = consumerRecord.value().toString();
    latch.countDown();
  }

  public void reset() {
    latch = new CountDownLatch(1);
    key = "";
    value = "";
  }
}
