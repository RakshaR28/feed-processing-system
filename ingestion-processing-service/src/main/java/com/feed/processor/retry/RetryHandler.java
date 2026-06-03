package com.feed.processor.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

@Slf4j
@Service
public class RetryHandler {

    public <T> T execute(Callable<T> task) {

        int maxRetries = 3;

        for (int i = 1; i <= maxRetries; i++) {

            try {
                return task.call();

            } catch (Exception e) {

                log.error("Retry attempt {} failed. Error: {}", i, e.getMessage(), e);

                if (i == maxRetries) {
                    log.error("Max retries reached. Failing permanently.");
                    throw new RuntimeException(e);
                }

                try {
                    long waitTime = (long) Math.pow(2, i) * 1000;
                    log.warn("Retrying after {} ms...", waitTime);
                    Thread.sleep(waitTime);
                } catch (InterruptedException ignored) {}
            }
        }

        return null;
    }
}