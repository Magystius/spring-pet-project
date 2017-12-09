package de.otto.prototype.metrics;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class CountedAspectTest {

    private SimpleMeterRegistry currentRegistry;

    private CountedAspect testee;

    @BeforeEach
    void setUp() {
        testee = new CountedAspect();
        currentRegistry = new SimpleMeterRegistry();
        Metrics.addRegistry(currentRegistry);
    }

    @AfterEach
    void tearDown() {
        Metrics.removeRegistry(currentRegistry);
    }

    @Test
    @DisplayName("should increment the counter value if called")
    void shouldIncrementCounter() {
        final JoinPoint method = mock(JoinPoint.class);
        final Signature signature = mock(Signature.class);

        given(method.getSignature()).willReturn(signature);
        given(signature.getDeclaringType()).willReturn(someClass.class);
        given(signature.getName()).willReturn("someMethod");

        testee.countMethodCallForMetrics(method);

        assertThat(Metrics.counter("someClass.someMethod").count(), Is.is(1.0));
    }

    private class someClass {
    }
}