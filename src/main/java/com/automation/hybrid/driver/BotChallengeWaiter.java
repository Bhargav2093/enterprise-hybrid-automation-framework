package com.automation.hybrid.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.time.Instant;

/**
 * The public demo site under test occasionally serves a transient bot-verification page
 * ("Please wait while your request is being verified...") to shared CI runner IPs before letting
 * the real page through - confirmed via failure screenshots captured on a CI run where every
 * self-healing locator exhausted its candidates because the real nav bar never rendered. No
 * locator change can work around a page that genuinely hasn't loaded yet, so this waits it out.
 */
public final class BotChallengeWaiter {

    private static final Logger LOGGER = LogManager.getLogger(BotChallengeWaiter.class);
    private static final String CHALLENGE_MARKER = "being verified";
    private static final Duration MAX_WAIT = Duration.ofSeconds(30);
    private static final long POLL_INTERVAL_MILLIS = 500;

    private BotChallengeWaiter() {
    }

    public static void waitForRealPage(WebDriver driver) {
        Instant deadline = Instant.now().plus(MAX_WAIT);
        boolean sawChallenge = false;

        while (Instant.now().isBefore(deadline)) {
            if (!safePageSource(driver).contains(CHALLENGE_MARKER)) {
                if (sawChallenge) {
                    LOGGER.info("Bot-verification challenge cleared");
                }
                return;
            }
            sawChallenge = true;
            sleep();
        }

        if (sawChallenge) {
            LOGGER.warn("Bot-verification challenge did not clear within {}", MAX_WAIT);
        }
    }

    private static String safePageSource(WebDriver driver) {
        try {
            String source = driver.getPageSource();
            return source == null ? "" : source;
        } catch (Exception e) {
            return "";
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(POLL_INTERVAL_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for challenge page to clear", e);
        }
    }
}
