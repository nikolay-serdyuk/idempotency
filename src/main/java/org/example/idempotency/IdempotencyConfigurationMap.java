package org.example.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyConfigurationMap {

  private Map<String, ObjectMapper> objectMapperMap;
  private Map<String, ResultChecker> resultCheckerMap;
}
