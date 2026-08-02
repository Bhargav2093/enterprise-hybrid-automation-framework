package com.automation.hybrid.tests.ui;

import com.automation.hybrid.pages.AccountInfoPage;
import com.automation.hybrid.pages.AccountPage;
import com.automation.hybrid.pages.HomePage;
import com.automation.hybrid.pages.LoginSignupPage;
import com.automation.hybrid.reporting.ReportLogger;
import com.automation.hybrid.tests.base.BaseTest;
import com.automation.hybrid.tests.util.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Enterprise Hybrid Automation Framework")
@Feature("Registration & Login")
public class RegistrationLoginTests extends BaseTest {

    @Test(groups = {"smoke", "regression"})
    @Description("Signup -> account created -> logged in -> logout -> login again -> delete account")
    public void signupLoginLogoutDeleteAccountFullLifecycle() {
        String email = TestDataFactory.uniqueEmail();
        String name = TestDataFactory.uniqueName("QA_User_");
        String password = "Test@12345";

        ReportLogger.step("Given a new user with email " + email);
        HomePage home = new HomePage(driver());
        LoginSignupPage loginSignup = home.goToLoginSignup();

        ReportLogger.step("When the user signs up and completes the account information form");
        AccountInfoPage accountInfo = loginSignup.signup(name, email);
        AccountInfoPage.AccountDetails details = AccountInfoPage.AccountDetails.sample("QA", "Automation");
        AccountPage accountCreated = accountInfo.fillAndSubmit(details);

        ReportLogger.step("Then the account is created and the user is logged in automatically");
        Assert.assertTrue(accountCreated.isConfirmationDisplayed(), "Expected 'Account Created' confirmation");
        home = accountCreated.continueToHome();
        Assert.assertTrue(home.isLoggedInAs(), "Expected to be logged in immediately after account creation");

        ReportLogger.step("When the user logs out and logs back in with the same credentials");
        home.logout();
        loginSignup = new LoginSignupPage(driver());
        home = loginSignup.login(email, password);

        ReportLogger.step("Then the user is logged in again");
        Assert.assertTrue(home.isLoggedInAs(), "Expected to be logged in after re-login with the same credentials");

        ReportLogger.step("When the user deletes their account (test cleanup)");
        home.goToDeleteAccount();
        AccountPage accountDeleted = new AccountPage(driver(), AccountPage.Mode.DELETED);

        ReportLogger.step("Then the account is deleted");
        Assert.assertTrue(accountDeleted.isConfirmationDisplayed(), "Expected 'Account Deleted' confirmation");
    }

    @Test(groups = {"smoke", "regression"})
    @Description("Logging in with a bogus email/password shows the expected inline error")
    public void loginWithInvalidCredentialsShowsError() {
        ReportLogger.step("Given a login form and a non-existent user's credentials");
        HomePage home = new HomePage(driver());
        LoginSignupPage loginSignup = home.goToLoginSignup();

        ReportLogger.step("When the user attempts to log in");
        loginSignup.login("no-such-user-" + System.currentTimeMillis() + "@mailinator.com", "WrongPassword123");

        ReportLogger.step("Then an invalid-login error message is shown");
        Assert.assertTrue(loginSignup.isLoginErrorDisplayed(), "Expected an invalid-login error message");
    }
}
