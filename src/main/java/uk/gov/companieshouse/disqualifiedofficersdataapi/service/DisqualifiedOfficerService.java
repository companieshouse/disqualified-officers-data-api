package uk.gov.companieshouse.disqualifiedofficersdataapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.ResourceChangedRequest;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.*;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.CorporateDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.api.DisqualifiedOfficerApiService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.exceptions.BadRequestException;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.Created;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.DisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.repository.NaturalDisqualifiedOfficerRepository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.transform.DisqualificationTransformer;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class DisqualifiedOfficerService {

    @Autowired
    private DisqualifiedOfficerRepository repository;

    @Autowired
    private NaturalDisqualifiedOfficerRepository naturalRepository;

    @Autowired
    private CorporateDisqualifiedOfficerRepository corporateRepository;

    @Autowired
    private DisqualificationTransformer transformer;
    @Autowired
    DisqualifiedOfficerApiService disqualifiedOfficerApiService;

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Save or update a natural disqualification
     * @param contextId     Id used for chsKafkaCall
     * @param officerId     Id used for mongo record
     * @param requestBody   Data to be saved
     */
    public void processNaturalDisqualification(String contextId, String officerId,
                                               InternalNaturalDisqualificationApi requestBody) {

        boolean isLatestRecord = isLatestRecord(officerId, requestBody.getInternalData().getDeltaAt());

        if (isLatestRecord) {

            DisqualificationDocument document = transformer.transformNaturalDisqualifiedOfficer(officerId, requestBody);

            saveAndCallChsKafka(contextId, officerId, document, DisqualificationResourceType.NATURAL);
        }
    }

    /**
     * Save or update a corporate disqualification
     * @param contextId     Id used for chsKafkaCall
     * @param officerId     Id used for mongo record
     * @param requestBody   Data to be saved
     */
    public void processCorporateDisqualification(String contextId, String officerId,
                                                 InternalCorporateDisqualificationApi requestBody) {

        boolean isLatestRecord = isLatestRecord(officerId, requestBody.getInternalData().getDeltaAt());

        if (isLatestRecord) {

            DisqualificationDocument document = transformer.transformCorporateDisqualifiedOfficer(officerId, requestBody);

            saveAndCallChsKafka(contextId, officerId, document, DisqualificationResourceType.CORPORATE);
        }
    }

    /**
     * Find and delete a disqualification record.
     * @param contextId passed into the call to changed-resource
     * @param officerId used to find the document to delete
     */
    public void deleteDisqualification(String contextId, String officerId) {
        DisqualificationDocument document = retrieveDeleteDisqualification(officerId);
        if (document.isCorporateOfficer()) {
            deleteCorporateDisqualification(contextId, officerId);
        } else {
            deleteNaturalDisqualification(contextId, officerId);
        }
    }

    /**
     * Delete a corporate disqualification record.
     * @param contextId passed into the call to changed-resource
     * @param officerId used to find the document to delete
     */
    private void deleteCorporateDisqualification(String contextId, String officerId) {
        CorporateDisqualificationDocument document = retrieveCorporateDisqualification(officerId);
        repository.delete(document);
        disqualifiedOfficerApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(contextId, officerId, 
                        DisqualificationResourceType.CORPORATE, document.getData(), true));
    }

    /**
     * Delete a natural disqualification record.
     * @param contextId passed into the call to changed-resource
     * @param officerId used to find the document to delete
     */
    private void deleteNaturalDisqualification(String contextId, String officerId) {
        NaturalDisqualificationDocument document = retrieveNaturalDisqualification(officerId);
        repository.delete(document);
        disqualifiedOfficerApiService.invokeChsKafkaApi(
                new ResourceChangedRequest(contextId, officerId, 
                        DisqualificationResourceType.NATURAL, document.getData(), true));
    }

    /**
     * Check the record hasn't been update more recently
     * @param officerId Mongo Id
     * @param deltaAt   Time of update
     * @return isLatestRecord True if we are updating the latest record
     */
    private boolean isLatestRecord(String officerId, OffsetDateTime deltaAt) {
        String formattedDate = deltaAt.format(dateTimeFormatter);
        List<DisqualificationDocument> disqualifications = repository
                .findUpdatedDisqualification(officerId, formattedDate);
        return disqualifications.isEmpty();
    }

    /**
     * Save or update the mongo record
     * @param contextId Chs kafka id
     * @param officerId Mongo id
     * @param document  Transformed Data
     */
    private void saveAndCallChsKafka(String contextId, String officerId,
            DisqualificationDocument document, DisqualificationResourceType type) {
        boolean savedToDb = false;
        Created created = getCreatedFromCurrentRecord(officerId);
        if(created == null) {
            document.setCreated(new Created().setAt(document.getUpdated().getAt()));
        } else {
            document.setCreated(created);
        }

        try {
            repository.save(document);
            savedToDb = true;
        } catch (IllegalArgumentException illegalArgumentEx) {
            throw new BadRequestException(illegalArgumentEx.getMessage());
        }
        
        if (savedToDb) {
            disqualifiedOfficerApiService.invokeChsKafkaApi(
                    new ResourceChangedRequest(contextId, officerId, 
                            type, null, false));
        }
    }

    /**
     * Check whether we are updating a record and if so persist created time
     * @param officerId Mongo Id
     * @return created if this is an update save the previous created to the new document
     */
    private Created getCreatedFromCurrentRecord(String officerId) {
        Optional<DisqualificationDocument> doc = repository.findById(officerId);

        return doc.isPresent() ? doc.get().getCreated(): null;
    }

    public DisqualificationDocument retrieveDeleteDisqualification(String officerId) {
        Optional<DisqualificationDocument> disqualificationDocumentOptional = 
                repository.findById(officerId);
        DisqualificationDocument disqualificationDocument = disqualificationDocumentOptional.orElseThrow(
                () -> new IllegalArgumentException(String.format(
                "Resource not found for officer ID: %s", officerId)));
        return disqualificationDocument;
    }

    public NaturalDisqualificationDocument retrieveNaturalDisqualification(String officerId) {
        Optional<NaturalDisqualificationDocument> disqualificationDocumentOptional =
                naturalRepository.findById(officerId);
        NaturalDisqualificationDocument disqualificationDocument = disqualificationDocumentOptional.orElseThrow(
                () -> new IllegalArgumentException(String.format(
                        "Resource not found for officer ID: %s", officerId)));
        if(disqualificationDocument.isCorporateOfficer()) {
            throw new IllegalArgumentException(String.format(
                    "Natural resource not found for officer ID: %s", officerId));
        }
        return disqualificationDocument;
    }

    public CorporateDisqualificationDocument retrieveCorporateDisqualification(String officerId) {
        Optional<CorporateDisqualificationDocument> disqualificationDocumentOptional =
                corporateRepository.findById(officerId);
        CorporateDisqualificationDocument disqualificationDocument = disqualificationDocumentOptional.orElseThrow(
                () -> new IllegalArgumentException(String.format(
                        "Resource not found for officer ID: %s", officerId)));
        if(!disqualificationDocument.isCorporateOfficer()) {
            throw new IllegalArgumentException(String.format(
                    "Corporate resource not found for officer ID: %s", officerId));
        }
        return disqualificationDocument;
    }

}
