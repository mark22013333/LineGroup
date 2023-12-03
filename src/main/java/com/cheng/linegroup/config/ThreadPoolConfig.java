package com.cheng.linegroup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Cheng
 * @since 2022/12/10 下午 04:14
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_POOL_SIZE = Math.max(CORE_POOL_SIZE * 4, 256);
    // 執行緒空閒時間 (單位:秒)
    private static final int KEEP_ALIVE_TIME = 10;
    private static final int QUEUE_CAPACITY = 1000;
    // 執行緒池中任務的等待時間，如果逾時就強制銷毀
    private static final int AWAIT_TERMINATION = 60;
    // 執行緒池關閉的時候等待所有任務都完成在繼續銷毀其他的bean
    private static final Boolean WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN = true;
    // 執行緒池的前綴名稱
    private static final String THREAD_NAME_PREFIX = "apiThreadPool_";

    @Bean
    public ThreadPoolTaskExecutor apiThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setKeepAliveSeconds(KEEP_ALIVE_TIME);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setWaitForTasksToCompleteOnShutdown(WAIT_FOR_TASKS_TO_COMPLETE_ON_SHUTDOWN);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION);
        // CallerRunsPolicy：不在新的Thread執行任務，而是由呼叫者所在的Thread來執行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
