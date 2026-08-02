package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

public class ContactUsPage extends BasePage {

    public ContactUsPage(WebDriver driver) {
        super(driver, "contactUsPage");
    }

    public ContactUsPage fillForm(String name, String email, String subject, String message) {
        type("nameInput", name);
        type("emailInput", email);
        type("subjectInput", subject);
        type("messageTextarea", message);
        return this;
    }

    public ContactUsPage uploadFile(String absoluteFilePath) {
        el("uploadFileInput").sendKeys(absoluteFilePath);
        return this;
    }

    public ContactUsPage submit() {
        click("submitButton");
        try {
            wait.until(org.openqa.selenium.support.ui.ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (org.openqa.selenium.TimeoutException ignored) {
            // some browsers/CI environments suppress the native confirm() dialog
        }
        return this;
    }

    public boolean isSuccessMessageDisplayed() {
        return isDisplayed("successMessage");
    }
}
