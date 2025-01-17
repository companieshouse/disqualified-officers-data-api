package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.TestHelper;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;

@ExtendWith(MockitoExtension.class)
class ResourceChangedRequestMapperTest {

    private static final String EXPECTED_CONTEXT_ID = "35234234";
    private static final String DATE = "date";
    private static final String OFFICER_ID = "CH4000056";

    private static final CorporateDisqualificationApi CORPORATE_DISQ = TestHelper.createPopulatedCorporateDisqualification();
    private static final NaturalDisqualificationApi NATURAL_DISQ = TestHelper.createPopulatedNaturalDisqualification();

    @Mock
    private Supplier<String> timestampGenerator;

    @Mock
    private ResourceChangedRequest request;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ResourceChangedRequestMapper mapper;

    @ParameterizedTest
    @MethodSource("resourceChangedScenarios")
    void testMapper(ResourceChangedTestArgument argument) throws Exception {
        // given
        when(timestampGenerator.get()).thenReturn(DATE);
        Object expectedDeletedData = objectMapper.readValue(
                objectMapper.writeValueAsString(argument.getChangedResource().getDeletedData()), Object.class);

        // when
        ChangedResource actual = mapper.mapChangedResource(argument.getRequest());

        // then
        assertEquals(argument.getChangedResource().getResourceUri(), actual.getResourceUri());
        assertEquals(argument.getChangedResource().getResourceKind(), actual.getResourceKind());
        assertEquals(argument.getChangedResource().getContextId(), actual.getContextId());
        assertEquals(expectedDeletedData, actual.getDeletedData());
        assertEquals(argument.getChangedResource().getEvent(), actual.getEvent());
    }

    @Test
    void testMapperThrowsIllegalStateExceptionIfResourceTypeNull() {
        // when
        Executable executable = () -> mapper.mapChangedResource(request);

        // then
        IllegalStateException expectedException = assertThrows(IllegalStateException.class, executable);
        assertEquals("Unknown disqualification type", expectedException.getMessage());
    }

    @Test
    void testMapperThrowsSerDesExceptionIfObjectMapperWriteFails() throws Exception {
        // given
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        // when
        Executable actual = () -> mapper.mapChangedResource(
                new ResourceChangedRequest(EXPECTED_CONTEXT_ID, OFFICER_ID, DisqualificationResourceType.NATURAL, null,
                        true));

        // then
        assertThrows(SerDesException.class, actual);
    }

    @Test
    void testMapperThrowsSerDesExceptionIfObjectMapperReadFails() throws Exception {
        // given
        when(objectMapper.writeValueAsString(any())).thenReturn("deletedDataAsString");
        when(objectMapper.readValue(anyString(), eq(Object.class))).thenThrow(JsonProcessingException.class);

        // when
        Executable actual = () -> mapper.mapChangedResource(
                new ResourceChangedRequest(EXPECTED_CONTEXT_ID, OFFICER_ID, DisqualificationResourceType.NATURAL, null,
                        true));

        // then
        assertThrows(SerDesException.class, actual);
    }

    static Stream<ResourceChangedTestArgument> resourceChangedScenarios() {
        return Stream.of(
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056",
                                DisqualificationResourceType.NATURAL, null, false))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/natural/CH4000056")
                        .withResourceKind("disqualified-officer-natural")
                        .withEventType("changed")
                        .withEventPublishedAt(DATE)
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056",
                                DisqualificationResourceType.CORPORATE, null, false))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/corporate/CH4000056")
                        .withResourceKind("disqualified-officer-corporate")
                        .withEventType("changed")
                        .withEventPublishedAt(DATE)
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056",
                                DisqualificationResourceType.CORPORATE, new CorporateDisqualificationApi(), true))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/corporate/CH4000056")
                        .withResourceKind("disqualified-officer-corporate")
                        .withEventType("deleted")
                        .withEventPublishedAt(DATE)
                        .withDeletedData(new CorporateDisqualificationApi())
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056",
                                DisqualificationResourceType.NATURAL, new NaturalDisqualificationApi(), true))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/natural/CH4000056")
                        .withResourceKind("disqualified-officer-natural")
                        .withEventType("deleted")
                        .withEventPublishedAt(DATE)
                        .withDeletedData(new NaturalDisqualificationApi())
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056",
                                DisqualificationResourceType.CORPORATE, CORPORATE_DISQ, true))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/corporate/CH4000056")
                        .withResourceKind("disqualified-officer-corporate")
                        .withEventType("deleted")
                        .withEventPublishedAt(DATE)
                        .withDeletedData(CORPORATE_DISQ)
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056",
                                DisqualificationResourceType.NATURAL, NATURAL_DISQ, true))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/natural/CH4000056")
                        .withResourceKind("disqualified-officer-natural")
                        .withEventType("deleted")
                        .withEventPublishedAt(DATE)
                        .withDeletedData(NATURAL_DISQ)
                        .build()
        );
    }

    static class ResourceChangedTestArgument {

        private final ResourceChangedRequest request;
        private final ChangedResource changedResource;

        public ResourceChangedTestArgument(ResourceChangedRequest request, ChangedResource changedResource) {
            this.request = request;
            this.changedResource = changedResource;
        }

        public ResourceChangedRequest getRequest() {
            return request;
        }

        public ChangedResource getChangedResource() {
            return changedResource;
        }

        public static ResourceChangedTestArgumentBuilder builder() {
            return new ResourceChangedTestArgumentBuilder();
        }

        @Override
        public String toString() {
            return this.request.toString();
        }
    }

    static class ResourceChangedTestArgumentBuilder {

        private ResourceChangedRequest request;
        private String resourceUri;
        private String resourceKind;
        private String contextId;
        private String eventType;
        private String eventPublishedAt;
        private Object deletedData;

        public ResourceChangedTestArgumentBuilder withRequest(ResourceChangedRequest request) {
            this.request = request;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withResourceUri(String resourceUri) {
            this.resourceUri = resourceUri;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withResourceKind(String resourceKind) {
            this.resourceKind = resourceKind;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withContextId(String contextId) {
            this.contextId = contextId;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withEventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withEventPublishedAt(String eventPublishedAt) {
            this.eventPublishedAt = eventPublishedAt;
            return this;
        }

        public ResourceChangedTestArgumentBuilder withDeletedData(Object deletedData) {
            this.deletedData = deletedData;
            return this;
        }

        public ResourceChangedTestArgument build() {
            ChangedResource changedResource = new ChangedResource();
            changedResource.setResourceUri(this.resourceUri);
            changedResource.setResourceKind(this.resourceKind);
            changedResource.setContextId(this.contextId);
            ChangedResourceEvent event = new ChangedResourceEvent();
            event.setType(this.eventType);
            event.setPublishedAt(this.eventPublishedAt);
            changedResource.setEvent(event);
            changedResource.setDeletedData(deletedData);
            return new ResourceChangedTestArgument(this.request, changedResource);
        }
    }
}
