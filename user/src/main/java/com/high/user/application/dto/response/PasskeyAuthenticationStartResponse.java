package com.high.user.application.dto.response;

public record PasskeyAuthenticationStartResponse(
    String publicKeyCredentialRequestOptions  // JSON for navigator.credentials.get()
) {
    public static PasskeyAuthenticationStartResponse of(String options) {
        return new PasskeyAuthenticationStartResponse(options);
    }
}