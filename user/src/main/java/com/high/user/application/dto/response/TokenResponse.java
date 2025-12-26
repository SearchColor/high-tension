package com.high.user.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 토큰 응답")
public record TokenResponse(
    @Schema(description = "Access Token (Bearer 토큰)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String refreshToken,

    @Schema(description = "Access Token 만료 시간 (초)", example = "3600")
    Long expiresIn
) {
    public static TokenResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, expiresIn);
    }
}
