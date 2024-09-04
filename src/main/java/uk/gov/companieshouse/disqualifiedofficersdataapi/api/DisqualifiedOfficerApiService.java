package uk.gov.companieshouse.disqualifiedofficersdataapi.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.ChangedResource;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateChangedResourcePost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;

import java.util.function.Function;

@Service
public class DisqualifiedOfficerApiService {

    private static final String CHANGED_RESOURCE_URI = "/private/resource-changed";
    private final Logger logger;
    private final String chsKafkaUrl;
    private final ApiClientService apiClientService;
    private final Function<ResourceChangedRequest, ChangedResource> mapper;

    /**
     * Invoke API.
     */
    public DisqualifiedOfficerApiService(@Value("${chs.kafka.api.endpoint}") String chsKafkaUrl,
            ApiClientService apiClientService,
            Logger logger,
            Function<ResourceChangedRequest, ChangedResource> mapper) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.apiClientService = apiClientService;
        this.logger = logger;
        this.mapper = mapper;
    }


    /**
     * Calls the CHS Kafka api.
     * @param resourceChangedRequest encapsulates details relating to the updated or deleted officer
     * @return the response from the kafka api
     */
    public ApiResponse<Void> invokeChsKafkaApi(ResourceChangedRequest resourceChangedRequest) {
        InternalApiClient internalApiClient = apiClientService.getInternalApiClient();
        internalApiClient.setBasePath(chsKafkaUrl);

        PrivateChangedResourcePost changedResourcePost =
                internalApiClient.privateChangedResourceHandler().postChangedResource(
                        CHANGED_RESOURCE_URI, mapper.apply(resourceChangedRequest));

        return handleApiCall(changedResourcePost);
    }

    private ApiResponse<Void> handleApiCall(PrivateChangedResourcePost changedResourcePost) {
        try {
            return changedResourcePost.execute();
        } catch (ApiErrorResponseException exp) {
            HttpStatus statusCode = HttpStatus.valueOf(exp.getStatusCode());
            if (!statusCode.is2xxSuccessful()) {
                logger.error("Unsuccessful call to /private/resource-changed endpoint", exp);
                throw new ServiceUnavailableException(exp.getMessage());
            } else {
                logger.error("Error occurred while calling /private/resource-changed endpoint", exp);
                throw new RuntimeException(exp);
            }
        }
    }
}
