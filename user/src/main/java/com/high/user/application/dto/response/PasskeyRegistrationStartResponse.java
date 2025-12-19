package com.high.user.application.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record PasskeyRegistrationStartResponse(
    JsonNode publicKeyCredentialCreationOptions  // JSON for navigator.credentials.create()
) {
    public static PasskeyRegistrationStartResponse of(JsonNode options) {
        return new PasskeyRegistrationStartResponse(options);
    }
}