package com.bhd.documentgateway.application.service;

import com.bhd.documentgateway.application.port.DocumentRepository;
import com.bhd.documentgateway.domain.DocumentAsset;
import com.bhd.documentgateway.domain.SearchCriteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class SearchDocumentsService {

    private final DocumentRepository documentRepository;

    public SearchDocumentsService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Flux<DocumentAsset> search(SearchCriteria criteria) {
        return documentRepository.search(criteria);
    }
}
