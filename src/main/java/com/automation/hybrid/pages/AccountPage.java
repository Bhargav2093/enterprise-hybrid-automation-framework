package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

/**
 * Covers both the "Account Created!" and "Account Deleted!" confirmation pages, which share
 * an identical layout (heading + Continue button) under different locator keys.
 */
public class AccountPage extends BasePage {

    public enum Mode {
        CREATED("accountCreatedPage", "accountCreatedHeading"),
        DELETED("accountDeletedPage", "accountDeletedHeading");

        final String pageKey;
        final String headingKey;

        Mode(String pageKey, String headingKey) {
            this.pageKey = pageKey;
            this.headingKey = headingKey;
        }
    }

    private final Mode mode;

    public AccountPage(WebDriver driver, Mode mode) {
        super(driver, mode.pageKey);
        this.mode = mode;
    }

    public boolean isConfirmationDisplayed() {
        return isDisplayed(mode.headingKey);
    }

    public HomePage continueToHome() {
        click("continueButton");
        return new HomePage(driver);
    }
}
