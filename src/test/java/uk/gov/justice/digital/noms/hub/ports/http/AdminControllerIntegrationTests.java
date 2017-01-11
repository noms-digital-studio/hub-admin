package uk.gov.justice.digital.noms.hub.ports.http;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminControllerIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void uploadsImageAndTitleSuccessfully() throws Exception {
        // given
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("file", new ClassPathResource("AdminController.class", getClass()));
        map.add("title", "aTitle");

        // when
        ResponseEntity<String> response = this.restTemplate.postForEntity("/content-items", map, String.class);

        // then
        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().get("Location").get(0)).contains("/content-items/");
    }

}
