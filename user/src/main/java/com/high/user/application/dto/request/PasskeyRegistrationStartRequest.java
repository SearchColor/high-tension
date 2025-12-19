package com.high.user.application.dto.request;

import jakarta.validation.constraints.Size;

public record PasskeyRegistrationStartRequest(
    @Size(max = 100, message = "패스키 이름은 100자 이하여야 합니다")
    String credentialName
) {}