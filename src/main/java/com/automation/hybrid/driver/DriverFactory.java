package com.automation.hybrid.driver;

import com.automation.hybrid.config.ConfigManager;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a WebDriver for one of three execution modes (execution.mode config key):
 *  - local:        Selenium Manager resolves the driver binary automatically (Selenium 4+)
 *  - grid:         self-hosted Selenium Grid, see docker/docker-compose.yml
 *  - browserstack: BrowserStack Automate cloud, credentials from env vars only
 */
public final class DriverFactory {

    private static final List<String> AD_DOMAIN_BLOCKLIST = List.of(
            "pagead2.googlesyndication.com",
            "googleads.g.doubleclick.net",
            "www.googletagservices.com",
            "ep2.adtrafficquality.google",
            "ep1.adtrafficquality.google"
    );

    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        String mode = ConfigManager.get("execution.mode", "local");
        String browserOverride = BrowserContext.get();
        String browser = (browserOverride != null ? browserOverride : ConfigManager.get("browser", "chrome"))
                .toLowerCase();

        WebDriver driver = switch (mode) {
            case "grid" -> createRemoteDriver(browser, gridUrl());
            case "browserstack" -> createBrowserStackDriver(browser);
            default -> createLocalDriver(browser);
        };

        driver.manage().timeouts().implicitlyWait(
                Duration.ofSeconds(ConfigManager.getInt("implicit.wait.seconds", 2)));
        return driver;
    }

    private static WebDriver createLocalDriver(String browser) {
        boolean headless = ConfigManager.getBoolean("headless", false);
        return switch (browser) {
            case "firefox" -> {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) {
                    options.addArguments("-headless");
                }
                yield new FirefoxDriver(options);
            }
            case "edge" -> {
                EdgeOptions options = new EdgeOptions();
                if (headless) {
                    options.addArguments("--headless=new");
                }
                yield new EdgeDriver(options);
            }
            default -> {
                ChromeOptions options = chromeOptions(headless);
                yield new ChromeDriver(options);
            }
        };
    }

    private static WebDriver createRemoteDriver(String browser, URL url) {
        MutableCapabilities capabilities = switch (browser) {
            case "firefox" -> new FirefoxOptions();
            case "edge" -> new EdgeOptions();
            default -> chromeOptions(ConfigManager.getBoolean("headless", false));
        };
        return new RemoteWebDriver(url, capabilities);
    }

    private static WebDriver createBrowserStackDriver(String browser) {
        String username = System.getenv("BROWSERSTACK_USERNAME");
        String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
        if (username == null || accessKey == null || username.isBlank() || accessKey.isBlank()) {
            throw new IllegalStateException(
                    "BrowserStack execution requires BROWSERSTACK_USERNAME and BROWSERSTACK_ACCESS_KEY " +
                            "environment variables to be set. They are never read from config files.");
        }

        Map<String, Object> bstackOptions = new HashMap<>();
        bstackOptions.put("userName", username);
        bstackOptions.put("accessKey", accessKey);
        bstackOptions.put("projectName", ConfigManager.get("browserstack.project", "Enterprise Hybrid Automation Framework"));
        bstackOptions.put("buildName", ConfigManager.get("browserstack.build", "local-build"));
        bstackOptions.put("sessionName", browser + " session");
        bstackOptions.put("os", ConfigManager.get("browserstack.os", "Windows"));
        bstackOptions.put("osVersion", ConfigManager.get("browserstack.osVersion", "11"));

        MutableCapabilities capabilities = switch (browser) {
            case "firefox" -> new FirefoxOptions();
            case "edge" -> new EdgeOptions();
            default -> new ChromeOptions();
        };
        capabilities.setCapability("bstack:options", bstackOptions);
        capabilities.setCapability("browserName", browser);

        try {
            URL url = URI.create(ConfigManager.get("browserstack.url",
                    "https://hub-cloud.browserstack.com/wd/hub")).toURL();
            return new RemoteWebDriver(url, capabilities);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid browserstack.url", e);
        }
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        // Ad/analytics domains on the public site under test can inject full-page interstitials
        // ("vignette" ads) that intercept clicks mid-navigation. Blocking them at the DNS level
        // is a standard test-stability technique and doesn't touch anything first-party.
        options.addArguments("--host-resolver-rules=" + AD_DOMAIN_BLOCKLIST.stream()
                .map(domain -> "MAP " + domain + " 127.0.0.1")
                .collect(java.util.stream.Collectors.joining(", ")));
        return options;
    }

    private static URL gridUrl() {
        try {
            return URI.create(ConfigManager.get("grid.url", "http://localhost:4444/wd/hub")).toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid grid.url", e);
        }
    }
}
