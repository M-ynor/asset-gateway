package com.bhd.documentgateway.application.service;

import com.bhd.documentgateway.application.port.DocumentRepository;
import com.bhd.documentgateway.application.port.UploadOrchestrator;
import com.bhd.documentgateway.domain.DocumentAsset;
import com.bhd.documentgateway.domain.DocumentUploadRequest;
import com.bhd.documentgateway.domain.DocumentUploadResponse;
import com.bhd.documentgateway.domain.enums.DocumentStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class UploadDocumentService {

    private final DocumentRepository documentRepository;
    private final UploadOrchestrator uploadOrchestrator;

    public UploadDocumentService(DocumentRepository documentRepository, UploadOrchestrator uploadOrchestrator) {
        this.documentRepository = documentRepository;
        this.uploadOrchestrator = uploadOrchestrator;
    }

    public Mono<DocumentUploadResponse> upload(DocumentUploadRequest request) {
        String id = UUID.randomUUID().toString();
        int size = request.encodedFile() != null ? Base64.getDecoder().decode(request.encodedFile()).length : 0;
        DocumentAsset asset = new DocumentAsset(
                id,
                request.filename(),
                request.contentType(),
                request.documentType(),
                request.channel(),
                request.customerId(),
                DocumentStatus.RECEIVED,
                null,
                size,
                Instant.now(),
                request.correlationId()
        );
        return documentRepository.save(asset)
                .doOnSuccess(ignored -> uploadOrchestrator.orchestrateAsync(id).subscribe())
                .thenReturn(new DocumentUploadResponse(id));
    }
}
