package com.high.user.infrastructure.security;

import com.high.user.domain.service.TokenProvider;
import com.high.user.domain.vo.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider
 * JJWT 0.12.x 사용
 * TokenProvider 인터페이스 구현 (DIP 적용)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProvider {

    @Value("${spring.security.jwt.secret}")
    private String secretKeyString;

    @Value("${spring.security.jwt.access-token-validity}")
    private Long accessTokenValidity;

    @Value("${spring.security.jwt.refresh-token-validity}")
    private Long refreshTokenValidity;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        // Secret Key를 바이트 배열로 변환 후 SecretKey 객체 생성
        this.secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
        log.info("JwtTokenProvider initialized with key length: {} bytes",
                 secretKeyString.getBytes(StandardCharsets.UTF_8).length);
    }

    /**
     * Access Token 생성
     * @param userId 사용자 ID
     * @param role 사용자 권한
     * @return JWT Access Token
     */
    @Override
    public String createAccessToken(UUID userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성
     * @param userId 사용자 ID
     * @return JWT Refresh Token
     */
    @Override
    public String createRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenValidity);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 사용자 ID 추출
     * @param token JWT 토큰
     * @return 사용자 ID (UUID)
     */
    @Override
    public UUID getUserId(String token) {
        Claims claims = parseClaims(token);
        String userIdString = claims.getSubject();
        return UUID.fromString(userIdString);
    }

    /**
     * 토큰에서 권한 추출
     * @param token JWT 토큰
     * @return 사용자 권한
     */
    @Override
    public String getRole(String token) {
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰 검증
     * @param token JWT 토큰
     * @return 유효성 여부
     */
    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰이 만료되었는지 확인
     * @param token JWT 토큰
     * @return 만료 여부
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Access Token 유효기간 반환 (밀리초)
     * @return 유효기간 (밀리초)
     */
    @Override
    public Long getAccessTokenValidity() {
        return accessTokenValidity;
    }

    /**
     * Access Token 유효기간 반환 (초 단위)
     * @return 유효기간 (초)
     */
    public Long getAccessTokenValidityInSeconds() {
        return accessTokenValidity / 1000;
    }

    /**
     * 토큰의 남은 유효 시간 조회
     * @param token 토큰
     * @return 남은 유효 시간(밀리초)
     */
    @Override
    public long getRemainingTime(String token) {
        Claims claims = parseClaims(token);
        Date expiration = claims.getExpiration();
        Date now = new Date();
        return Math.max(0, expiration.getTime() - now.getTime());
    }

    /**
     * Claims 파싱 (만료된 토큰도 파싱 가능)
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰도 Claims는 반환
            return e.getClaims();
        }
    }
}
