package com.automation.hybrid.reporting;

import com.aventstack.extentreports.ExtentTest;

/**
 * Thread-local holder for the current test's ExtentTest node, mirroring DriverManager's pattern
 * so parallel TestNG execution never leaks entries across threads.
 */
public final class ExtentTestManager {

    private static final ThreadLocal<ExtentTest> TEST = new ThreadLocal<>();

    private ExtentTestManager() {
    }

    public static ExtentTest startTest(String name, String description) {
        ExtentTest test = ExtentManager.getInstance().createTest(name, description);
        TEST.set(test);
        return test;
    }

    public static ExtentTest getTest() {
        return TEST.get();
    }

    public static void clear() {
        TEST.remove();
    }
}
