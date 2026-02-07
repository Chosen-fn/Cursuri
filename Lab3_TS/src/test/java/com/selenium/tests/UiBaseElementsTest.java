package com.selenium.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UiBaseElementsTest extends UiDriverTestBase {
    @Test
    public void shouldDisplayBaseElementsOnHomePage() {
        UiTestSupport.openHomePage(driver, wait);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBox")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".rt-table")));
        assertTrue("Expected page to mention Book Store", driver.getPageSource().contains("Book Store"));
    }
}
