package uk.gov.companieshouse.disqualifiedofficersdataapi.converter;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.InternalServerErrorException;

@ReadingConverter
public class DisqualifiedCorporateOfficerReadConverter implements Converter<Document, CorporateDisqualificationApi> {

    private final ObjectMapper objectMapper;

    public DisqualifiedCorporateOfficerReadConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Write convertor.
     * @param source source Document.
     * @return charge BSON object.
     */
    @Override
    public CorporateDisqualificationApi convert(Document source) {
        try {
            return objectMapper.readValue(source.toJson(), CorporateDisqualificationApi.class);
        } catch (Exception ex) {
            throw new InternalServerErrorException("failed to convert JSON to CorporateDisqualificationApi object", ex);
        }
    }
}
