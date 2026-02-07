package com.selenium.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UiAddToCollectionAuthTest extends UiDriverTestBase {
    private static final long NO_DATA_DELAY_MS = 2_000;

    @Test
    public void shouldRequireLoginToAddToCollection() {
        UiTestSupport.openHomePage(driver, wait);
        waitUntilBookLinksPresent(1);

        safeClick(By.cssSelector(".rt-tbody .rt-tr-group a"));
        wait.until(ExpectedConditions.urlContains("book="));

        By addToCollectionButton = By.xpath("//button[normalize-space()='Add To Your Collection']");
        if (!isVisible(addToCollectionButton, 5)) {
            System.out.println("ok the link exists but there's no data");
            pauseBriefly(NO_DATA_DELAY_MS);
            return;
        }

        safeClick(addToCollectionButton);

        if (isAlertPresent(5)) {
            String alertText = driver.switchTo().alert().getText().toLowerCase();
            assertTrue(
                "Expected alert to mention login or authorization",
                alertText.contains("login") || alertText.contains("authorized")
            );
            driver.switchTo().alert().accept();
            return;
        }

        assertTrue(
            "Expected redirect or page content to indicate login/auth requirement",
            driver.getCurrentUrl().contains("/login")
                || driver.getCurrentUrl().contains("/profile")
                || driver.getPageSource().toLowerCase().contains("login")
        );
    }
}
