package org.example.idempotency;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {IdempotencyProcessor.class},
    properties = {
        "idempotency.enabled=true",
        "idempotency.keyGenerator=MD5"
    }
)
@ExtendWith({RestAssuredExtension.class})
@ComponentScan
@EnableAutoConfiguration
@Sql("/init.sql")
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
abstract class
BaseIntegrationTest {

}
