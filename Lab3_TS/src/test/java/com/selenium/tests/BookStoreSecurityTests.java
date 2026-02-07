package com.selenium.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BookStoreSecurityTests {
    private static final String BASE_URL = "https://demoqa.com/books";
    private WebDriver driver;
    private WebDriverWait wait;

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

    @Test
    public void shouldHaveValidSslCertificate() throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(BASE_URL).openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);
        connection.connect();

        int status = connection.getResponseCode();
        assertEquals(200, status);
        assertTrue("Expected server to present SSL certificates", connection.getServerCertificates().length > 0);
        connection.disconnect();
    }

    @Test
    public void shouldNotExecuteXssInSearchBox() {
        UiTestSupport.openHomePage(driver, wait);

        WebElement searchBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        searchBox.clear();
        searchBox.sendKeys("<script>alert('xss')</script>");

        assertFalse("Unexpected alert triggered", isAlertPresent());
        wait.until(d -> d.findElements(By.cssSelector(".rt-tbody .rt-tr-group a")).isEmpty());
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
