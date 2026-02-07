package com.selenium.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.Assume;
import org.junit.Test;

public class ApiBooksListTest {
    @Test
    public void shouldReturnBooksListFromApi() throws Exception {
        ApiTestSupport.HttpResponse response;
        try {
            response = ApiTestSupport.httpGet(ApiTestSupport.API_BASE + "/Books");
        } catch (SocketException | UnknownHostException e) {
            Assume.assumeTrue("Skipping test because API endpoint is not reachable: " + e.getMessage(), false);
            return;
        }

        assertEquals(200, response.statusCode);
        assertTrue("Expected non-empty API response body", response.body != null && !response.body.isEmpty());
        assertTrue("Expected books array in response", response.body.contains("\"books\""));
        assertTrue("Expected at least one ISBN", response.body.contains("\"isbn\""));
    }
}
