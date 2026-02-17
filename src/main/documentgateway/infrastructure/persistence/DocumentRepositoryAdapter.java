package com.bhd.documentgateway.infrastructure.persistence;

import com.bhd.documentgateway.application.port.DocumentRepository;
import com.bhd.documentgateway.domain.DocumentAsset;
import com.bhd.documentgateway.domain.SearchCriteria;
import com.bhd.documentgateway.domain.enums.Channel;
import com.bhd.documentgateway.domain.enums.DocumentStatus;
import com.bhd.documentgateway.domain.enums.DocumentType;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class DocumentRepositoryAdapter implements DocumentRepository {

    private final R2dbcDocumentRepository repository;
    private final R2dbcEntityTemplate template;

    public DocumentRepositoryAdapter(R2dbcDocumentRepository repository, R2dbcEntityTemplate template) {
        this.repository = repository;
        this.template = template;
    }

    @Override
    public Mono<DocumentAsset> save(DocumentAsset document) {
        DocumentEntity entity = toEntity(document);
        return template.insert(entity).thenReturn(document);
    }

    @Override
    public Mono<DocumentAsset> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<DocumentAsset> updateStatus(String id, DocumentStatus status, String url) {
        Update update = Update.update("status", status.name());
        if (url != null) {
            update = update.set("url", url);
        }
        return template.update(DocumentEntity.class)
                .matching(Query.query(Criteria.where("id").is(id)))
                .apply(update)
                .then(findById(id));
    }

    @Override
    public Flux<DocumentAsset> search(SearchCriteria criteria) {
        List<Criteria> conditions = new ArrayList<>();
        if (criteria.id() != null && !criteria.id().isBlank()) {
            conditions.add(Criteria.where("id").is(criteria.id()));
        }
        if (criteria.uploadDateStart() != null) {
            conditions.add(Criteria.where("upload_date").greaterThanOrEquals(criteria.uploadDateStart()));
        }
        if (criteria.uploadDateEnd() != null) {
            conditions.add(Criteria.where("upload_date").lessThanOrEquals(criteria.uploadDateEnd()));
        }
        if (criteria.filename() != null && !criteria.filename().isBlank()) {
            conditions.add(Criteria.where("filename").like("%" + criteria.filename() + "%"));
        }
        if (criteria.contentType() != null && !criteria.contentType().isBlank()) {
            conditions.add(Criteria.where("content_type").is(criteria.contentType()));
        }
        if (criteria.documentType() != null) {
            conditions.add(Criteria.where("document_type").is(criteria.documentType().name()));
        }
        if (criteria.status() != null) {
            conditions.add(Criteria.where("status").is(criteria.status().name()));
        }
        if (criteria.customerId() != null && !criteria.customerId().isBlank()) {
            conditions.add(Criteria.where("customer_id").is(criteria.customerId()));
        }
        if (criteria.channel() != null) {
            conditions.add(Criteria.where("channel").is(criteria.channel().name()));
        }
        String sortBy = mapSortField(criteria.sortBy());
        Sort.Direction direction = SearchCriteria.SORT_DESC.equalsIgnoreCase(criteria.sortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Query query = conditions.isEmpty()
                ? Query.empty().sort(sort)
                : Query.query(conditions.stream().reduce(Criteria::and).orElseThrow()).sort(sort);
        return template.select(DocumentEntity.class)
                .matching(query)
                .all()
                .map(this::toDomain);
    }

    private String mapSortField(String sortBy) {
        return switch (sortBy != null ? sortBy : "uploadDate") {
            case "filename" -> "filename";
            case "documentType" -> "document_type";
            case "status" -> "status";
            default -> "upload_date";
        };
    }

    private DocumentEntity toEntity(DocumentAsset d) {
        DocumentEntity e = new DocumentEntity();
        e.setId(d.id());
        e.setFilename(d.filename());
        e.setContentType(d.contentType());
        e.setDocumentType(d.documentType().name());
        e.setChannel(d.channel().name());
        e.setCustomerId(d.customerId());
        e.setStatus(d.status().name());
        e.setUrl(d.url());
        e.setSize(d.size());
        e.setUploadDate(d.uploadDate());
        e.setCorrelationId(d.correlationId());
        return e;
    }

    private DocumentAsset toDomain(DocumentEntity e) {
        return new DocumentAsset(
                e.getId(),
                e.getFilename(),
                e.getContentType(),
                DocumentType.valueOf(e.getDocumentType()),
                Channel.valueOf(e.getChannel()),
                e.getCustomerId(),
                DocumentStatus.valueOf(e.getStatus()),
                e.getUrl(),
                e.getSize(),
                e.getUploadDate(),
                e.getCorrelationId()
        );
    }
}
