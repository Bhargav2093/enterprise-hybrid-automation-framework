package com.automation.hybrid.tests.ui;

import com.automation.hybrid.pages.CartPage;
import com.automation.hybrid.pages.HomePage;
import com.automation.hybrid.pages.ProductsPage;
import com.automation.hybrid.reporting.ReportLogger;
import com.automation.hybrid.tests.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Enterprise Hybrid Automation Framework")
@Feature("Products & Cart")
public class ProductsCartTests extends BaseTest {

    @Test(groups = {"smoke", "regression"})
    @Description("Searching for a keyword shows the 'Searched Products' section with matching results")
    public void searchingProductsShowsMatchingResults() {
        ReportLogger.step("Given the Products page");
        HomePage home = new HomePage(driver());
        ProductsPage products = home.goToProducts();
        Assert.assertTrue(products.isAllProductsHeadingDisplayed(), "Expected the All Products page to load");

        ReportLogger.step("When the user searches for 'Dress'");
        products.searchProduct("Dress");

        ReportLogger.step("Then matching results are shown under 'Searched Products'");
        Assert.assertTrue(products.isSearchedProductsHeadingDisplayed(), "Expected 'Searched Products' heading");
        Assert.assertTrue(products.productCount() > 0, "Expected at least one search result");
    }

    @Test(groups = {"regression"})
    @Description("Adding a product to the cart and viewing the cart shows the item in the cart table")
    public void addingProductToCartShowsItInCart() {
        ReportLogger.step("Given the Products page");
        HomePage home = new HomePage(driver());
        ProductsPage products = home.goToProducts();

        ReportLogger.step("When the user adds the first product to the cart and views the cart");
        products.addFirstProductToCart();
        CartPage cart = products.viewCartFromModal();

        ReportLogger.step("Then the cart contains the added product");
        Assert.assertTrue(cart.itemCount() > 0, "Expected the cart to contain the added product");
    }
}
