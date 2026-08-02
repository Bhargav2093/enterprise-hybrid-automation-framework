package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

public class OrderPlacedPage extends BasePage {

    public OrderPlacedPage(WebDriver driver) {
        super(driver, "orderPlacedPage");
    }

    public boolean isOrderPlacedHeadingDisplayed() {
        return isDisplayed("orderPlacedHeading");
    }

    public HomePage continueToHome() {
        click("continueButton");
        return new HomePage(driver);
    }
}
