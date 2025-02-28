package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DeleteRequestParameters;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;

@ExtendWith(MockitoExtension.class)
class DeleteDisqualifiedOfficerServiceTest {

    private static final String CONTEXT_ID = "context-id";
    private static final String OFFICER_ID = "officerId";
    private static final String REQUEST_DELTA_AT = "20240925171003950844";
    private static final String NATURAL = "natural";
    private static final String CORPORATE = "corporate";

    private final ApiResponse<Void> successResponse = new ApiResponse<>(200, new HashMap<>());
    private final ApiResponse<Void> unsuccessfulResponse = new ApiResponse<>(503, new HashMap<>());

    @InjectMocks
    private DeleteDisqualifiedOfficerService service;

    @Mock
    private DisqualifiedOfficerRepository repository;
    @Mock
    private DisqualifiedOfficerApiService disqualifiedOfficerApiService;
    @Mock
    private DeletionDataService deletionDataService;

    @Mock
    private Object dataObject;


    @Test
    void shouldDeleteCorporateDisqualification() {
        // given
        when(deletionDataService.processCorporateDisqualificationData(anyString(), anyString())).thenReturn(dataObject);
        when(disqualifiedOfficerApiService.invokeChsKafkaApi(any())).thenReturn(successResponse);

        DeleteRequestParameters deleteRequestParameters = DeleteRequestParameters.builder()
                .contextId(CONTEXT_ID)
                .officerId(OFFICER_ID)
                .requestDeltaAt(REQUEST_DELTA_AT)
                .officerType(CORPORATE)
                .build();

        // when
        service.deleteDisqualification(deleteRequestParameters);

        // then
        verify(deletionDataService).processCorporateDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);
        verify(repository).deleteById(OFFICER_ID);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(
                new ResourceChangedRequest(CONTEXT_ID, OFFICER_ID, DisqualificationResourceType.CORPORATE, dataObject,
                        true));
    }

    @Test
    void shouldDeleteCorporateDisqualificationWhenFactoryReturnsNull() {
        // given
        when(deletionDataService.processCorporateDisqualificationData(anyString(), anyString())).thenReturn(null);
        when(disqualifiedOfficerApiService.invokeChsKafkaApi(any())).thenReturn(successResponse);

        DeleteRequestParameters deleteRequestParameters = DeleteRequestParameters.builder()
                .contextId(CONTEXT_ID)
                .officerId(OFFICER_ID)
                .requestDeltaAt(REQUEST_DELTA_AT)
                .officerType(CORPORATE)
                .build();

        // when
        service.deleteDisqualification(deleteRequestParameters);

        // then
        verify(deletionDataService).processCorporateDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);
        verifyNoInteractions(repository);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(
                new ResourceChangedRequest(CONTEXT_ID, OFFICER_ID, DisqualificationResourceType.CORPORATE, null,
                        true));
    }

    @Test
    void shouldDeleteCorporateDisqualificationWhenChsKafkaApiReturnsNon200OK() {
        // given
        when(deletionDataService.processCorporateDisqualificationData(anyString(), anyString())).thenReturn(dataObject);
        when(disqualifiedOfficerApiService.invokeChsKafkaApi(any())).thenReturn(unsuccessfulResponse);

        DeleteRequestParameters deleteRequestParameters = DeleteRequestParameters.builder()
                .contextId(CONTEXT_ID)
                .officerId(OFFICER_ID)
                .requestDeltaAt(REQUEST_DELTA_AT)
                .officerType(CORPORATE)
                .build();

        // when
        service.deleteDisqualification(deleteRequestParameters);

        // then
        verify(deletionDataService).processCorporateDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);
        verify(repository).deleteById(OFFICER_ID);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(
                new ResourceChangedRequest(CONTEXT_ID, OFFICER_ID, DisqualificationResourceType.CORPORATE, dataObject,
                        true));
    }

    @Test
    void shouldDeleteNaturalDisqualification() {
        // given
        when(deletionDataService.processNaturalDisqualificationData(anyString(), anyString())).thenReturn(dataObject);
        when(disqualifiedOfficerApiService.invokeChsKafkaApi(any())).thenReturn(successResponse);

        DeleteRequestParameters deleteRequestParameters = DeleteRequestParameters.builder()
                .contextId(CONTEXT_ID)
                .officerId(OFFICER_ID)
                .requestDeltaAt(REQUEST_DELTA_AT)
                .officerType(NATURAL)
                .build();

        // when
        service.deleteDisqualification(deleteRequestParameters);

        // then
        verify(deletionDataService).processNaturalDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);
        verify(repository).deleteById(OFFICER_ID);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(
                new ResourceChangedRequest(CONTEXT_ID, OFFICER_ID, DisqualificationResourceType.NATURAL, dataObject,
                        true));
    }

    @Test
    void shouldNotCallDeleteNaturalDisqualificationWhenFactoryReturnsNull() {
        // given
        when(deletionDataService.processNaturalDisqualificationData(anyString(), anyString())).thenReturn(null);
        when(disqualifiedOfficerApiService.invokeChsKafkaApi(any())).thenReturn(successResponse);

        DeleteRequestParameters deleteRequestParameters = DeleteRequestParameters.builder()
                .contextId(CONTEXT_ID)
                .officerId(OFFICER_ID)
                .requestDeltaAt(REQUEST_DELTA_AT)
                .officerType(NATURAL)
                .build();

        // when
        service.deleteDisqualification(deleteRequestParameters);

        // then
        verify(deletionDataService).processNaturalDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);
        verifyNoInteractions(repository);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(
                new ResourceChangedRequest(CONTEXT_ID, OFFICER_ID, DisqualificationResourceType.NATURAL, null,
                        true));
    }

    @Test
    void shouldDeleteNaturalDisqualificationWhenChsKafkaApiReturnsNon200OK() {
        // given
        when(deletionDataService.processNaturalDisqualificationData(anyString(), anyString())).thenReturn(dataObject);
        when(disqualifiedOfficerApiService.invokeChsKafkaApi(any())).thenReturn(unsuccessfulResponse);

        DeleteRequestParameters deleteRequestParameters = DeleteRequestParameters.builder()
                .contextId(CONTEXT_ID)
                .officerId(OFFICER_ID)
                .requestDeltaAt(REQUEST_DELTA_AT)
                .officerType(NATURAL)
                .build();

        // when
        service.deleteDisqualification(deleteRequestParameters);

        // then
        verify(deletionDataService).processNaturalDisqualificationData(OFFICER_ID, REQUEST_DELTA_AT);
        verify(repository).deleteById(OFFICER_ID);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(
                new ResourceChangedRequest(CONTEXT_ID, OFFICER_ID, DisqualificationResourceType.NATURAL, dataObject,
                        true));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "invalid",
            "null",
            "''"
    }, nullValues = "null")
    void shouldThrowBadRequestWhenOfficerTypeIsInvalid(final String officerType) {
        // given
        DeleteRequestParameters deleteRequestParameters = DeleteRequestParameters.builder()
                .contextId(CONTEXT_ID)
                .officerId(OFFICER_ID)
                .requestDeltaAt(REQUEST_DELTA_AT)
                .officerType(officerType)
                .build();

        // when
        Executable ex = () -> service.deleteDisqualification(deleteRequestParameters);

        // then
        assertThrows(BadRequestException.class, ex);
        verifyNoInteractions(deletionDataService);
        verifyNoInteractions(repository);
        verifyNoInteractions(disqualifiedOfficerApiService);
    }
}
