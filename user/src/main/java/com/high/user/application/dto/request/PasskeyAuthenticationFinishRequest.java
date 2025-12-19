package com.high.user.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PasskeyAuthenticationFinishRequest(
    @NotBlank(message = "Credential 응답은 필수입니다")
    String credentialJson  // navigator.credentials.get() 응답 JSON
) {}