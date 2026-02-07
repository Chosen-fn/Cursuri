package com.selenium.tests;

import java.util.Arrays;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

final class UiTestSupport {
    private static final String BASE_URL = "https://demoqa.com/books";
    private static final int MAX_ATTEMPTS = 3;

    private UiTestSupport() {
    }

    static void openHomePage(WebDriver driver, WebDriverWait wait) {
        openUrlWithRetries(driver, wait, BASE_URL);
    }

    static void openUrlWithRetries(WebDriver driver, WebDriverWait wait, String url) {
        TimeoutException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                driver.get(url);
                waitForReadyState(driver, wait);
                removeAds(driver);
                waitForCoreElements(wait);
                return;
            } catch (TimeoutException e) {
                lastError = e;
                removeAds(driver);
                if (attempt < MAX_ATTEMPTS) {
                    driver.navigate().refresh();
                }
            }
        }
        if (lastError != null) {
            throw lastError;
        }
    }

    static void removeAds(WebDriver driver) {
        List<String> selectors = Arrays.asList(
            "#fixedban",
            "#adplus-anchor",
            "#adplus",
            "#adplus-container",
            "iframe[id^='google_ads_iframe']",
            "iframe[src*='googlesyndication']",
            "iframe[src*='doubleclick']",
            "ins.adsbygoogle"
        );

        ((JavascriptExecutor) driver).executeScript(
            "const selectors = arguments[0];"
                + "selectors.forEach(sel => document.querySelectorAll(sel).forEach(el => el.remove()));",
            selectors
        );
    }

    private static void waitForReadyState(WebDriver driver, WebDriverWait wait) {
        wait.until(d -> "complete".equals(
            ((JavascriptExecutor) d).executeScript("return document.readyState")
        ));
    }

    private static void waitForCoreElements(WebDriverWait wait) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".rt-table")));
    }
}
