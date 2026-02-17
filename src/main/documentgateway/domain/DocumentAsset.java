package com.bhd.documentgateway.domain;

import com.bhd.documentgateway.domain.enums.Channel;
import com.bhd.documentgateway.domain.enums.DocumentStatus;
import com.bhd.documentgateway.domain.enums.DocumentType;

import java.time.Instant;

public record DocumentAsset(
        String id,
        String filename,
        String contentType,
        DocumentType documentType,
        Channel channel,
        String customerId,
        DocumentStatus status,
        String url,
        long size,
        Instant uploadDate,
        String correlationId
) {}
