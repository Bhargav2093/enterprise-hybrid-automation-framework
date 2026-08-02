package com.automation.hybrid.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.automation.hybrid.config.ConfigManager;

/**
 * Lazily-initialized singleton ExtentReports instance backed by a single self-contained HTML
 * file (target/extent-report/ExtentReport.html) - no separate viewer/CLI needed, unlike Allure.
 */
public final class ExtentManager {

    private static volatile ExtentReports extentReports;

    private ExtentManager() {
    }

    public static ExtentReports getInstance() {
        if (extentReports == null) {
            synchronized (ExtentManager.class) {
                if (extentReports == null) {
                    extentReports = create();
                }
            }
        }
        return extentReports;
    }

    private static ExtentReports create() {
        String reportPath = ConfigManager.get("extent.report.path", "target/extent-report/ExtentReport.html");
        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Enterprise Hybrid Automation Framework - Test Report");
        sparkReporter.config().setReportName("Hybrid UI + API Test Results");

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(sparkReporter);
        extent.setSystemInfo("Browser", ConfigManager.get("browser", "chrome"));
        extent.setSystemInfo("Execution Mode", ConfigManager.get("execution.mode", "local"));
        extent.setSystemInfo("Environment", ConfigManager.get("env", "qa"));
        extent.setSystemInfo("UI Base URL", ConfigManager.get("ui.base.url", "https://automationexercise.com"));
        extent.setSystemInfo("API Base URL", ConfigManager.get("api.base.url", "https://jsonplaceholder.typicode.com"));
        return extent;
    }
}
