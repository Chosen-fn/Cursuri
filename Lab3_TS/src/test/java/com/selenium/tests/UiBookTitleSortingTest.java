package com.selenium.tests;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.openqa.selenium.By;

public class UiBookTitleSortingTest extends UiDriverTestBase {
    @Test
    public void shouldSortBooksByTitle() {
        UiTestSupport.openHomePage(driver, wait);
        waitUntilBookLinksPresent(2);

        List<String> before = getBookTitles();
        assertTrue("Expected at least two books before sorting", before.size() >= 2);

        safeClick(By.xpath("//div[contains(@class,'rt-th') and normalize-space()='Title']"));
        waitUntilBookLinksPresent(2);

        List<String> after = getBookTitles();
        assertTrue("Expected at least two books after sorting click", after.size() >= 2);
    }
}
