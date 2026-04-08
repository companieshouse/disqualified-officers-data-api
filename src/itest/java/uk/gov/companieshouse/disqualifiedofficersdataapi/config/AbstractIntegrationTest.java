package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@ActiveProfiles({"test"})
public abstract class AbstractIntegrationTest extends AbstractMongoConfig {

    @MockitoBean
    public DisqualifiedOfficerApiService disqualifiedOfficerApiService;
}