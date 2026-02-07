package com.selenium.tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UiLoginNavigationTest extends UiDriverTestBase {
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
}
