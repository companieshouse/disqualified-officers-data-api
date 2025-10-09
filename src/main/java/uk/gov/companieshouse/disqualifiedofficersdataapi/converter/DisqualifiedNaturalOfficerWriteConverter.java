package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.InternalServerErrorException;

@WritingConverter
public class DisqualifiedNaturalOfficerWriteConverter implements Converter<NaturalDisqualificationApi, BasicDBObject> {

    private final ObjectMapper objectMapper;

    public DisqualifiedNaturalOfficerWriteConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public BasicDBObject convert(NaturalDisqualificationApi source) {
        try {
            return BasicDBObject.parse(objectMapper.writeValueAsString(source));
        } catch (Exception ex) {
            throw new InternalServerErrorException("failed to convert JSON to BasicDBObject object", ex);
        }
    }
}
