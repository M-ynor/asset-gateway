package com.bhd.documentgateway.infrastructure.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface R2dbcDocumentRepository extends ReactiveCrudRepository<DocumentEntity, String> {

    Mono<DocumentEntity> findById(String id);
}
