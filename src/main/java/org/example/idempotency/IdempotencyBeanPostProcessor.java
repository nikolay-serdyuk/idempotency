package org.example.idempotency;

import lombok.RequiredArgsConstructor;
import org.example.idempotency.annotation.Idempotent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import static org.example.idempotency.IdempotencyInterceptor.proxiedBean;

@RequiredArgsConstructor
public class IdempotencyBeanPostProcessor implements BeanPostProcessor, Ordered {

  private final IdempotencyProcessor idempotencyProcessor;

  @Override
  public Object postProcessBeforeInitialization(
      @Nullable Object bean,
      @Nullable String beanName
  ) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(
      @Nonnull Object bean,
      @Nonnull String beanName
  ) throws BeansException {
    if (bean.getClass().getAnnotation(Idempotent.class) != null) {
      return proxiedBean(bean, idempotencyProcessor);
    }
    return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
  }

  @Override
  public int getOrder() {
    return HIGHEST_PRECEDENCE;
  }
}
