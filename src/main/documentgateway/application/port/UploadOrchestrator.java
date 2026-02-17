package com.bhd.documentgateway.application.port;

import com.bhd.documentgateway.domain.DocumentAsset;
import reactor.core.publisher.Mono;

public interface UploadOrchestrator {

    Mono<Void> orchestrateAsync(String documentId);
}
