package com.high.external.application.dto.request;

import lombok.Getter;

@Getter
public class SendSlackMessageRequest {
    private String recipientId;
    private String message;
}
