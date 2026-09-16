package com.matzip.common.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> parentContext = MDC.getCopyOfContextMap();
        return () -> {
            if (parentContext != null) {
                MDC.setContextMap(parentContext);
            } else {
                MDC.clear();
            }
            try {
                runnable.run();
            } finally {
                MDC.clear(); // 풀 스레드 재사용 시 이전 요청 컨텍스트가 남지 않도록 정리
            }
        };
    }
}
