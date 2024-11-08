package uk.gov.companieshouse.disqualifiedofficersdataapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi.KindEnum;
import uk.gov.companieshouse.api.disqualification.InternalCorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.InternalNaturalDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.CorporateDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.NaturalDisqualificationDocument;
import uk.gov.companieshouse.disqualifiedofficersdataapi.service.DeleteDisqualifiedOfficerService;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DeleteRequestParameters;
import uk.gov.companieshouse.disqualifiedofficersdataapi.service.DisqualifiedOfficerService;
import uk.gov.companieshouse.logging.Logger;

@RestController
public class DisqualifiedOfficerController {

    private final Logger logger;
    private final DisqualifiedOfficerService service;
    private final DeleteDisqualifiedOfficerService deleteService;

    public DisqualifiedOfficerController(Logger logger, DisqualifiedOfficerService service,
            DeleteDisqualifiedOfficerService deleteService) {
        this.logger = logger;
        this.service = service;
        this.deleteService = deleteService;
    }

    /**
     * PUT request to save or update a Natural Disqualified Officer.
     *
     * @param officerId   the id for the disqualified officer
     * @param requestBody the request body containing disqualified officer data
     * @return no response
     */
    @PutMapping("/disqualified-officers/natural/{officer_id}/internal")
    public ResponseEntity<Void> naturalDisqualifiedOfficer(
            @RequestHeader("x-request-id") String contextId,
            @PathVariable("officer_id") String officerId,
            @RequestBody InternalNaturalDisqualificationApi requestBody
    ) throws JsonProcessingException {
        logger.info(String.format(
                "Processing disqualified officer information for officer id %s",
                officerId));

        service.processNaturalDisqualification(contextId, officerId, requestBody);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     * PUT request to save or update a Corporate Disqualified Officers.
     *
     * @param officerId   the id for the disqualified officer
     * @param requestBody the request body containing disqualified officer data
     * @return no response
     */
    @PutMapping("/disqualified-officers/corporate/{officer_id}/internal")
    public ResponseEntity<Void> corporateDisqualifiedOfficer(
            @RequestHeader("x-request-id") String contextId,
            @PathVariable("officer_id") String officerId,
            @RequestBody InternalCorporateDisqualificationApi requestBody
    ) throws JsonProcessingException {
        logger.info(String.format(
                "Processing disqualified officer information for officer id %s",
                officerId));

        service.processCorporateDisqualification(contextId, officerId, requestBody);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Retrieve natural disqualified officer information for a officer ID.
     *
     * @param officerId the officer ID for the disqualification
     * @return NaturalDisqualificationDocument return natural disqualified officer information
     */
    @GetMapping("/disqualified-officers/natural/{officer_id}")
    public ResponseEntity<NaturalDisqualificationApi> naturalDisqualification(
            @PathVariable("officer_id") final String officerId) {
        logger.info(String.format(
                "Retrieving natural officer disqualification information for officer ID %s",
                officerId));

        NaturalDisqualificationDocument disqualification = service.retrieveNaturalDisqualification(officerId);
        disqualification.getData().setKind(uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi
                .KindEnum.NATURAL_DISQUALIFICATION);

        return ResponseEntity.status(HttpStatus.OK).body(disqualification.getData());
    }

    /**
     * Retrieve corporate disqualified officer information for a officer ID.
     *
     * @param officerId the officer ID for the disqualification
     * @return CorporateDisqualificationDocument return corporate disqualified officer information
     */
    @GetMapping("/disqualified-officers/corporate/{officer_id}")
    public ResponseEntity<CorporateDisqualificationApi> corporateDisqualification(
            @PathVariable("officer_id") String officerId) {
        logger.info(String.format(
                "Retrieving corporate officer disqualification information for officer ID %s",
                officerId));

        CorporateDisqualificationDocument disqualification = service.retrieveCorporateDisqualification(
                officerId);
        disqualification.getData().setKind(KindEnum.CORPORATE_DISQUALIFICATION);

        return ResponseEntity.status(HttpStatus.OK).body(disqualification.getData());
    }

    /**
     * Delete disqualification information for an officer id.
     *
     * @param officerId the officer id to be deleted
     * @return return 200 status with empty body
     */
    @DeleteMapping("/disqualified-officers/{officer_type}/{officer_id}/internal")
    public ResponseEntity<Void> deleteDisqualification(
            @RequestHeader("x-request-id") String contextId,
            @RequestHeader("x-delta-at") String requestDeltaAt,
            @PathVariable("officer_type") String officerType,
            @PathVariable("officer_id") String officerId) {
        logger.info(String.format(
                "Deleting disqualified officer information for officer id %s", officerId));

        deleteService.deleteDisqualification(DeleteRequestParameters.builder()
                .contextId(contextId)
                .requestDeltaAt(requestDeltaAt)
                .officerType(officerType)
                .officerId(officerId)
                .build());

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
