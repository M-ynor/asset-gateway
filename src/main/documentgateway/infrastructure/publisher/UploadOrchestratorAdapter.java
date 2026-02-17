package com.bhd.documentgateway.infrastructure.publisher;

import com.bhd.documentgateway.application.port.DocumentPublisher;
import com.bhd.documentgateway.application.port.DocumentRepository;
import com.bhd.documentgateway.application.port.UploadOrchestrator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UploadOrchestratorAdapter implements UploadOrchestrator {

    private final DocumentRepository documentRepository;
    private final DocumentPublisher documentPublisher;

    public UploadOrchestratorAdapter(DocumentRepository documentRepository, DocumentPublisher documentPublisher) {
        this.documentRepository = documentRepository;
        this.documentPublisher = documentPublisher;
    }

    @Override
    public Mono<Void> orchestrateAsync(String documentId) {
        return documentRepository.findById(documentId)
                .flatMap(documentPublisher::publish)
                .flatMap(result -> documentRepository.updateStatus(documentId, result.toStatus(), result.url()))
                .then();
    }
}
