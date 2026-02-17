package com.bhd.documentgateway.application.service;

import com.bhd.documentgateway.application.port.DocumentRepository;
import com.bhd.documentgateway.application.port.UploadOrchestrator;
import com.bhd.documentgateway.domain.DocumentAsset;
import com.bhd.documentgateway.domain.DocumentUploadRequest;
import com.bhd.documentgateway.domain.DocumentUploadResponse;
import com.bhd.documentgateway.domain.enums.Channel;
import com.bhd.documentgateway.domain.enums.DocumentStatus;
import com.bhd.documentgateway.domain.enums.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadDocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UploadOrchestrator uploadOrchestrator;

    @InjectMocks
    private UploadDocumentService uploadDocumentService;

    @Test
    void upload_persistsAndReturns202Response() {
        DocumentUploadRequest request = new DocumentUploadRequest(
                "test.pdf",
                java.util.Base64.getEncoder().encodeToString("content".getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                "application/pdf",
                DocumentType.CONTRACT,
                Channel.DIGITAL,
                null,
                null
        );
        when(documentRepository.save(any(DocumentAsset.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(uploadOrchestrator.orchestrateAsync(any())).thenReturn(Mono.empty());

        StepVerifier.create(uploadDocumentService.upload(request))
                .expectNextMatches(r -> r.id() != null && !r.id().isBlank())
                .verifyComplete();

        verify(documentRepository).save(any(DocumentAsset.class));
    }
}
