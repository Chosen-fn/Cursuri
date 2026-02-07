package com.selenium.tests;

import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class UiSideMenuNavigationTest extends UiDriverTestBase {
    @Test
    public void shouldUseSideMenuToNavigateSections() {
        UiTestSupport.openHomePage(driver, wait);

        clickSideMenuItem("Profile");
        wait.until(ExpectedConditions.urlContains("/profile"));

        clickSideMenuItem("Book Store");
        wait.until(ExpectedConditions.urlContains("/books"));
    }
}
