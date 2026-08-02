package com.automation.hybrid.listeners;

import com.automation.hybrid.config.ConfigManager;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Retries a failed @Test method up to retry.max.count times (config), to absorb transient
 * flakiness (network blips, slow-rendering elements) without masking real regressions.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private final AtomicInteger attempts = new AtomicInteger(0);

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = ConfigManager.getInt("retry.max.count", 1);
        if (attempts.getAndIncrement() < maxRetries) {
            return true;
        }
        return false;
    }
}
