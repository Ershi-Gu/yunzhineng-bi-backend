package com.ershi.bibackend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * 超时任务线程池配置类
 * @author Ershi
 * @date 2024/05/22
 */
@Configuration
public class ScheduledThreadPoolExecutorConfig {

    /**
     * 自定义线程工厂
     */
    ThreadFactory threadFactory = new ThreadFactory() {
        private int count = 1;

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("超时任务线程-" + count);
            count++;
            return thread;
        }
    };

    /**
     * 超时任务线程池
     * @return {@link ScheduledThreadPoolExecutor}
     */
    @Bean
    public ScheduledThreadPoolExecutor scheduledThreadPoolExecutor() {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
                new ScheduledThreadPoolExecutor(2, threadFactory);
        return scheduledThreadPoolExecutor;
    }
}
