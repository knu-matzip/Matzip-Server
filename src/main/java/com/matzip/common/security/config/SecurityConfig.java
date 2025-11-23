package com.matzip.common.security.config;

import com.matzip.common.security.jwt.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${ADMIN_ORIGIN:}")
    private String adminOrigin;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 프론트엔드 서버 주소 허용
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001", "https://knu-matzip-dev.vercel.app", "https://knu-matzip.vercel.app", adminOrigin));
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));
        // 쿠키 등 자격 증명 정보 허용
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 추가
                // CSRF 보호 비활성화 (JWT 사용 시 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 관리 방식을 STATELESS로 설정 (JWT 사용 시 불필요)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // HTTP 요청에 대한 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/events/entries").authenticated()
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                // 직접 구현한 JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 전에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
