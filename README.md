# disqualified-officers-data-api

Handles CRUD functions for disqualified officers. Stores and retrieves natural and corporate officer disqualification data in MongoDB and publishes change events to the CHS Kafka API.

## Requirements

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [Docker](https://www.docker.com/) (for running locally with MongoDB)

## Technology Stack

| Technology | Version |
|---|---|
| Java | 21 |
| Spring Boot | 4.x |
| Jackson | 3.x (`tools.jackson`) |
| MongoDB | 6 |
| Testcontainers | 1.21.x |
| Cucumber | 7.23.x |

## Build

Common commands used for development and running locally can be found in the Makefile, each make target has a description which can be listed by running `make help`.

```text
Target               Description
------               -----------
all                  Calls methods required to build a locally runnable version, typically the build target
build                Pull down any dependencies and compile code into an executable if required
clean                Reset repo to pre-build state (i.e. a clean checkout state)
package              Create a single versioned deployable package (i.e. jar, zip, tar, etc.). May be dependent on the
build target being run before package
sonar                Run sonar scan
test                 Run all test-* targets (convenience method for developers)
test-unit            Run unit tests
```

## Building the docker image

```bash
mvn compile jib:dockerBuild
```

## Running Tests

### Unit tests

```bash
mvn test
```

### Integration tests (Cucumber + Testcontainers)

```bash
mvn verify
```

Integration tests use Testcontainers to spin up a real MongoDB instance. Docker must be running. The "database is down" Cucumber scenarios must be the last scenario in their respective feature files — this is a constraint of the Testcontainers lifecycle approach used.

## Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/healthcheck` | Health check — returns 200 if service is running |
| PUT | `/disqualified-officers/natural/{officer_id}/internal` | Save or update a natural disqualified officer record |
| PUT | `/disqualified-officers/corporate/{officer_id}/internal` | Save or update a corporate disqualified officer record |
| GET | `/disqualified-officers/natural/{officer_id}` | Retrieve a natural disqualified officer record |
| GET | `/disqualified-officers/corporate/{officer_id}` | Retrieve a corporate disqualified officer record |
| DELETE | `/disqualified-officers/{officer_type}/{officer_id}/internal` | Delete a disqualified officer record |

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MONGODB_URL` | `mongodb://mongo:27017/disqualifications` | Full MongoDB connection URI including database name |
| `CHS_KAFKA_API_URL` | `http://localhost:8889` | CHS Kafka API endpoint |
| `CHS_API_KEY` | `chsApiKey` | CHS API key |
| `DSQ_STREAM_HOOK_ENABLED` | `true` | Feature flag to enable/disable CHS Kafka API publishing |

## Migration Notes — Spring Boot 3 → 4 / Jackson 2 → 3

This service was migrated from Spring Boot 3 to Spring Boot 4 and from Jackson 2 (`com.fasterxml.jackson`) to Jackson 3 (`tools.jackson`). Key changes made:

**Jackson 3 (`tools.jackson`)**
- All `com.fasterxml.jackson` imports replaced with `tools.jackson` (except `jackson-annotations` which remains `com.fasterxml`)
- `ObjectMapper` is now immutable — uses `JsonMapper.builder()` pattern
- `JsonSerializer`/`JsonDeserializer` → `StdSerializer`/`StdDeserializer`
- `SerializerProvider` → `SerializationContext`
- `JsonProcessingException` (checked) → `JacksonException` (unchecked)
- `textValue()` → `stringValue()`, `isTextual()` → `isString()`
- `JavaTimeModule` is built-in to Jackson 3 — no manual registration needed

**Spring Boot 4 package moves**
- `@WebMvcTest` → `org.springframework.boot.webmvc.test.autoconfigure`
- `@AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure`
- `@AutoConfigureWebTestClient` → `org.springframework.boot.webflux.test.autoconfigure`
- `@DataMongoTest` → `org.springframework.boot.data.mongodb.test.autoconfigure`
- `@MockitoBean` → `org.springframework.test.context.bean.override.mockito`
- `spring.data.mongodb.uri` property renamed to `spring.mongodb.uri`
- `management.health.mongo.enabled` deprecated → `management.health.mongodb.enabled`

**Test infrastructure**
- Cucumber JUnit 4 (`@RunWith`/`@CucumberOptions`) → JUnit Platform (`@Suite`/`@IncludeEngines`)
- `TestRestTemplate` → `WebTestClient`
- Testcontainers wired via `@DynamicPropertySource` with Awaitility readiness check
- `@CucumberContextConfiguration` moved to dedicated `CucumberSpringConfiguration` class

**Known limitations / TODOs**
- OpenTelemetry auto-configuration is excluded in tests pending resolution of compatibility between `structured-logging` (which pulls in the legacy OTel instrumentation starter transitively) and the SB4 native `spring-boot-starter-opentelemetry`. See TODO in `application-test.properties`.
- "Database is down" Cucumber scenarios must remain last in their respective feature files due to Testcontainers lifecycle constraints.

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS. This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.

| Application specific attributes | Value | Description |
|:---|:---|:---|
| **ECS Cluster** | public-data | ECS cluster (stack) the service belongs to |
| **Load balancer** | {env}-chs-apichgovuk <br> {env}-chs-apichgovuk-private | The load balancer that sits in front of the service |
| **Concourse pipeline** | [Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/disqualified-officers-data-api) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/disqualified-officers-data-api) | Concourse pipeline link in shared services |

### Contributing

Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing

Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart). If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates

Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links

- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
