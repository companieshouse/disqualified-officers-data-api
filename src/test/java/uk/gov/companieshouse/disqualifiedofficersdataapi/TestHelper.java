package uk.gov.companieshouse.disqualifiedofficersdataapi;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import uk.gov.companieshouse.api.disqualification.Address;
import uk.gov.companieshouse.api.disqualification.CorporateDisqualificationApi;
import uk.gov.companieshouse.api.disqualification.Disqualification;
import uk.gov.companieshouse.api.disqualification.DisqualificationLinks;
import uk.gov.companieshouse.api.disqualification.NaturalDisqualificationApi;

public class TestHelper {

    private static final String ETAG = "etag";
    private static final String NAME = "name";
    private static final String TYPE = "disqualificationType";
    private static final Address ADDRESS = new Address();
    private static final Object REASON = new Object();
    private static final LocalDate DATE_FROM = LocalDate.of(2020, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.of(2021, 1, 1);
    private static final DisqualificationLinks LINKS = new DisqualificationLinks("links");
    private static final CorporateDisqualificationApi.KindEnum CORPORATE_KIND =
            CorporateDisqualificationApi.KindEnum.CORPORATE_DISQUALIFICATION;
    private static final NaturalDisqualificationApi.KindEnum NATURAL_KIND =
            NaturalDisqualificationApi.KindEnum.NATURAL_DISQUALIFICATION;

    public static NaturalDisqualificationApi createPopulatedNaturalDisqualification() {
        return new NaturalDisqualificationApi(
                ETAG, NATURAL_KIND, NAME, LINKS, createPopulatedDisqualification()
        );
    }

    public static CorporateDisqualificationApi createPopulatedCorporateDisqualification() {
        return new CorporateDisqualificationApi(
                ETAG, CORPORATE_KIND, NAME, LINKS, createPopulatedDisqualification()
        );
    }

    public static List<Disqualification> createPopulatedDisqualification() {
        return Collections.singletonList(new Disqualification(
                ADDRESS, TYPE, DATE_FROM, DATE_TO, REASON
        ));
    }

}
