package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {
    private ApplicationConfig config;

    @BeforeEach
    void setUp() {
        config = new ApplicationConfig();
    }

    @Test
    void whenMongoCustomConversionsInvoked_thenBeanInstantiated() {
        final MongoCustomConversions customConversions = config.mongoCustomConversions();
        assertThat(customConversions).isNotNull();
    }

    @Test
    void whenTimeStampGeneratorInvoked_thenBeanInstantiated() {
        final Supplier<String> timestampGenerator = config.timestampGenerator();
        assertThat(timestampGenerator).isNotNull();
    }

    @Test
    void whenMapperInvoked_thenBeanInstantiated() {
        final Function<ResourceChangedRequest, ChangedResource> mapper = config.mapper(mock(ObjectMapper.class));
        assertThat(mapper).isNotNull();
    }

    @Test
    void whenJacksonCustomConversionsInvoked_thenBeanInstantiated() {
        final JsonMapperBuilderCustomizer customizer = config.jacksonCustomizer();
        assertThat(customizer).isNotNull();
    }

}
