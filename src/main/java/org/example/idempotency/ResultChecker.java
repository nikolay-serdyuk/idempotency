package org.example.idempotency;

public interface ResultChecker {

  boolean isOk(Object o);
}
