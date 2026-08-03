package com.automation.hybrid.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Best-effort dismissal of cookie-consent banners and ad interstitials that public demo sites
 * sometimes inject on first page load. On a fresh, cookie-less CI browser profile these can cover
 * or delay the real page content long enough that self-healing locators exhaust all candidates
 * looking for nav elements that technically exist but haven't rendered under the overlay yet.
 *
 * Never fails the caller: if nothing matches, this is a silent no-op.
 */
public final class OverlayDismisser {

    private static final Logger LOGGER = LogManager.getLogger(OverlayDismisser.class);

    private static final List<By> DISMISS_BUTTON_CANDIDATES = List.of(
            By.cssSelector("#onetrust-accept-btn-handler"),
            By.xpath("//button[contains(translate(normalize-space(text())," +
                    "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'accept')]"),
            By.cssSelector("[aria-label='Close'], [aria-label='close']"),
            By.cssSelector(".ad-close, .close-ad, .dismiss-ad")
    );

    private OverlayDismisser() {
    }

    public static void dismissIfPresent(WebDriver driver) {
        for (By locator : DISMISS_BUTTON_CANDIDATES) {
            try {
                List<WebElement> matches = driver.findElements(locator);
                if (!matches.isEmpty() && matches.get(0).isDisplayed()) {
                    matches.get(0).click();
                    LOGGER.info("Dismissed overlay/banner using {}", locator);
                    return;
                }
            } catch (Exception e) {
                // Best-effort only - a failed dismiss attempt must never fail test setup.
            }
        }
    }
}
