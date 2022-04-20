package uk.gov.companieshouse.disqualifiedofficersdataapi.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateDeSerializer extends JsonDeserializer<LocalDate> {
    public static final String APPLICATION_NAME_SPACE = "disqualified-officers-data-api";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            JsonNode jsonNode = jsonParser.readValueAsTree();
            return LocalDate.parse(jsonNode.get("$date").textValue(), dateTimeFormatter);
        } catch (Exception exception) {
            LOGGER.error("Deserialization failed.", exception);
            throw new BadRequestException(exception.getMessage());
        }
    }
}