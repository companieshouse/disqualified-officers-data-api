package uk.gov.companieshouse.disqualifiedofficersdataapi.serialization;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateSerializer extends StdSerializer<LocalDate> {

    public LocalDateSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate localDate, JsonGenerator jsonGenerator,
                          SerializationContext serializationContext) {
        if (localDate == null) {
            jsonGenerator.writeNull();
        } else {
            DateTimeFormatter dateTimeFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            String format = localDate.atStartOfDay().format(dateTimeFormatter);
            jsonGenerator.writeRawValue("ISODate(\"" + format + "\")");
        }
    }
}