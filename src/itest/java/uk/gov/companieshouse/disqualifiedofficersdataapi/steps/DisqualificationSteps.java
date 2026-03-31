package uk.gov.companieshouse.disqualifiedofficersdataapi.steps;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractIntegrationTest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.CucumberContext;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class DisqualificationSteps extends AbstractIntegrationTest {

    private static final String DELTA_AT = "20240925171003950844";
    private static final String STALE_DELTA_AT = "20220925171003950844";
    private static final String X_DELTA_AT = "x-delta-at";
    private static final String DELETE_NATURAL_URI = "/disqualified-officers/natural/{officer_id}/internal";

    @Autowired
    private NaturalDisqualifiedOfficerRepository naturalRepository;

    @Autowired
    private CorporateDisqualifiedOfficerRepository corporateRepository;

    @Autowired
    public DisqualifiedOfficerApiService disqualifiedApiService;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DisqualifiedOfficerRepository repository;

    @Before
    public void dbCleanUp() {
        startMongo();
        repository.deleteAll();
    }

    @Given("disqualified officers data api service is running")
    public void theApplicationRunning() {
        assertThat(webTestClient).isNotNull();
    }

    @Given("the disqualification database is down")
    public void the_disqualification_db_is_down() {
        stopMongo();
    }

    @When("CHS kafka API service is unavailable")
    public void chs_kafka_service_unavailable() {
        doThrow(ServiceUnavailableException.class)
                .when(disqualifiedApiService).invokeChsKafkaApi(any(ResourceChangedRequest.class));
    }

    @When("I send DELETE request with officer id {string}")
    public void send_delete_request_for_officer(String officerId) {
        CucumberContext.CONTEXT.set("contextId", "5234234234");
        CucumberContext.CONTEXT.set("officerType", DisqualificationResourceType.NATURAL);

        int statusCode = webTestClient.delete()
                .uri(DELETE_NATURAL_URI, officerId)
                .header("x-request-id", (String) CucumberContext.CONTEXT.get("contextId"))
                .header("ERIC-Identity", "TEST-IDENTITY")
                .header("ERIC-Identity-Type", "KEY")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header(X_DELTA_AT, DELTA_AT)
                .exchange()
                .returnResult(Void.class)
                .getStatus()
                .value();

        CucumberContext.CONTEXT.set("statusCode", statusCode);
    }

    @When("I send DELETE request with officer id {string} with a stale delta at")
    public void send_delete_request_for_officer_stale_delta_at(String officerId) {
        CucumberContext.CONTEXT.set("contextId", "5234234234");
        CucumberContext.CONTEXT.set("officerType", DisqualificationResourceType.NATURAL);

        int statusCode = webTestClient.delete()
                .uri(DELETE_NATURAL_URI, officerId)
                .header("x-request-id", (String) CucumberContext.CONTEXT.get("contextId"))
                .header("ERIC-Identity", "TEST-IDENTITY")
                .header("ERIC-Identity-Type", "KEY")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header(X_DELTA_AT, STALE_DELTA_AT)
                .exchange()
                .returnResult(Void.class)
                .getStatus()
                .value();

        CucumberContext.CONTEXT.set("statusCode", statusCode);
    }

    @When("I send DELETE request with an invalid officer_type and officer id {string}")
    public void send_delete_request_for_officer_with_invalid_officer_type(String officerId) {
        String uri = "/disqualified-officers/invalid/{officer_id}/internal";

        CucumberContext.CONTEXT.set("contextId", "5234234234");
        CucumberContext.CONTEXT.set("officerType", DisqualificationResourceType.NATURAL);

        int statusCode = webTestClient.delete()
                .uri(uri, officerId)
                .header("x-request-id", (String) CucumberContext.CONTEXT.get("contextId"))
                .header("ERIC-Identity", "TEST-IDENTITY")
                .header("ERIC-Identity-Type", "KEY")
                .header("ERIC-Authorised-Key-Privileges", "internal-app")
                .header(X_DELTA_AT, DELTA_AT)
                .exchange()
                .returnResult(Void.class)
                .getStatus()
                .value();

        CucumberContext.CONTEXT.set("statusCode", statusCode);
    }

    @When("officer id does not exists for {string}")
    public void officer_does_not_exists(String officerId) {
        Assertions.assertThat(repository.existsById(officerId)).isFalse();
    }

    @Then("the CHS Kafka API is not invoked")
    public void chs_kafka_api_not_invoked() {
        verify(disqualifiedApiService, never()).invokeChsKafkaApi(any(ResourceChangedRequest.class));
    }

    @Then("the CHS Kafka API is invoked with {string}")
    public void chs_kafka_api_invoked(String officerId) {
        boolean isDelete = officerId.equals("id_to_delete");

        verify(disqualifiedApiService).invokeChsKafkaApi(new ResourceChangedRequest(
                CucumberContext.CONTEXT.get("contextId"),
                officerId,
                CucumberContext.CONTEXT.get("officerType"),
                isDelete ? CucumberContext.CONTEXT.get("disqualificationData") : null,
                isDelete
        ));
    }

    @Then("the CHS Kafka API is invoked with {string} with null data")
    public void chs_kafka_api_invoked_with_null_type_and_null_data(String officerId) {
        verify(disqualifiedApiService).invokeChsKafkaApi(new ResourceChangedRequest(
                CucumberContext.CONTEXT.get("contextId"),
                officerId,
                CucumberContext.CONTEXT.get("officerType"),
                null,
                true
        ));
    }

    @Then("nothing is persisted in the database")
    public void nothing_persisted_to_database() {
        List<DisqualificationDocument> disqDoc = repository.findAll();
        Assertions.assertThat(disqDoc).isEmpty();
    }

    @Then("a document is persisted to the database")
    public void document_is_persisted_to_database() {
        List<DisqualificationDocument> disqDoc = repository.findAll();
        Assertions.assertThat(disqDoc).isNotEmpty();
    }

    @Then("I should receive {int} status code")
    public void i_should_receive_status_code(Integer statusCode) {
        int expectedStatusCode = CucumberContext.CONTEXT.get("statusCode");
        Assertions.assertThat(expectedStatusCode).isEqualTo(statusCode);
    }

    @Then("the disqualified officer with officer id {string} does not exist in the database")
    public void disqualified_officer_exists(String officerId) {
        Assertions.assertThat(repository.existsById(officerId)).isFalse();
    }
}