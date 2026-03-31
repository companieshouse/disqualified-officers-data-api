package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
public class CucumberSpringConfiguration extends AbstractIntegrationTest {
    // Everything needed is inherited:
    // @SpringBootTest(MOCK), @AutoConfigureMockMvc, @AutoConfigureWebTestClient,
    // @ActiveProfiles("test"), @MockitoBean DisqualifiedOfficerApiService,
    // and mongoDBContainer started + MONGODB_URL system property set
}