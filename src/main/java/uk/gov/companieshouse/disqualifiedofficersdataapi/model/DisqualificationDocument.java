package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "disqualifications")
public class DisqualificationDocument {

    @Id
    private String id;

    @Field("officer_disq_id")
    private String officerDisqId;

    @Field("officer_detail_id")
    private String officerDetailId;

    @Field("officer_id_raw")
    private String officerIdRaw;

    private Created created;

    @Field("delta_at")
    private String deltaAt;

    @Field("is_corporate_officer")
    private boolean isCorporateOfficer;

    private Updated updated;

    public String getId() {
        return id;
    }

    public DisqualificationDocument setId(String id) {
        this.id = id;
        return this;
    }

    public String getOfficerDisqId() {
        return officerDisqId;
    }

    public DisqualificationDocument setOfficerDisqId(String officerDisqId) {
        this.officerDisqId = officerDisqId;
        return this;
    }

    public String getOfficerDetailId() {
        return officerDetailId;
    }

    public DisqualificationDocument setOfficerDetailId(String officerDetailId) {
        this.officerDetailId = officerDetailId;
        return this;
    }

    public String getOfficerIdRaw() {
        return officerIdRaw;
    }

    public DisqualificationDocument setOfficerIdRaw(String officerIdRaw) {
        this.officerIdRaw = officerIdRaw;
        return this;
    }

    public Created getCreated() {
        return created;
    }

    public DisqualificationDocument setCreated(Created created) {
        this.created = created;
        return this;
    }

    public String getDeltaAt() {
        return deltaAt;
    }

    public DisqualificationDocument setDeltaAt(String deltaAt) {
        this.deltaAt = deltaAt;
        return this;
    }

    public boolean isCorporateOfficer() {
        return isCorporateOfficer;
    }

    public DisqualificationDocument setCorporateOfficer(boolean corporateOfficer) {
        isCorporateOfficer = corporateOfficer;
        return this;
    }

    public Updated getUpdated() {
        return updated;
    }

    public DisqualificationDocument setUpdated(Updated updated) {
        this.updated = updated;
        return this;
    }
}
