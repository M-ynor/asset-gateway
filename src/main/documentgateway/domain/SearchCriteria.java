package com.bhd.documentgateway.domain;

import com.bhd.documentgateway.domain.enums.Channel;
import com.bhd.documentgateway.domain.enums.DocumentStatus;
import com.bhd.documentgateway.domain.enums.DocumentType;

import java.time.Instant;

public record SearchCriteria(
        String id,
        Instant uploadDateStart,
        Instant uploadDateEnd,
        String filename,
        String contentType,
        DocumentType documentType,
        DocumentStatus status,
        String customerId,
        Channel channel,
        String sortBy,
        String sortDirection
) {
    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";
}
