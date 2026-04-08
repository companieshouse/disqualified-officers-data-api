package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import tools.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DisqualifiedNaturalOfficerWriteConverterTest {

    private static final String OFFICER_ID = "officerId";

    private DisqualifiedNaturalOfficerWriteConverter converter;

    @BeforeEach
    void setUp() {
        converter = new DisqualifiedNaturalOfficerWriteConverter(new ObjectMapper());
    }

    @Test
    void canConvertDocument() {
        NaturalDisqualificationApi api = new NaturalDisqualificationApi();
        api.setPersonNumber(OFFICER_ID);

        BasicDBObject object = converter.convert(api);

        String json = object.toJson();
        assertTrue(json.contains(OFFICER_ID));
    }
}
