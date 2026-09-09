package com.matzip.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 요청마다 traceId를 MDC에 심어 모든 로그에 노출한다.
 * - nginx가 발급한 X-Request-Id를 우선 사용(없으면 생성)해 nginx와 앱 로그를 같은 id로 연결한다.
 * - Spring Security 필터체인보다 먼저 실행되도록 최우선 순서로 등록한다(401·예외 로그에도 traceId가 남도록)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final int MAX_TRACE_ID_LENGTH = 64;
    private static final Pattern VALID_TRACE_ID = Pattern.compile("[A-Za-z0-9-]+");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            MDC.put(TRACE_ID, resolveTraceId(request));
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String headerValue = request.getHeader(REQUEST_ID_HEADER);
        if (isValid(headerValue)) {
            return headerValue;
        }
        return generateTraceId();
    }

    private boolean isValid(String value) {
        return StringUtils.hasText(value)
                && value.length() <= MAX_TRACE_ID_LENGTH
                && VALID_TRACE_ID.matcher(value).matches();
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
