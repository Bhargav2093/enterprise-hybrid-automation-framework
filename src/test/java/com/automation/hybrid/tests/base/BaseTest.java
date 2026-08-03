package com.automation.hybrid.tests.base;

import com.automation.hybrid.config.ConfigManager;
import com.automation.hybrid.driver.BotChallengeWaiter;
import com.automation.hybrid.driver.BrowserContext;
import com.automation.hybrid.driver.DriverFactory;
import com.automation.hybrid.driver.DriverManager;
import com.automation.hybrid.driver.OverlayDismisser;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base class for all UI test classes: creates a fresh WebDriver per test method (parallel-safe
 * via DriverManager's ThreadLocal) and tears it down afterward.
 */
public abstract class BaseTest {

    protected String baseUrl;

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser"})
    public void setUp(@Optional("") String browserParam) {
        BrowserContext.set(browserParam);
        WebDriver driver = DriverFactory.createDriver();
        DriverManager.setDriver(driver);
        baseUrl = ConfigManager.get("ui.base.url", "https://automationexercise.com");
        driver.get(baseUrl);
        BotChallengeWaiter.waitForRealPage(driver);
        OverlayDismisser.dismissIfPresent(driver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        DriverManager.quitDriver();
        BrowserContext.clear();
    }

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }
}
