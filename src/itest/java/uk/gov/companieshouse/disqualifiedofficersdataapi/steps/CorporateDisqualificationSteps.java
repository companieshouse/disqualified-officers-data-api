package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi.KindEnum;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.util.FileReaderUtil;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractMongoConfig.mongoDBContainer;

public class CorporateDisqualificationSteps {

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

    @Given("the corporate disqualified officer information exists for {string}")
    public void the_disqualification_information_exists_for(String officerId) throws IOException {
        File corpFile = new ClassPathResource("/json/output/retrieve_corporate_disqualified_officer.json").getFile();
        CorporateDisqualificationApi corpData = objectMapper.readValue(corpFile, CorporateDisqualificationApi.class);
        CorporateDisqualificationDocument corporateDisqualification = new CorporateDisqualificationDocument();
        corporateDisqualification.setData(corpData);
        corporateDisqualification.setId(officerId);
        corporateDisqualification.setCorporateOfficer(true);

        mongoTemplate.save(corporateDisqualification);
    }

    @When("I send corporate GET request with officer Id {string}")
    public void i_send_corporate_get_request_with_officer_id(String officerId) throws IOException {
        String uri = "/disqualified-officers/corporate/{officerId}";

        HttpHeaders headers = new HttpHeaders();
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "KEY");
        HttpEntity<String> request = new HttpEntity<String>(null, headers);

        ResponseEntity<CorporateDisqualificationApi> response = restTemplate.exchange(uri, HttpMethod.GET, request,
                CorporateDisqualificationApi.class, officerId);

        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
        CucumberContext.CONTEXT.set("getResponseBody", response.getBody());
    }


    @When("I send corporate PUT request with payload {string} file")
    public void i_send_corporate_put_request_with_payload(String dataFile) throws IOException {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        headers.set("x-request-id", this.contextId);
        headers.set("ERIC-Identity", "TEST-IDENTITY");
        headers.set("ERIC-Identity-Type", "KEY");

        HttpEntity request = new HttpEntity(data, headers);
        String uri = "/disqualified-officers/corporate/{officerId}/internal";
        CucumberContext.CONTEXT.set("officerType", DisqualificationResourceType.CORPORATE);
        String officerId = "1234567891";
        ResponseEntity<Void> response = restTemplate.exchange(uri, HttpMethod.PUT, request, Void.class, officerId);

        this.officerId = officerId;
        CucumberContext.CONTEXT.set("statusCode", response.getStatusCodeValue());
    }

    @Then("the corporate Get call response body should match {string} file")
    public void the_corporate_get_call_response_body_should_match(String dataFile) throws IOException {
       File file = new ClassPathResource("/json/output/" + dataFile + ".json").getFile();
       CorporateDisqualificationApi expected = objectMapper.readValue(file, CorporateDisqualificationApi.class);
       expected.setKind(KindEnum.CORPORATE_DISQUALIFICATION);

        CorporateDisqualificationApi actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getName()).isEqualTo(actual.getName());
        assertThat(expected.getDisqualifications()).isEqualTo(actual.getDisqualifications());
        assertThat(expected.getKind()).isEqualTo(actual.getKind());
    }

    @After
    public void dbStop(){
        mongoDBContainer.stop();
    }

}
