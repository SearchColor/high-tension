package com.high.user.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Gateway 헤더 기반 인증 필터
 * <p>
 * Gateway에서 주입한 X-User-Id, X-User-Role 헤더를 SecurityContext로 변환합니다.
 * </p>
 *
 * <h3>실행 순서</h3>
 * HeaderAuthenticationFilter (Order 1) → JwtAuthenticationFilter (Order 2)
 *
 * <h3>동작 방식</h3>
 * <ul>
 *   <li>Gateway 통과 요청: X-User-Id, X-User-Role 헤더 존재 → SecurityContext 설정</li>
 *   <li>직접 호출: 헤더 없음 → JwtAuthenticationFilter가 JWT 검증</li>
 *   <li>잘못된 헤더: 헤더 파싱 실패 → fallback to JWT</li>
 * </ul>
 */
@Slf4j
@Component
@Order(1)  // JwtAuthenticationFilter보다 먼저 실행
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader(USER_ID_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);

        // 헤더가 있고 유효한 경우에만 SecurityContext 설정
        if (userId != null && userRole != null) {
            try {
                // UUID 유효성 검증
                UUID userUuid = UUID.fromString(userId);

                // Authentication 객체 생성
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userUuid,  // Principal: UUID 객체 (JpaAuditingConfig에서 toString() 사용)
                        null,      // Credentials: 비밀번호 불필요 (Gateway에서 이미 인증됨)
                        List.of(new SimpleGrantedAuthority("ROLE_" + userRole))
                );

                // SecurityContext 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Gateway header authentication successful - UserId: {}, Role: {}",
                          userId, userRole);

            } catch (IllegalArgumentException e) {
                // UUID 파싱 실패 → 로그만 남기고 fallback
                log.warn("Invalid UUID format in X-User-Id header: {} - Falling back to JWT",
                         userId);
            }
        } else {
            log.debug("Gateway headers not found or invalid - Falling back to JWT authentication");
        }

        // 항상 다음 필터로 진행 (예외 발생 안 함)
        filterChain.doFilter(request, response);
    }
}
