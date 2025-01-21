Feature: Process natural disqualified officer information

  Scenario Outline: Processing natural disqualified officer information successfully

    Given disqualified officers data api service is running
    When I send natural PUT request with payload "<data>" file
    Then I should receive 200 status code
    And the CHS Kafka API is invoked with "<officerId>"

    Examples:
      | data                                  | officerId     |
      | natural_disqualified_officer          | 1234567890    |

  Scenario Outline: Retrieve natural disqualified officer information successfully

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "<officerId>"
    When I send natural GET request with officer Id "<officerId>"
    Then I should receive 200 status code
    And the natural Get call response body should match "<result>" file

    Examples:
      | officerId     | result                                    |
      | 1234567890    | retrieve_natural_disqualified_officer     |

  Scenario Outline: Processing natural disqualified officer with stale delta_at returns 409

    Given disqualified officers data api service is running
    And the natural disqualified officer information exists for "<officer_id>" with delta_at "<delta_at>"
    When I send natural PUT request with payload "<data>" file
    Then I should receive 409 status code
    And the natural record with id "<officer_id>" is unchanged
    And the CHS Kafka API is not invoked

    Examples:
      | officer_id | delta_at             | data                         |
      | 1234567890 | 20500405155100786000 | natural_disqualified_officer |
