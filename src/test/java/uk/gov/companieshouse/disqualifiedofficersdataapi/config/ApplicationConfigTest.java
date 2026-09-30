package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    @Test
    void whenJacksonCustomizerInstantiated_thenSettingsCorrect() {
        final JsonMapper.Builder builder = JsonMapper.builder();
        config.jacksonCustomizer().customize(builder);
        final JsonMapper mapper = builder.build();

        final DateFormat dateFormat = mapper.serializationConfig().getDateFormat();
        assertThat(dateFormat).isInstanceOf(SimpleDateFormat.class);
        assertThat(((SimpleDateFormat) dateFormat).toPattern()).isEqualTo("yyyy-MM-dd");

        final JsonInclude.Value inclusions = mapper.serializationConfig().getDefaultPropertyInclusion();
        assertThat(inclusions).isNotNull();
        assertThat(inclusions.getContentInclusion()).isEqualTo(JsonInclude.Include.NON_NULL);
        assertThat(inclusions.getValueInclusion()).isEqualTo(JsonInclude.Include.NON_NULL);
    }
}
