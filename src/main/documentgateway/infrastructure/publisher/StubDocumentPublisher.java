package com.bhd.documentgateway.infrastructure.publisher;

import com.bhd.documentgateway.application.port.DocumentPublisher;
import com.bhd.documentgateway.application.port.PublishResult;
import com.bhd.documentgateway.domain.DocumentAsset;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class StubDocumentPublisher implements DocumentPublisher {

    private final Retry retry;
    private final CircuitBreaker circuitBreaker;

    public StubDocumentPublisher(RetryRegistry retryRegistry, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.retry = retryRegistry.retry("documentPublisher");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("documentPublisher");
    }

    @Override
    public Mono<PublishResult> publish(DocumentAsset document) {
        return Mono.fromCallable(() -> {
            return new PublishResult(true, "https://storage.internal/bhd/" + document.id() + "/" + UUID.randomUUID());
        })
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }
}
