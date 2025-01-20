package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import static uk.gov.companieshouse.disqualifiedofficersdataapi.DisqualifiedOfficersDataApiApplication.NAMESPACE;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadGatewayException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.ConflictException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.NotFoundException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.logging.DataMapHolder;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.Created;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationResourceType;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.transform.DisqualificationTransformer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import java.util.Optional;

@Service
public class DisqualifiedOfficerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String STALE_DELTA_AT_MESSAGE = "Delta at field on request is stale";

    private final DisqualifiedOfficerRepository repository;
    private final NaturalDisqualifiedOfficerRepository naturalRepository;
    private final CorporateDisqualifiedOfficerRepository corporateRepository;
    private final DisqualificationTransformer transformer;
    private final DisqualifiedOfficerApiService disqualifiedOfficerApiService;
    private final DeltaAtHandler deltaAtHandler;

    public DisqualifiedOfficerService(DisqualifiedOfficerRepository repository,
            NaturalDisqualifiedOfficerRepository naturalRepository,
            CorporateDisqualifiedOfficerRepository corporateRepository, DisqualificationTransformer transformer,
            DisqualifiedOfficerApiService disqualifiedOfficerApiService, DeltaAtHandler deltaAtHandler) {
        this.repository = repository;
        this.naturalRepository = naturalRepository;
        this.corporateRepository = corporateRepository;
        this.transformer = transformer;
        this.disqualifiedOfficerApiService = disqualifiedOfficerApiService;
        this.deltaAtHandler = deltaAtHandler;
    }

    /**
     * Save or update a natural disqualification
     *
     * @param contextId   Id used for chsKafkaCall
     * @param officerId   Id used for mongo record
     * @param requestBody Data to be saved
     */
    public void processNaturalDisqualification(String contextId, String officerId,
            InternalNaturalDisqualificationApi requestBody) {
        Optional<DisqualificationDocument> existingDocument = repository.findById(officerId);

        if (existingDocument.isEmpty() ||
                !deltaAtHandler.isRequestStale(requestBody.getInternalData().getDeltaAt(),
                        existingDocument.get().getDeltaAt())) {
            DisqualificationDocument document = transformer.transformNaturalDisqualifiedOfficer(officerId, requestBody);
            saveAndCallChsKafka(contextId, officerId, document, DisqualificationResourceType.NATURAL,
                    existingDocument.orElse(null));
        } else {
            LOGGER.error(STALE_DELTA_AT_MESSAGE, DataMapHolder.getLogMap());
            throw new ConflictException(STALE_DELTA_AT_MESSAGE);
        }
    }

    /**
     * Save or update a corporate disqualification
     *
     * @param contextId   Id used for chsKafkaCall
     * @param officerId   Id used for mongo record
     * @param requestBody Data to be saved
     */
    public void processCorporateDisqualification(String contextId, String officerId,
            InternalCorporateDisqualificationApi requestBody) {
        Optional<DisqualificationDocument> existingDocument = repository.findById(officerId);

        if (existingDocument.isEmpty() ||
                !deltaAtHandler.isRequestStale(requestBody.getInternalData().getDeltaAt(),
                        existingDocument.get().getDeltaAt())) {
            DisqualificationDocument document = transformer.transformCorporateDisqualifiedOfficer(officerId,
                    requestBody);
            saveAndCallChsKafka(contextId, officerId, document, DisqualificationResourceType.CORPORATE,
                    existingDocument.orElse(null));
        } else {
            LOGGER.error(STALE_DELTA_AT_MESSAGE, DataMapHolder.getLogMap());
            throw new ConflictException(STALE_DELTA_AT_MESSAGE);
        }
    }

    /**
     * Save or update the mongo record
     *
     * @param contextId Chs kafka id
     * @param officerId Mongo id
     * @param document  Transformed Data
     */
    private void saveAndCallChsKafka(
            String contextId, String officerId,
            DisqualificationDocument document, DisqualificationResourceType type,
            DisqualificationDocument existingDocument) {

        DataMapHolder.get().officerType(String.valueOf(type));
        Optional.ofNullable(existingDocument)
                .map(DisqualificationDocument::getCreated)
                .ifPresentOrElse(document::setCreated,
                        () -> document.setCreated(new Created().setAt(document.getUpdated().getAt())));

        try {
            repository.save(document);
        } catch (TransientDataAccessException ex) {
            LOGGER.info("Recoverable MongoDB error when inserting/updating document", DataMapHolder.getLogMap());
            throw new BadGatewayException("Recoverable MongoDB error when inserting/updating document", ex);
        } catch (DataAccessException ex) {
            LOGGER.error("MongoDB error when inserting/updating document", ex, DataMapHolder.getLogMap());
            throw new BadGatewayException("MongoDB error when inserting/updating document", ex);
        }

        disqualifiedOfficerApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(contextId, officerId,
                        type, null, false));
        LOGGER.info("ChsKafka api CHANGED invoked successfully", DataMapHolder.getLogMap());
    }

    public NaturalDisqualificationDocument retrieveNaturalDisqualification(String officerId) {
        NaturalDisqualificationDocument disqualificationDocument =
                naturalRepository.findById(officerId)
                        .orElseGet(() -> {
                            LOGGER.info("Record not found in MongoDB", DataMapHolder.getLogMap());
                            throw new NotFoundException("Record no found in MongoDB");
                        });
        if (disqualificationDocument.isCorporateOfficer()) {
            LOGGER.info("Natural type record not found in MongoDB", DataMapHolder.getLogMap());
            throw new NotFoundException("Natural type record not found in MongoDB");
        }
        return disqualificationDocument;
    }

    public CorporateDisqualificationDocument retrieveCorporateDisqualification(String officerId) {
        CorporateDisqualificationDocument disqualificationDocument =
                corporateRepository.findById(officerId)
                        .orElseGet(() -> {
                            LOGGER.info("Record not found in MongoDB", DataMapHolder.getLogMap());
                            throw new NotFoundException("Record no found in MongoDB");
                        });
        if (!disqualificationDocument.isCorporateOfficer()) {
            LOGGER.info("Corporate type record not found in MongoDB", DataMapHolder.getLogMap());
            throw new NotFoundException("Corporate type record not found in MongoDB");
        }
        return disqualificationDocument;
    }

}
