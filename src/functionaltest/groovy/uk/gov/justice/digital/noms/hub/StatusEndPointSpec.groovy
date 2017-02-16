package uk.gov.justice.digital.noms.hub

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification

class StatusEndPointSpec extends Specification {

    @Shared
    private Hub theHub = new Hub()

    def 'health resource returns success when app is running'() {

        when:
        HttpResponse<JsonNode> response = Unirest.get(theHub.adminUri + "/health").asJson();

        then:
        response.getStatus() == (HttpStatus.OK.value());
        response.getBody().getObject().get("status") == ("UP");
    }

}
