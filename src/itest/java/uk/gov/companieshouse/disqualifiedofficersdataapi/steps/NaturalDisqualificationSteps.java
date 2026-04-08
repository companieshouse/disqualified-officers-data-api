package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi.KindEnum;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractIntegrationTest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.util.FileReaderUtil;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class NaturalDisqualificationSteps extends AbstractIntegrationTest {

    private String contextId;

    private final ObjectMapper objectMapper;
    private final WebTestClient webTestClient;
    private final NaturalDisqualifiedOfficerRepository naturalRepository;
    private final CorporateDisqualifiedOfficerRepository corporateRepository;
    private final DisqualifiedOfficerRepository repository;
    private final MongoTemplate mongoTemplate;
    public final DisqualifiedOfficerApiService disqualifiedApiService;

    @Autowired
    public NaturalDisqualificationSteps(ObjectMapper objectMapper,
                                        WebTestClient webTestClient,
                                        NaturalDisqualifiedOfficerRepository naturalRepository,
                                        CorporateDisqualifiedOfficerRepository corporateRepository,
                                        DisqualifiedOfficerRepository repository,
                                        MongoTemplate mongoTemplate,
                                        DisqualifiedOfficerApiService disqualifiedApiService) {
        this.objectMapper = objectMapper;
        this.webTestClient = webTestClient;
        this.naturalRepository = naturalRepository;
        this.corporateRepository = corporateRepository;
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        this.disqualifiedApiService = disqualifiedApiService;
    }

    @Before
    public void dbCleanUp() {
        startMongo();
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
        naturalDisqualification.setDeltaAt("20230925171003950844");

        mongoTemplate.save(naturalDisqualification);
        natData.setKind(KindEnum.NATURAL_DISQUALIFICATION);
        CucumberContext.CONTEXT.set("disqualificationData", natData);
    }

    @And("the natural disqualified officer information exists for {string} with delta_at {string}")
    public void the_natural_disqualification_information_exists_for_with_delta_at(String officerId, String deltaAt) throws IOException {
        File natFile = new ClassPathResource("/json/output/retrieve_natural_disqualified_officer.json").getFile();
        NaturalDisqualificationApi natData = objectMapper.readValue(natFile, NaturalDisqualificationApi.class);
        NaturalDisqualificationDocument naturalDisqualification = new NaturalDisqualificationDocument();
        naturalDisqualification.setData(natData);
        naturalDisqualification.setId(officerId);
        naturalDisqualification.setDeltaAt(deltaAt);

        mongoTemplate.save(naturalDisqualification);
        CucumberContext.CONTEXT.set("disqualificationData", naturalDisqualification);
    }

    @When("I send natural GET request with officer Id {string}")
    public void i_send_natural_get_request_with_officer_id(String officerId) {
        NaturalDisqualificationApi responseBody = webTestClient.get()
                .uri("/disqualified-officers/natural/{officerId}", officerId)
                .header("ERIC-Identity", "TEST-IDENTITY")
                .header("ERIC-Identity-Type", "KEY")
                .exchange()
                .expectStatus().isOk()
                .returnResult(NaturalDisqualificationApi.class)
                .getResponseBody()
                .blockFirst();

        CucumberContext.CONTEXT.set("statusCode", 200);
        CucumberContext.CONTEXT.set("getResponseBody", responseBody);
    }

    @When("I send natural PUT request with payload {string} file")
    public void i_send_natural_put_request_with_payload(String dataFile) {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/" + dataFile + ".json");

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        CucumberContext.CONTEXT.set("officerType", DisqualificationResourceType.NATURAL);

        String officerId = "1234567890";

        int statusCode = webTestClient.put()
                .uri("/disqualified-officers/natural/{officerId}/internal", officerId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("x-request-id", this.contextId)
                .header("ERIC-Identity", "TEST-IDENTITY")
                .header("ERIC-Identity-Type", "KEY")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .bodyValue(data)
                .exchange()
                .returnResult(Void.class)
                .getStatus()
                .value();

        CucumberContext.CONTEXT.set("statusCode", statusCode);
    }

    @When("I send natural PUT request without ERIC headers")
    public void i_send_natural_put_request_without_ERIC_headers() {
        String data = FileReaderUtil.readFile("src/itest/resources/json/input/natural_disqualified_officer.json");

        this.contextId = "5234234234";
        CucumberContext.CONTEXT.set("contextId", this.contextId);
        CucumberContext.CONTEXT.set("officerType", DisqualificationResourceType.NATURAL);

        String officerId = "1234567890";

        int statusCode = webTestClient.put()
                .uri("/disqualified-officers/natural/{officerId}/internal", officerId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("x-request-id", this.contextId)
                .bodyValue(data)
                .exchange()
                .returnResult(Void.class)
                .getStatus()
                .value();

        CucumberContext.CONTEXT.set("statusCode", statusCode);
    }

    @Then("the natural Get call response body should match {string} file")
    public void the_natural_get_call_response_body_should_match(String dataFile) throws IOException {
        File file = new ClassPathResource("/json/output/" + dataFile + ".json").getFile();
        NaturalDisqualificationApi expected = objectMapper.readValue(file, NaturalDisqualificationApi.class);
        expected.setKind(KindEnum.NATURAL_DISQUALIFICATION);

        NaturalDisqualificationApi actual = CucumberContext.CONTEXT.get("getResponseBody");

        assertThat(expected.getSurname()).isEqualTo(actual.getSurname());
        assertThat(expected.getDisqualifications()).isEqualTo(actual.getDisqualifications());
        assertThat(expected.getDateOfBirth()).isEqualTo(actual.getDateOfBirth());
        assertThat(expected.getKind()).isEqualTo(actual.getKind());
    }

    @And("the natural record with id {string} is unchanged")
    public void the_natural_record_with_id_is_unchanged(String officerId) {
        NaturalDisqualificationDocument actual = naturalRepository.findById(officerId).get();
        NaturalDisqualificationDocument expected = CucumberContext.CONTEXT.get("disqualificationData");

        Assertions.assertEquals(expected.getData(), actual.getData());
        Assertions.assertEquals(expected.getDeltaAt(), actual.getDeltaAt());
        Assertions.assertEquals(expected.getId(), actual.getId());
    }
}