package com.matzip.common.config;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    @Bean
    public TaskExecutor asyncTaskExecutor() {
        return new ThreadPoolTaskExecutorBuilder()
                .corePoolSize(10)
                .maxPoolSize(50)
                .queueCapacity(100)
                .threadNamePrefix("Async-")
                .build();
    }
}
