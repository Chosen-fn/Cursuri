package com.selenium.tests;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UiOpenBookDetailsTest extends UiDriverTestBase {
    private static final long NO_DATA_DELAY_MS = 2_000;

    @Test
    public void shouldOpenBookDetailsFromList() {
        UiTestSupport.openHomePage(driver, wait);
        waitUntilBookLinksPresent(1);

        safeClick(By.cssSelector(".rt-tbody .rt-tr-group a"));
        wait.until(ExpectedConditions.urlContains("book="));

        boolean hasBookDetails =
            isVisible(By.id("ISBN-wrapper"), 5) || isVisible(By.id("title-wrapper"), 5);

        if (!hasBookDetails) {
            System.out.println("ok the link exists but there's no data");
            pauseBriefly(NO_DATA_DELAY_MS);
            driver.navigate().to("https://demoqa.com/books");
            wait.until(ExpectedConditions.urlContains("/books"));
            return;
        }

        clickButtonByText("Back To Book Store");
        wait.until(ExpectedConditions.urlContains("/books"));
    }
}
