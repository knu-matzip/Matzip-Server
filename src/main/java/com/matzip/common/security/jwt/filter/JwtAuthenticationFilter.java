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

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 요청 헤더에서 토큰 추출
        String token = resolveToken(request);

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
                    // 토큰 파싱 중 예외 발생 시 401 반환
                    handleJwtException(response, JwtProvider.TokenValidationResult.INVALID);
                    return;
                }
            } else {
                // 토큰이 만료되었거나 유효하지 않은 경우 401 반환
                handleJwtException(response, validationResult);
                return;
            }
        }

        filterChain.doFilter(request, response);
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

    // 요청 헤더에서 "Authorization" 헤더를 찾아 Bearer 토큰을 추출하는 메서드
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
