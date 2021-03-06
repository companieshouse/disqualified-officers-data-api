package uk.gov.companieshouse.disqualifiedofficersdataapi.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.disqualifiedofficersdataapi.model.DisqualificationDocument;

import java.util.List;

@Repository
public interface DisqualifiedOfficerRepository extends MongoRepository<DisqualificationDocument, String> {

    @Query("{'_id': ?0, 'updated.at':{$gte : { \"$date\" : \"?1\" } }}")
    List<DisqualificationDocument> findUpdatedDisqualification(String officerId, String at);
}
