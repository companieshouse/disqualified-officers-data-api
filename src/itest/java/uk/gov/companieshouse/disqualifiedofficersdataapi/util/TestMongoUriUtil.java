package uk.gov.companieshouse.disqualifiedofficersdataapi.util;

import org.testcontainers.containers.MongoDBContainer;

public class TestMongoUriUtil {
    private static final String TIMEOUTS = "serverSelectionTimeoutMS=100&connectTimeoutMS=100&socketTimeoutMS=100";

    public static String getMongoUriWithTimeouts(MongoDBContainer container) {
        String baseUri = container.getReplicaSetUrl();
        // strip /test, add /disqualifications and timeouts
        String withoutDb = baseUri.substring(0, baseUri.lastIndexOf('/'));
        return withoutDb + "/disqualifications?" + TIMEOUTS;
    }
}