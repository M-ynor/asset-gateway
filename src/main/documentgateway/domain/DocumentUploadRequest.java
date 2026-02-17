package com.bhd.documentgateway.domain;

import com.bhd.documentgateway.domain.enums.Channel;
import com.bhd.documentgateway.domain.enums.DocumentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DocumentUploadRequest(
        @NotBlank String filename,
        @NotBlank String encodedFile,
        @NotBlank String contentType,
        @NotNull DocumentType documentType,
        @NotNull Channel channel,
        String customerId,
        String correlationId
) {}
