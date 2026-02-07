package com.selenium.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class BookStoreDatabaseTests {
    private static final String API_BASE = "https://demoqa.com/BookStore/v1";
    private static final int TIMEOUT_MS = 10_000;

    @Test
    public void shouldReturnBooksListFromApi() throws Exception {
        HttpResponse response = httpGet(API_BASE + "/Books");
        assertEquals(200, response.statusCode);
        assertTrue("Expected books array in response", response.body.contains("\"books\""));
        assertTrue("Expected at least one ISBN", response.body.contains("\"isbn\""));
    }

    @Test
    public void shouldReturnSpecificBookByIsbn() throws Exception {
        String isbn = "9781449325862"; // Git Pocket Guide
        HttpResponse response = httpGet(API_BASE + "/Book?ISBN=" + isbn);
        assertEquals(200, response.statusCode);
        assertTrue(response.body.contains("\"isbn\":\"" + isbn + "\""));
        assertTrue(response.body.contains("\"title\":\"Git Pocket Guide\""));
    }

    @Test
    public void shouldResolveIsbnFromListAndFetchBook() throws Exception {
        HttpResponse listResponse = httpGet(API_BASE + "/Books");
        assertEquals(200, listResponse.statusCode);

        String isbn = extractFirstIsbn(listResponse.body);
        HttpResponse bookResponse = httpGet(API_BASE + "/Book?ISBN=" + isbn);
        assertEquals(200, bookResponse.statusCode);
        assertTrue(bookResponse.body.contains("\"isbn\":\"" + isbn + "\""));
    }

    private HttpResponse httpGet(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(TIMEOUT_MS);
        connection.setReadTimeout(TIMEOUT_MS);
        connection.connect();

        int status = connection.getResponseCode();
        InputStream stream = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
        String body = stream == null ? "" : readAll(stream);
        connection.disconnect();
        return new HttpResponse(status, body);
    }

    private String readAll(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private String extractFirstIsbn(String json) {
        Matcher matcher = Pattern.compile("\\\"isbn\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new AssertionError("No ISBN found in book list response");
    }

    private static class HttpResponse {
        private final int statusCode;
        private final String body;

        private HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
