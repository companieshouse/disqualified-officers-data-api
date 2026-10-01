package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import tools.jackson.databind.json.JsonMapper;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DisqualifiedCorporateOfficerWriteConverterTest {

    private static final String OFFICER_ID = "officerId";

    private DisqualifiedCorporateOfficerWriteConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DisqualifiedCorporateOfficerWriteConverter(new JsonMapper());
    }

    @Test
    void canConvertDocument() {
        CorporateDisqualificationApi api = new CorporateDisqualificationApi();
        api.setCompanyNumber(OFFICER_ID);

        BasicDBObject object = converter.convert(api);

        String json = object.toJson();
        assertTrue(json.contains(OFFICER_ID));
    }
}
