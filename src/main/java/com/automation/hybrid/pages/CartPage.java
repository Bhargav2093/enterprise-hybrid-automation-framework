package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {

    public CartPage(WebDriver driver) {
        super(driver, "cartPage");
    }

    public int itemCount() {
        return elAll("cartItemRows").size();
    }

    public boolean isEmptyCartMessageDisplayed() {
        return isDisplayed("emptyCartMessage");
    }

    public CheckoutPage proceedToCheckout() {
        click("proceedToCheckoutButton");
        return new CheckoutPage(driver);
    }
}
