package com.automation.hybrid.listeners;

import com.automation.hybrid.driver.DriverManager;
import com.automation.hybrid.reporting.ExtentTestManager;
import com.automation.hybrid.utils.ScreenshotUtils;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.util.Base64;

/**
 * Drives both reports from one TestNG listener: creates an ExtentTest per method and logs its
 * pass/fail/skip outcome, and on failure saves + attaches a screenshot to both Extent and Allure.
 * API-only tests have no active WebDriver, so screenshot steps are skipped rather than failing
 * the listener itself.
 */
public class TestListener implements ITestListener {

    private static final Logger LOGGER = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTestManager.startTest(result.getMethod().getMethodName(), descriptionOf(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        currentExtentTest().log(Status.PASS, "Test passed");
        ExtentTestManager.clear();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.error("Test failed: {}", result.getName(), result.getThrowable());
        ExtentTest extentTest = currentExtentTest();
        extentTest.log(Status.FAIL, result.getThrowable());

        WebDriver driver = safeDriver();
        if (driver != null) {
            byte[] screenshot = ScreenshotUtils.captureBytes(driver);
            ScreenshotUtils.saveBytes(screenshot, result.getMethod().getMethodName());
            Allure.addAttachment("Screenshot on failure: " + result.getName(), new ByteArrayInputStream(screenshot));
            extentTest.fail("Screenshot on failure", MediaEntityBuilder
                    .createScreenCaptureFromBase64String(Base64.getEncoder().encodeToString(screenshot))
                    .build());
        }
        ExtentTestManager.clear();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        currentExtentTest().log(Status.SKIP, result.getThrowable() != null
                ? result.getThrowable().getMessage() : "Test skipped");
        ExtentTestManager.clear();
    }

    private ExtentTest currentExtentTest() {
        ExtentTest test = ExtentTestManager.getTest();
        return test != null ? test : ExtentTestManager.startTest("unknown", "");
    }

    private String descriptionOf(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        Description description = method.getAnnotation(Description.class);
        return description != null ? description.value() : "";
    }

    private WebDriver safeDriver() {
        try {
            return DriverManager.getDriver();
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
