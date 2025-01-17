package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.Supplier;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.chskafka.ChangedResourceEvent;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.SerDesException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;


public class ResourceChangedRequestMapper {

    private final Supplier<String> timestampGenerator;
    private final ObjectMapper objectMapper;

    public ResourceChangedRequestMapper(Supplier<String> timestampGenerator, ObjectMapper objectMapper) {
        this.timestampGenerator = timestampGenerator;
        this.objectMapper = objectMapper;
    }

    public ChangedResource mapChangedResource(ResourceChangedRequest request) {
        ChangedResource changedResource = new ChangedResource();

        if (request.getType() == DisqualificationResourceType.NATURAL) {
            changedResource.setResourceUri("/disqualified-officers/natural/" + request.getOfficerId());
            changedResource.setResourceKind("disqualified-officer-natural");
        } else if (request.getType() == DisqualificationResourceType.CORPORATE) {
            changedResource.setResourceUri("/disqualified-officers/corporate/" + request.getOfficerId());
            changedResource.setResourceKind("disqualified-officer-corporate");
        } else {
            throw new IllegalStateException("Unknown disqualification type");
        }

        ChangedResourceEvent event = new ChangedResourceEvent();
        if (request.getIsDelete()) {
            event.setType("deleted");
            try {
                Object disqualificationAsObject = objectMapper.readValue(
                        objectMapper.writeValueAsString(request.getDisqualificationData()), Object.class
                );
                changedResource.setDeletedData(disqualificationAsObject);
            } catch (JsonProcessingException ex) {
                throw new SerDesException("Failed to serialise/deserialise data", ex);
            }
        } else {
            event.setType("changed");
        }
        event.publishedAt(this.timestampGenerator.get());

        changedResource.event(event);
        changedResource.setContextId(request.getContextId());

        return changedResource;
    }
}
