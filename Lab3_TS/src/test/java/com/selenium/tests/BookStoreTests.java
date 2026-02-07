package com.selenium.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BookStoreTests {
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
    public void shouldDisplayBaseElementsOnHomePage() {
        UiTestSupport.openHomePage(driver, wait);

        WebElement mainHeader = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-header"))
        );
        assertTrue("Expected main header to contain Book Store", mainHeader.getText().contains("Book Store"));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".rt-table")));
    }

    @Test
    public void shouldNavigateToLoginForm() {
        UiTestSupport.openHomePage(driver, wait);

        WebElement loginButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.id("login"))
        );
        loginButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userName")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        assertTrue("Expected to be on login page", driver.getCurrentUrl().contains("/login"));
    }

    @Test
    public void shouldUseSideMenuToNavigateSections() {
        UiTestSupport.openHomePage(driver, wait);

        clickSideMenuItem("Profile");
        wait.until(ExpectedConditions.urlContains("/profile"));

        clickSideMenuItem("Book Store");
        wait.until(ExpectedConditions.urlContains("/books"));
    }

    @Test
    public void shouldFilterBooksBySearch() {
        UiTestSupport.openHomePage(driver, wait);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        waitUntilBookLinksPresent(3);
        WebElement searchBox = driver.findElement(By.id("searchBox"));
        searchBox.clear();
        searchBox.sendKeys("Git Pocket Guide");

        wait.until(d -> d.findElements(By.cssSelector(".rt-tbody .rt-tr-group a")).size() == 1);
        WebElement onlyLink = driver.findElement(By.cssSelector(".rt-tbody .rt-tr-group a"));
        assertEquals("Git Pocket Guide", onlyLink.getText().trim());
    }

    @Test
    public void shouldSortBooksByTitle() {
        UiTestSupport.openHomePage(driver, wait);
        waitUntilBookLinksPresent(2);

        WebElement titleHeader = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'rt-th') and normalize-space()='Title']")
            )
        );
        titleHeader.click();

        wait.until(d -> {
            List<String> titles = getBookTitles();
            if (titles.size() < 2) {
                return false;
            }
            List<String> sorted = new ArrayList<>(titles);
            sorted.sort(String.CASE_INSENSITIVE_ORDER);
            return titles.equals(sorted);
        });
    }

    @Test
    public void shouldOpenBookDetailsFromList() {
        UiTestSupport.openHomePage(driver, wait);
        waitUntilBookLinksPresent(1);

        WebElement firstBook = driver.findElement(By.cssSelector(".rt-tbody .rt-tr-group a"));
        firstBook.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ISBN-wrapper")));
        clickButtonByText("Back To Book Store");

        wait.until(ExpectedConditions.urlContains("/books"));
    }

    @Test
    public void shouldRequireLoginToAddToCollection() {
        UiTestSupport.openHomePage(driver, wait);
        waitUntilBookLinksPresent(1);

        WebElement firstBook = driver.findElement(By.cssSelector(".rt-tbody .rt-tr-group a"));
        firstBook.click();

        clickButtonByText("Add To Your Collection");
        String alertText = wait.until(ExpectedConditions.alertIsPresent()).getText().toLowerCase();
        assertTrue(
            "Expected alert to mention login or authorization",
            alertText.contains("login") || alertText.contains("authorized")
        );
        driver.switchTo().alert().accept();
    }

    private void waitUntilBookLinksPresent(int minCount) {
        wait.until(d -> d.findElements(By.cssSelector(".rt-tbody .rt-tr-group a")).size() >= minCount);
    }

    private void clickSideMenuItem(String label) {
        UiTestSupport.removeAds(driver);
        By locator = By.xpath("//span[normalize-space()='" + label + "']");
        WebElement item = wait.until(ExpectedConditions.elementToBeClickable(locator));
        item.click();
    }

    private void clickButtonByText(String text) {
        UiTestSupport.removeAds(driver);
        By locator = By.xpath("//button[normalize-space()='" + text + "']");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
        button.click();
    }

    private List<String> getBookTitles() {
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
