package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

public class ProductsPage extends BasePage {

    public ProductsPage(WebDriver driver) {
        super(driver, "productsPage");
    }

    public boolean isAllProductsHeadingDisplayed() {
        return isDisplayed("allProductsHeading");
    }

    public ProductsPage searchProduct(String keyword) {
        type("searchInput", keyword);
        click("searchButton");
        return this;
    }

    public boolean isSearchedProductsHeadingDisplayed() {
        return isDisplayed("searchedProductsHeading");
    }

    public int productCount() {
        return elAll("productList").size();
    }

    public ProductsPage addFirstProductToCart() {
        click("firstProductAddToCart");
        return this;
    }

    public CartPage viewCartFromModal() {
        click("viewCartModalLink");
        return new CartPage(driver);
    }

    public ProductsPage continueShopping() {
        click("continueShoppingButton");
        return this;
    }
}
