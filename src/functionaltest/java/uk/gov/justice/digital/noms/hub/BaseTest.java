package uk.gov.justice.digital.noms.hub;

import org.junit.Before;

public class BaseTest {
    String applicationUrl;
    String username;
    String password;

    @Before
    public void readHostnameAndPort() {
        applicationUrl = System.getenv("APPLICATION_URL");
        if (applicationUrl == null || applicationUrl.isEmpty()) {
            applicationUrl = "http://localhost:8080/hub-admin";
        }
    }

    @Before
    public void setupBasicAuth() {
        username = System.getenv("BASIC_AUTH_USERNAME");
        if (username == null || username.isEmpty()) {
            username = "user";
        }

        password = System.getenv("BASIC_AUTH_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = "password";
        }
    }

}
