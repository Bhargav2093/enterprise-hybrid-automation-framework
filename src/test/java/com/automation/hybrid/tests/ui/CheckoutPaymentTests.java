package com.automation.hybrid.tests.ui;

import com.automation.hybrid.pages.*;
import com.automation.hybrid.reporting.ReportLogger;
import com.automation.hybrid.tests.base.BaseTest;
import com.automation.hybrid.tests.util.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Enterprise Hybrid Automation Framework")
@Feature("Checkout & Payment")
public class CheckoutPaymentTests extends BaseTest {

    @Test(groups = {"regression"})
    @Description("End-to-end: signup -> add to cart -> checkout -> pay -> order confirmation -> delete account")
    public void fullCheckoutAndPaymentFlow() {
        String email = TestDataFactory.uniqueEmail();
        String name = TestDataFactory.uniqueName("QA_Checkout_");
        String firstName = "QA";
        String lastName = "Checkout";

        ReportLogger.step("Given a new user account is created for " + email);
        HomePage home = new HomePage(driver());
        LoginSignupPage loginSignup = home.goToLoginSignup();
        AccountInfoPage accountInfo = loginSignup.signup(name, email);
        AccountPage accountCreated = accountInfo.fillAndSubmit(
                AccountInfoPage.AccountDetails.sample(firstName, lastName));
        Assert.assertTrue(accountCreated.isConfirmationDisplayed(), "Expected account creation to succeed");
        home = accountCreated.continueToHome();

        ReportLogger.step("When the user adds a product to the cart");
        ProductsPage products = home.goToProducts();
        products.addFirstProductToCart();
        CartPage cart = products.viewCartFromModal();
        Assert.assertTrue(cart.itemCount() > 0, "Expected a product in the cart before checkout");

        ReportLogger.step("And proceeds through checkout with an order comment");
        CheckoutPage checkout = cart.proceedToCheckout();
        Assert.assertTrue(checkout.isAddressDetailsDisplayed(), "Expected delivery address to be shown at checkout");
        PaymentPage payment = checkout.addOrderComment("Automated test order - please ignore").placeOrder();

        ReportLogger.step("And pays with a test card");
        OrderPlacedPage orderPlaced = payment.payWithCard(
                PaymentPage.CardDetails.sample(firstName + " " + lastName));

        ReportLogger.step("Then the order is confirmed");
        Assert.assertTrue(orderPlaced.isOrderPlacedHeadingDisplayed(), "Expected 'Order Placed' confirmation");

        ReportLogger.step("And the test account is deleted (cleanup)");
        home = orderPlaced.continueToHome();
        home.goToDeleteAccount();
        AccountPage accountDeleted = new AccountPage(driver(), AccountPage.Mode.DELETED);
        Assert.assertTrue(accountDeleted.isConfirmationDisplayed(), "Expected account cleanup to succeed");
    }
}
