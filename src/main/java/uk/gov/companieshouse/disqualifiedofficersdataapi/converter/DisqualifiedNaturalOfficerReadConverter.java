package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.InternalServerErrorException;

@ReadingConverter
public class DisqualifiedNaturalOfficerReadConverter implements Converter<Document, NaturalDisqualificationApi> {

    private final ObjectMapper objectMapper;

    public DisqualifiedNaturalOfficerReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public NaturalDisqualificationApi convert(Document source) {
        try {
            return objectMapper.readValue(source.toJson(), NaturalDisqualificationApi.class);
        } catch (Exception ex) {
            throw new InternalServerErrorException("failed to convert JSON to NaturalDisqualificationApi object", ex);
        }
    }
}
