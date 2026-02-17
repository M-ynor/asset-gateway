package com.bhd.documentgateway.infrastructure.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        String error,
        String message,
        int status,
        String path,
        Instant timestamp,
        List<FieldError> fields
) {
    public record FieldError(String field, String message) {}
}
