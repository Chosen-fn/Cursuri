package com.selenium.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ApiTestSupport {
    static final String API_BASE = "https://demoqa.com/BookStore/v1";
    private static final int TIMEOUT_MS = 10_000;

    private ApiTestSupport() {
    }

    static HttpResponse httpGet(String url) throws IOException {
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

    static String extractFirstIsbn(String json) {
        Matcher matcher = Pattern.compile("\\\"isbn\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new AssertionError("No ISBN found in book list response");
    }

    private static String readAll(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    static final class HttpResponse {
        final int statusCode;
        final String body;

        HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}
