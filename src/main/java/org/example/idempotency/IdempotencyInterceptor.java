package org.example.idempotency;

import lombok.RequiredArgsConstructor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.example.idempotency.annotation.IdempotentResource;
import org.springframework.aop.framework.ProxyFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


@RequiredArgsConstructor
public class IdempotencyInterceptor implements MethodInterceptor {

  private final IdempotencyProcessor idempotencyProcessor;

  @Nullable
  @Override
  public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
    if (invocation.getMethod().getAnnotation(IdempotentResource.class) != null) {
      return idempotencyProcessor.execute(invocation);
    }
    return invocation.proceed();
  }

  public static Object proxiedBean(Object bean, IdempotencyProcessor processor) {
    ProxyFactory factory = new ProxyFactory(bean);
    factory.addAdvice(new IdempotencyInterceptor(processor));
    return factory.getProxy();
  }
}
