package uk.gov.companieshouse.disqualifiedofficersdataapi.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.config.AbstractMongoConfig;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;

import java.time.OffsetDateTime;

@Testcontainers
@DataMongoTest
class RepositoryITest extends AbstractMongoConfig {

  @Autowired
  private NaturalDisqualifiedOfficerRepository naturalRepository;

  @BeforeAll
  static void setup() {
    mongoDBContainer.start();
  }

  @Test
  void should_save_and_retrieve_disqualified_officer_data() {
    NaturalDisqualificationDocument disqualificationDocument = createDisqualificationDocument("1234567890");
    naturalRepository.save(disqualificationDocument);

    Assertions.assertThat(naturalRepository.findById("1234567890")).isNotEmpty();
  }

  private NaturalDisqualificationDocument createDisqualificationDocument(String officerId) {
    NaturalDisqualificationDocument disqualificationDocument = new NaturalDisqualificationDocument();

    NaturalDisqualificationApi data = new NaturalDisqualificationApi();
    disqualificationDocument.setDeltaAt(OffsetDateTime.now().toString());
    disqualificationDocument.setId(officerId);
    disqualificationDocument.setData(data);

    return disqualificationDocument;
  }

  @AfterAll
  static void tear() {
    mongoDBContainer.stop();
  }
}