package uk.gov.companieshouse.disqualifiedofficersdataapi;

import static io.cucumber.core.options.Constants.PLUGIN_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.core.options.Constants.FILTER_TAGS_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.TestExecutionListeners;
import org.testcontainers.junit.jupiter.Testcontainers;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("/features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,json:target/cucumber-report.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "uk.gov.companieshouse.disqualifiedofficersdataapi")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @Ignored")
@AutoConfigureWebTestClient
@Testcontainers
@TestExecutionListeners(mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class CucumberFeaturesRunnerITest {
}