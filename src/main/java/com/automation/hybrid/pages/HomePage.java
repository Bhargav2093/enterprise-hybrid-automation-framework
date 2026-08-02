package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {

    public HomePage(WebDriver driver) {
        super(driver, "homePage");
    }

    public ProductsPage goToProducts() {
        click("productsNavLink");
        return new ProductsPage(driver);
    }

    public CartPage goToCart() {
        click("cartNavLink");
        return new CartPage(driver);
    }

    public LoginSignupPage goToLoginSignup() {
        click("signupLoginNavLink");
        return new LoginSignupPage(driver);
    }

    public ContactUsPage goToContactUs() {
        click("contactUsNavLink");
        return new ContactUsPage(driver);
    }

    public void goToDeleteAccount() {
        click("deleteAccountNavLink");
    }

    public void logout() {
        click("logoutNavLink");
    }

    public boolean isLoggedInAs() {
        return isDisplayed("loggedInAsText");
    }

    public void subscribeToNewsletter(String email) {
        scrollIntoView("subscribeEmailInput");
        type("subscribeEmailInput", email);
        click("subscribeButton");
    }

    public boolean isSubscriptionSuccessMessageVisible() {
        return isDisplayed("subscribeSuccessMessage");
    }
}
