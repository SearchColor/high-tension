package com.high.user.infrastructure.security;

import com.high.user.application.service.RefreshTokenService;
import com.high.user.domain.service.TokenProvider;
import com.library.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@Order(2)  // HeaderAuthenticationFilter 다음 실행
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // SecurityContext 체크: 이미 인증되어 있으면 JWT 검증 스킵
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth != null
            && existingAuth.isAuthenticated()
            && !(existingAuth instanceof AnonymousAuthenticationToken)) {

            log.debug("SecurityContext already set (via Gateway headers) - Skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = resolveToken(request);

            if (token != null && tokenProvider.validateToken(token)) {
                // Blacklist 확인
                if (refreshTokenService.isBlacklisted(token)) {
                    log.warn("Blacklisted token used: {}", token);
                    // 블랙리스트된 토큰은 인증 설정 안함 -> EntryPoint에서 처리
                } else {
                    Authentication authentication = getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT authentication successful (direct call)");
                }
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
            // 예외 발생 시 SecurityContext를 비워서 EntryPoint가 처리하도록 함
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Authentication getAuthentication(String token) {
        UUID userId = tokenProvider.getUserId(token);
        String role = tokenProvider.getRole(token);

        UserPrincipal principal = new UserPrincipal(userId, role);
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);

        return new UsernamePasswordAuthenticationToken(principal, null, Collections.singletonList(authority));
    }
}
