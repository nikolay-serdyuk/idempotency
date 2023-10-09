package org.example.idempotency;

import org.example.idempotency.hash.KeyGenerator;
import org.example.idempotency.hash.Md5KeyGenerator;
import org.example.idempotency.repository.DefaultIdempotentRepository;
import org.example.idempotency.repository.IdempotentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "idempotency", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(IdempotencyProperty.class)
public class IdempotencyConfiguration {

  @Bean
  public IdempotencyBeanPostProcessor idempotencyBeanPostProcessor(
      IdempotencyProcessor idempotencyProcessor
  ) {
    return new IdempotencyBeanPostProcessor(idempotencyProcessor);
  }

  @Bean
  @ConditionalOnMissingBean
  public IdempotencyConfigurationMap idempotencyConfigurationMap() {
    return new IdempotencyConfigurationMap(Map.of(), Map.of());
  }

  @Bean
  @ConditionalOnMissingBean
  public KeyGenerator keyGenerator(IdempotencyProperty properties) {
    if ("MD5".equals(properties.getKeyGenerator())) {
      return new Md5KeyGenerator();
    }
    throw new IdempotencyException("Unkkown key generator specified");
  }

  @Bean
  @ConditionalOnMissingBean
  public IdempotentRepository idempotentRepository(
      JdbcTemplate jdbcTemplate
  ) {
    return new DefaultIdempotentRepository(jdbcTemplate);
  }
}
