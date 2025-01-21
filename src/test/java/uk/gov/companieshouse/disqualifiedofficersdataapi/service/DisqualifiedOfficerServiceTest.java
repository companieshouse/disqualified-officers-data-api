package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalDisqualificationApiInternalData;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.Created;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.Updated;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.transform.DisqualificationTransformer;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DisqualifiedOfficerServiceTest {

    private static final String OFFICER_ID = "officerId";
    private static final String CURRENT_DATE = "20240121133129395348";
    private static final String PAST_DATE = "20220121133129395348";
    private static final String FUTURE_DATE = "30220121133129395348";
    private static final OffsetDateTime CURRENT_ZDT = ZonedDateTime.parse(CURRENT_DATE,
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
                            .withZone(ZoneOffset.UTC))
            .toOffsetDateTime();
    private static final String EXISTING_DELTA_AT = "20230925171003950844";

    private InternalNaturalDisqualificationApi request;
    private InternalCorporateDisqualificationApi corpRequest;
    private DisqualificationDocument document;

    @InjectMocks
    private DisqualifiedOfficerService service;

    @Mock
    private DisqualifiedOfficerRepository repository;
    @Mock
    private NaturalDisqualifiedOfficerRepository naturalRepository;
    @Mock
    private CorporateDisqualifiedOfficerRepository corporateRepository;
    @Mock
    private DisqualificationTransformer transformer;
    @Mock
    private DisqualifiedOfficerApiService disqualifiedOfficerApiService;
    @Mock
    private DeltaAtHandler deltaAtHandler;

    @BeforeEach
    void setUp() {
        request = new InternalNaturalDisqualificationApi();
        corpRequest = new InternalCorporateDisqualificationApi();
        InternalDisqualificationApiInternalData internal = new InternalDisqualificationApiInternalData();
        internal.setDeltaAt(CURRENT_ZDT);
        request.setInternalData(internal);
        corpRequest.setInternalData(internal);
        document = new DisqualificationDocument();
        document.setUpdated(new Updated().setAt(LocalDateTime.now()));
        document.setDeltaAt(EXISTING_DELTA_AT);
    }

    @Test
    void processNaturalDisqualificationSavesDisqualificationIfNoExistingDocument() {
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.empty());
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, null, false));
    }

    @Test
    void processNaturalDisqualificationSavesDisqualificationIfExistingDocumentHasEmptyDeltaAt() {
        document.setDeltaAt("");
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, null, false));
    }

    @Test
    void processNaturalDisqualificationSavesDisqualificationIfExistingDocumentHasNullDeltaAt() {
        document.setDeltaAt(null);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, null, false));
    }

    @Test
    void shouldProcessNaturalDisqualificationWhenRequestDeltaAtIsMoreRecent() {
        document.setCreated(new Created().setAt(LocalDateTime.now()));
        document.setDeltaAt(PAST_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, null, false));
    }

    @Test
    void processRetriedNaturalDisqualificationDelta() {
        document.setCreated(new Created().setAt(LocalDateTime.now()));
        document.setDeltaAt(CURRENT_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);

        service.processNaturalDisqualification("", OFFICER_ID, request);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.NATURAL, null, false));
    }

    @Test
    void processRetriedNaturalDisqualificationDeltaFailsSave() {
        document.setCreated(new Created().setAt(LocalDateTime.now()));
        document.setDeltaAt(PAST_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformNaturalDisqualifiedOfficer(OFFICER_ID, request)).thenReturn(document);
        when(repository.save(document)).thenThrow(new BadGatewayException("MongoDB error when deleting document"));

        Executable executable = () -> service.processNaturalDisqualification("", OFFICER_ID, request);

        assertThrows(BadGatewayException.class, executable);
        verify(disqualifiedOfficerApiService, never()).invokeChsKafkaApi(any());
    }

    @Test
    void shouldNotProcessNaturalDisqualificationWhenRequestIsStale() {
        document.setDeltaAt(FUTURE_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(deltaAtHandler.isRequestStale(any(OffsetDateTime.class), any())).thenReturn(true);

        Executable actual = () -> service.processNaturalDisqualification("", OFFICER_ID, request);

        assertThrows(ConflictException.class, actual);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(transformer);
        verifyNoInteractions(disqualifiedOfficerApiService);
    }

    @Test
    void processCorporateDisqualificationSavesDisqualificationIfNoExistingDocument() {
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.empty());
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, null, false));
    }

    @Test
    void processCorporateDisqualificationSavesDisqualificationIfExistingDocumentHasEmptyDeltaAt() {
        document.setDeltaAt("");
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, null, false));
    }

    @Test
    void processCorporateDisqualificationSavesDisqualificationIfExistingDocumentHasNullDeltaAt() {
        document.setDeltaAt(null);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, null, false));
    }

    @Test
    void shouldProcessCorporateDisqualificationWhenRequestDeltaAtIsMoreRecent() {
        document.setCreated(new Created().setAt(LocalDateTime.now()));
        document.setDeltaAt(PAST_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(deltaAtHandler.isRequestStale(any(OffsetDateTime.class), any())).thenReturn(false);
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, null, false));
    }

    @Test
    void processRetriedCorporateDisqualificationDeltaAt() {
        document.setCreated(new Created().setAt(LocalDateTime.now()));
        document.setDeltaAt(CURRENT_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(transformer.transformCorporateDisqualifiedOfficer(OFFICER_ID, corpRequest)).thenReturn(document);

        service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        verify(repository).save(document);
        verify(disqualifiedOfficerApiService).invokeChsKafkaApi(new ResourceChangedRequest("", "officerId",
                DisqualificationResourceType.CORPORATE, null, false));
    }

    @Test
    void shouldNotProcessCorporateDisqualificationWhenRequestIsStale() {
        document.setDeltaAt(FUTURE_DATE);
        when(repository.findById(OFFICER_ID)).thenReturn(Optional.of(document));
        when(deltaAtHandler.isRequestStale(any(OffsetDateTime.class), any())).thenReturn(true);

        Executable actual = () -> service.processCorporateDisqualification("", OFFICER_ID, corpRequest);

        assertThrows(ConflictException.class, actual);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(transformer);
        verifyNoInteractions(disqualifiedOfficerApiService);
    }

    @Test
    void correctOfficerIdIsGivenReturnsDisqualification() {
        NaturalDisqualificationDocument naturalDocument = new NaturalDisqualificationDocument();
        naturalDocument.setData(new NaturalDisqualificationApi());
        naturalDocument.setId(OFFICER_ID);
        when(naturalRepository.findById(OFFICER_ID)).thenReturn(Optional.of(naturalDocument));

        NaturalDisqualificationDocument disqualification = service.retrieveNaturalDisqualification(OFFICER_ID);

        assertNotNull(disqualification);
        verify(naturalRepository, times(1)).findById(any());
    }

    @Test
    void correctOfficerIdIsGivenReturnsCorporateDisqualification() {
        CorporateDisqualificationDocument corporateDocument = new CorporateDisqualificationDocument();
        corporateDocument.setData(new CorporateDisqualificationApi());
        corporateDocument.setCorporateOfficer(true);
        corporateDocument.setId(OFFICER_ID);
        when(corporateRepository.findById(OFFICER_ID)).thenReturn(Optional.of(corporateDocument));

        CorporateDisqualificationDocument disqualification = service.retrieveCorporateDisqualification(OFFICER_ID);

        assertNotNull(disqualification);
        verify(corporateRepository, times(1)).findById(any());
    }

    @Test
    void throwsExceptionWhenCorporateIndIsTrueButNaturalOfficerCalled() {
        NaturalDisqualificationDocument naturalDocument = new NaturalDisqualificationDocument();
        naturalDocument.setData(new NaturalDisqualificationApi());
        naturalDocument.setCorporateOfficer(true);
        naturalDocument.setId(OFFICER_ID);
        when(naturalRepository.findById(OFFICER_ID)).thenReturn(Optional.of(naturalDocument));

        assertThrows(RuntimeException.class, () -> service.retrieveNaturalDisqualification
                (OFFICER_ID));
        verify(naturalRepository, times(1)).findById(any());

    }

    @Test
    void throwsExceptionWhenCorporateIndIsFalseButCorporateOfficerCalled() {
        CorporateDisqualificationDocument corporateDocument = new CorporateDisqualificationDocument();
        corporateDocument.setData(new CorporateDisqualificationApi());
        corporateDocument.setCorporateOfficer(false);
        corporateDocument.setId(OFFICER_ID);
        when(corporateRepository.findById(OFFICER_ID)).thenReturn(Optional.of(corporateDocument));

        assertThrows(RuntimeException.class, () -> service.retrieveCorporateDisqualification
                (OFFICER_ID));
        verify(corporateRepository, times(1)).findById(any());

    }

    @Test
    void throwsExceptionWhenInvalidIdGiven() {

        assertThrows(RuntimeException.class, () -> service.retrieveNaturalDisqualification
                ("asdfasdfasdf"));
        verify(naturalRepository, times(1)).findById(any());

    }
}