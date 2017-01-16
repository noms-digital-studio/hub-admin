package uk.gov.justice.digital.noms;

import org.junit.Before;

public class BaseTest {
    protected String hostname;
    protected String port;

    @Before
    public void readHostnameAndPort() {
        hostname = System.getenv("APPLICATION_HOSTNAME");
        if (hostname == null || hostname.isEmpty()) {
            hostname = "localhost";
        }

        port = System.getenv("APPLICATION_PORT");
        if (port == null || port.isEmpty()) {
            port = "8080";
        }
    }

}
