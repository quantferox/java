package com.quantferox.lumeo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async + Scheduling configuration.
 *
 * <p>Single named executor {@code lumeoTaskExecutor} is used for:
 * <ul>
 *   <li>Event listeners ({@code @Async})</li>
 *   <li>Notification service methods ({@code @Async})</li>
 *   <li>Scheduled jobs ({@code @Scheduled}) run in the default scheduler thread</li>
 * </ul>
 *
 * <p>Thread pool is sized conservatively - tune via properties for prod.
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "lumeoTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("lumeo-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("[ASYNC ERROR] method={} params={} error={}",
                        method.getName(), params, ex.getMessage(), ex);
    }
}
