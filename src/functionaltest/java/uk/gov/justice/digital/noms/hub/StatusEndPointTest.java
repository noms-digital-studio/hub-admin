package uk.gov.justice.digital.noms.hub;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.springframework.http.HttpStatus;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusEndPointTest extends BaseTest {
    @Test
    public void statusEndpointReturnsSuccess() throws Exception {
        // when
        HttpResponse<JsonNode> response = Unirest.get(applicationUrl + "/health").asJson();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getBody().getObject().get("status")).isEqualTo("UP");
    }
}
