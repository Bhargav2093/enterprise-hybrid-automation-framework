package com.automation.hybrid.tests.ui;

import com.automation.hybrid.pages.ContactUsPage;
import com.automation.hybrid.pages.HomePage;
import com.automation.hybrid.reporting.ReportLogger;
import com.automation.hybrid.tests.base.BaseTest;
import com.automation.hybrid.tests.util.TestDataFactory;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

@Epic("Enterprise Hybrid Automation Framework")
@Feature("Contact Us")
public class ContactUsTests extends BaseTest {

    @Test(groups = {"regression"})
    @Description("Submitting the Contact Us form (including a file upload) shows the success message")
    public void submittingContactFormShowsSuccessMessage() {
        ReportLogger.step("Given the Contact Us page");
        HomePage home = new HomePage(driver());
        ContactUsPage contactUs = home.goToContactUs();

        String uploadPath = Path.of("src", "test", "resources", "testdata", "sample-upload.txt")
                .toAbsolutePath().toString();

        ReportLogger.step("When the user fills the form, attaches a file, and submits it");
        contactUs.fillForm(
                TestDataFactory.uniqueName("QA Contact "),
                TestDataFactory.uniqueEmail(),
                "Automated framework smoke test",
                "This message was submitted by an automated test in the Enterprise Hybrid Automation Framework."
        ).uploadFile(uploadPath).submit();

        ReportLogger.step("Then the success message is displayed");
        Assert.assertTrue(contactUs.isSuccessMessageDisplayed(), "Expected the contact form success message");
    }
}
