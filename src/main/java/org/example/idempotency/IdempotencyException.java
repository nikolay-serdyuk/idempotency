package org.example.idempotency;

public class IdempotencyException extends RuntimeException {

  public IdempotencyException(String message) {
    super(message);
  }

  public IdempotencyException(Throwable t) {
    super(t);
  }
}
