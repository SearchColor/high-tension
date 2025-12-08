package com.high.order.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

        System.out.println("HeaderAuthenticationFilter 유입");
        System.out.println("X-User-Id: " + request.getHeader(USER_ID_HEADER));
        System.out.println("X-User-Role: " + request.getHeader(USER_ROLE_HEADER));



        String userId = request.getHeader(USER_ID_HEADER);
        String userRole = request.getHeader(USER_ROLE_HEADER);

        if (userId != null && userRole != null) {
            try {
                UUID userUuid = UUID.fromString(userId);

                UserPrincipal principal = new UserPrincipal(
                    userUuid,
                    List.of(new SimpleGrantedAuthority("ROLE_" + userRole))
                );

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Gateway header authentication successful - UserId: {}, Role: {}",
                    userId, userRole);

            } catch (IllegalArgumentException e) {
                log.warn("Invalid UUID format in X-User-Id header: {} - Falling back to JWT",
                    userId);
            }
        } else {
            log.debug("Gateway headers not found or invalid - Falling back to JWT authentication");
        }

        filterChain.doFilter(request, response);
    }
}
