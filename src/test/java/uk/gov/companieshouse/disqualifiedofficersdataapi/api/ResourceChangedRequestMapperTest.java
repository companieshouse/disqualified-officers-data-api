package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;

@ExtendWith(MockitoExtension.class)
public class ResourceChangedRequestMapperTest {

    private static final String EXPECTED_CONTEXT_ID = "35234234";
    private static final String DATE = "date";

    @Mock
    private Supplier<String> timestampGenerator;

    @Mock
    private ResourceChangedRequest request;

    @InjectMocks
    private ResourceChangedRequestMapper mapper;

    @ParameterizedTest
    @MethodSource("resourceChangedScenarios")
    void testMapper(ResourceChangedTestArgument argument) {
        // given
        when(timestampGenerator.get()).thenReturn(DATE);

        // when
        ChangedResource actual = mapper.mapChangedResource(argument.getRequest());

        // then
        assertEquals(argument.getChangedResource(), actual);
    }

    @Test
    void testMapperThrowsIllegalStateExceptionIfResourceTypeNull() {
        // when
        Executable executable = () -> mapper.mapChangedResource(request);

        // then
        IllegalStateException expectedException = assertThrows(IllegalStateException.class, executable);
        assertEquals("Unknown disqualification type", expectedException.getMessage());
    }

    static Stream<ResourceChangedTestArgument> resourceChangedScenarios() {
        return Stream.of(
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056", DisqualificationResourceType.NATURAL, null))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/natural/CH4000056")
                        .withResourceKind("disqualified-officer-natural")
                        .withEventType("changed")
                        .withEventPublishedAt(DATE)
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056", DisqualificationResourceType.CORPORATE, null))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/corporate/CH4000056")
                        .withResourceKind("disqualified-officer-corporate")
                        .withEventType("changed")
                        .withEventPublishedAt(DATE)
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056", DisqualificationResourceType.CORPORATE, new CorporateDisqualificationApi()))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/corporate/CH4000056")
                        .withResourceKind("disqualified-officer-corporate")
                        .withEventType("deleted")
                        .withEventPublishedAt(DATE)
                        .withDeletedData(new CorporateDisqualificationApi())
                        .build(),
                ResourceChangedTestArgument.builder()
                        .withRequest(new ResourceChangedRequest(EXPECTED_CONTEXT_ID, "CH4000056", DisqualificationResourceType.NATURAL, new NaturalDisqualificationApi()))
                        .withContextId(EXPECTED_CONTEXT_ID)
                        .withResourceUri("/disqualified-officers/natural/CH4000056")
                        .withResourceKind("disqualified-officer-natural")
                        .withEventType("deleted")
                        .withEventPublishedAt(DATE)
                        .withDeletedData(new NaturalDisqualificationApi())
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
