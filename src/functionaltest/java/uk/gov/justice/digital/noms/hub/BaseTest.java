package uk.gov.justice.digital.noms.hub;

import org.junit.Before;

public class BaseTest {
    protected String applicationUrl;

    @Before
    public void readHostnameAndPort() {
        applicationUrl = System.getenv("APPLICATION_URL");
        if (applicationUrl == null || applicationUrl.isEmpty()) {
            applicationUrl = "http://localhost:8080/hub-admin";
        }
    }

}
