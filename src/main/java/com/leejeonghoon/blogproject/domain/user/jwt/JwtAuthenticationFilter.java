package com.leejeonghoon.blogproject.domain.user.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtTokenProvider.resolveToken(request);

        log.info("요청 URL: {}", requestURI);
        log.info("추출한 JWT 토큰: {}", token);

        if (token != null) {
            token = token.replace("Bearer ", "").trim();

            if (jwtTokenProvider.validateToken(token)) {
                Boolean isBlacklisted = redisTemplate.hasKey(token);
                if (Boolean.TRUE.equals(isBlacklisted)) {
                    log.warn("로그아웃된 토큰으로 요청이 들어왔습니다.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("로그아웃된 토큰입니다.");
                    return;
                }

                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("SecurityContextHolder에 인증 정보 저장됨: {}", authentication.getName());
            } else {
                log.warn("유효하지 않은 JWT 토큰 (URL: {})", requestURI);
            }
        }

        filterChain.doFilter(request, response);
    }

}
