package uk.gov.companieshouse.disqualifiedofficersdataapi.serialization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalDateDeSerializerTest {
    private LocalDateDeSerializer deserializer;

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    @BeforeEach
    void setUp() {
        deserializer = new LocalDateDeSerializer();
    }

    @Test
    void whenDeserializtionInvokedAndException_thenBadRequestExceptionThrown() {
        when(jsonParser.readValueAsTree()).thenThrow(new NullPointerException("Test Message"));

        assertThatThrownBy(
                () -> deserializer.deserialize(jsonParser, deserializationContext)
        ).isInstanceOf(BadRequestException.class)
                .hasMessage("Test Message");
    }

    @Test
    void whenDeserializationInvokedWithNonNullDateString_thenBadRequestExceptionThrown() {
        final JsonNode jsonNode = mock(JsonNode.class);

        when(jsonParser.readValueAsTree()).thenReturn(jsonNode);

        final JsonNode dateNode = mock(JsonNode.class);

        when(jsonNode.get("$date")).thenReturn(dateNode);
        when(dateNode.stringValue()).thenReturn("2026-11-10T01:45:56Z");

        final LocalDate deserializedLocalDate = deserializer.deserialize(jsonParser, deserializationContext);

        assertThat(deserializedLocalDate)
                .isNotNull()
                .isEqualTo(LocalDate.of(2026, 11, 10));
    }

    @Test
    void whenDeserializationInvokedWithNullNullDateString_thenBadRequestExceptionThrown() {
        final JsonNode jsonNode = mock(JsonNode.class);

        when(jsonParser.readValueAsTree()).thenReturn(jsonNode);

        final JsonNode dateNode = mock(JsonNode.class);

        when(jsonNode.get("$date")).thenReturn(dateNode);
        when(dateNode.stringValue()).thenReturn(null);

        final JsonNode numberLongNode = mock(JsonNode.class);
        when(dateNode.get("$numberLong")).thenReturn(numberLongNode);
        when(numberLongNode.asLong()).thenReturn(1462515182933L);

        final LocalDate deserializedLocalDate = deserializer.deserialize(jsonParser, deserializationContext);

        assertThat(deserializedLocalDate)
                .isNotNull()
                .isEqualTo(LocalDate.of(2016, 5, 6));
    }
}
