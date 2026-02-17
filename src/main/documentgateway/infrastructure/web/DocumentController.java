package com.bhd.documentgateway.infrastructure.web;

import com.bhd.documentgateway.application.service.SearchDocumentsService;
import com.bhd.documentgateway.application.service.UploadDocumentService;
import com.bhd.documentgateway.domain.DocumentUploadRequest;
import com.bhd.documentgateway.domain.SearchCriteria;
import com.bhd.documentgateway.domain.enums.Channel;
import com.bhd.documentgateway.domain.enums.DocumentStatus;
import com.bhd.documentgateway.domain.enums.DocumentType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/bhd/mgmt/1/documents")
public class DocumentController {

    private static final java.util.Set<String> VALID_SORT_FIELDS = java.util.Set.of("uploadDate", "filename", "documentType", "status");

    private final UploadDocumentService uploadDocumentService;
    private final SearchDocumentsService searchDocumentsService;

    public DocumentController(UploadDocumentService uploadDocumentService, SearchDocumentsService searchDocumentsService) {
        this.uploadDocumentService = uploadDocumentService;
        this.searchDocumentsService = searchDocumentsService;
    }

    @PostMapping(path = "/actions/upload", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<org.springframework.http.ResponseEntity<com.bhd.documentgateway.domain.DocumentUploadResponse>> upload(
            @Valid @RequestBody DocumentUploadRequest request) {
        return uploadDocumentService.upload(request)
                .map(response -> {
                    var body = org.springframework.http.ResponseEntity.<com.bhd.documentgateway.domain.DocumentUploadResponse>status(HttpStatus.ACCEPTED).body(response);
                    if (request.correlationId() != null && !request.correlationId().isBlank()) {
                        return org.springframework.http.ResponseEntity.<com.bhd.documentgateway.domain.DocumentUploadResponse>status(HttpStatus.ACCEPTED)
                                .header("X-Correlation-Id", request.correlationId())
                                .body(response);
                    }
                    return body;
                });
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<org.springframework.http.ResponseEntity<Flux<com.bhd.documentgateway.domain.DocumentAsset>>> search(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String uploadDateStart,
            @RequestParam(required = false) String uploadDateEnd,
            @RequestParam(required = false) String filename,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) DocumentType documentType,
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) Channel channel,
            @RequestParam(required = false, defaultValue = "uploadDate") String sortBy,
            @RequestParam(required = false, defaultValue = "ASC") String sortDirection) {
        if (sortBy != null && !VALID_SORT_FIELDS.contains(sortBy)) {
            return Mono.just(org.springframework.http.ResponseEntity.<Flux<com.bhd.documentgateway.domain.DocumentAsset>>badRequest().body(Flux.empty()));
        }
        if (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)) {
            return Mono.just(org.springframework.http.ResponseEntity.<Flux<com.bhd.documentgateway.domain.DocumentAsset>>badRequest().body(Flux.empty()));
        }
        Instant start = parseInstant(uploadDateStart);
        Instant end = parseInstant(uploadDateEnd);
        if ((uploadDateStart != null && start == null) || (uploadDateEnd != null && end == null)) {
            return Mono.just(org.springframework.http.ResponseEntity.<Flux<com.bhd.documentgateway.domain.DocumentAsset>>badRequest().body(Flux.empty()));
        }
        String effectiveSortBy = sortBy != null ? sortBy : "uploadDate";
        SearchCriteria criteria = new SearchCriteria(id, start, end, filename, contentType, documentType, status, customerId, channel, effectiveSortBy, sortDirection);
        return Mono.just(org.springframework.http.ResponseEntity.ok(searchDocumentsService.search(criteria)));
    }

    private static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
