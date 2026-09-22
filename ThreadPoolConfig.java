package com.hackforge.orderprocessing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService orderExecutor() {

        /*
         * Bounded pool:
         *  - 10 core + 10 max threads
         *  - bounded queue of 500 pending tasks
         *  - CallerRunsPolicy: when full, the submitting thread runs the
         *    task itself, applying natural backpressure instead of
         *    silently buffering unboundedly and risking OOM.
         */
        return new ThreadPoolExecutor(
                10,
                10,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(500),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}