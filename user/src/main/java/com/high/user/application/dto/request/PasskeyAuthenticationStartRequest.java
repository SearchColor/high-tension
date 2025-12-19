package com.high.user.application.dto.request;

public record PasskeyAuthenticationStartRequest(
    String email  // Optional: username-less authentication 시 null
) {}