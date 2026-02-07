package com.selenium.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.junit.Test;

public class SecuritySslCertificateTest {
    @Test
    public void shouldHaveValidSslCertificate() throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL("https://demoqa.com/books").openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);
        connection.connect();

        int status = connection.getResponseCode();
        assertEquals(200, status);
        assertTrue("Expected server to present SSL certificates", connection.getServerCertificates().length > 0);
        connection.disconnect();
    }
}
