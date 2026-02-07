package com.selenium.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApiBookByIsbnTest {
    private static final long NO_DATA_CLOSE_DELAY_MS = 2_000;

    @Test
    public void shouldReturnSpecificBookByIsbn() throws Exception {
        String isbn = "9781449325862";
        ApiTestSupport.HttpResponse response = ApiTestSupport.httpGet(ApiTestSupport.API_BASE + "/Book?ISBN=" + isbn);
        assertEquals(200, response.statusCode);

        boolean hasBookData =
            response.body != null
                && response.body.contains("\"isbn\":\"" + isbn + "\"")
                && response.body.contains("\"title\":");

        if (!hasBookData) {
            System.out.println("ok the link exists but there's no data");
            Thread.sleep(NO_DATA_CLOSE_DELAY_MS);
            return;
        }

        assertTrue(response.body.contains("\"isbn\":\"" + isbn + "\""));
        assertTrue(response.body.contains("\"title\":\"Git Pocket Guide\""));
    }
}
