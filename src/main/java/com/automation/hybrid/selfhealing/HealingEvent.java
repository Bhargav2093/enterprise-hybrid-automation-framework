package com.automation.hybrid.selfhealing;

import java.time.Instant;

/**
 * Recorded whenever the primary locator for an element failed but a fallback candidate healed it.
 */
public record HealingEvent(
        String page,
        String elementKey,
        int healedFromIndex,
        String strategyType,
        String strategyValue,
        Instant timestamp
) {
}
