package com.selenium.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApiBooksListTest {
    @Test
    public void shouldReturnBooksListFromApi() throws Exception {
        ApiTestSupport.HttpResponse response = ApiTestSupport.httpGet(ApiTestSupport.API_BASE + "/Books");
        assertEquals(200, response.statusCode);
        assertTrue("Expected books array in response", response.body.contains("\"books\""));
        assertTrue("Expected at least one ISBN", response.body.contains("\"isbn\""));
    }
}
