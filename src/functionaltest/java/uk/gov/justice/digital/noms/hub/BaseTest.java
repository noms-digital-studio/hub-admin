package uk.gov.justice.digital.noms.hub;

import org.junit.Before;

public class BaseTest {
    protected String applicationUrl;
    protected String userName;
    protected String password;

    @Before
    public void readHostnameAndPort() {
        applicationUrl = System.getenv("APPLICATION_URL");
        if (applicationUrl == null || applicationUrl.isEmpty()) {
            applicationUrl = "http://localhost:8080/hub-admin";
        }
    }

    @Before
    public void setupBasicAuth() {
        String basicAuth = System.getenv("BASIC_AUTH");
        if(basicAuth == null || basicAuth.isEmpty()) {
            basicAuth = "user:password";
        }
        String[] stringArr =  basicAuth.split(":");
        userName = stringArr[0];
        password = stringArr[1];
    }

}
