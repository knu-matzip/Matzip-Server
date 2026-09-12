package com.matzip.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    private static final int SHUTDOWN_AWAIT_SECONDS = 15;

    @Override
    public Executor getAsyncExecutor() {
        return generalExecutor();
    }

    @Bean
    public ThreadPoolTaskExecutor generalExecutor() {
        // Hikari 기본 커넥션 풀(10)을 웹 요청과 나눠 쓰도록 max 4로 제한
        return buildExecutor("async-", 3, 4, 200, loggingDiscardPolicy());
    }

    @Bean
    public ThreadPoolTaskExecutor externalExecutor() {
        return buildExecutor("ext-", 1, 2, 30, new ThreadPoolExecutor.DiscardPolicy());
    }

    private ThreadPoolTaskExecutor buildExecutor(String threadNamePrefix, int corePoolSize, int maxPoolSize,
                                                 int queueCapacity, RejectedExecutionHandler rejectedHandler) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(rejectedHandler);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(SHUTDOWN_AWAIT_SECONDS);
        executor.initialize();
        return executor;
    }

    // ViewCountService가 REQUIRES_NEW라 CallerRunsPolicy는 커넥션 데드락 위험이 있어 log & discard 사용
    private RejectedExecutionHandler loggingDiscardPolicy() {
        return (rejected, executor) ->
                log.warn("[비동기 작업 유실] general 풀 포화로 작업을 버립니다. activeThreads={}, queueSize={}",
                        executor.getActiveCount(), executor.getQueue().size());
    }
}
