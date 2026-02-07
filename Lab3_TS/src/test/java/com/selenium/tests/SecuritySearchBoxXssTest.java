package com.selenium.tests;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SecuritySearchBoxXssTest extends UiDriverTestBase {
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
