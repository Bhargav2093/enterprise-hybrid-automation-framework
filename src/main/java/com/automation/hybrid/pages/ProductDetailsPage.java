package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ProductDetailsPage extends BasePage {

    public ProductDetailsPage(WebDriver driver) {
        super(driver, "productDetailsPage");
    }

    public String productName() {
        return textOf("productName");
    }

    public ProductDetailsPage setQuantity(int quantity) {
        WebElement quantityField = el("quantityInput");
        quantityField.clear();
        quantityField.sendKeys(String.valueOf(quantity));
        return this;
    }

    public ProductDetailsPage addToCart() {
        click("addToCartButton");
        return this;
    }
}
