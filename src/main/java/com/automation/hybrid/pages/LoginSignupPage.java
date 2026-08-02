package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

public class LoginSignupPage extends BasePage {

    public LoginSignupPage(WebDriver driver) {
        super(driver, "loginSignupPage");
    }

    public AccountInfoPage signup(String name, String email) {
        type("signupNameInput", name);
        type("signupEmailInput", email);
        click("signupButton");
        return new AccountInfoPage(driver);
    }

    public boolean isSignupErrorDisplayed() {
        return isDisplayed("signupErrorMessage");
    }

    public HomePage login(String email, String password) {
        type("loginEmailInput", email);
        type("loginPasswordInput", password);
        click("loginButton");
        return new HomePage(driver);
    }

    public boolean isLoginErrorDisplayed() {
        return isDisplayed("loginErrorMessage");
    }
}
