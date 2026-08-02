package com.automation.hybrid.pages;

import org.openqa.selenium.WebDriver;

public class PaymentPage extends BasePage {

    public PaymentPage(WebDriver driver) {
        super(driver, "paymentPage");
    }

    public OrderPlacedPage payWithCard(CardDetails card) {
        type("nameOnCardInput", card.nameOnCard());
        type("cardNumberInput", card.cardNumber());
        type("cvcInput", card.cvc());
        type("expiryMonthInput", card.expiryMonth());
        type("expiryYearInput", card.expiryYear());
        click("payButton");
        return new OrderPlacedPage(driver);
    }

    public record CardDetails(String nameOnCard, String cardNumber, String cvc, String expiryMonth, String expiryYear) {
        public static CardDetails sample(String nameOnCard) {
            return new CardDetails(nameOnCard, "4111111111111111", "123", "12", "2030");
        }
    }
}
