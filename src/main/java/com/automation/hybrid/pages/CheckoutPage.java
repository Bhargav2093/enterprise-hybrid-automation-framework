package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

public class CheckoutPage extends BasePage {

    public CheckoutPage(WebDriver driver) {
        super(driver, "checkoutPage");
    }

    public boolean isAddressDetailsDisplayed() {
        return isDisplayed("addressDetailsSection");
    }

    public CheckoutPage addOrderComment(String comment) {
        type("commentTextarea", comment);
        return this;
    }

    public PaymentPage placeOrder() {
        click("placeOrderLink");
        return new PaymentPage(driver);
    }
}
