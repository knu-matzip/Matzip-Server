package com.matzip.common.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long accessTokenExpirationTime;

    @Value("${jwt.refresh-expiration-time}")
    private long refreshTokenExpirationTime; // 리프레시 토큰 만료 시간

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(UTF_8));
    }

    /**
     * Access Token 생성
     */
    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenExpirationTime);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenExpirationTime);
    }

    private String createToken(Long userId, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 토큰에서 userId 추출
     */
    public Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired at {}", e.getClaims().getExpiration());
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("JWT signature invalid or malformed: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT illegal argument (null/empty?): {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT validate failed: {}", e.getMessage());
        }
        return false;
    }

}