package com.high.user.application.dto.response;

import com.high.user.domain.entity.Passkey;

import java.time.LocalDateTime;
import java.util.UUID;

public record PasskeyRegistrationFinishResponse(
    UUID passkeyId,
    String credentialName,
    LocalDateTime createdAt
) {
    public static PasskeyRegistrationFinishResponse from(Passkey passkey) {
        return new PasskeyRegistrationFinishResponse(
            passkey.getPasskeyId(),
            passkey.getCredentialName(),
            passkey.getCreatedAt()
        );
    }
}