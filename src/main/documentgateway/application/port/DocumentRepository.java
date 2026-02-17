package com.bhd.documentgateway.application.port;

import com.bhd.documentgateway.domain.DocumentAsset;
import com.bhd.documentgateway.domain.SearchCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentRepository {

    Mono<DocumentAsset> save(DocumentAsset document);

    Mono<DocumentAsset> findById(String id);

    Mono<DocumentAsset> updateStatus(String id, com.bhd.documentgateway.domain.enums.DocumentStatus status, String url);

    Flux<DocumentAsset> search(SearchCriteria criteria);
}
