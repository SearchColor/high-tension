package com.high.user.infrastructure.security;

import com.high.user.domain.vo.UserRole;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * JWT 인증 필터
 * Authorization 헤더에서 Bearer 토큰을 추출하여 검증하고 SecurityContext에 인증 정보 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String jwt = extractJwtFromRequest(request);

            // 2. 토큰이 존재하고 유효한 경우
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                // 3. 토큰에서 사용자 정보 추출
                UUID userId = jwtTokenProvider.getUserIdFromToken(jwt);
                UserRole role = jwtTokenProvider.getRoleFromToken(jwt);

                // 4. Spring Security Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userId,  // Principal
                        null,    // Credentials (비밀번호 불필요)
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()))
                    );

                // 5. Request 정보 설정
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("JWT authentication successful for user: {}, role: {}", userId, role);
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        // 7. 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }
}
