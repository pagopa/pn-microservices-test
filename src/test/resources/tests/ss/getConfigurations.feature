Feature: Get configurations

  @GetConfigurations @getDocumentsConfigs
  Scenario Outline: Reperimento tipologie di documenti e caratteristiche di storage.
    Given the SafeStorage client "<clientId>" authenticated by "<APIKey>"
    When I get documents configs
    Then I get "<statusCode>" statusCode
    Examples:
      | clientId            | APIKey            | statusCode |
      | @clientId-test      | @apiKey_test      | 200        |
      | @clientId-test      | INVALID_API_KEY   | 403        |
      | @clientId-unknown   | @apiKey_test      | 403        |

  @GetConfigurations @getCurrentClientConfig
  Scenario Outline: Reperimento dei dettagli di configurazione di un client.
    Given the SafeStorage client "<clientId>" authenticated by "<APIKey>"
    When I get current client config
    Then I get "<statusCode>" statusCode
    Examples:
      | clientId            | APIKey            | statusCode |
      | @clientId-test      | @apiKey_test      | 200        |
      | @clientId-unknown   | @apiKey_test      | 403        |
