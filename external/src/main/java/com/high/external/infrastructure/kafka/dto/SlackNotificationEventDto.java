package com.high.external.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackNotificationEventDto {

    @JsonProperty("urlPath")
    private String urlPath;

    @JsonProperty("message")
    private String message;

    @JsonProperty("targetSlackId")
    private String targetSlackId;

    @JsonProperty("serviceName")
    private String serviceName;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("type")
    private String type;

}
