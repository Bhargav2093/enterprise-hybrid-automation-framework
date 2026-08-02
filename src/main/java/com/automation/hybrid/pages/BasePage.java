package com.automation.hybrid.pages;

import com.automation.hybrid.config.ConfigManager;
import com.automation.hybrid.reporting.ReportLogger;
import com.automation.hybrid.selfhealing.SelfHealingLocator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Common behavior for all Page Objects. Element lookups go through SelfHealingLocator, keyed by
 * this page's name (pageKey) + an element key defined in locators.json.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final String pageKey;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver, String pageKey) {
        this.driver = driver;
        this.pageKey = pageKey;
        this.wait = new WebDriverWait(driver,
                Duration.ofSeconds(ConfigManager.getInt("explicit.wait.seconds", 10)));
    }

    protected WebElement el(String elementKey) {
        return SelfHealingLocator.find(driver, pageKey, elementKey);
    }

    protected java.util.List<WebElement> elAll(String elementKey) {
        return SelfHealingLocator.findAll(driver, pageKey, elementKey);
    }

    protected void click(String elementKey) {
        waitUntilClickable(elementKey).click();
        ReportLogger.step("Click " + pageKey + "." + elementKey);
    }

    protected void type(String elementKey, String text) {
        WebElement element = waitUntilVisible(elementKey);
        element.clear();
        element.sendKeys(text);
        String logValue = elementKey.toLowerCase().contains("password") ? "••••••••" : text;
        ReportLogger.step("Enter \"" + logValue + "\" into " + pageKey + "." + elementKey);
    }

    protected String textOf(String elementKey) {
        return waitUntilVisible(elementKey).getText();
    }

    protected boolean isDisplayed(String elementKey) {
        try {
            return el(elementKey).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected WebElement waitUntilVisible(String elementKey) {
        return wait.until(d -> {
            WebElement element = el(elementKey);
            return element.isDisplayed() ? element : null;
        });
    }

    protected WebElement waitUntilClickable(String elementKey) {
        WebElement element = waitUntilVisible(elementKey);
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected void scrollIntoView(String elementKey) {
        WebElement element = el(elementKey);
        ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

}
