package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

/**
 * The "ENTER ACCOUNT INFORMATION" form shown right after submitting the signup name/email.
 */
public class AccountInfoPage extends BasePage {

    public AccountInfoPage(WebDriver driver) {
        super(driver, "accountInfoPage");
    }

    public AccountPage fillAndSubmit(AccountDetails details) {
        click("genderMrRadio");
        type("passwordInput", details.password());
        new Select(el("daysSelect")).selectByVisibleText(details.day());
        new Select(el("monthsSelect")).selectByVisibleText(details.month());
        new Select(el("yearsSelect")).selectByVisibleText(details.year());
        type("firstNameInput", details.firstName());
        type("lastNameInput", details.lastName());
        type("addressInput", details.address());
        new Select(el("countrySelect")).selectByVisibleText(details.country());
        type("stateInput", details.state());
        type("cityInput", details.city());
        type("zipcodeInput", details.zipcode());
        type("mobileNumberInput", details.mobileNumber());
        click("createAccountButton");
        return new AccountPage(driver, AccountPage.Mode.CREATED);
    }

    public record AccountDetails(
            String password, String day, String month, String year,
            String firstName, String lastName, String address,
            String country, String state, String city, String zipcode, String mobileNumber
    ) {
        public static AccountDetails sample(String firstName, String lastName) {
            return new AccountDetails(
                    "Test@12345", "15", "May", "1995",
                    firstName, lastName, "123 QA Street",
                    "United States", "California", "San Francisco", "94105", "5551234567");
        }
    }
}
