package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
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

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        ObjectMapper objectMapper = mongoDbObjectMapper();
        return new MongoCustomConversions(List.of(
                new DisqualifiedNaturalOfficerWriteConverter(objectMapper),
                new DisqualifiedCorporateOfficerWriteConverter(objectMapper),
                new DisqualifiedNaturalOfficerReadConverter(objectMapper),
                new DisqualifiedCorporateOfficerReadConverter(objectMapper)));
    }

    @Bean
    public Supplier<String> timestampGenerator() {
        return () -> dateTimeFormatter.format(Instant.now());
    }

    @Bean
    public Function<ResourceChangedRequest, ChangedResource> mapper(
            @Qualifier("objectMapper") ObjectMapper objectMapper) {
        ResourceChangedRequestMapper mapper = new ResourceChangedRequestMapper(timestampGenerator(), objectMapper);
        return mapper::mapChangedResource;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd"))
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    private ObjectMapper mongoDbObjectMapper() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeSerializer());

        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_EMPTY))
                .addMixIn(PermissionToAct.class, PermissionToActMixIn.class)
                .addModule(module)
                .build();
    }
}