package uk.gov.justice.digital.noms.hub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UploadImageTest {

    @Test
    public void uploadsImageAndTitleSuccessfully() throws Exception {

        // when
        Client client = ClientBuilder.newClient();

        Response response = client.target("http://localhost:8080")
                .path("content-items/")
                .request()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .post(Entity.json(createJsonPayload()));

        // then
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getLocation().toString()).contains("http://localhost:8080/content-items/");
    }

    private static String createJsonPayload() throws JsonProcessingException {
        Map<String, Object> values = ImmutableMap.of("title", "foo");
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(values);
    }

}