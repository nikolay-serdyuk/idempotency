package org.example.idempotency;

import java.util.concurrent.CountDownLatch;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootConfiguration
public class TestConfiguration {

  @Bean
  public CountDownLatch countDownLatch() {
    return new CountDownLatch(1);
  }


}
