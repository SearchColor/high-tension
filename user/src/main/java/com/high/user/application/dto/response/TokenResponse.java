package com.high.user.application.dto.response;

/**
 * JWT 토큰 응답 DTO
 * AccessToken과 RefreshToken을 클라이언트에게 반환
 */
public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn  // 초 단위 (예: 3600 = 1시간)
) {
    public static TokenResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenResponse(
            accessToken,
            refreshToken,
            "Bearer",
            expiresIn
        );
    }
}
