package org.example.idempotency;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

public class RestAssuredExtension implements BeforeEachCallback {

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    Optional.ofNullable(
            SpringExtension
                .getApplicationContext(context)
                .getEnvironment()
                .getProperty("local.server.port", Integer.class)
        )
        .ifPresent(localPort -> RestAssured.requestSpecification = new RequestSpecBuilder()
            .setBasePath("http://localhost")
            .setPort(localPort)
            .build()
        );
  }
}
