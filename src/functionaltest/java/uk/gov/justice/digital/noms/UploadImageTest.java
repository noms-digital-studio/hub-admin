package uk.gov.justice.digital.noms;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class UploadImageTest {

    private String hostname;
    private String port;

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

    @Test
    public void uploadsImageAndTitleSuccessfully() throws Exception {
        HttpResponse<String> response = Unirest.post("http://" + hostname + ":" + port +"/content-items")
                .header("accept", "application/json")
                .field("title", "A one pixel image")
                .field("file", new File(this.getClass().getResource("/1-pixel.png").toURI()))
                .asString();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }

}