package com.automation.hybrid.tests.util;

import java.time.Instant;

/**
 * Generates unique test data (mainly emails) so signup-based tests don't collide with
 * "email already exists" state left over from previous runs.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static String uniqueEmail() {
        return "qa.hybrid." + Instant.now().toEpochMilli() + "@mailinator.com";
    }

    public static String uniqueName(String prefix) {
        return prefix + Instant.now().toEpochMilli();
    }
}
