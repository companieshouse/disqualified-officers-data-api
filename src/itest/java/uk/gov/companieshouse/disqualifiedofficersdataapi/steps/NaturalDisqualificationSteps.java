package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.util.FileReaderUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractMongoConfig.mongoDBContainer;

public class NaturalDisqualificationSteps {

    private String officerId;
    private String contextId;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private NaturalDisqualifiedOfficerRepository naturalRepository;

    @Autowired
    private CorporateDisqualifiedOfficerRepository corporateRepository;

    @Autowired
    private DisqualifiedOfficerRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    public DisqualifiedOfficerApiService disqualifiedApiService;

    @Before
    public void dbCleanUp(){
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
        repository.deleteAll();
        naturalRepository.deleteAll();
        corporateRepository.deleteAll();
    }

    @Given("the natural disqualified officer information exists for {string}")
    public void the_natural_disqualification_information_exists_for(String officerId) throws IOException {
        File natFile = new ClassPathResource("/json/output/retrieve_natural_disqualified_officer.json").getFile();
        NaturalDisqualificationApi natData = objectMapper.readValue(natFile, NaturalDisqualificationApi.class);
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        naturalDisqualification.setData(natData);
        naturalDisqualification.setId(officerId);

        mongoTemplate.save(naturalDisqualification);
    }

    @When("I send natural GET request with officer Id {string}")
    public void i_send_natural_get_request_with_officer_id(String officerId) throws IOException {
        String uri = "/disqualified-officers/natural/{officerId}";
        ResponseEntity<NaturalDisqualificationApi> response = restTemplate.exchange(uri, HttpMethod.GET, null,
                NaturalDisqualificationApi.class, officerId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }


    @When("I send natural PUT request with payload {string} file")
    public void i_send_natural_put_request_with_payload(String dataFile) throws IOException {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        headers.set("x-request-id", this.contextId);

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/disqualified-officers/natural/{officerId}/internal";
        String officerId = "1234567890";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, officerId);

        this.officerId = officerId;
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @Then("the natural Get call response body should match {string} file")
    public void the_natural_get_call_response_body_should_match(String dataFile) throws IOException {
        File file = new ClassPathResource("/json/output/" + dataFile + ".json").getFile();
        NaturalDisqualificationApi expected = objectMapper.readValue(file, NaturalDisqualificationApi.class);

        NaturalDisqualificationApi actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getSurname()).isEqualTo(actual.getSurname());
        assertThat(expected.getDisqualifications()).isEqualTo(actual.getDisqualifications());
    }

}