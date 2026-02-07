package com.selenium.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApiResolveIsbnAndFetchBookTest {
    @Test
    public void shouldResolveIsbnFromListAndFetchBook() throws Exception {
        ApiTestSupport.HttpResponse listResponse = ApiTestSupport.httpGet(ApiTestSupport.API_BASE + "/Books");
        assertEquals(200, listResponse.statusCode);

        String isbn = ApiTestSupport.extractFirstIsbn(listResponse.body);
        ApiTestSupport.HttpResponse bookResponse = ApiTestSupport.httpGet(ApiTestSupport.API_BASE + "/Book?ISBN=" + isbn);
        assertEquals(200, bookResponse.statusCode);
        assertTrue(bookResponse.body.contains("\"isbn\":\"" + isbn + "\""));
    }
}
