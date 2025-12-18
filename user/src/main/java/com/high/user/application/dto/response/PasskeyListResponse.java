package com.high.user.application.dto.response;

import com.high.user.domain.entity.Passkey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PasskeyListResponse(
    UUID passkeyId,
    String credentialName,
    String aaguid,
    LocalDateTime lastUsedAt,
    LocalDateTime createdAt
) {
    public static PasskeyListResponse from(Passkey passkey) {
        return new PasskeyListResponse(
            passkey.getPasskeyId(),
            passkey.getCredentialName(),
            passkey.getAaguid(),
            passkey.getLastUsedAt(),
            passkey.getCreatedAt()
        );
    }

    public static List<PasskeyListResponse> fromList(List<Passkey> passkeys) {
        return passkeys.stream()
            .map(PasskeyListResponse::from)
            .toList();
    }
}