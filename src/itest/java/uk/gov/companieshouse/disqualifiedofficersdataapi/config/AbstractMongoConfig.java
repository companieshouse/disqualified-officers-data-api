package uk.gov.companieshouse.disqualifiedofficersdataapi.config;

import com.mongodb.client.MongoClients;
import org.awaitility.Awaitility;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.disqualifiedofficersdataapi.util.TestMongoUriUtil;

import java.time.Duration;

public class AbstractMongoConfig {

    private static MongoDBContainer mongoDBContainer;

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        startMongo();
        registry.add("spring.mongodb.uri",
                () -> TestMongoUriUtil.getMongoUriWithTimeouts(mongoDBContainer));
    }

    protected static void startMongo() {
        if (mongoDBContainer == null || !mongoDBContainer.isRunning()) {
            mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6"));
            mongoDBContainer.start();
        }
        Awaitility.await()
                .atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .ignoreExceptions()
                .untilAsserted(() -> {
                    try (var client = MongoClients.create(
                            TestMongoUriUtil.getMongoUriWithTimeouts(mongoDBContainer))) {
                        client.getDatabase("disqualifications").listCollectionNames().first();
                    }
                });
    }

    protected static void stopMongo() {
        if (mongoDBContainer != null && mongoDBContainer.isRunning()) {
            mongoDBContainer.stop();
        }
    }
}