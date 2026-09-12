package com.matzip.common.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTaskDecoratorTest {

    private static final String TRACE_ID = "traceId";

    private final MdcTaskDecorator decorator = new MdcTaskDecorator();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void 부모_스레드의_MDC를_실행_스레드로_복사한다() throws Exception {
        MDC.put(TRACE_ID, "abc123"); // 제출(부모) 스레드에서 데코레이트
        AtomicReference<String> seenDuringRun = new AtomicReference<>();
        Runnable decorated = decorator.decorate(() -> seenDuringRun.set(MDC.get(TRACE_ID)));

        // 다른 스레드에서 실행 → ThreadLocal이라 데코레이터 없이는 전파되지 않음
        runAndJoin(new Thread(decorated));

        assertThat(seenDuringRun.get()).isEqualTo("abc123");
    }

    @Test
    void 실행_후_스레드의_MDC를_정리한다() throws Exception {
        MDC.put(TRACE_ID, "abc123");
        Runnable decorated = decorator.decorate(() -> {});

        // 실행이 끝난 뒤 워커 스레드에 값이 남으면 재사용 시 다음 작업을 오염시킨다
        AtomicReference<String> afterRun = new AtomicReference<>("SENTINEL");
        runAndJoin(new Thread(() -> {
            decorated.run();
            afterRun.set(MDC.get(TRACE_ID));
        }));

        assertThat(afterRun.get()).isNull();
    }

    @Test
    void 부모_MDC가_없으면_워커에_남은_잔여값을_지운다() throws Exception {
        MDC.clear(); // 부모(제출) 스레드에 컨텍스트 없음
        AtomicReference<String> seenDuringRun = new AtomicReference<>("SENTINEL");
        Runnable decorated = decorator.decorate(() -> seenDuringRun.set(MDC.get(TRACE_ID)));

        // 재사용된 워커 스레드에 이전 작업의 값이 남아있는 상황을 모사
        runAndJoin(new Thread(() -> {
            MDC.put(TRACE_ID, "STALE");
            decorated.run();
        }));

        assertThat(seenDuringRun.get()).isNull();
    }

    private void runAndJoin(Thread worker) throws InterruptedException {
        worker.start();
        worker.join();
    }
}
