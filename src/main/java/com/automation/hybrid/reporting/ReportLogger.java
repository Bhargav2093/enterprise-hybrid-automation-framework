package com.automation.hybrid.reporting;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Records one test step to every reporting surface at once (Allure step, ExtentTest info log,
 * and the console/file log) so a single call site keeps both HTML reports narrated without
 * per-report boilerplate in tests or page objects.
 */
public final class ReportLogger {

    private static final Logger LOGGER = LogManager.getLogger(ReportLogger.class);

    private ReportLogger() {
    }

    public static void step(String message) {
        LOGGER.info(message);
        Allure.step(message);
        ExtentTest test = ExtentTestManager.getTest();
        if (test != null) {
            test.log(Status.INFO, message);
        }
    }
}
