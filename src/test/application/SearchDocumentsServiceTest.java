package com.bhd.documentgateway.application.service;

import com.bhd.documentgateway.application.port.DocumentRepository;
import com.bhd.documentgateway.domain.DocumentAsset;
import com.bhd.documentgateway.domain.SearchCriteria;
import com.bhd.documentgateway.domain.enums.Channel;
import com.bhd.documentgateway.domain.enums.DocumentStatus;
import com.bhd.documentgateway.domain.enums.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchDocumentsServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private SearchDocumentsService searchDocumentsService;

    @Test
    void search_delegatesToRepository() {
        SearchCriteria criteria = new SearchCriteria(
                null, null, null, null, null, null, null, null, null,
                "uploadDate", "ASC"
        );
        DocumentAsset asset = new DocumentAsset(
                "id1", "f.pdf", "application/pdf", DocumentType.KYC, Channel.BRANCH,
                null, DocumentStatus.RECEIVED, null, 100L, Instant.now(), null
        );
        when(documentRepository.search(any(SearchCriteria.class))).thenReturn(Flux.just(asset));

        StepVerifier.create(searchDocumentsService.search(criteria))
                .expectNext(asset)
                .verifyComplete();

        verify(documentRepository).search(criteria);
    }
}
