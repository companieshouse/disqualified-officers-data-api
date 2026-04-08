package uk.gov.companieshouse.disqualifiedofficersdataapi.serialization;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LocalDateDeSerializer extends StdDeserializer<LocalDate> {
    public static final String APPLICATION_NAME_SPACE = "disqualified-officers-data-api";

    private static final Logger LOGGER = LoggerFactory.getLogger(APPLICATION_NAME_SPACE);

    public LocalDateDeSerializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext
            deserializationContext) {
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            JsonNode jsonNode = jsonParser.readValueAsTree();
            JsonNode dateNode = jsonNode.get("$date");

            return dateNode.isString() ?
                    LocalDate.parse(dateNode.stringValue(), dateTimeFormatter) :
                    LocalDate.ofInstant(Instant.ofEpochMilli(dateNode.get("$numberLong").asLong()), ZoneId.systemDefault());
        } catch (Exception exception) {
            LOGGER.error("Deserialization failed.", exception);
            throw new BadRequestException(exception.getMessage());
        }
    }
}