package org.example.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.example.idempotency.annotation.IdempotentParameter;
import org.example.idempotency.annotation.IdempotentResource;
import org.example.idempotency.hash.KeyGenerator;
import org.example.idempotency.repository.IdempotentRepository;
import org.springframework.stereotype.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.example.idempotency.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor
@ParametersAreNonnullByDefault
public class IdempotencyProcessor {

  private static final ObjectMapper OBJECT_KEY_MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .registerModule(DEFAULT_OBJECT_MAPPER_MODULE)
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

  private final IdempotencyConfigurationMap idempotencyConfigurationMap;
  private final KeyGenerator keyGenerator;
  private final IdempotentRepository idempotentRepository;

  public Object execute(MethodInvocation methodInvocation) throws Throwable {
    IdempotentRequest request = generateIdempotentRequest(methodInvocation);
    log.debug("Started processing request {}", request);

    String json = OBJECT_KEY_MAPPER.writeValueAsString(request);
    String key = keyGenerator.generate(json);

    IdempotentResource annotation = methodInvocation.getMethod()
        .getAnnotation(IdempotentResource.class);
    String objectMapperId = annotation.objectMapperId();
    ObjectMapper objectMapper = StringUtils.isEmpty(objectMapperId) ? DEFAULT_OBJECT_MAPPER :
        idempotencyConfigurationMap.getObjectMapperMap().get(objectMapperId);
    if (objectMapper == null) {
      throw new IdempotencyException("Couldn't find objectMapperId == " + objectMapperId);
    }

    String resultCheckerId = annotation.resultCheckerId();
    ResultChecker resultChecker = StringUtils.isEmpty(resultCheckerId) ? DEFAULT_RESULT_CHECKER :
        idempotencyConfigurationMap.getResultCheckerMap().get(resultCheckerId);
    if (resultChecker == null) {
      throw new IdempotencyException("Couldn't find resultCheckerId == " + resultCheckerId);
    }

    Object result = execute(
        key,
        methodInvocation::proceed,
        objectMapper,
        resultChecker,
        methodInvocation.getMethod().getReturnType()
    );

    log.debug("finished processing request {}", request);

    return result;
  }

  private Object execute(
      String key,
      SupplierWithException<Object, Throwable> supplier,
      ObjectMapper objectMapper,
      ResultChecker resultChecker,
      Class<?> returnType
  ) throws Throwable {

    if (idempotentRepository.contains(key)) {
      String value = idempotentRepository.get(key);
      return objectMapper.readValue(value, returnType);
    }

    idempotentRepository.reserve(key);

    Object result;
    try {
      result = supplier.get();
    } catch (Throwable t) {
      idempotentRepository.remove(key);
      throw t;
    }

    if (resultChecker.isOk(result)) {
      String value = objectMapper.writeValueAsString(result);
      idempotentRepository.update(key, value);
      result = objectMapper.readValue(value, returnType);
    } else {
      idempotentRepository.remove(key);
    }

    return result;
  }

  private IdempotentRequest generateIdempotentRequest(MethodInvocation methodInvocation) {
    List<Object> parameters = new ArrayList<>();
    Annotation[][] annotations = methodInvocation.getMethod().getParameterAnnotations();
    Object[] args = methodInvocation.getArguments();
    for (int i = 0; i < args.length; i++) {
      for (Annotation annotation : annotations[i]) {
        if (annotation instanceof IdempotentParameter) {
          parameters.add(args[i]);
        }
      }
    }

    if (parameters.isEmpty()) {
      throw new IdempotencyException(
          "Couldn't find an argument annotated with @IdempotentParameter"
      );
    }

    IdempotentResource annotation = methodInvocation.getMethod()
        .getAnnotation(IdempotentResource.class);
    if (StringUtils.isBlank(annotation.resourceId())) {
      throw new IdempotencyException("resourceId cannot be blank or empty");
    }

    return new IdempotentRequest(annotation.resourceId(), parameters);
  }

  @FunctionalInterface
  public interface SupplierWithException<T, E extends Throwable> {

    T get() throws E;
  }
}
