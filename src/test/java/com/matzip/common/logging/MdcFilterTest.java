package com.matzip.common.logging;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcFilterTest {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final MdcFilter filter = new MdcFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void 체인_실행_중_traceId가_MDC에_세팅된다() throws Exception {
        String captured = runFilterAndCaptureTraceId(new MockHttpServletRequest());

        assertThat(captured).isNotBlank();
    }

    @Test
    void 필터_종료_후_MDC가_정리된다() throws Exception {
        FilterChain chain = (req, res) -> {
        };

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), chain);

        assertThat(MDC.get(MdcFilter.TRACE_ID)).isNull();
    }

    @Test
    void 유효한_X_Request_Id_헤더는_그대로_사용한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(REQUEST_ID_HEADER, "nginx-req-123");

        String captured = runFilterAndCaptureTraceId(request);

        assertThat(captured).isEqualTo("nginx-req-123");
    }

    @Test
    void 헤더가_없으면_traceId를_생성한다() throws Exception {
        String captured = runFilterAndCaptureTraceId(new MockHttpServletRequest());

        assertThat(captured).isNotBlank();
        assertThat(captured).matches("[A-Za-z0-9-]+");
    }

    @ParameterizedTest(name = "부적합 헤더값[{0}]은 생성값으로 대체된다")
    @ValueSource(strings = {
            "bad id",                    // 공백(허용되지 않는 문자)
            "inject\nlog",               // 개행(로그 인젝션 시도)
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // 길이 초과(65자)
    })
    void 부적합한_헤더값은_생성값으로_대체한다(String invalidHeaderValue) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(REQUEST_ID_HEADER, invalidHeaderValue);

        String captured = runFilterAndCaptureTraceId(request);

        assertThat(captured).isNotEqualTo(invalidHeaderValue);
        assertThat(captured).matches("[A-Za-z0-9-]+");
    }

    private String runFilterAndCaptureTraceId(MockHttpServletRequest request) throws Exception {
        AtomicReference<String> captured = new AtomicReference<>();
        FilterChain chain = (req, res) -> captured.set(MDC.get(MdcFilter.TRACE_ID));

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        return captured.get();
    }
}
