package com.automation.hybrid.listeners;

import com.automation.hybrid.reporting.ExtentManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * Flushes the ExtentReports HTML file at suite end. ExtentSparkReporter writes incrementally,
 * so this only needs to run once after all tests in the suite have reported their outcome.
 */
public class ExtentReportSuiteListener implements ISuiteListener {

    private static final Logger LOGGER = LogManager.getLogger(ExtentReportSuiteListener.class);

    @Override
    public void onFinish(ISuite suite) {
        ExtentManager.getInstance().flush();
        LOGGER.info("Wrote Extent report");
    }
}
