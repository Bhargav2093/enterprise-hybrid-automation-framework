package com.automation.hybrid.selfhealing;

import com.automation.hybrid.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Resolves an element via its ordered list of candidate locators (locators.json). The first
 * candidate that matches wins; if that candidate isn't index 0, the element "healed" and the
 * event is recorded via HealingReportManager. Retries the whole candidate list until the
 * configured explicit-wait timeout elapses, so slow-rendering elements aren't mistaken for
 * missing ones.
 */
public final class SelfHealingLocator {

    private static final Logger LOGGER = LogManager.getLogger(SelfHealingLocator.class);
    private static final long POLL_INTERVAL_MILLIS = 250;

    private SelfHealingLocator() {
    }

    public static List<WebElement> findAll(WebDriver driver, String page, String elementKey) {
        List<LocatorCandidate> candidates = LocatorRepository.get(page, elementKey);
        for (int i = 0; i < candidates.size(); i++) {
            LocatorCandidate candidate = candidates.get(i);
            List<WebElement> matches = driver.findElements(candidate.toBy());
            if (!matches.isEmpty()) {
                if (i > 0) {
                    heal(page, elementKey, i, candidate);
                }
                return matches;
            }
        }
        return List.of();
    }

    public static WebElement find(WebDriver driver, String page, String elementKey) {
        List<LocatorCandidate> candidates = LocatorRepository.get(page, elementKey);
        long timeoutMillis = Duration.ofSeconds(ConfigManager.getInt("explicit.wait.seconds", 10)).toMillis();
        long deadline = System.currentTimeMillis() + timeoutMillis;

        do {
            for (int i = 0; i < candidates.size(); i++) {
                LocatorCandidate candidate = candidates.get(i);
                List<WebElement> matches = driver.findElements(candidate.toBy());
                if (!matches.isEmpty()) {
                    if (i > 0) {
                        heal(page, elementKey, i, candidate);
                    }
                    return matches.get(0);
                }
            }
            sleep();
        } while (System.currentTimeMillis() < deadline);

        throw new NoSuchElementException("Self-healing locator exhausted all " + candidates.size() +
                " candidate(s) for " + page + "." + elementKey + ": " + candidates);
    }

    private static void heal(String page, String elementKey, int healedFromIndex, LocatorCandidate candidate) {
        LOGGER.warn("Self-healed {}.{} using fallback candidate #{} ({})",
                page, elementKey, healedFromIndex, candidate);
        HealingReportManager.record(new HealingEvent(
                page, elementKey, healedFromIndex, candidate.type(), candidate.value(), Instant.now()));
    }

    private static void sleep() {
        try {
            Thread.sleep(POLL_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling for element", e);
        }
    }
}
