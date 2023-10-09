package org.example.idempotency.repository;

import edu.umd.cs.findbugs.annotations.ReturnValuesAreNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@ReturnValuesAreNonnullByDefault
public interface IdempotentRepository {

  boolean contains(String key);

  void reserve(String key);

  void update(String key, String value);

  String get(String key);

  void remove(String key);
}
