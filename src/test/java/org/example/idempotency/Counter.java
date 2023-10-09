package org.example.idempotency;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class Counter {
  private final AtomicInteger counter = new AtomicInteger();

  public void reset() {
    counter.set(0);
  }

  public void inc() {
    counter.incrementAndGet();
  }

  public void throwException() {
    throw new IdempotencyException("test");
  }
}
