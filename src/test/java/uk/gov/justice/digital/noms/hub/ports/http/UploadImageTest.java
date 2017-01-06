package uk.gov.justice.digital.noms.hub.ports.http;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class UploadImageTest {

    @Test
    public void uploadsImageAndTitleSuccessfully() throws Exception {

        HttpResponse<String> response = Unirest.post("http://localhost:8080/content-items")
                .header("accept", "application/json")
                .field("title", "A one pixel image")
                .field("file", new File("/Users/nick/Downloads/1-pixel.png"))
                .asString();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    }

}