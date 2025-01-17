package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.disqualification.PermissionToAct;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequestMapper;
import uk.gov.companieshouse.disqualifiedofficersdataapi.converter.DisqualifiedCorporateOfficerReadConverter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.converter.DisqualifiedCorporateOfficerWriteConverter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.converter.DisqualifiedNaturalOfficerReadConverter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.converter.DisqualifiedNaturalOfficerWriteConverter;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.PermissionToActMixIn;
import uk.gov.companieshouse.disqualifiedofficersdataapi.serialization.LocalDateDeSerializer;
import uk.gov.companieshouse.disqualifiedofficersdataapi.serialization.LocalDateSerializer;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

@Configuration
public class ApplicationConfig {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    /**
     * mongoCustomConversions.
     *
     * @return MongoCustomConversions.
     */
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(List.of(new DisqualifiedNaturalOfficerWriteConverter(objectMapper),
                new DisqualifiedCorporateOfficerWriteConverter(objectMapper),
                new DisqualifiedNaturalOfficerReadConverter(objectMapper),
                new DisqualifiedCorporateOfficerReadConverter(objectMapper)));
    }

    @Bean
    public Supplier<String> timestampGenerator() {
        return () -> dateTimeFormatter.format(Instant.now());
    }

    @Bean
    public Function<ResourceChangedRequest, ChangedResource> mapper() {
        ResourceChangedRequestMapper mapper = new ResourceChangedRequestMapper(timestampGenerator(), objectMapper());
        return mapper::mapChangedResource;
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    /**
     * Mongo DB Object Mapper.
     *
     * @return ObjectMapper.
     */
    private ObjectMapper mongoDbObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.addMixIn(PermissionToAct.class, PermissionToActMixIn.class);
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
