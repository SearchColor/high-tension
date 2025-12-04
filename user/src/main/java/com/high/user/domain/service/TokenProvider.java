package com.high.user.domain.service;

import java.util.UUID;

/**
 * 토큰 생성 및 검증을 위한 인터페이스
 * DIP(의존성 역전 원칙)를 적용하여 Application 레이어가 구체적인 토큰 구현에 의존하지 않도록 함
 */
public interface TokenProvider {

    /**
     * Access Token 생성
     * @param userId 사용자 ID
     * @param role 사용자 권한
     * @return Access Token
     */
    String createAccessToken(UUID userId, String role);

    /**
     * Refresh Token 생성
     * @param userId 사용자 ID
     * @return Refresh Token
     */
    String createRefreshToken(UUID userId);

    /**
     * 토큰 유효성 검증
     * @param token 검증할 토큰
     * @return 유효 여부
     */
    boolean validateToken(String token);

    /**
     * 토큰에서 사용자 ID 추출
     * @param token 토큰
     * @return 사용자 ID
     */
    UUID getUserId(String token);

    /**
     * 토큰에서 권한 정보 추출
     * @param token 토큰
     * @return 권한
     */
    String getRole(String token);

    /**
     * Access Token 유효 시간 조회
     * @return 유효 시간(밀리초)
     */
    Long getAccessTokenValidity();

    /**
     * 토큰의 남은 유효 시간 조회
     * @param token 토큰
     * @return 남은 유효 시간(밀리초)
     */
    long getRemainingTime(String token);
}
