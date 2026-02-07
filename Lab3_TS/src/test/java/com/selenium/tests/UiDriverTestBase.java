package com.selenium.tests;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class UiDriverTestBase {
    protected WebDriver driver;
    protected WebDriverWait wait;

    @Before
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--window-size=1280,720");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void waitUntilBookLinksPresent(int minCount) {
        wait.until(d -> d.findElements(By.cssSelector(".rt-tbody .rt-tr-group a")).size() >= minCount);
    }

    protected void clickSideMenuItem(String label) {
        By locator = By.xpath("//span[normalize-space()='" + label + "']");
        safeClick(locator);
    }

    protected void clickButtonByText(String text) {
        By locator = By.xpath("//button[normalize-space()='" + text + "']");
        safeClick(locator);
    }

    protected void safeClick(By locator) {
        UiTestSupport.removeAds(driver);
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});",
            element
        );
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (ElementClickInterceptedException | TimeoutException e) {
            UiTestSupport.removeAds(driver);
            element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    protected boolean isVisible(By locator, int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected boolean isAlertPresent(int timeoutSeconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.alertIsPresent());
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void pauseBriefly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected List<String> getBookTitles() {
        List<WebElement> links = driver.findElements(By.cssSelector(".rt-tbody .rt-tr-group a"));
        List<String> titles = new ArrayList<>();
        for (WebElement link : links) {
            String text = link.getText().trim();
            if (!text.isEmpty()) {
                titles.add(text);
            }
        }
        return titles;
    }
}
