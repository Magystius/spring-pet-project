package de.otto.prototype.metrics;

import io.micrometer.core.instrument.Metrics;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CountedAspect {

    @AfterReturning("@annotation(Counted)")
    public void countMethodCallForMetrics(JoinPoint method) {
        Metrics.counter(method.getSignature().getDeclaringType().getSimpleName() + "." + method.getSignature().getName())
                .increment();
    }
}
