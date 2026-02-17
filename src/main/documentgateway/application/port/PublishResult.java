package com.bhd.documentgateway.application.port;

import com.bhd.documentgateway.domain.enums.DocumentStatus;

public record PublishResult(boolean success, String url) {

    public DocumentStatus toStatus() {
        return success ? DocumentStatus.SENT : DocumentStatus.FAILED;
    }
}
