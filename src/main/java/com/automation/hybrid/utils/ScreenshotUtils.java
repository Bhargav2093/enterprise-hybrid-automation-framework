package com.automation.hybrid.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public final class ScreenshotUtils {

    private static final Logger LOGGER = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private ScreenshotUtils() {
    }

    public static byte[] captureBytes(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    public static Path captureToFile(WebDriver driver, String testName) {
        return saveBytes(captureBytes(driver), testName);
    }

    public static Path saveBytes(byte[] screenshot, String testName) {
        try {
            Path targetDir = Path.of("target", "screenshots");
            Files.createDirectories(targetDir);
            String fileName = testName + "-" + LocalDateTime.now().format(TIMESTAMP) + ".png";
            Path destination = targetDir.resolve(fileName);
            Files.write(destination, screenshot);
            return destination;
        } catch (IOException e) {
            LOGGER.error("Failed to save screenshot for {}", testName, e);
            return null;
        }
    }
}
