package com.company.birthday.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("birthdayTaskExecutor")
    public Executor birthdayTaskExecutor(
            @Value("${birthday.async.core-pool-size:4}") int corePoolSize,
            @Value("${birthday.async.max-pool-size:8}") int maxPoolSize,
            @Value("${birthday.async.queue-capacity:50}") int queueCapacity,
            @Value("${birthday.async.thread-name-prefix:BdayWorker-}") String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }
}

