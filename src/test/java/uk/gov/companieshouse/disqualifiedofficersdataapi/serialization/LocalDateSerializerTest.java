package uk.gov.companieshouse.disqualifiedofficersdataapi.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalDateSerializerTest {

    private LocalDateSerializer serializer;

    @Mock
    private JsonGenerator generator;

    @Captor
    private ArgumentCaptor<String> dateString;

    @BeforeEach
    void setUp() {
        serializer = new LocalDateSerializer();
    }

    @Test
    void dateShouldSerialize() throws Exception {
        LocalDate date = LocalDate.of(2020, 1, 1);

        serializer.serialize(date, generator, null);

        verify(generator).writeRawValue(dateString.capture());
        assertEquals("ISODate(\"2020-01-01T00:00:00.000Z\")", dateString.getValue());
    }
}
