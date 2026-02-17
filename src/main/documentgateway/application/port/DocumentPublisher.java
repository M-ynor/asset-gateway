package com.bhd.documentgateway.application.port;

import com.bhd.documentgateway.domain.DocumentAsset;
import reactor.core.publisher.Mono;

public interface DocumentPublisher {

    Mono<PublishResult> publish(DocumentAsset document);
}
