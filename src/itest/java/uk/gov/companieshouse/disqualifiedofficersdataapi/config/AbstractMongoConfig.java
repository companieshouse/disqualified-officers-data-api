package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

/**
 * Mongodb configuration runs on test container.
 */
public class AbstractMongoConfig {

    @Container
    @ServiceConnection
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse("mongo:6"));

    static {
        mongoDBContainer.start();
    }
}