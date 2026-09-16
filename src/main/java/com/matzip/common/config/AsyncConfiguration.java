package com.matzip.common.config;

import com.matzip.common.logging.MdcTaskDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;@Slf4j
@EnableAsync
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    public static final String GENERAL_EXECUTOR = "generalExecutor";
    public static final String EXTERNAL_EXECUTOR = "externalExecutor";

    private static final int SHUTDOWN_AWAIT_SECONDS = 15;

    @Override
    public Executor getAsyncExecutor() {
        return generalExecutor();
    }

    @Bean(GENERAL_EXECUTOR)
    public ThreadPoolTaskExecutor generalExecutor() {
        // Hikari 기본 커넥션 풀(10)을 웹 요청과 나눠 쓰도록 max 4로 제한
        return buildExecutor("async-", 3, 4, 200, loggingDiscardPolicy(GENERAL_EXECUTOR));
    }

    @Bean(EXTERNAL_EXECUTOR)
    public ThreadPoolTaskExecutor externalExecutor() {
        return buildExecutor("ext-", 1, 2, 30, loggingDiscardPolicy(EXTERNAL_EXECUTOR));
    }

    private ThreadPoolTaskExecutor buildExecutor(String threadNamePrefix, int corePoolSize, int maxPoolSize,
                                                 int queueCapacity, RejectedExecutionHandler rejectedHandler) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(rejectedHandler);
        executor.setTaskDecorator(mdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(SHUTDOWN_AWAIT_SECONDS);
        executor.initialize();
        return executor;
    }

    private TaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    // ViewCountService가 REQUIRES_NEW라 CallerRunsPolicy는 커넥션 데드락 위험이 있어 log & discard 사용
    private RejectedExecutionHandler loggingDiscardPolicy(String poolName) {
        return (rejected, executor) ->
                log.warn("[비동기 작업 유실] {}} 풀 포화로 작업을 버립니다. activeThreads={}, queueSize={}",
                        poolName, executor.getActiveCount(), executor.getQueue().size());
    }
}
