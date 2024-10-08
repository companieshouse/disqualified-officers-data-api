Feature: Response codes scenarios for disqualification officer

  Scenario Outline: Processing disqualified officers information unsuccessfully

    Given disqualified officers data api service is running
    When I send natural PUT request with payload "<data>" file
    Then I should receive <response_code> status code
    And the CHS Kafka API is not invoked
    And nothing is persisted in the database

    Examples:
        | data                | response_code |
        | bad_request_natural | 400           |

Scenario: Processing disqualified officers information unsuccessfully after internal server error

    Given disqualified officers data api service is running
    When I send natural PUT request with payload "internal_server_error_request" file
    Then I should receive 500 status code

  Scenario Outline: Processing disqualified officers information unsuccessfully but saves to database

    Given disqualified officers data api service is running
    When CHS kafka API service is unavailable
    And I send natural PUT request with payload "<data>" file
    Then I should receive 503 status code
    And the CHS Kafka API is invoked with "<officerId>"
    And a document is persisted to the database
    Examples:
        | data                         | officerId  |
        | natural_disqualified_officer | 1234567890 |

  Scenario: Processing disqualified officers information while database is down

    Given disqualified officers data api service is running
    And the disqualification database is down
    When I send natural PUT request with payload "natural_disqualified_officer" file
    Then I should receive 503 status code
    And the CHS Kafka API is not invoked

  Scenario: Proccessing disqualified officers information without ERIC headers
    Given disqualified officers data api service is running
    When I send natural PUT request without ERIC headers
    Then I should receive 401 status code
