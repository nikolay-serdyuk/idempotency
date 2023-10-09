package org.example.idempotency;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

@UtilityClass
public class Constants {

  public static final ResultChecker DEFAULT_RESULT_CHECKER = o -> true;
  public static final SimpleModule DEFAULT_OBJECT_MAPPER_MODULE = new SimpleModule()
      .addSerializer(ConsumerRecord.class, new ConsumerRecordSerializer());
  public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .registerModule(DEFAULT_OBJECT_MAPPER_MODULE)
      .addMixIn(ResponseEntity.class, ResponseEntityMixin.class)
      .addMixIn(HttpStatus.class, HttpStatusMixin.class)
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ResponseEntityMixin {

    private ResponseEntityMixin() {

    }

    @JsonCreator
    public ResponseEntityMixin(
        @JsonProperty("body") Object body,
        @JsonDeserialize(as = LinkedMultiValueMap.class)
        @JsonProperty("headers") MultiValueMap<String, String> headers,
        @JsonProperty("statusCodeValue") HttpStatus status
    ) {

    }
  }

  public static class HttpStatusMixin {

    private HttpStatusMixin() {
    }

    @JsonCreator
    public static HttpStatus resolve(int statusCode) {
      return HttpStatus.NO_CONTENT;
    }
  }

  private static class ConsumerRecordSerializer extends JsonSerializer<ConsumerRecord> {

    @Override
    public void serialize(
        ConsumerRecord object,
        JsonGenerator gen,
        SerializerProvider provider
    ) throws IOException {
      gen.writeStartObject();
      gen.writeObjectField("key", object.key());
      gen.writeObjectField("value", object.value());
      gen.writeEndObject();
    }
  }
}
