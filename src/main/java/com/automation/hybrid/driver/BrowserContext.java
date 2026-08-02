package com.automation.hybrid.driver;

/**
 * Thread-local browser override, set from a TestNG <parameter> (see testng-crossbrowser.xml)
 * so parallel cross-browser <test> tags don't race on a shared System property.
 */
public final class BrowserContext {

    private static final ThreadLocal<String> BROWSER = new ThreadLocal<>();

    private BrowserContext() {
    }

    public static void set(String browser) {
        if (browser != null && !browser.isBlank()) {
            BROWSER.set(browser);
        }
    }

    public static String get() {
        return BROWSER.get();
    }

    public static void clear() {
        BROWSER.remove();
    }
}
