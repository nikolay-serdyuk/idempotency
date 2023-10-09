package org.example.idempotency;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IdempotentRequest {

  private String resourceId;
  private List<Object> parameters;
}
