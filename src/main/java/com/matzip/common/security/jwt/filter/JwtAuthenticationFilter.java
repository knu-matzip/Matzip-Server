package com.matzip.common.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matzip.common.exception.code.ErrorCode;
import com.matzip.common.response.ApiResponse;
import com.matzip.common.security.jwt.JwtProvider;
import com.matzip.common.security.UserPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth",
            "/api/v2/auth",
            "/api/v1/places",
            "/api/v1/categories",
            "/api/v1/events",
            "/admin/api",
            "/actuator/prometheus"
    );

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 토큰 추출
        String token = resolveToken(request);
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        boolean authenticationRequired = isAuthenticationRequired(requestPath, requestMethod);

        if (token == null && authenticationRequired) {
            handleUnauthorized(response);
            return;
        }

        // 2. 토큰이 있는 경우 유효성 검증
        if (token != null) {
            JwtProvider.TokenValidationResult validationResult = jwtProvider.validateTokenWithResult(token);
            
            if (validationResult == JwtProvider.TokenValidationResult.VALID) {
                // 3. 토큰이 유효하면 인증 정보를 생성하여 SecurityContext에 저장
                try {
                    Long userId = jwtProvider.getUserId(token);
                    UserPrincipal userPrincipal = new UserPrincipal(userId);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (JwtException e) {
                    if (authenticationRequired) {
                        handleJwtException(response, JwtProvider.TokenValidationResult.INVALID);
                        return;
                    }
                }
            } else {
                if (authenticationRequired) {
                    handleJwtException(response, validationResult);
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticationRequired(String requestPath, String requestMethod) {
        // 인증이 필요 없는 공개 경로인지 확인
        for (String publicPath : PUBLIC_PATHS) {
            if (requestPath.startsWith(publicPath)) {
                // /api/v1/events/entries 인증 필요
                if (requestPath.equals("/api/v1/events/entries") && "POST".equals(requestMethod)) {
                    return true;
                }
                
                // /api/v1/places 경로 중 인증이 필요한 경로들
                if (publicPath.equals("/api/v1/places")) {
                    if (requestPath.equals("/api/v1/places/like") && "GET".equals(requestMethod)) {
                        return true;
                    }
                    if (requestPath.matches("/api/v1/places/\\d+/like") &&
                        ("POST".equals(requestMethod) || "DELETE".equals(requestMethod))) {
                        return true;
                    }
                }

                return false;
            }
        }

        return true;
    }

    /**
     * JWT 예외 발생 시 401 응답 반환
     */
    private void handleJwtException(HttpServletResponse response, JwtProvider.TokenValidationResult validationResult) throws IOException {
        ErrorCode errorCode;
        
        if (validationResult == JwtProvider.TokenValidationResult.EXPIRED) {
            errorCode = ErrorCode.TOKEN_EXPIRED;
        } else {
            errorCode = ErrorCode.INVALID_TOKEN;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<Void> apiResponse = ApiResponse.error(errorCode);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }

    private void handleUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.error(ErrorCode.UNAUTHORIZED);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(jsonResponse);
    }

    // 요청 헤더에서 "Authorization" 헤더를 찾아 Bearer 토큰을 추출하는 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
