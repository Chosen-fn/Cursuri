package com.selenium.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UiBookSearchFilterTest extends UiDriverTestBase {
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
}
