package uk.gov.companieshouse.disqualifiedofficersdataapi.model;

import java.time.LocalDateTime;

public class Updated {

    private LocalDateTime at;

    public LocalDateTime getAt() {
        return at;
    }

    public Updated setAt(LocalDateTime at) {
        this.at = at;
        return this;
    }
}
