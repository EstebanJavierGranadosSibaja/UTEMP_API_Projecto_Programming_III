package org.una.programmingIII.UTEMP_Project.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync(proxyTargetClass = true)
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("Async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();

        logger.info("Async Executor initialized with core pool size: {}, max pool size: {}, queue capacity: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        startExecutorMonitor(executor);

        return executor;
    }

    // Método para monitorear el estado del ThreadPoolExecutor
    private void startExecutorMonitor(ThreadPoolTaskExecutor executor) {
        new Thread(() -> {
            while (true) {
//                try {
//                    Thread.sleep(5000);  // Actualiza cada 5 segundos
//
//                    ThreadPoolExecutor threadPoolExecutor = executor.getThreadPoolExecutor();
//                    logger.info("ThreadPool status: Active Threads: {}, Queue Size: {}, Completed Tasks: {}, Rejected Tasks: {}",
//                            threadPoolExecutor.getActiveCount(),
//                            threadPoolExecutor.getQueue().size(),
//                            threadPoolExecutor.getCompletedTaskCount(),
//                            threadPoolExecutor.getRejectedExecutionHandler().toString());
//                } catch (InterruptedException e) {
//                    logger.error("Error monitoring thread pool", e);
//                }
            }
        }).start();
    }
}